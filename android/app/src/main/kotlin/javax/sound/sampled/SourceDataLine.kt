@file:Suppress("PackageDirectoryMismatch")

package javax.sound.sampled

import android.media.AudioAttributes
import android.media.AudioTrack
import kotlin.math.max
import kotlin.math.min

interface SourceDataLine {
    val format: AudioFormat
    val isOpen: Boolean
    val isRunning: Boolean
    val isActive: Boolean
    val framePosition: Int
    val longFramePosition: Long

    fun open()
    fun open(format: AudioFormat)
    fun open(format: AudioFormat, bufferSize: Int)
    fun start()
    fun stop()
    fun flush()
    fun drain()
    fun close()
    fun write(b: ByteArray, off: Int, len: Int): Int
    fun available(): Int
    fun getBufferSize(): Int
}

/**
 * [SourceDataLine] implemented with a streaming [AudioTrack].
 *
 * Mirrors Java Sound semantics used by the player: `write` blocks until all data is queued, but returns early when the
 * line is flushed or closed; `drain` blocks until the queued data is played.
 */
internal class AudioTrackSourceDataLine(override var format: AudioFormat) : SourceDataLine {

    private val lock = Object()
    private var track: AudioTrack? = null
    private var bufferSizeInBytes = 0

    @Volatile
    private var running = false

    @Volatile
    private var closed = false

    @Volatile
    private var flushGeneration = 0

    /** Frames written since the last flush. */
    @Volatile
    private var framesWritten = 0L

    /** Playback head position of the track at the last flush. */
    @Volatile
    private var headBase = 0L

    private val outputChannels get() = min(format.channels, 2)
    private val outputFrameSize get() = outputChannels * 2

    override val isOpen: Boolean get() = track != null && !closed
    override val isRunning: Boolean get() = running
    override val isActive: Boolean get() = running
    override val framePosition: Int get() = longFramePosition.toInt()
    override val longFramePosition: Long
        get() = track?.let { (it.playbackHeadPosition.toLong() and 0xFFFFFFFFL) - headBase } ?: 0L

    override fun open() = open(format)

    override fun open(format: AudioFormat) = open(format, AudioSystem.NOT_SPECIFIED)

    override fun open(format: AudioFormat, bufferSize: Int) {
        synchronized(lock) {
            this.format = format
            if (format.encoding != AudioFormat.Encoding.PCM_SIGNED || format.sampleSizeInBits != 16) {
                throw LineUnavailableException("Only 16 bit signed PCM is supported for playback: $format")
            }
            val sampleRate = format.sampleRate.toInt()
            val channelMask = if (outputChannels == 1) {
                android.media.AudioFormat.CHANNEL_OUT_MONO
            } else {
                android.media.AudioFormat.CHANNEL_OUT_STEREO
            }
            val minBuffer = AudioTrack.getMinBufferSize(
                sampleRate,
                channelMask,
                android.media.AudioFormat.ENCODING_PCM_16BIT,
            ).coerceAtLeast(outputFrameSize * 256)
            bufferSizeInBytes = max(minBuffer, sampleRate * outputFrameSize / 20) // ~50ms
            track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build(),
                )
                .setAudioFormat(
                    android.media.AudioFormat.Builder()
                        .setEncoding(android.media.AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(channelMask)
                        .build(),
                )
                .setTransferMode(AudioTrack.MODE_STREAM)
                .setBufferSizeInBytes(bufferSizeInBytes)
                .build()
            closed = false
            framesWritten = 0
            headBase = 0
        }
    }

    override fun start() {
        val track = track ?: return
        if (!running) {
            running = true
            runCatching { track.play() }
        }
    }

    override fun stop() {
        val track = track ?: return
        if (running) {
            running = false
            runCatching { track.pause() }
        }
    }

    override fun flush() {
        val track = track ?: return
        synchronized(lock) {
            flushGeneration++
            runCatching { track.flush() }
            framesWritten = 0
            headBase = track.playbackHeadPosition.toLong() and 0xFFFFFFFFL
        }
    }

    override fun drain() {
        val track = track ?: return
        val generation = flushGeneration
        val target = framesWritten
        if (target <= 0) return
        // A streaming AudioTrack may wait for its buffer to be filled before starting, so pad with silence.
        writeSilence(bufferSizeInBytes, generation)
        while (!closed && running && generation == flushGeneration) {
            val played = (track.playbackHeadPosition.toLong() and 0xFFFFFFFFL) - headBase
            if (played >= target) break
            Thread.sleep(2)
        }
    }

    override fun close() {
        synchronized(lock) {
            closed = true
            running = false
            flushGeneration++
            track?.let {
                runCatching { it.pause() }
                runCatching { it.flush() }
                runCatching { it.release() }
            }
            track = null
        }
    }

    private fun convertToOutput(b: ByteArray, off: Int, len: Int): ByteArray {
        val channels = format.channels
        val needsSwap = format.isBigEndian
        if (channels <= 2 && !needsSwap) {
            return if (off == 0 && len == b.size) b else b.copyOfRange(off, off + len)
        }
        val inFrameSize = format.frameSize
        val frames = len / inFrameSize
        val out = ByteArray(frames * outputFrameSize)
        var o = 0
        for (f in 0 until frames) {
            val base = off + f * inFrameSize
            for (c in 0 until outputChannels) {
                val i = base + c * 2
                if (needsSwap) {
                    out[o] = b[i + 1]
                    out[o + 1] = b[i]
                } else {
                    out[o] = b[i]
                    out[o + 1] = b[i + 1]
                }
                o += 2
            }
        }
        return out
    }

    override fun write(b: ByteArray, off: Int, len: Int): Int {
        val track = track ?: return 0
        val generation = flushGeneration
        val data = convertToOutput(b, off, len)
        var written = 0
        val chunkSize = max(outputFrameSize, bufferSizeInBytes / 4 / outputFrameSize * outputFrameSize)
        while (written < data.size) {
            if (closed || generation != flushGeneration) break
            if (!running) {
                // Java Sound blocks writes on a stopped line until it's started, flushed or closed
                Thread.sleep(2)
                continue
            }
            val count = min(chunkSize, data.size - written)
            val result = track.write(data, written, count, AudioTrack.WRITE_NON_BLOCKING)
            if (result < 0) break
            if (result == 0) {
                Thread.sleep(2)
            } else {
                written += result
                framesWritten += result / outputFrameSize
            }
        }
        val inputWritten = if (data.size == 0) 0 else (written.toLong() * len / data.size).toInt()
        return inputWritten
    }

    private fun writeSilence(bytes: Int, generation: Int) {
        val track = track ?: return
        val silence = ByteArray(min(bytes, 1 shl 16))
        var remaining = bytes
        while (remaining > 0 && !closed && running && generation == flushGeneration) {
            val result = track.write(silence, 0, min(remaining, silence.size), AudioTrack.WRITE_NON_BLOCKING)
            if (result < 0) break
            if (result == 0) Thread.sleep(2) else remaining -= result
        }
    }

    override fun available(): Int = bufferSizeInBytes

    override fun getBufferSize(): Int = bufferSizeInBytes
}
