@file:Suppress("PackageDirectoryMismatch", "unused")

package androidx.compose.ui.res

/*
 * Desktop-only resource APIs, reading Java resources packaged in the APK.
 */

import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import java.io.InputStream

private object ResourceLoaderAnchor

fun openResourceStream(resourcePath: String): InputStream {
    val path = resourcePath.removePrefix("/")
    return ResourceLoaderAnchor::class.java.classLoader?.getResourceAsStream(path)
        ?: Thread.currentThread().contextClassLoader?.getResourceAsStream(path)
        ?: throw IllegalArgumentException("Resource $resourcePath not found")
}

inline fun <T> useResource(resourcePath: String, block: (InputStream) -> T): T =
    openResourceStream(resourcePath).use(block)

fun loadImageBitmap(inputStream: InputStream): ImageBitmap {
    val bitmap = BitmapFactory.decodeStream(inputStream)
        ?: throw IllegalArgumentException("Cannot decode image")
    return bitmap.asImageBitmap()
}

@Composable
fun painterResource(resourcePath: String): Painter = remember(resourcePath) {
    // .ico is not always supported by BitmapFactory, use the png version of the app icon instead
    val path = if (resourcePath.endsWith(".ico")) resourcePath.removeSuffix(".ico") + ".png" else resourcePath
    BitmapPainter(useResource(path, ::loadImageBitmap))
}
