@file:Suppress("unused", "PackageDirectoryMismatch")

package androidx.compose.foundation

/*
 * Android implementations of desktop-only APIs of Compose Foundation that are used by vLabeler:
 * scrollbars, tooltips and context menus.
 */

import androidx.compose.foundation.gestures.detectDragGestures
import android.os.SystemClock
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.sdercolin.vlabeler.android.AndroidInputSettings
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// region Scrollbars

/**
 * Minimal version of the desktop `v2.ScrollbarAdapter`.
 */
interface ScrollbarAdapter {
    /** Scroll offset of the content in pixels. */
    val scrollOffset: Double

    /** Size of the content in pixels. */
    val contentSize: Double

    /** Size of the viewport in pixels. */
    val viewportSize: Double

    /** Instantly jump to [scrollOffset] in pixels. */
    suspend fun scrollTo(scrollOffset: Double)
}

private class ScrollStateScrollbarAdapter(private val scrollState: ScrollState) : ScrollbarAdapter {
    override val scrollOffset: Double get() = scrollState.value.toDouble()
    override val contentSize: Double get() = scrollState.maxValue.toDouble() + viewportSize
    override val viewportSize: Double get() = scrollState.viewportSize.toDouble()
    override suspend fun scrollTo(scrollOffset: Double) {
        scrollState.scrollTo(scrollOffset.roundToInt())
    }
}

private class LazyListScrollbarAdapter(private val state: LazyListState) : ScrollbarAdapter {
    private val averageItemSize: Double
        get() {
            val items = state.layoutInfo.visibleItemsInfo
            if (items.isEmpty()) return 0.0
            return items.sumOf { it.size }.toDouble() / items.size
        }
    override val scrollOffset: Double
        get() = state.firstVisibleItemIndex * averageItemSize + state.firstVisibleItemScrollOffset
    override val contentSize: Double
        get() = state.layoutInfo.totalItemsCount * averageItemSize
    override val viewportSize: Double
        get() = (state.layoutInfo.viewportEndOffset - state.layoutInfo.viewportStartOffset).toDouble()

    override suspend fun scrollTo(scrollOffset: Double) {
        val itemSize = averageItemSize
        if (itemSize <= 0) return
        val index = (scrollOffset / itemSize).toInt().coerceIn(0, (state.layoutInfo.totalItemsCount - 1).coerceAtLeast(0))
        val offset = (scrollOffset - index * itemSize).roundToInt()
        state.scrollToItem(index, offset)
    }
}

private class LazyGridScrollbarAdapter(private val state: LazyGridState) : ScrollbarAdapter {
    private val averageItemSize: Double
        get() {
            val items = state.layoutInfo.visibleItemsInfo
            if (items.isEmpty()) return 0.0
            return items.sumOf { it.size.height }.toDouble() / items.size
        }
    override val scrollOffset: Double
        get() = state.firstVisibleItemIndex * averageItemSize + state.firstVisibleItemScrollOffset
    override val contentSize: Double
        get() = state.layoutInfo.totalItemsCount * averageItemSize
    override val viewportSize: Double
        get() = (state.layoutInfo.viewportEndOffset - state.layoutInfo.viewportStartOffset).toDouble()

    override suspend fun scrollTo(scrollOffset: Double) {
        val itemSize = averageItemSize
        if (itemSize <= 0) return
        val index = (scrollOffset / itemSize).toInt().coerceIn(0, (state.layoutInfo.totalItemsCount - 1).coerceAtLeast(0))
        state.scrollToItem(index)
    }
}

fun ScrollbarAdapter(scrollState: ScrollState): ScrollbarAdapter = ScrollStateScrollbarAdapter(scrollState)
fun ScrollbarAdapter(scrollState: LazyListState): ScrollbarAdapter = LazyListScrollbarAdapter(scrollState)
fun ScrollbarAdapter(scrollState: LazyGridState): ScrollbarAdapter = LazyGridScrollbarAdapter(scrollState)

@Composable
fun rememberScrollbarAdapter(scrollState: ScrollState): ScrollbarAdapter =
    remember(scrollState) { ScrollbarAdapter(scrollState) }

@Composable
fun rememberScrollbarAdapter(scrollState: LazyListState): ScrollbarAdapter =
    remember(scrollState) { ScrollbarAdapter(scrollState) }

@Composable
fun rememberScrollbarAdapter(scrollState: LazyGridState): ScrollbarAdapter =
    remember(scrollState) { ScrollbarAdapter(scrollState) }

@Immutable
class ScrollbarStyle(
    val minimalHeight: Dp,
    val thickness: Dp,
    val shape: Shape,
    val hoverDurationMillis: Int,
    val unhoverColor: Color,
    val hoverColor: Color,
)

