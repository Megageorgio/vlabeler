@file:Suppress("PackageDirectoryMismatch", "unused")

package androidx.compose.ui.graphics

/**
 * Converts the Skia bitmap shim to a Compose [ImageBitmap].
 */
fun org.jetbrains.skia.Bitmap.asComposeImageBitmap(): ImageBitmap = androidBitmap.asImageBitmap()
