@file:Suppress("PackageDirectoryMismatch", "unused")

package androidx.compose.ui.text.platform

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.sdercolin.vlabeler.android.AndroidPlatform
import java.io.File
import java.security.MessageDigest

/**
 * Desktop `Font(identity, data, ...)`: on Android the data is written to a cache file and loaded from there.
 */
fun Font(
    identity: String,
    data: ByteArray,
    weight: FontWeight = FontWeight.Normal,
    style: FontStyle = FontStyle.Normal,
): Font {
    val digest = MessageDigest.getInstance("MD5").digest(data).joinToString("") { "%02x".format(it) }
    val dir = File(AndroidPlatform.context.cacheDir, "fonts").apply { mkdirs() }
    val file = File(dir, "$digest.ttf")
    if (!file.exists() || file.length() != data.size.toLong()) {
        file.writeBytes(data)
    }
    return androidx.compose.ui.text.font.Font(file, weight, style)
}
