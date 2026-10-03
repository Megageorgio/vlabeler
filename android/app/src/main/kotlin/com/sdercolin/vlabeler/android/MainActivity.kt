package com.sdercolin.vlabeler.android

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.darkColors
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sdercolin.vlabeler.AndroidKeyDispatcher
import com.sdercolin.vlabeler.VLabelerApp
import com.sdercolin.vlabeler.env.Log
import kotlin.system.exitProcess

class MainActivity : ComponentActivity() {

    private var showApp by mutableStateOf(false)

    private val legacyPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { onPermissionResult() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterFullscreen()
        AndroidPlatform.init(this)
        AndroidPlatform.attachActivity(this)
        AndroidUiScale.load(this)

        showApp = AndroidEnvironment.isInitialized || AndroidPlatform.hasAllFilesAccess || isStorageChoiceSkipped()
        val args = listOfNotNull(intent?.data?.takeIf { it.scheme == "file" }?.path)

        setContent {
            if (showApp) {
                ensureEnvironment()
                VLabelerApp(args) { exitApplication() }
            } else {
                PermissionScreen(
                    onGrant = ::requestStoragePermission,
                    onSkip = {
                        getPreferences(Context.MODE_PRIVATE).edit().putBoolean(KEY_SKIP_STORAGE, true).apply()
                        showApp = true
                    },
                )
            }
        }
    }

    private fun isStorageChoiceSkipped() = getPreferences(Context.MODE_PRIVATE).getBoolean(KEY_SKIP_STORAGE, false)

    private fun ensureEnvironment() {
        if (AndroidEnvironment.isInitialized) return
        val androidHandler = Thread.getDefaultUncaughtExceptionHandler()
        AndroidEnvironment.setup(this)
        val appHandler = Thread.getDefaultUncaughtExceptionHandler()
        if (appHandler !== androidHandler) {
            // Keep the logging of the shared code, but let Android handle the crash afterwards
            Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                runCatching { appHandler?.uncaughtException(thread, throwable) }
                androidHandler?.uncaughtException(thread, throwable)
            }
        }
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = Intent(
                Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                Uri.parse("package:$packageName"),
            )
            runCatching { startActivity(intent) }.onFailure {
                startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
            }
        } else {
            legacyPermissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                ),
            )
        }
    }

    private fun onPermissionResult() {
        if (AndroidPlatform.hasAllFilesAccess) showApp = true
    }

    /**
     * Full screen: the app draws behind the system bars, which are hidden and can be shown temporarily by a swipe from
     * the screen edge.
     */
    private fun enterFullscreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) enterFullscreen()
    }

    override fun onResume() {
        super.onResume()
        AndroidPlatform.attachActivity(this)
        if (!showApp && AndroidPlatform.hasAllFilesAccess) showApp = true
    }

    override fun onDestroy() {
        AndroidPlatform.detachActivity(this)
        super.onDestroy()
    }

    private fun isEditingText(): Boolean {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        return imm.isAcceptingText
    }

    /**
     * Hardware keyboard: shortcuts are handled before the focused component unless a text field is being edited,
     * similar to the desktop window.
     */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val handler = AndroidKeyDispatcher.handler
        if (handler == null || event.keyCode == KeyEvent.KEYCODE_BACK) return super.dispatchKeyEvent(event)
        val composeEvent = androidx.compose.ui.input.key.KeyEvent(event)
        if (!isEditingText()) {
            if (handler(composeEvent)) return true
            return super.dispatchKeyEvent(event)
        }
        if (super.dispatchKeyEvent(event)) return true
        return handler(composeEvent)
    }

    private fun exitApplication() {
        Log.info("Exit application")
        finishAndRemoveTask()
        // The shared code keeps global state in singletons, so the process is restarted like the desktop app.
        window.decorView.postDelayed({ exitProcess(0) }, 300)
    }

    companion object {
        private const val KEY_SKIP_STORAGE = "skip_all_files_access"
    }
}

@androidx.compose.runtime.Composable
private fun PermissionScreen(onGrant: () -> Unit, onSkip: () -> Unit) {
    MaterialTheme(colors = darkColors()) {
        Surface(Modifier.fillMaxSize()) {
            Column(
                Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("vLabeler", style = MaterialTheme.typography.h4)
                Spacer(Modifier.height(24.dp))
                Text(
                    "vLabeler works with folders of audio samples and label files (oto.ini, .lab, ...) " +
                        "like the desktop version. Please allow access to all files so that you can open " +
                        "samples anywhere in the storage.\n\n" +
                        "Your settings, labelers and plugins will be stored in the \"vLabeler\" folder of the storage.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 520.dp),
                )
                Spacer(Modifier.height(32.dp))
                Button(onClick = onGrant) { Text("Allow access to all files") }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = onSkip) { Text("Continue with app storage only") }
            }
        }
    }
}
