package com.sdercolin.vlabeler.audio.conversion

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import com.sdercolin.vlabeler.env.Log
import com.sdercolin.vlabeler.model.AppConf
import com.sdercolin.vlabeler.ui.string.Strings
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Converts audio files (mp3, flac, ogg, m4a, aac, opus, ...) to 16 bit PCM wav files with Android's MediaCodec.
 */
class MediaCodecConverter : WaveConverter {

    override fun accept(inputFile: File, conf: AppConf.Conversion): Boolean {
        return inputFile.isFile && (inputFile.extension.lowercase() != "wav" || conf.useConversionForWav)
    }

    override suspend fun convert(inputFile: File, outputFile: File, conf: AppConf.Conversion) {
        withContext(Dispatchers.IO) {
            try {
                decode(inputFile, outputFile)
            } catch (t: Throwable) {
                if (t is CancellationException) throw t
                Log.error(t)
                throw ConversionException(t)
            }
        }
    }

    private suspend fun decode(inputFile: File, outputFile: File) = withContext(Dispatchers.IO) {
        val extractor = MediaExtractor()
        extractor.setDataSource(inputFile.absolutePath)
        val trackIndex = (0 until extractor.trackCount).firstOrNull {
            extractor.getTrackFormat(it).getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true
        } ?: throw IllegalArgumentException("No audio track found in ${inputFile.absolutePath}")
        extractor.selectTrack(trackIndex)
        val inputFormat = extractor.getTrackFormat(trackIndex)
        val mime = requireNotNull(inputFormat.getString(MediaFormat.KEY_MIME))
        var sampleRate = inputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        var channelCount = inputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        var pcmEncoding = android.media.AudioFormat.ENCODING_PCM_16BIT
        val codec = MediaCodec.createDecoderByType(mime)
        codec.configure(inputFormat, null, null, 0)
        codec.start()

        outputFile.parentFile?.mkdirs()
        val output = RandomAccessFile(outputFile, "rw")
        output.setLength(0)
        output.write(ByteArray(WAV_HEADER_SIZE)) // placeholder
        var dataSize = 0L
        val info = MediaCodec.BufferInfo()
        var inputDone = false
        var outputDone = false
        var chunk = ByteArray(0)
        try {
            while (!outputDone) {
                ensureActive()
                if (!inputDone) {
                    val inIndex = codec.dequeueInputBuffer(TIMEOUT_US)
                    if (inIndex >= 0) {
                        val buffer = requireNotNull(codec.getInputBuffer(inIndex))
                        val size = extractor.readSampleData(buffer, 0)
                        if (size < 0) {
                            codec.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            inputDone = true
                        } else {
                            codec.queueInputBuffer(inIndex, 0, size, extractor.sampleTime, 0)
                            extractor.advance()
                        }
                    }
                }
                val outIndex = codec.dequeueOutputBuffer(info, TIMEOUT_US)
                when {
                    outIndex >= 0 -> {
                        val buffer = requireNotNull(codec.getOutputBuffer(outIndex))
                        if (info.size > 0) {
                            buffer.position(info.offset)
                            buffer.limit(info.offset + info.size)
                            val pcm16 = toPcm16(buffer, pcmEncoding)
                            if (chunk.size < pcm16.remaining()) chunk = ByteArray(pcm16.remaining())
                            val length = pcm16.remaining()
                            pcm16.get(chunk, 0, length)
                            output.write(chunk, 0, length)
                            dataSize += length
                        }
                        codec.releaseOutputBuffer(outIndex, false)
                        if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) outputDone = true
                    }
                    outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        val format = codec.outputFormat
                        sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                        channelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                        if (format.containsKey(MediaFormat.KEY_PCM_ENCODING)) {
                            pcmEncoding = format.getInteger(MediaFormat.KEY_PCM_ENCODING)
                        }
                    }
                }
            }
            output.seek(0)
            output.write(createWavHeader(sampleRate, channelCount, dataSize))
            Log.info("Converted ${inputFile.absolutePath} to wav: ${sampleRate}Hz, $channelCount ch, $dataSize bytes")
        } finally {
            output.close()
            runCatching { codec.stop() }
            runCatching { codec.release() }
            extractor.release()
        }
    }

    private fun toPcm16(buffer: ByteBuffer, encoding: Int): ByteBuffer {
        val source = buffer.slice().order(ByteOrder.LITTLE_ENDIAN)
        return when (encoding) {
            android.media.AudioFormat.ENCODING_PCM_FLOAT -> {
                val floats = source.asFloatBuffer()
                val out = ByteBuffer.allocate(floats.remaining() * 2).order(ByteOrder.LITTLE_ENDIAN)
                while (floats.hasRemaining()) {
                    val value = (floats.get().coerceIn(-1f, 1f) * Short.MAX_VALUE).toInt()
                    out.putShort(value.toShort())
                }
                out.flip()
                out
            }
            android.media.AudioFormat.ENCODING_PCM_8BIT -> {
                val out = ByteBuffer.allocate(source.remaining() * 2).order(ByteOrder.LITTLE_ENDIAN)
                while (source.hasRemaining()) {
                    val value = ((source.get().toInt() and 0xFF) - 128) shl 8
                    out.putShort(value.toShort())
                }
                out.flip()
                out
            }
            else -> source
        }
    }

    private fun createWavHeader(sampleRate: Int, channels: Int, dataSize: Long): ByteArray {
        val header = ByteBuffer.allocate(WAV_HEADER_SIZE).order(ByteOrder.LITTLE_ENDIAN)
        val byteRate = sampleRate * channels * 2
        header.put("RIFF".toByteArray(Charsets.US_ASCII))
        header.putInt((36 + dataSize).coerceAtMost(0xFFFFFFFFL).toInt())
        header.put("WAVE".toByteArray(Charsets.US_ASCII))
        header.put("fmt ".toByteArray(Charsets.US_ASCII))
        header.putInt(16)
        header.putShort(1)
        header.putShort(channels.toShort())
        header.putInt(sampleRate)
        header.putInt(byteRate)
        header.putShort((channels * 2).toShort())
        header.putShort(16)
        header.put("data".toByteArray(Charsets.US_ASCII))
        header.putInt(dataSize.coerceAtMost(0xFFFFFFFFL).toInt())
        return header.array()
    }

    class ConversionException(cause: Throwable?) : WaveConverterException(Strings.FFmpegConverterException, cause)

    companion object {
        private const val TIMEOUT_US = 10_000L
        private const val WAV_HEADER_SIZE = 44
    }
}
