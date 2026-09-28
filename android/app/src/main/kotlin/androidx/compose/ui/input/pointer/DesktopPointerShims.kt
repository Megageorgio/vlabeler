@file:Suppress("PackageDirectoryMismatch", "unused")

package androidx.compose.ui.input.pointer

/*
 * Android implementations of desktop-only pointer APIs used by vLabeler.
 */

import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import com.sdercolin.vlabeler.android.AndroidInputSettings
import com.sdercolin.vlabeler.android.compat.awt.Cursor

/**
 * Mouse button of a pointer event (desktop-only API in Compose Multiplatform).
 */
@JvmInline
value class PointerButton(val index: Int) {
    companion object {
        val Primary = PointerButton(0)
        val Secondary = PointerButton(1)
        val Tertiary = PointerButton(2)
        val Back = PointerButton(3)
        val Forward = PointerButton(4)
    }
}

internal object PointerButtonTracker {
    @Volatile
    var lastPressedButton: PointerButton = PointerButton.Primary
}

val PointerEvent.isTouchEvent: Boolean
    get() = changes.any { it.type == PointerType.Touch || it.type == PointerType.Stylus }

/**
 * The button that caused this press/release event.
 *
 * For touch input, the primary button is reported unless the "right click" mode is enabled in the on-screen toolbar.
 */
val PointerEvent.button: PointerButton?
    get() = when (type) {
        PointerEventType.Press -> {
            val result = when {
                isTouchEvent -> if (AndroidInputSettings.rightClickMode) PointerButton.Secondary else PointerButton.Primary
                buttons.isSecondaryPressed -> PointerButton.Secondary
                buttons.isTertiaryPressed -> PointerButton.Tertiary
                buttons.isBackPressed -> PointerButton.Back
                buttons.isForwardPressed -> PointerButton.Forward
                else -> PointerButton.Primary
            }
            PointerButtonTracker.lastPressedButton = result
            result
        }
        PointerEventType.Release -> {
            if (isTouchEvent) {
                if (AndroidInputSettings.rightClickMode) PointerButton.Secondary else PointerButton.Primary
            } else {
                PointerButtonTracker.lastPressedButton
            }
        }
        else -> null
    }

private fun PointerEvent.isTouchPress(): Boolean =
    isTouchEvent && changes.any { it.pressed && !it.previousPressed }

/**
 * Desktop `Modifier.onPointerEvent` implemented on Android.
 *
 * To emulate a mouse with touch input, a touch press is also delivered to `Move` handlers (during the initial pass,
 * i.e. before `Press` handlers), because a mouse always hovers over the position before pressing.
 */
fun Modifier.onPointerEvent(
    eventType: PointerEventType,
    pass: PointerEventPass = PointerEventPass.Main,
    onEvent: AwaitPointerEventScope.(event: PointerEvent) -> Unit,
): Modifier = composed {
    val currentOnEvent by rememberUpdatedState(onEvent)
    pointerInput(eventType, pass) {
        awaitPointerEventScope {
            while (true) {
                val initialEvent = awaitPointerEvent(PointerEventPass.Initial)
                if (eventType == PointerEventType.Move && initialEvent.isTouchPress()) {
                    currentOnEvent(initialEvent)
                }
                val event = if (pass == PointerEventPass.Initial) initialEvent else awaitPointerEvent(pass)
                // Events consumed by an ancestor in the initial pass (e.g. two-finger navigation) are skipped
                val consumedByAncestor = pass != PointerEventPass.Initial && initialEvent.changes.isNotEmpty() &&
                    initialEvent.changes.all { it.isConsumed }
                if (event.type == eventType && !consumedByAncestor) {
                    currentOnEvent(event)
                }
            }
        }
    }
}

/**
 * Converts an AWT cursor (compat) to a pointer icon.
 */
fun PointerIcon(cursor: Cursor): PointerIcon = when (cursor.type) {
    Cursor.HAND_CURSOR -> PointerIcon.Hand
    Cursor.TEXT_CURSOR -> PointerIcon.Text
    Cursor.CROSSHAIR_CURSOR -> PointerIcon.Crosshair
    else -> PointerIcon.Default
}
