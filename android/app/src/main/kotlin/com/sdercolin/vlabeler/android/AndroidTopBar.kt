package com.sdercolin.vlabeler.android

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sdercolin.vlabeler.android.menu.AndroidMenuButton
import com.sdercolin.vlabeler.android.menu.AndroidMenuModel
import com.sdercolin.vlabeler.model.action.KeyAction
import com.sdercolin.vlabeler.model.key.Key
import com.sdercolin.vlabeler.ui.AppState
import com.sdercolin.vlabeler.ui.editor.Tool
import com.sdercolin.vlabeler.ui.string.string
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import com.sdercolin.vlabeler.util.getNullableOrElse

/**
 * Performs a [KeyAction] as if its key combination was pressed.
 */
fun AppState.performKeyAction(action: KeyAction) {
    if (action.isInMenu) {
        val keySet = appConf.keymaps.keyActionMap.getNullableOrElse(action) { action.defaultKeySet }
        val shortcut = keySet?.toShortCut()
        if (shortcut != null && AndroidMenuModel.performShortcut(shortcut)) return
    }
    keyboardViewModel.emitAction(action)
}

/**
 * The Android top bar: the application menu and an on-screen toolbar replacing common keyboard/mouse operations.
 */
@Composable
fun AndroidTopBar(appState: AppState?, title: String) {
    LaunchedEffect(appState, AndroidInputSettings.virtualModifiers) {
        appState?.keyboardViewModel?.setVirtualSubKeys(AndroidInputSettings.virtualModifiers)
    }
    Surface(elevation = 4.dp, color = MaterialTheme.colors.surface) {
        Row(
            Modifier.fillMaxWidth().height(48.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AndroidMenuButton()
            val context = androidx.compose.ui.platform.LocalContext.current
            Box(
                Modifier
                    .padding(horizontal = 2.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { AndroidUiScale.cycle(context) }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text("UI " + AndroidUiScale.label(), fontSize = 12.sp)
            }
            if (appState == null || !appState.hasProject) {
                Text(
                    title,
                    style = MaterialTheme.typography.subtitle1,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            } else {
                EditorToolbar(appState)
            }
        }
    }
}

@Composable
private fun RowScope.EditorToolbar(appState: AppState) {
    Row(
        Modifier.weight(1f).horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ToolbarButton(Icons.Default.Save, "Save", enabled = appState.hasUnsavedChanges) {
            appState.performKeyAction(KeyAction.SaveProject)
        }
        ToolbarButton(Icons.AutoMirrored.Filled.Undo, "Undo") {
            appState.performKeyAction(KeyAction.Undo)
        }
        ToolbarButton(Icons.AutoMirrored.Filled.Redo, "Redo") {
            appState.performKeyAction(KeyAction.Redo)
        }
        ToolbarDivider()
        val editor = appState.editor
        if (editor != null) {
            Tool.entries.forEach { tool ->
                ToolButton(tool, selected = editor.tool == tool) { editor.tool = tool }
            }
            ToolbarDivider()
        }
        ToolbarButton(Icons.Default.KeyboardArrowUp, "Previous entry") {
            appState.performKeyAction(KeyAction.NavigatePreviousEntry)
        }
        ToolbarButton(Icons.Default.KeyboardArrowDown, "Next entry") {
            appState.performKeyAction(KeyAction.NavigateNextEntry)
        }
        ToolbarButton(Icons.Default.PlayArrow, "Play entry") {
            appState.performKeyAction(KeyAction.ToggleEntryPlayback)
        }
        ToolbarButton(Icons.Default.ZoomOut, "Zoom out") {
            appState.performKeyAction(KeyAction.IncreaseResolution)
        }
        ToolbarButton(Icons.Default.ZoomIn, "Zoom in") {
            appState.performKeyAction(KeyAction.DecreaseResolution)
        }
        ToolbarDivider()
        ToggleChip("Ctrl", Key.Ctrl in AndroidInputSettings.virtualModifiers) {
            AndroidInputSettings.toggleModifier(Key.Ctrl)
        }
        ToggleChip("Shift", Key.Shift in AndroidInputSettings.virtualModifiers) {
            AndroidInputSettings.toggleModifier(Key.Shift)
        }
        ToggleChip("Alt", Key.Alt in AndroidInputSettings.virtualModifiers) {
            AndroidInputSettings.toggleModifier(Key.Alt)
        }
        ToggleChip("RMB", AndroidInputSettings.rightClickMode) {
            AndroidInputSettings.rightClickMode = !AndroidInputSettings.rightClickMode
        }
        Spacer(Modifier.width(8.dp))
    }
}

@Composable
private fun ToolbarButton(icon: ImageVector, description: String, enabled: Boolean = true, onClick: () -> Unit) {
    IconButton(onClick = onClick, enabled = enabled, modifier = Modifier.size(44.dp)) {
        Icon(icon, contentDescription = description)
    }
}

@Composable
private fun ToolButton(tool: Tool, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colors
    Box(
        Modifier
            .padding(horizontal = 2.dp)
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) colors.onSurface.copy(alpha = 0.16f) else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            tool.icon,
            contentDescription = string(tool.stringKey),
            modifier = Modifier.size(22.dp).rotate(tool.iconRotate),
            tint = if (selected) colors.primary else colors.onSurface,
        )
    }
}

@Composable
private fun ToolbarDivider() {
    Divider(Modifier.padding(horizontal = 4.dp).width(1.dp).height(28.dp))
}

@Composable
private fun ToggleChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colors
    Box(
        Modifier
            .padding(horizontal = 3.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) colors.primary else colors.onSurface.copy(alpha = 0.08f))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(
            text,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) colors.onPrimary else colors.onSurface,
        )
    }
}
