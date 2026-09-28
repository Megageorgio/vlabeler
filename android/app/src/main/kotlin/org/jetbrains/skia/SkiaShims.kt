@file:Suppress("PackageDirectoryMismatch", "unused")

package org.jetbrains.skia

/*
 * Tiny subset of the Skia (Skiko) API used by vLabeler to build chart images from raw pixel data,
 * implemented with android.graphics.Bitmap.
 */

enum class ColorType { UNKNOWN, ALPHA_8, RGB_565, ARGB_4444, RGBA_8888, RGB_888X, BGRA_8888 }

enum class ColorAlphaType { UNKNOWN, OPAQUE, PREMUL, UNPREMUL }

class ImageInfo(
    val width: Int,
    val height: Int,
    val colorType: ColorType,
    val colorAlphaType: ColorAlphaType,
)

class Image private constructor(val imageInfo: ImageInfo, val bytes: ByteArray, val rowBytes: Int) {
    val width get() = imageInfo.width
    val height get() = imageInfo.height

    companion object {
        fun makeRaster(imageInfo: ImageInfo, bytes: ByteArray, rowBytes: Int): Image =
            Image(imageInfo, bytes, rowBytes)
    }
}

class Bitmap private constructor(val androidBitmap: android.graphics.Bitmap) {
    companion object {
        fun makeFromImage(image: Image): Bitmap {
            val info = image.imageInfo
            val width = info.width.coerceAtLeast(1)
            val height = info.height.coerceAtLeast(1)
            val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
            if (info.width > 0 && info.height > 0) {
                val src = image.bytes
                val rowBytes = image.rowBytes
                val packed = ByteArray(width * height * 4)
                // Android ARGB_8888 is stored as RGBA bytes (premultiplied)
                val swapRedBlue = info.colorType == ColorType.BGRA_8888
                for (y in 0 until height) {
                    var srcIndex = y * rowBytes
                    var dstIndex = y * width * 4
                    for (x in 0 until width) {
                        val c0 = src[srcIndex]
                        val c1 = src[srcIndex + 1]
                        val c2 = src[srcIndex + 2]
                        val a = src[srcIndex + 3]
                        if (swapRedBlue) {
                            packed[dstIndex] = c2
                            packed[dstIndex + 2] = c0
                        } else {
                            packed[dstIndex] = c0
                            packed[dstIndex + 2] = c2
                        }
                        packed[dstIndex + 1] = c1
                        packed[dstIndex + 3] = a
                        srcIndex += 4
                        dstIndex += 4
                    }
                }
                bitmap.copyPixelsFromBuffer(java.nio.ByteBuffer.wrap(packed))
            }
            return Bitmap(bitmap)
        }
    }
}
