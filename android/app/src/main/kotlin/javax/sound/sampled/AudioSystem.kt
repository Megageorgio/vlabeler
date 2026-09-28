@file:Suppress("PackageDirectoryMismatch")

package javax.sound.sampled

import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.EOFException
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import kotlin.math.floor

/**
 * Android implementation of the subset of `javax.sound.sampled.AudioSystem` used by vLabeler.
 *
 * - Reading: WAV files (PCM 8/16/24/32 bit, IEEE float 32/64 bit, WAVE_FORMAT_EXTENSIBLE).
 *   Other formats are converted to WAV in advance by `MediaCodecConverter`.
 * - Conversion: to signed PCM with another sample size/endianness, and down-sampling (linear interpolation).
 * - Playback: [SourceDataLine] backed by `android.media.AudioTrack`.
 */
object AudioSystem {

    const val NOT_SPECIFIED = AudioFormat.NOT_SPECIFIED

    @JvmStatic
    fun getAudioInputStream(file: File): AudioInputStream {
        val input = BufferedInputStream(FileInputStream(file), 1 shl 16)
        try {
            return WavReader.read(input, file.length())
        } catch (t: Throwable) {
            input.close()
            if (t is UnsupportedAudioFileException) throw t
            throw UnsupportedAudioFileException("Cannot read audio file ${file.absolutePath}: ${t.message}")
        }
    }

    @JvmStatic
    fun getAudioInputStream(targetFormat: AudioFormat?, sourceStream: AudioInputStream): AudioInputStream {
        requireNotNull(targetFormat)
        val sourceFormat = sourceStream.format
        if (sourceFormat.matches(targetFormat)) return sourceStream
        if (targetFormat.encoding != AudioFormat.Encoding.PCM_SIGNED) {
            throw IllegalArgumentException("Unsupported conversion: $sourceFormat -> $targetFormat")
        }
        if (targetFormat.channels != sourceFormat.channels) {
            throw IllegalArgumentException("Unsupported conversion of channel count: $sourceFormat -> $targetFormat")
        }
        return ConvertingAudioInputStream(sourceStream, targetFormat)
    }

    @JvmStatic
    fun getSourceDataLine(format: AudioFormat?): SourceDataLine = AudioTrackSourceDataLine(requireNotNull(format))
}

private object WavReader {

    private fun DataInputStream.readIntLE(): Int {
        val b0 = readUnsignedByte()
        val b1 = readUnsignedByte()
        val b2 = readUnsignedByte()
        val b3 = readUnsignedByte()
        return b0 or (b1 shl 8) or (b2 shl 16) or (b3 shl 24)
    }

    private fun DataInputStream.readShortLE(): Int {
        val b0 = readUnsignedByte()
        val b1 = readUnsignedByte()
        return b0 or (b1 shl 8)
    }

    private fun DataInputStream.readTag(): String {
        val bytes = ByteArray(4)
        readFully(bytes)
        return String(bytes, Charsets.US_ASCII)
    }

    private fun DataInputStream.skipFully(n: Long) {
        var remaining = n
        while (remaining > 0) {
            val skipped = skip(remaining)
            if (skipped <= 0) {
                read().takeIf { it >= 0 } ?: throw EOFException()
                remaining--
            } else {
                remaining -= skipped
            }
        }
    }

