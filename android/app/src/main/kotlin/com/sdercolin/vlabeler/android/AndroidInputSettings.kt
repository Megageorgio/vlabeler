package com.sdercolin.vlabeler.android

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.sdercolin.vlabeler.model.key.Key

/**
 * Settings of touch input emulation, controlled by the on-screen toolbar.
 */
object AndroidInputSettings {

    /** When enabled, touches are treated as right (secondary) mouse button clicks. */
    var rightClickMode: Boolean by mutableStateOf(false)

    /** Show tooltips on long press. */
    var longPressTooltips: Boolean by mutableStateOf(false)

    /** Modifier keys held by the on-screen toolbar (Ctrl, Shift, Alt, Windows/Meta). */
    var virtualModifiers: Set<Key> by mutableStateOf(emptySet())

    fun toggleModifier(key: Key) {
        virtualModifiers = if (key in virtualModifiers) virtualModifiers - key else virtualModifiers + key
    }
}

/**
 * On Android, the canvas is not scrolled by one-finger drags, which are used to emulate the mouse.
 */
const val isMouseScrollEnabled = false
