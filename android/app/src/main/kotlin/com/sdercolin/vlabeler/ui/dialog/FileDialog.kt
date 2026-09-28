package com.sdercolin.vlabeler.ui.dialog

import androidx.compose.runtime.Composable
import com.sdercolin.vlabeler.android.compat.awt.FileDialog.LOAD
import com.sdercolin.vlabeler.android.compat.awt.FileDialog.SAVE

/*
 * Android: native file dialogs are not used because the app works with file paths. The built-in file browser
 * (CustomFileDialog) is always used.
 */

@Composable
fun OpenFileDialog(
    title: String,
    initialDirectory: String? = null,
    initialFileName: String? = null,
    extensions: List<String>? = null,
    directoryMode: Boolean = false,
    onCloseRequest: (parent: String?, name: String?) -> Unit,
) = CustomFileDialog(LOAD, title, initialDirectory, initialFileName, extensions, directoryMode, onCloseRequest)

@Composable
fun SaveFileDialog(
    title: String,
    initialDirectory: String? = null,
    initialFileName: String? = null,
    extensions: List<String>? = null,
    onCloseRequest: (parent: String?, name: String?) -> Unit,
) = CustomFileDialog(SAVE, title, initialDirectory, initialFileName, extensions, false, onCloseRequest)
