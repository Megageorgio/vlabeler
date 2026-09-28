package com.sdercolin.vlabeler.android

/**
 * Hit-test tolerances for touch input (desktop values are in pixels for a precise mouse pointer).
 */
object TouchTolerance {
    fun dpToPx(dp: Float): Float {
        val density = runCatching { AndroidPlatform.context.resources.displayMetrics.density }.getOrDefault(1f)
        return dp * density
    }
}
