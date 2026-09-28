@file:Suppress("unused", "PackageDirectoryMismatch", "UNUSED_PARAMETER")

package androidx.compose.ui.window

/*
 * Android implementations of desktop-only window APIs used by vLabeler.
 *
 * - DialogWindow is shown as a (nearly) full-screen Compose Dialog.
 * - MenuBar builds a menu model which is rendered by the Android top bar (see AndroidMenuBar.kt).
 */

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.sdercolin.vlabeler.android.menu.AndroidMenuModel
import com.sdercolin.vlabeler.android.menu.LocalMenuRenderMode
import com.sdercolin.vlabeler.android.menu.MenuRenderMode
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberUpdatedState

// region Window scopes

/** Placeholder for the AWT window. There is no such window on Android. */
class AndroidWindowStub

interface WindowScope {
    val window: AndroidWindowStub get() = AndroidWindowStub()
}

interface FrameWindowScope : WindowScope

interface DialogWindowScope : WindowScope

internal object DefaultFrameWindowScope : FrameWindowScope

internal object DefaultDialogWindowScope : DialogWindowScope

sealed class WindowPosition {
    object PlatformDefault : WindowPosition()
    class Aligned(val alignment: Alignment) : WindowPosition()
    class Absolute(val x: Dp, val y: Dp) : WindowPosition()
}

fun WindowPosition(alignment: Alignment): WindowPosition = WindowPosition.Aligned(alignment)
fun WindowPosition(x: Dp, y: Dp): WindowPosition = WindowPosition.Absolute(x, y)

// endregion

// region Dialog window

@Stable
class DialogState(
    position: WindowPosition = WindowPosition.PlatformDefault,
    size: DpSize = DpSize(400.dp, 300.dp),
) {
    var position by mutableStateOf(position)
    var size by mutableStateOf(size)
}

@Composable
fun rememberDialogState(
    position: WindowPosition = WindowPosition(Alignment.Center),
    size: DpSize = DpSize(400.dp, 300.dp),
): DialogState = remember { DialogState(position, size) }

@Composable
fun rememberDialogState(
    position: WindowPosition = WindowPosition(Alignment.Center),
    width: Dp = 400.dp,
    height: Dp = 300.dp,
): DialogState = rememberDialogState(position, DpSize(width, height))

/**
 * A desktop dialog window is shown as a Compose [Dialog] on Android. Its size follows the desktop size but is limited
 * by the screen size.
 */
@Composable
fun DialogWindow(
    onCloseRequest: () -> Unit,
    state: DialogState = rememberDialogState(),
    visible: Boolean = true,
    title: String = "Untitled",
    icon: Painter? = null,
    undecorated: Boolean = false,
    transparent: Boolean = false,
    resizable: Boolean = true,
    enabled: Boolean = true,
    focusable: Boolean = true,
    onPreviewKeyEvent: ((KeyEvent) -> Boolean) = { false },
    onKeyEvent: ((KeyEvent) -> Boolean) = { false },
    content: @Composable DialogWindowScope.() -> Unit,
) {
    if (!visible) return
    Dialog(
        onDismissRequest = onCloseRequest,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = true,
        ),
    ) {
        val deviceDensity = androidx.compose.ui.platform.LocalDensity.current
        val appDensity = com.sdercolin.vlabeler.android.LocalAppDensity.current ?: deviceDensity
        val ratio = appDensity.density / deviceDensity.density
        val configuration = androidx.compose.ui.platform.LocalConfiguration.current
        val maxWidth = (configuration.screenWidthDp - 16).dp
        val maxHeight = (configuration.screenHeightDp - 16).dp
        val width = (state.size.width * ratio).coerceIn(minOf(320.dp, maxWidth), maxWidth)
        val height = (state.size.height * ratio).coerceIn(minOf(200.dp, maxHeight), maxHeight)
        Box(
            Modifier
                .size(width, height)
                .onPreviewKeyEvent(onPreviewKeyEvent)
                .onKeyEvent(onKeyEvent),
        ) {
            Surface(Modifier.fillMaxSize()) {
                com.sdercolin.vlabeler.android.ReprovideAppDensity {
                    DefaultDialogWindowScope.content()
                }
            }
        }
    }
}