fun defaultScrollbarStyle() = ScrollbarStyle(
    minimalHeight = 24.dp,
    thickness = 6.dp,
    shape = RoundedCornerShape(3.dp),
    hoverDurationMillis = 300,
    unhoverColor = Color.Gray.copy(alpha = 0.4f),
    hoverColor = Color.Gray.copy(alpha = 0.7f),
)

val LocalScrollbarStyle = staticCompositionLocalOf { defaultScrollbarStyle() }

@Composable
fun VerticalScrollbar(
    adapter: ScrollbarAdapter,
    modifier: Modifier = Modifier,
    reverseLayout: Boolean = false,
    style: ScrollbarStyle = LocalScrollbarStyle.current,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) = Scrollbar(adapter, modifier, reverseLayout, style, isVertical = true)

@Composable
fun HorizontalScrollbar(
    adapter: ScrollbarAdapter,
    modifier: Modifier = Modifier,
    reverseLayout: Boolean = false,
    style: ScrollbarStyle = LocalScrollbarStyle.current,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) = Scrollbar(adapter, modifier, reverseLayout, style, isVertical = false)

/**
 * A simple draggable scrollbar, which is useful on touch screens for long lists and the labeler canvas.
 */
@Composable
private fun Scrollbar(
    adapter: ScrollbarAdapter,
    modifier: Modifier,
    reverseLayout: Boolean,
    style: ScrollbarStyle,
    isVertical: Boolean,
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    var dragging by remember { mutableStateOf(false) }
    BoxWithConstraints(
        modifier.run { if (isVertical) fillMaxHeight().width(style.thickness * 2) else fillMaxWidth() },
    ) {
        val trackLength = with(density) { (if (isVertical) maxHeight else maxWidth).toPx() }
        val contentSize = adapter.contentSize
        val viewportSize = adapter.viewportSize
        val visible = contentSize > viewportSize && contentSize > 0.0 && trackLength > 0f && trackLength.isFinite()
        if (visible) {
            // the track can be shorter than the minimal thumb (e.g. when the on-screen keyboard is shown)
            val minThumb = with(density) { style.minimalHeight.toPx() }.coerceAtMost(trackLength)
            val thumbLength = (trackLength * viewportSize / contentSize).toFloat().coerceIn(minThumb, trackLength)
            val maxScroll = (contentSize - viewportSize).coerceAtLeast(1.0)
            val thumbOffsetRatio by remember(adapter, maxScroll) { derivedStateOf { adapter.scrollOffset / maxScroll } }
            val rawOffset = ((trackLength - thumbLength) * thumbOffsetRatio.coerceIn(0.0, 1.0)).toFloat()
            val thumbOffset = if (reverseLayout) trackLength - thumbLength - rawOffset else rawOffset
            val thumbLengthDp = with(density) { thumbLength.toDp() }
            Box(
                Modifier
                    .run { if (isVertical) fillMaxHeight() else fillMaxWidth() }
                    .pointerInput(adapter, trackLength, thumbLength, maxScroll) {
                        detectTapGestures { offset ->
                            val position = if (isVertical) offset.y else offset.x
                            val ratio = ((position - thumbLength / 2) / (trackLength - thumbLength).coerceAtLeast(1f))
                                .coerceIn(0f, 1f)
                            val target = if (reverseLayout) 1f - ratio else ratio
                            scope.launch { adapter.scrollTo(target * maxScroll) }
                        }
                    }
                    .pointerInput(adapter, trackLength, thumbLength) {
                        detectDragGestures(
                            onDragStart = { dragging = true },
                            onDragEnd = { dragging = false },
                            onDragCancel = { dragging = false },
                        ) { change, dragAmount ->
                            change.consume()
                            val delta = if (isVertical) dragAmount.y else dragAmount.x
                            val sign = if (reverseLayout) -1 else 1
                            val scrollDelta = delta * sign * maxScroll / (trackLength - thumbLength).coerceAtLeast(1f)
                            scope.launch {
                                adapter.scrollTo((adapter.scrollOffset + scrollDelta).coerceIn(0.0, maxScroll))
                            }
                        }
                    },
            ) {
                Box(
                    Modifier
                        .offset {
                            if (isVertical) IntOffset(0, thumbOffset.roundToInt()) else IntOffset(thumbOffset.roundToInt(), 0)
                        }
                        .run {
                            if (isVertical) {
                                size(style.thickness, thumbLengthDp).align(Alignment.TopCenter)
                            } else {
                                size(thumbLengthDp, style.thickness).align(Alignment.CenterStart)
                            }
                        }
                        .clip(style.shape)
                        .background(if (dragging) style.hoverColor else style.unhoverColor),
                )
            }
        }
    }
}

// endregion

// region Tooltip

sealed interface TooltipPlacement {
    class CursorPoint(val offset: DpOffset = DpOffset.Zero, val alignment: Alignment = Alignment.BottomEnd) :
        TooltipPlacement

    class ComponentRect(
        val anchor: Alignment = Alignment.BottomCenter,
        val alignment: Alignment = Alignment.BottomCenter,
        val offset: DpOffset = DpOffset.Zero,
    ) : TooltipPlacement
}

