package com.sdercolin.vlabeler.video

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sdercolin.vlabeler.model.AppConf

/**
 * Android: the "new window" video mode is shown as a dialog.
 */
@Suppress("UNUSED_PARAMETER")
@Composable
fun NewWindowVideo(videoState: VideoState, appConf: AppConf) {
    Dialog(
        onDismissRequest = { videoState.exit() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            Modifier.fillMaxWidth().background(Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            VideoPanel(videoState, Modifier.fillMaxWidth().aspectRatio(4f / 3f))
        }
    }
}
