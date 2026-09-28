package com.sdercolin.vlabeler.android

import androidx.compose.foundation.ScrollState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import com.sdercolin.vlabeler.model.action.KeyAction
import com.sdercolin.vlabeler.ui.AppState

/**
 * Two-finger navigation on the labeler canvas:
 * - drag with two fingers to scroll horizontally,
 * - pinch to zoom (changes the canvas resolution).
 *
 * One-finger touches are passed to the canvas and work like a mouse.
 */
fun touchNavigation(scrollState: ScrollState, appState: AppState): Modifier =
    Modifier.pointerInput(scrollState, appState) {
        awaitPointerEventScope {
            var zoomAccumulator = 1f
            var previousDistance: Float? = null
            var previousCentroid: Offset? = null
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val touches = event.changes.filter { it.pressed && it.type != PointerType.Mouse }
                if (touches.size < 2) {
                    previousDistance = null
                    previousCentroid = null
                    zoomAccumulator = 1f
                    continue
                }
                val first = touches[0].position
                val second = touches[1].position
                val centroid = (first + second) / 2f
                val distance = (first - second).getDistance()
                previousCentroid?.let { last ->
                    val dx = centroid.x - last.x
                    if (dx != 0f) scrollState.dispatchRawDelta(-dx)
                }
                previousDistance?.let { last ->
                    if (last > 0f) {
                        zoomAccumulator *= distance / last
                        val keyboardViewModel = appState.keyboardViewModel
                        when {
                            zoomAccumulator > ZOOM_STEP -> {
                                keyboardViewModel.emitAction(KeyAction.DecreaseResolution)
                                zoomAccumulator = 1f
                            }
                            zoomAccumulator < 1f / ZOOM_STEP -> {
                                keyboardViewModel.emitAction(KeyAction.IncreaseResolution)
                                zoomAccumulator = 1f
                            }
                        }
                    }
                }
                previousCentroid = centroid
                previousDistance = distance
                event.changes.forEach { it.consume() }
            }
        }
    }

private const val ZOOM_STEP = 1.3f