    fun read(input: InputStream, fileLength: Long): AudioInputStream {
        val data = DataInputStream(input)
        val riff = data.readTag()
        if (riff != "RIFF" && riff != "RF64") throw UnsupportedAudioFileException("Not a RIFF file")
        data.readIntLE() // riff size
        if (data.readTag() != "WAVE") throw UnsupportedAudioFileException("Not a WAVE file")
        var consumed = 12L
        var format: AudioFormat? = null
        var ds64DataSize: Long? = null
        while (true) {
            val tag = try {
                data.readTag()
            } catch (e: EOFException) {
                throw UnsupportedAudioFileException("No data chunk found")
            }
            val size = data.readIntLE().toLong() and 0xFFFFFFFFL
            consumed += 8
            when (tag) {
                "ds64" -> {
                    data.readIntLE()
                    data.readIntLE() // riff size 64
                    val low = data.readIntLE().toLong() and 0xFFFFFFFFL
                    val high = data.readIntLE().toLong() and 0xFFFFFFFFL
                    ds64DataSize = low or (high shl 32)
                    data.skipFully(size - 16 + (size and 1))
                }
                "fmt " -> {
                    val formatTag = data.readShortLE()
                    val channels = data.readShortLE()
                    val sampleRate = data.readIntLE()
                    data.readIntLE() // byte rate
                    val blockAlign = data.readShortLE()
                    val bitsPerSample = data.readShortLE()
                    var read = 16L
                    var actualTag = formatTag
                    if (formatTag == 0xFFFE && size >= 40) {
                        data.readShortLE() // cbSize
                        data.readShortLE() // valid bits
                        data.readIntLE() // channel mask
                        actualTag = data.readShortLE() // first 2 bytes of the sub format GUID
                        data.skipFully(14)
                        read = 40
                    }
                    data.skipFully(size - read + (size and 1))
                    val encoding = when (actualTag) {
                        1 -> if (bitsPerSample <= 8) AudioFormat.Encoding.PCM_UNSIGNED else AudioFormat.Encoding.PCM_SIGNED
                        3 -> AudioFormat.Encoding.PCM_FLOAT
                        6 -> AudioFormat.Encoding.ALAW
                        7 -> AudioFormat.Encoding.ULAW
                        else -> throw UnsupportedAudioFileException("Unsupported WAV format tag: $actualTag")
                    }
                    val frameSize = if (blockAlign > 0) blockAlign else (bitsPerSample + 7) / 8 * channels
                    format = AudioFormat(
                        encoding = encoding,
                        sampleRate = sampleRate.toFloat(),
                        sampleSizeInBits = bitsPerSample,
                        channels = channels,
                        frameSize = frameSize,
                        frameRate = sampleRate.toFloat(),
                        isBigEndian = false,
                    )
                }
                "data" -> {
                    val audioFormat = format ?: throw UnsupportedAudioFileException("fmt chunk not found before data")
                    var dataSize = if (size == 0xFFFFFFFFL && ds64DataSize != null) ds64DataSize else size
                    consumed += 0
                    val maxAvailable = fileLength - consumed
                    if (dataSize > maxAvailable || dataSize == 0L) dataSize = maxAvailable.coerceAtLeast(0)
                    val frameLength = dataSize / audioFormat.frameSize
                    val limited = LimitedInputStream(data, frameLength * audioFormat.frameSize)
                    return AudioInputStream(limited, audioFormat, frameLength)
                }
                else -> data.skipFully(size + (size and 1))
            }
            consumed += size + (size and 1)
        }
    }
}

private class LimitedInputStream(private val input: InputStream, private var remaining: Long) : InputStream() {
    override fun read(): Int {
        if (remaining <= 0) return -1
        val value = input.read()
        if (value >= 0) remaining--
        return value
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (remaining <= 0) return -1
        val count = input.read(b, off, minOf(len.toLong(), remaining).toInt())
        if (count > 0) remaining -= count
        return count
    }

    override fun skip(n: Long): Long {
        val skipped = input.skip(minOf(n, remaining))
        if (skipped > 0) remaining -= skipped
        return skipped
    }

    override fun available(): Int = minOf(input.available().toLong(), remaining).toInt()

    override fun close() = input.close()
}

/**
 * Decodes one sample to a normalized value in [-1, 1].
 */
