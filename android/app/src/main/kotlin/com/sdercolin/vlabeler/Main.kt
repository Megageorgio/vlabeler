package com.sdercolin.vlabeler

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.window.DefaultFrameWindowScope
import com.sdercolin.vlabeler.android.AndroidTopBar
import com.sdercolin.vlabeler.android.ProvideAppUiScale
import com.sdercolin.vlabeler.android.menu.AndroidMenuModel
import com.sdercolin.vlabeler.audio.Player
import com.sdercolin.vlabeler.debug.DebugState
import com.sdercolin.vlabeler.env.KeyboardViewModel
import com.sdercolin.vlabeler.env.Locale
import com.sdercolin.vlabeler.env.appVersion
import com.sdercolin.vlabeler.env.isDebug
import com.sdercolin.vlabeler.env.osInfo
import com.sdercolin.vlabeler.env.osName
import com.sdercolin.vlabeler.env.runtimeVersion
import com.sdercolin.vlabeler.io.ensureDirectories
import com.sdercolin.vlabeler.io.initializeGlobalRepositories
import com.sdercolin.vlabeler.io.loadAppConf
import com.sdercolin.vlabeler.io.produceAppState
import com.sdercolin.vlabeler.io.runMigration
import com.sdercolin.vlabeler.model.AppRecord
import com.sdercolin.vlabeler.model.action.KeyAction
import com.sdercolin.vlabeler.model.parseArgs
import com.sdercolin.vlabeler.tracking.event.LaunchEvent
import com.sdercolin.vlabeler.ui.App
import com.sdercolin.vlabeler.ui.AppRecordStore
import com.sdercolin.vlabeler.ui.AppState
import com.sdercolin.vlabeler.ui.Menu
import com.sdercolin.vlabeler.ui.ProjectChangesListener
import com.sdercolin.vlabeler.ui.ProjectWriter
import com.sdercolin.vlabeler.ui.Splash
import com.sdercolin.vlabeler.ui.dialog.StandaloneDialogs
import com.sdercolin.vlabeler.ui.string.*
import com.sdercolin.vlabeler.ui.theme.AppTheme
import com.sdercolin.vlabeler.util.AppRecordFile
import com.sdercolin.vlabeler.util.MemoryUsageMonitor
import com.sdercolin.vlabeler.util.parseJson
import kotlinx.coroutines.CoroutineScope

var hasUncaughtError = false

/**
 * On Android, the built-in file browser is always used.
 */
val UseCustomFileDialog = compositionLocalOf { true }

/**
 * Receives hardware keyboard events that were not consumed by focused components (set by [VLabelerApp]).
 */
object AndroidKeyDispatcher {
    var handler: ((KeyEvent) -> Boolean)? = null
}

/**
 * Android version of the desktop `main` function: the whole application in a single full-screen window.
 *
 * @param args Launch arguments (e.g. a project file path opened from another app).
 * @param exitApplication Called when the application should exit.
 */
@Composable
fun VLabelerApp(args: List<String>, exitApplication: () -> Unit) {
    remember { ensureDirectories() }

    val mainScope = rememberCoroutineScope()
    val appRecordStore = rememberAppRecordStore(mainScope)
    remember { initializeGlobalRepositories(appRecordStore) }
    remember { runMigration(appRecordStore) }

    val appConf = remember { loadAppConf(mainScope, appRecordStore) }

    currentLanguage = appConf.value.view.language

    val appState by produceState(null as AppState?) {
        value = produceAppState(mainScope, appConf, appRecordStore, parseArgs(args))
    }

    DisposableEffect(appState) {
        val state = appState
        AndroidKeyDispatcher.handler = { event ->
            AndroidMenuModel.handleKeyEvent(event) || (state?.keyboardViewModel?.onKeyEvent(event) ?: false)
        }
        onDispose { AndroidKeyDispatcher.handler = null }
    }

    BackHandler(enabled = appState != null) {
        val state = appState ?: return@BackHandler
        when {
            AndroidMenuModel.isExpanded -> AndroidMenuModel.close()
            state.anyDialogOpening() || state.embeddedDialog != null -> {
                state.closeEmbeddedDialog()
                state.closeAllDialogs()
            }
            else -> state.requestExit()
        }
    }

    LaunchedEffect(DebugState.printMemoryUsage) {
        if (DebugState.printMemoryUsage) {
            MemoryUsageMonitor().run()
        }
    }

    val title = appState?.project?.projectFile?.name ?: string(Strings.AppName)

    CompositionLocalProvider(UseCustomFileDialog.provides(true)) {
        AppTheme(appConf.value.view) {
          ProvideAppUiScale {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colors.background).safeDrawingPadding()) {
                Column(Modifier.fillMaxSize()) {
                    AndroidTopBar(appState, title)
                    Box(Modifier.fillMaxWidth().weight(1f)) {
                        with(DefaultFrameWindowScope) {
                            Menu(mainScope, appState, appConf.value.view)
                        }

                        if (appState == null) {
                            AppTheme(appConf.value.view) { Splash() }
                        }

                        appState?.let { state ->
                            LaunchValidate(state)
                            LaunchKeyboardEvent(state.keyboardViewModel, state, state.player)
                            LaunchExit(state, exitApplication)
                            LaunchTrackingLaunch(state)
                            AppTheme(state.appConf.view) { App(mainScope, state) }
                            StandaloneDialogs(mainScope, state)
                            ProjectChangesListener(state)
                            ProjectWriter(state)
                            SnackbarBox(state)
                        }
                    }
                }
            }
          }
        }
    }
}

@Composable
private fun SnackbarBox(state: AppState) {
    AppTheme(state.appConf.view) {
        Box(Modifier.fillMaxSize()) {
            SnackbarHost(
                state.snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                Snackbar(
                    it,
                    actionColor = MaterialTheme.colors.primary,
                    backgroundColor = MaterialTheme.colors.background,
                    contentColor = MaterialTheme.colors.onBackground,
                )
            }
        }
    }
}

@Composable
private fun rememberAppRecordStore(scope: CoroutineScope) = remember {
    val recordText = AppRecordFile.takeIf { it.exists() }?.readText()
    val appRecord = runCatching { recordText?.parseJson<AppRecord>() }.getOrNull() ?: AppRecord()
    AppRecordStore(appRecord, scope)
}

@Composable
private fun LaunchValidate(state: AppState) {
    LaunchedEffect(state) {
        state.validate()
    }
}

@Composable
private fun LaunchKeyboardEvent(
    keyboardViewModel: KeyboardViewModel,
    appState: AppState,
    player: Player,
) {
    LaunchedEffect(appState, keyboardViewModel, player) {
        keyboardViewModel.keyboardActionFlow.collect { action ->
            if (action == KeyAction.CancelDialog) {
                appState.closeAllDialogs()
            }
        }
    }
}

@Composable
private fun LaunchExit(appState: AppState, exit: () -> Unit) {
    val shouldExit = appState.shouldExit
    LaunchedEffect(shouldExit) {
        if (shouldExit) exit()
    }
}

@Composable
private fun LaunchTrackingLaunch(appState: AppState) {
    LaunchedEffect(appState) {
        appState.track(
            LaunchEvent(
                appVersion = appVersion.toString(),
                runtime = runtimeVersion.orEmpty(),
                osName = osName,
                osInfo = osInfo,
                isDebug = isDebug,
                locale = Locale.toString(),
            ),
        )
    }
}