/**
 * On Android there is no mouse hover, so the tooltip is shown on long press when enabled in settings.
 */
@Composable
fun TooltipArea(
    tooltip: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    delayMillis: Int = 500,
    tooltipPlacement: TooltipPlacement = TooltipPlacement.CursorPoint(offset = DpOffset(0.dp, 16.dp)),
    content: @Composable () -> Unit,
) {
    var shown by remember { mutableStateOf(false) }
    Box(
        modifier.pointerInput(Unit) {
            if (!AndroidInputSettings.longPressTooltips) return@pointerInput
            awaitPointerEventScope {
                while (true) {
                    val down = awaitPointerEvent(PointerEventPass.Initial)
                    if (down.changes.any { it.pressed && !it.previousPressed }) {
                        val start = System.currentTimeMillis()
                        var released = false
                        while (!released) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            released = event.changes.none { it.pressed }
                            if (!released && System.currentTimeMillis() - start > 600) {
                                shown = true
                            }
                        }
                    }
                }
            }
        },
    ) {
        content()
        if (shown) {
            DropdownMenu(expanded = true, onDismissRequest = { shown = false }) {
                Box(Modifier.padding(horizontal = 8.dp)) { tooltip() }
            }
        }
    }
}

// endregion

// region Context menu

class ContextMenuItem(
    val label: String,
    val onClick: () -> Unit,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ContextMenuItem) return false
        return label == other.label
    }

    override fun hashCode(): Int = label.hashCode()
}

interface ContextMenuRepresentation

class DefaultContextMenuRepresentation(
    val backgroundColor: Color = Color.White,
    val textColor: Color = Color.Black,
    val itemHoverColor: Color = Color.Transparent,
) : ContextMenuRepresentation

val LocalContextMenuRepresentation = staticCompositionLocalOf<ContextMenuRepresentation> {
    DefaultContextMenuRepresentation()
}

/**
 * Context menu area. On desktop it's opened by right click. On Android, it's opened by long press
 * (or by a click with the virtual right button mode enabled in the on-screen toolbar).
 */
@Composable
fun ContextMenuArea(
    items: () -> List<ContextMenuItem>,
    state: Any? = null,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    var menuOffset by remember { mutableStateOf<Offset?>(null) }
    val representation = LocalContextMenuRepresentation.current as? DefaultContextMenuRepresentation
    val density = LocalDensity.current
    Box(
        Modifier.pointerInput(enabled) {
            if (!enabled) return@pointerInput
            val slop = viewConfiguration.touchSlop
            val longPressTimeout = viewConfiguration.longPressTimeoutMillis
            awaitPointerEventScope {
                while (true) {
                    val downEvent = awaitPointerEvent(PointerEventPass.Initial)
                    val down = downEvent.changes.firstOrNull { it.pressed && !it.previousPressed } ?: continue
                    val downPosition = down.position
                    val start = SystemClock.uptimeMillis()
                    var opened = false
                    var cancelled = downEvent.changes.size > 1
                    while (true) {
                        val event = withTimeoutOrNull(40) { awaitPointerEvent(PointerEventPass.Initial) }
                        if (event != null) {
                            if (event.changes.size > 1) cancelled = true
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!opened && (change.position - downPosition).getDistance() > slop) cancelled = true
                            if (opened) event.changes.forEach { it.consume() }
                            if (!change.pressed) {
                                if (!opened && !cancelled && AndroidInputSettings.rightClickMode) {
                                    menuOffset = change.position
                                    change.consume()
                                }
                                break
                            }
                        }
                        if (!opened && !cancelled && SystemClock.uptimeMillis() - start >= longPressTimeout) {
                            opened = true
                            menuOffset = downPosition
                        }
                    }
                }
            }
        },
    ) {
        content()
        val offset = menuOffset
        if (offset != null) {
            val currentItems = remember(offset) { items() }
            if (currentItems.isEmpty()) {
                SideEffect { menuOffset = null }
            } else {
                val dpOffset = with(density) { DpOffset(offset.x.toDp(), 0.dp) }
                DropdownMenu(
                    expanded = true,
                    onDismissRequest = { menuOffset = null },
                    offset = dpOffset,
                    modifier = Modifier.background(representation?.backgroundColor ?: MaterialTheme.colors.surface),
                ) {
                    Column {
                        currentItems.forEach { item ->
                            DropdownMenuItem(
                                onClick = {
                                    menuOffset = null
                                    item.onClick()
                                },
                            ) {
                                Text(
                                    item.label,
                                    color = representation?.textColor ?: MaterialTheme.colors.onSurface,
                                    style = MaterialTheme.typography.body2,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContextMenuDataProvider(
    items: () -> List<ContextMenuItem>,
    content: @Composable () -> Unit,
) = CompositionLocalProvider(content = content)

// endregion
