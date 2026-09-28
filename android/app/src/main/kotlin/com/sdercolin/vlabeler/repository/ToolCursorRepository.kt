package com.sdercolin.vlabeler.repository

import com.sdercolin.vlabeler.android.compat.awt.Cursor
import com.sdercolin.vlabeler.ui.editor.Tool

/**
 * Repository for tool cursors. Android: custom mouse cursors are not used.
 */
object ToolCursorRepository {
    fun get(tool: Tool): Cursor = when (tool) {
        Tool.Cursor -> Cursor.getDefaultCursor()
        else -> Cursor(Cursor.CROSSHAIR_CURSOR)
    }
}
