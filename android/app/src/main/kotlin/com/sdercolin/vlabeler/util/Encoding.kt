package com.sdercolin.vlabeler.util

import org.mozilla.universalchardet.UniversalDetector

val AvailableEncodings = listOf(
    "UTF-8",
    "Shift-JIS",
    "GBK",
    "ISO-8859-1",
    "Windows-1251",
    "Windows-1252",
    "GB2312",
    "ISO-8859-9",
    "EUC-JP",
    "EUC-KR",
)

val DefaultEncoding = AvailableEncodings[0]

fun encodingNameEquals(first: String, second: String) =
    cleanEncodingName(first).equals(cleanEncodingName(second), ignoreCase = true)

private fun cleanEncodingName(name: String) = name
    .replace("_", " ")
    .replace("-", " ")

/**
 * Android: detected with juniversalchardet instead of Apache Tika.
 */
fun ByteArray.detectEncoding(): String? {
    val detector = UniversalDetector(null)
    detector.handleData(this, 0, size)
    detector.dataEnd()
    val detected = detector.detectedCharset
    detector.reset()
    return when {
        detected != null -> detected
        all { it >= 0 } -> "UTF-8" // pure ASCII
        else -> null
    }
}
