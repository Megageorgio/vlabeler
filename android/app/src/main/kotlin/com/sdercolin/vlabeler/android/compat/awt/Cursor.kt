package com.sdercolin.vlabeler.android.compat.awt

/**
 * Minimal replacement of `java.awt.Cursor`, only used to choose pointer icons.
 */
class Cursor(val type: Int) {
    companion object {
        const val DEFAULT_CURSOR = 0
        const val CROSSHAIR_CURSOR = 1
        const val TEXT_CURSOR = 2
        const val WAIT_CURSOR = 3
        const val SW_RESIZE_CURSOR = 4
        const val SE_RESIZE_CURSOR = 5
        const val NW_RESIZE_CURSOR = 6
        const val NE_RESIZE_CURSOR = 7
        const val N_RESIZE_CURSOR = 8
        const val S_RESIZE_CURSOR = 9
        const val W_RESIZE_CURSOR = 10
        const val E_RESIZE_CURSOR = 11
        const val HAND_CURSOR = 12
        const val MOVE_CURSOR = 13

        fun getDefaultCursor() = Cursor(DEFAULT_CURSOR)
    }
}
