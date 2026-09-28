package com.sdercolin.vlabeler.android.menu

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

enum class MenuRenderMode { Registry, Display }

val LocalMenuRenderMode = staticCompositionLocalOf { MenuRenderMode.Registry }

/**
 * Holds the application menu declared by the shared code (Menu.kt) through the `MenuBar` shim.
 */
object AndroidMenuModel {

    /** The composable menu declaration. */
    var content: (@Composable () -> Unit)? by mutableStateOf(null)

    /** The path (list of menu titles) of the currently displayed menu level. Empty for the top level. */
    var openedPath: List<String> by mutableStateOf(emptyList())

    var isExpanded: Boolean by mutableStateOf(false)

    private class RegisteredItem(
        val path: List<String>,
        val enabled: () -> Boolean,
        val shortcut: KeyShortcut?,
        val onClick: () -> Unit,
    )

    private val registeredItems = mutableListOf<RegisteredItem>()

    @Composable
    internal fun RegisterItem(path: List<String>, enabled: Boolean, shortcut: KeyShortcut?, onClick: () -> Unit) {
        val currentEnabled by rememberUpdatedState(enabled)
        val currentOnClick by rememberUpdatedState(onClick)
        DisposableEffect(path, shortcut) {
            val item = RegisteredItem(path, { currentEnabled }, shortcut) { currentOnClick() }
            synchronized(registeredItems) { registeredItems += item }
            onDispose { synchronized(registeredItems) { registeredItems -= item } }
        }
    }

    /**
     * Handles a hardware keyboard event by invoking the matching menu item.
     */
    fun handleKeyEvent(event: KeyEvent): Boolean {
        val item = synchronized(registeredItems) {
            registeredItems.lastOrNull { it.shortcut != null && it.shortcut.matches(event) && it.enabled() }
        } ?: return false
        item.onClick()
        return true
    }

    /**
     * Invokes the menu item with the given shortcut. Used by the on-screen toolbar.
     */
    fun performShortcut(shortcut: KeyShortcut): Boolean {
        val item = synchronized(registeredItems) {
            registeredItems.lastOrNull { it.shortcut == shortcut && it.enabled() }
        } ?: return false
        item.onClick()
        return true
    }

    fun open() {
        openedPath = emptyList()
        isExpanded = true
    }

    fun close() {
        isExpanded = false
        openedPath = emptyList()
    }

    @Composable
    internal fun RenderSubMenuEntry(text: String, enabled: Boolean, onClick: () -> Unit) {
        DropdownMenuItem(onClick = onClick, enabled = enabled) {
            Text(
                text,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        }
    }

    @Composable
    internal fun RenderItem(
        text: String,
        checked: Boolean?,
        enabled: Boolean,
        shortcut: KeyShortcut?,
        onClick: () -> Unit,
    ) {
        DropdownMenuItem(
            onClick = {
                close()
                onClick()
            },
            enabled = enabled,
        ) {
            Box(Modifier.width(28.dp)) {
                if (checked == true) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }
            Text(
                text,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (shortcut != null) {
                Spacer(Modifier.width(16.dp))
                CompositionLocalProvider(LocalContentColor provides LocalContentColor.current.copy(ContentAlpha.medium)) {
                    Text(shortcut.toString(), style = MaterialTheme.typography.caption)
                }
            }
        }
    }

    @Composable
    internal fun RenderSeparator() {
        Divider(Modifier.padding(vertical = 4.dp))
    }
}

/**
 * The "hamburger" button showing the application menu.
 */
@Composable
fun AndroidMenuButton(modifier: Modifier = Modifier) {
    Box(modifier) {
        IconButton(onClick = { AndroidMenuModel.open() }) {
            Icon(Icons.Default.Menu, contentDescription = "Menu")
        }
        val content = AndroidMenuModel.content
        DropdownMenu(
            expanded = AndroidMenuModel.isExpanded && content != null,
            onDismissRequest = { AndroidMenuModel.close() },
            modifier = Modifier.widthIn(min = 260.dp, max = 420.dp),
        ) {
            val path = AndroidMenuModel.openedPath
            if (path.isNotEmpty()) {
                DropdownMenuItem(onClick = { AndroidMenuModel.openedPath = path.dropLast(1) }) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            path.last(),
                            style = MaterialTheme.typography.subtitle1,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Divider()
            }
            if (content != null) {
                CompositionLocalProvider(LocalMenuRenderMode provides MenuRenderMode.Display) {
                    content()
                }
            }
        }
    }
}
