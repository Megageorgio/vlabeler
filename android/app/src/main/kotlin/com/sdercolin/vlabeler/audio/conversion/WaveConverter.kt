package com.sdercolin.vlabeler.audio.conversion

import com.sdercolin.vlabeler.model.AppConf
import java.io.File

/**
 * An interface for audio converters to convert audio files to wav.
 *
 * Android: FFmpeg is not available, so non-wav files are decoded with the platform codecs ([MediaCodecConverter]).
 */
interface WaveConverter {

    /**
     * Check if the converter can convert the given file.
     */
    fun accept(inputFile: File, conf: AppConf.Conversion): Boolean

    /**
     * Convert the given file to wav.
     */
    suspend fun convert(inputFile: File, outputFile: File, conf: AppConf.Conversion)

    companion object {
        val converters: List<WaveConverter> = listOf(MediaCodecConverter())
    }
}
