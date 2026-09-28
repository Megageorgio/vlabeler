package com.sdercolin.vlabeler.android

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

/**
 * UI scale of the application. The desktop UI is designed for large windows, so on small screens it is scaled down.
 *
 * `0` means automatic: the scale is chosen so that the screen is at least about 960x560 dp (limited to 60%..100%).
 */
object AndroidUiScale {
    private const val PREF_KEY = "ui_scale"
    val options = listOf(0f, 1f, 0.9f, 0.8f, 0.7f, 0.6f)

    var setting: Float by mutableFloatStateOf(0f)
        private set

    fun load(context: Context) {
        setting = context.getSharedPreferences("android_ui", Context.MODE_PRIVATE).getFloat(PREF_KEY, 0f)
    }

    fun cycle(context: Context) {
        val index = options.indexOf(setting).takeIf { it >= 0 } ?: 0
        setting = options[(index + 1) % options.size]
        context.getSharedPreferences("android_ui", Context.MODE_PRIVATE).edit().putFloat(PREF_KEY, setting).apply()
    }

    fun label(): String = if (setting == 0f) "Auto" else "${(setting * 100).toInt()}%"

    fun resolve(widthDp: Int, heightDp: Int): Float {
        if (setting > 0f) return setting
        val byWidth = widthDp / 960f
        val byHeight = heightDp / 560f
        return minOf(byWidth, byHeight).coerceIn(0.6f, 1f)
    }
}

/** The scaled density of the app, re-provided in dialogs which reset [LocalDensity]. */
val LocalAppDensity = staticCompositionLocalOf<Density?> { null }

@Composable
fun ProvideAppUiScale(content: @Composable () -> Unit) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val scale = AndroidUiScale.resolve(configuration.screenWidthDp, configuration.screenHeightDp)
    val scaled = Density(density.density * scale, density.fontScale)
    CompositionLocalProvider(LocalDensity provides scaled, LocalAppDensity provides scaled, content = content)
}

/** Re-applies the app density inside a new window (e.g. a dialog). */
@Composable
fun ReprovideAppDensity(content: @Composable () -> Unit) {
    val density = LocalAppDensity.current
    if (density != null) {
        CompositionLocalProvider(LocalDensity provides density, content = content)
    } else {
        content()
    }
}
