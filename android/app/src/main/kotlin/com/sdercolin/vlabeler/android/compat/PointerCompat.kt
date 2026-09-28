package com.sdercolin.vlabeler.android.compat

import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.areAnyPressed
import androidx.compose.ui.input.pointer.isAltPressed
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.isMetaPressed
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.isShiftPressed
import androidx.compose.ui.input.pointer.isTouchEvent
import com.sdercolin.vlabeler.android.AndroidInputSettings
import com.sdercolin.vlabeler.model.key.Key

/*
 * Replacements of `PointerEvent.buttons.xxx` checks, which also work for touch pointers
 * (touch pointers don't set any mouse buttons).
 */

val PointerEvent.areAnyPressedCompat: Boolean
    get() = if (isTouchEvent) changes.any { it.pressed } else changes.any { it.pressed } || buttons.areAnyPressed

val PointerEvent.isPrimaryPressedCompat: Boolean
    get() = if (isTouchEvent) {
        changes.any { it.pressed } && !AndroidInputSettings.rightClickMode
    } else {
        buttons.isPrimaryPressed
    }

val PointerEvent.isSecondaryPressedCompat: Boolean
    get() = if (isTouchEvent) {
        changes.any { it.pressed } && AndroidInputSettings.rightClickMode
    } else {
        buttons.isSecondaryPressed
    }

/**
 * Replacement of `PointerEvent.keyboardModifiers`, which also considers the modifier keys held by the on-screen
 * toolbar (e.g. Ctrl + tap to add an entry to the multi-selection in the entry list).
 */
class KeyboardModifiersCompat(
    val isCtrlPressed: Boolean,
    val isMetaPressed: Boolean,
    val isShiftPressed: Boolean,
    val isAltPressed: Boolean,
)

val PointerEvent.keyboardModifiersCompat: KeyboardModifiersCompat
    get() {
        val actual = keyboardModifiers
        val virtual = AndroidInputSettings.virtualModifiers
        return KeyboardModifiersCompat(
            isCtrlPressed = actual.isCtrlPressed || Key.Ctrl in virtual,
            isMetaPressed = actual.isMetaPressed,
            isShiftPressed = actual.isShiftPressed || Key.Shift in virtual,
            isAltPressed = actual.isAltPressed || Key.Alt in virtual,
        )
    }
