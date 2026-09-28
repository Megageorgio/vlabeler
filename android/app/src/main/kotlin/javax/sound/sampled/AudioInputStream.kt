@file:Suppress("PackageDirectoryMismatch")

package javax.sound.sampled

import java.io.InputStream

/**
 * An input stream of audio data with a known [format] and [frameLength].
 *
 * `readNBytes` and `readAllBytes` are implemented here because `InputStream` only provides them since Android 13.
 */
open class AudioInputStream(
    private val stream: InputStream,
    val format: AudioFormat,
    val frameLength: Long,
) : InputStream() {

    override fun read(): Int = stream.read()

    override fun read(b: ByteArray, off: Int, len: Int): Int = stream.read(b, off, len)

    override fun skip(n: Long): Long {
        var remaining = n
        while (remaining > 0) {
            val skipped = stream.skip(remaining)
            if (skipped <= 0) {
                // fall back to reading
                if (stream.read() < 0) break
                remaining--
            } else {
                remaining -= skipped
            }
        }
        return n - remaining
    }

    override fun available(): Int = stream.available()

    override fun close() = stream.close()

    override fun readNBytes(b: ByteArray, off: Int, len: Int): Int {
        var total = 0
        while (total < len) {
            val count = read(b, off + total, len - total)
            if (count < 0) break
            total += count
        }
        return total
    }

    override fun readAllBytes(): ByteArray {
        val output = java.io.ByteArrayOutputStream(
            if (frameLength > 0 && format.frameSize > 0) {
                (frameLength * format.frameSize).coerceAtMost(Int.MAX_VALUE.toLong() - 8).toInt()
            } else {
                1 shl 16
            },
        )
        val buffer = ByteArray(1 shl 16)
        while (true) {
            val count = read(buffer, 0, buffer.size)
            if (count < 0) break
            output.write(buffer, 0, count)
        }
        return output.toByteArray()
    }

    override fun markSupported(): Boolean = false
}