internal fun decodeSample(bytes: ByteArray, offset: Int, format: AudioFormat): Double {
    val size = format.bytesPerSample
    val bigEndian = format.isBigEndian
    fun byteAt(i: Int): Int = bytes[offset + if (bigEndian) i else size - 1 - i].toInt() and 0xFF // i=0: MSB
    return when (format.encoding) {
        AudioFormat.Encoding.PCM_FLOAT -> {
            var bits = 0L
            for (i in 0 until size) bits = (bits shl 8) or byteAt(i).toLong()
            if (size == 8) java.lang.Double.longBitsToDouble(bits) else java.lang.Float.intBitsToFloat(bits.toInt()).toDouble()
        }
        AudioFormat.Encoding.PCM_UNSIGNED -> {
            var value = 0L
            for (i in 0 until size) value = (value shl 8) or byteAt(i).toLong()
            val half = 1L shl (size * 8 - 1)
            (value - half).toDouble() / half
        }
        AudioFormat.Encoding.ULAW -> ulawToLinear(bytes[offset].toInt() and 0xFF) / 32768.0
        AudioFormat.Encoding.ALAW -> alawToLinear(bytes[offset].toInt() and 0xFF) / 32768.0
        else -> {
            var value = 0L
            for (i in 0 until size) value = (value shl 8) or byteAt(i).toLong()
            val bits = size * 8
            // sign extend
            value = (value shl (64 - bits)) shr (64 - bits)
            value.toDouble() / (1L shl (bits - 1)).toDouble()
        }
    }
}

private fun ulawToLinear(value: Int): Int {
    val u = value.inv() and 0xFF
    val t = ((u and 0x0F) shl 3) + 0x84
    val shifted = t shl ((u and 0x70) shr 4)
    return if (u and 0x80 != 0) 0x84 - shifted else shifted - 0x84
}

private fun alawToLinear(value: Int): Int {
    val a = value xor 0x55
    var t = (a and 0x0F) shl 4
    val seg = (a and 0x70) shr 4
    when (seg) {
        0 -> t += 8
        1 -> t += 0x108
        else -> {
            t += 0x108
            t = t shl (seg - 1)
        }
    }
    return if (a and 0x80 != 0) t else -t
}

/**
 * Encodes a normalized value to signed PCM.
 */
internal fun encodeSample(value: Double, bytes: ByteArray, offset: Int, format: AudioFormat) {
    val size = format.bytesPerSample
    val bits = size * 8
    val max = (1L shl (bits - 1)) - 1
    val min = -(1L shl (bits - 1))
    val intValue = (value * (max + 1)).roundToLong().coerceIn(min, max)
    for (i in 0 until size) {
        // i = 0: least significant byte
        val byte = (intValue shr (8 * i)).toByte()
        val index = if (format.isBigEndian) offset + size - 1 - i else offset + i
        bytes[index] = byte
    }
}

private fun Double.roundToLong(): Long = kotlin.math.round(this).toLong()

/**
 * Converts sample size / encoding / endianness and down-samples with linear interpolation.
 */
