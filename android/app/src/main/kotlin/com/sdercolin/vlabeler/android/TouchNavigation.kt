package com.sdercolin.vlabeler.android

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import com.sdercolin.vlabeler.env.Log
import com.sdercolin.vlabeler.ui.AppState
import com.sdercolin.vlabeler.ui.editor.labeler.CanvasState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.roundToInt

/**
 * Two-finger navigation on the labeler canvas:
 * - drag with two fingers to scroll horizontally,
 * - pinch to zoom.
 *
 * While pinching, the canvas is only scaled visually (smooth, no re-rendering). When the fingers are lifted, the
 * canvas resolution is changed once, and the scroll position is adjusted so that the point under the fingers stays in
 * place (instead of jumping to the start or the center of the sample).
 *
 * One-finger touches are passed to the canvas and work like a mouse.
 */
fun touchNavigation(scrollState: ScrollState, appState: AppState): Modifier = Modifier.composed {
    var visualScale by remember { mutableFloatStateOf(1f) }
    var pivotX by remember { mutableFloatStateOf(0f) }
    var widthPx by remember { mutableFloatStateOf(1f) }
    val scope = rememberCoroutineScope()

    Modifier
        .pointerInput(scrollState, appState) {
            awaitPointerEventScope {
                var active = false
                var startDistance = 1f
                var previousCentroid: Offset? = null
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    widthPx = size.width.toFloat().coerceAtLeast(1f)
                    val touches = event.changes.filter { it.pressed && it.type != PointerType.Mouse }
                    if (touches.size >= 2) {
                        val first = touches[0].position
                        val second = touches[1].position
                        val centroid = (first + second) / 2f
                        val distance = (first - second).getDistance().coerceAtLeast(1f)
                        if (!active) {
                            active = true
                            startDistance = distance
                            pivotX = centroid.x
                        } else {
                            previousCentroid?.let { last ->
                                val dx = centroid.x - last.x
                                // scroll in the coordinates of the unscaled canvas
                                if (dx != 0f) scrollState.dispatchRawDelta(-dx / visualScale)
                            }
                            visualScale = (distance / startDistance).coerceIn(MIN_GESTURE_SCALE, MAX_GESTURE_SCALE)
                        }
                        previousCentroid = centroid
                        event.changes.forEach { it.consume() }
                    } else if (active) {
                        // gesture finished: commit the zoom
                        active = false
                        previousCentroid = null
                        event.changes.forEach { it.consume() }
                        val scale = visualScale
                        val pivot = pivotX
                        scope.launch {
                            commitZoom(appState, scrollState, scale, pivot)
                            visualScale = 1f
                        }
                    }
                }
            }
        }
        .graphicsLayer {
            scaleX = visualScale
            transformOrigin = TransformOrigin((pivotX / widthPx).coerceIn(0f, 1f), 0.5f)
        }
}

private suspend fun commitZoom(appState: AppState, scrollState: ScrollState, scale: Float, pivotX: Float) {
    try {
        if (scale in 0.95f..1.05f) return
        val editor = appState.editor ?: return
        val loaded = editor.canvasState as? CanvasState.Loaded ?: return
        val conf = appState.appConf.painter.canvasResolution
        val oldResolution = editor.canvasResolution
        val newResolution = (oldResolution / scale).roundToInt().coerceIn(conf.min, conf.max)
        if (newResolution == oldResolution) return

        val oldLength = loaded.params.lengthInPixel
        // position (0..1) in the sample that is under the fingers
        val anchor = ((scrollState.value + pivotX) / oldLength).coerceIn(0f, 1f)
        val newLength = oldLength * oldResolution / newResolution
        val oldMax = scrollState.maxValue

        editor.changeResolution(newResolution)

        // wait until the canvas is re-measured, then let the default scroll adjustment run before overriding it
        withTimeoutOrNull(2000) { snapshotFlow { scrollState.maxValue }.first { it != oldMax } }
        delay(50)
        val target = (anchor * newLength - pivotX).roundToInt().coerceIn(0, scrollState.maxValue)
        scrollState.scrollTo(target)
    } catch (t: Throwable) {
        if (t is kotlinx.coroutines.CancellationException) throw t
        Log.error(t)
    }
}

private const val MIN_GESTURE_SCALE = 0.05f
private const val MAX_GESTURE_SCALE = 20f