// endregion

// region Menu bar

/**
 * Menu scope used by the shared menu declaration (Menu.kt).
 *
 * The same composable menu declaration is composed in two modes (see [AndroidMenuModel]):
 * - Registry mode: always composed, invisible. Items with shortcuts are registered so that hardware keyboard shortcuts
 *   and the on-screen toolbar work.
 * - Display mode: composed inside the drop-down menu of the Android top bar, rendering the currently opened level.
 */
open class MenuScope internal constructor(internal val path: List<String>) {

    @Composable
    fun Menu(
        text: String,
        mnemonic: Char? = null,
        enabled: Boolean = true,
        content: @Composable MenuScope.() -> Unit,
    ) {
        val myPath = path + text
        when (LocalMenuRenderMode.current) {
            MenuRenderMode.Registry -> MenuScope(myPath).content()
            MenuRenderMode.Display -> {
                val openedPath = AndroidMenuModel.openedPath
                when {
                    openedPath == path -> AndroidMenuModel.RenderSubMenuEntry(text, enabled) {
                        AndroidMenuModel.openedPath = myPath
                    }
                    openedPath.size >= myPath.size && openedPath.subList(0, myPath.size) == myPath ->
                        MenuScope(myPath).content()
                }
            }
        }
    }

    @Composable
    fun Item(
        text: String,
        icon: Painter? = null,
        enabled: Boolean = true,
        mnemonic: Char? = null,
        shortcut: KeyShortcut? = null,
        onClick: () -> Unit,
    ) = RenderItem(text, null, enabled, shortcut, onClick)

    @Composable
    fun CheckboxItem(
        text: String,
        checked: Boolean,
        icon: Painter? = null,
        enabled: Boolean = true,
        mnemonic: Char? = null,
        shortcut: KeyShortcut? = null,
        onCheckedChange: (Boolean) -> Unit,
    ) = RenderItem(text, checked, enabled, shortcut) { onCheckedChange(!checked) }

    @Composable
    fun RadioButtonItem(
        text: String,
        selected: Boolean,
        icon: Painter? = null,
        enabled: Boolean = true,
        mnemonic: Char? = null,
        shortcut: KeyShortcut? = null,
        onClick: () -> Unit,
    ) = RenderItem(text, selected, enabled, shortcut, onClick)

    @Composable
    fun Separator() {
        if (LocalMenuRenderMode.current == MenuRenderMode.Display && AndroidMenuModel.openedPath == path) {
            AndroidMenuModel.RenderSeparator()
        }
    }

    @Composable
    private fun RenderItem(
        text: String,
        checked: Boolean?,
        enabled: Boolean,
        shortcut: KeyShortcut?,
        onClick: () -> Unit,
    ) {
        when (LocalMenuRenderMode.current) {
            MenuRenderMode.Registry -> AndroidMenuModel.RegisterItem(path + text, enabled, shortcut, onClick)
            MenuRenderMode.Display -> if (AndroidMenuModel.openedPath == path) {
                AndroidMenuModel.RenderItem(text, checked, enabled, shortcut, onClick)
            }
        }
    }
}

class MenuBarScope internal constructor() : MenuScope(emptyList())

/**
 * Stores the menu declared by the shared code into [AndroidMenuModel], which is rendered in the Android top bar and
 * also used to handle keyboard shortcuts of hardware keyboards.
 */
@Composable
fun FrameWindowScope.MenuBar(content: @Composable MenuBarScope.() -> Unit) {
    val currentContent by rememberUpdatedState(content)
    DisposableEffect(Unit) {
        AndroidMenuModel.content = { currentContent.invoke(MenuBarScope()) }
        onDispose { AndroidMenuModel.content = null }
    }
    CompositionLocalProvider(LocalMenuRenderMode provides MenuRenderMode.Registry) {
        MenuBarScope().content()
    }
}

// endregion
