@file:Suppress("PackageDirectoryMismatch")

package javax.sound.sampled

/**
 * Android implementation of the subset of Java Sound API (`javax.sound.sampled`) used by vLabeler.
 */
open class AudioFormat(
    val encoding: Encoding,
    val sampleRate: Float,
    val sampleSizeInBits: Int,
    val channels: Int,
    val frameSize: Int,
    val frameRate: Float,
    val isBigEndian: Boolean,
) {
    constructor(
        sampleRate: Float,
        sampleSizeInBits: Int,
        channels: Int,
        signed: Boolean,
        bigEndian: Boolean,
    ) : this(
        encoding = if (signed) Encoding.PCM_SIGNED else Encoding.PCM_UNSIGNED,
        sampleRate = sampleRate,
        sampleSizeInBits = sampleSizeInBits,
        channels = channels,
        frameSize = if (channels == NOT_SPECIFIED || sampleSizeInBits == NOT_SPECIFIED) {
            NOT_SPECIFIED
        } else {
            (sampleSizeInBits + 7) / 8 * channels
        },
        frameRate = sampleRate,
        isBigEndian = bigEndian,
    )

    val bytesPerSample: Int get() = (sampleSizeInBits + 7) / 8

    fun matches(format: AudioFormat): Boolean =
        format.encoding == encoding &&
            (format.channels == NOT_SPECIFIED || format.channels == channels) &&
            (format.sampleRate == NOT_SPECIFIED.toFloat() || format.sampleRate == sampleRate) &&
            format.sampleSizeInBits == sampleSizeInBits &&
            (sampleSizeInBits <= 8 || format.isBigEndian == isBigEndian)

    override fun toString(): String =
        "$encoding $sampleRate Hz, $sampleSizeInBits bit, " +
            (if (channels == 1) "mono" else if (channels == 2) "stereo" else "$channels channels") +
            ", $frameSize bytes/frame, " + (if (isBigEndian) "big-endian" else "little-endian")

    class Encoding(private val name: String) {
        override fun toString(): String = name
        override fun equals(other: Any?): Boolean = other is Encoding && other.name == name
        override fun hashCode(): Int = name.hashCode()

        companion object {
            @JvmField
            val PCM_SIGNED = Encoding("PCM_SIGNED")

            @JvmField
            val PCM_UNSIGNED = Encoding("PCM_UNSIGNED")

            @JvmField
            val PCM_FLOAT = Encoding("PCM_FLOAT")

            @JvmField
            val ULAW = Encoding("ULAW")

            @JvmField
            val ALAW = Encoding("ALAW")
        }
    }

    companion object {
        const val NOT_SPECIFIED = -1
    }
}

class UnsupportedAudioFileException(message: String) : Exception(message)

class LineUnavailableException(message: String) : Exception(message)