private class ConvertingAudioInputStream(
    private val source: AudioInputStream,
    private val targetFormat: AudioFormat,
) : AudioInputStream(
    stream = nullInputStream,
    format = targetFormat,
    frameLength = if (source.frameLength < 0) {
        -1
    } else {
        (source.frameLength * targetFormat.sampleRate.toDouble() / source.format.sampleRate).toLong()
    },
) {
    private val sourceFormat = source.format
    private val channels = sourceFormat.channels
    private val ratio = sourceFormat.sampleRate.toDouble() / targetFormat.sampleRate.toDouble()
    private val sourceFrameSize = sourceFormat.frameSize
    private val targetFrameSize = targetFormat.frameSize

    /** Index of the next target frame to produce. */
    private var targetFrameIndex = 0L

    /** Index of the source frame stored in [current]. -1 if nothing is loaded. */
    private var currentIndex = -1L
    private val current = DoubleArray(channels)
    private val next = DoubleArray(channels)
    private var nextValid = false

    /** Index of the next unread source frame. */
    private var sourcePosition = 0L
    private var sourceEnded = false

    private val frameBuffer = ByteArray(sourceFrameSize)
    private val outFrame = ByteArray(targetFrameSize)
    private var outFrameOffset = targetFrameSize // no pending bytes

    private fun readSourceFrame(into: DoubleArray): Boolean {
        if (sourceEnded) return false
        var total = 0
        while (total < sourceFrameSize) {
            val count = source.read(frameBuffer, total, sourceFrameSize - total)
            if (count < 0) {
                sourceEnded = true
                return false
            }
            total += count
        }
        val sampleBytes = sourceFormat.bytesPerSample
        for (c in 0 until channels) {
            into[c] = decodeSample(frameBuffer, c * sampleBytes, sourceFormat)
        }
        sourcePosition++
        return true
    }

    private fun skipSourceFrames(count: Long) {
        if (count <= 0) return
        val bytes = count * sourceFrameSize
        val skipped = source.skip(bytes)
        sourcePosition += skipped / sourceFrameSize
        if (skipped < bytes) sourceEnded = true
    }

    /** Make [current] hold source frame [index] and [next] hold [index] + 1 (if available). */
    private fun ensureSourceFrames(index: Long): Boolean {
        if (currentIndex == index) return true
        if (currentIndex + 1 == index && nextValid) {
            next.copyInto(current)
            currentIndex = index
            nextValid = readSourceFrame(next)
            return true
        }
        if (index < sourcePosition) {
            // cannot go back, reuse the current frame
            return currentIndex >= 0
        }
        skipSourceFrames(index - sourcePosition)
        if (!readSourceFrame(current)) return false
        currentIndex = index
        nextValid = readSourceFrame(next)
        return true
    }

    private fun produceFrame(): Boolean {
        val position = targetFrameIndex * ratio
        val index = floor(position).toLong()
        val weight = position - index
        if (!ensureSourceFrames(index)) return false
        val sampleBytes = targetFormat.bytesPerSample
        for (c in 0 until channels) {
            val value = if (nextValid && weight > 0) {
                current[c] * (1 - weight) + next[c] * weight
            } else {
                current[c]
            }
            encodeSample(value, outFrame, c * sampleBytes, targetFormat)
        }
        targetFrameIndex++
        outFrameOffset = 0
        return true
    }

    override fun read(): Int {
        val single = ByteArray(1)
        val count = read(single, 0, 1)
        return if (count <= 0) -1 else single[0].toInt() and 0xFF
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (len == 0) return 0
        var written = 0
        while (written < len) {
            if (outFrameOffset >= targetFrameSize) {
                if (frameLength >= 0 && targetFrameIndex >= frameLength) break
                if (!produceFrame()) break
            }
            val count = minOf(len - written, targetFrameSize - outFrameOffset)
            System.arraycopy(outFrame, outFrameOffset, b, off + written, count)
            outFrameOffset += count
            written += count
        }
        return if (written == 0) -1 else written
    }

    override fun skip(n: Long): Long {
        if (n <= 0) return 0
        var skipped = 0L
        // drop pending bytes of the current frame
        if (outFrameOffset < targetFrameSize) {
            val count = minOf(n, (targetFrameSize - outFrameOffset).toLong())
            outFrameOffset += count.toInt()
            skipped += count
        }
        val frames = (n - skipped) / targetFrameSize
        if (frames > 0) {
            val maxFrames = if (frameLength >= 0) (frameLength - targetFrameIndex).coerceAtLeast(0) else frames
            val actual = minOf(frames, maxFrames)
            targetFrameIndex += actual
            skipped += actual * targetFrameSize
        }
        val rest = n - skipped
        if (rest > 0) {
            val buffer = ByteArray(rest.toInt())
            val count = read(buffer, 0, buffer.size)
            if (count > 0) skipped += count
        }
        return skipped
    }

    override fun available(): Int = 0

    override fun close() = source.close()

    companion object {
        private val nullInputStream = object : InputStream() {
            override fun read(): Int = -1
        }
    }
}
