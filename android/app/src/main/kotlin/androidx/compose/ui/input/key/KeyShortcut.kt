@file:Suppress("PackageDirectoryMismatch")

package androidx.compose.ui.input.key

/**
 * Android version of the desktop-only `KeyShortcut` used by menu items.
 */
data class KeyShortcut(
    val key: Key,
    val ctrl: Boolean = false,
    val meta: Boolean = false,
    val alt: Boolean = false,
    val shift: Boolean = false,
) {
    override fun toString(): String = buildString {
        if (ctrl) append("Ctrl+")
        if (meta) append("Meta+")
        if (alt) append("Alt+")
        if (shift) append("Shift+")
        append(KeyShortcutNames.name(key))
    }

    /** Whether this shortcut matches the given key down event. */
    fun matches(event: KeyEvent): Boolean =
        event.type == KeyEventType.KeyDown &&
            event.key == key &&
            event.isCtrlPressed == ctrl &&
            event.isMetaPressed == meta &&
            event.isAltPressed == alt &&
            event.isShiftPressed == shift
}

internal object KeyShortcutNames {
    fun name(key: Key): String {
        val raw = android.view.KeyEvent.keyCodeToString(key.nativeKeyCode).removePrefix("KEYCODE_")
        return when (raw) {
            "DPAD_UP" -> "Up"
            "DPAD_DOWN" -> "Down"
            "DPAD_LEFT" -> "Left"
            "DPAD_RIGHT" -> "Right"
            "MINUS" -> "-"
            "EQUALS" -> "="
            "SLASH" -> "/"
            "ESCAPE" -> "Esc"
            "SPACE" -> "Space"
            "ENTER" -> "Enter"
            "DEL" -> "Backspace"
            "FORWARD_DEL" -> "Delete"
            else -> raw.lowercase().replaceFirstChar { it.uppercase() }
        }
    }
}
