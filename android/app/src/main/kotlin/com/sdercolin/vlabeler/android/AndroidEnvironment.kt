package com.sdercolin.vlabeler.android

import android.content.Context
import android.os.Environment
import com.sdercolin.vlabeler.env.Log
import java.io.File

/**
 * Prepares the environment expected by the shared desktop code:
 *
 * - `user.home`: the home directory. With "All files access", it's the shared storage root, so the app data folder is
 *   `/storage/emulated/0/vLabeler` (labelers, plugins, logs and settings can be edited by users like on desktop).
 *   Otherwise, the app specific external directory is used.
 * - `compose.application.resources.dir`: built-in labelers, plugins and the default config, extracted from the APK
 *   assets.
 */
object AndroidEnvironment {

    @Volatile
    var isInitialized = false
        private set

    lateinit var homeDirectory: File
        private set

    fun setup(context: Context) {
        if (isInitialized) return
        AndroidPlatform.init(context)
        Thread.currentThread().contextClassLoader = AndroidEnvironment::class.java.classLoader

        homeDirectory = if (AndroidPlatform.hasAllFilesAccess) {
            Environment.getExternalStorageDirectory()
        } else {
            context.getExternalFilesDir(null) ?: context.filesDir
        }
        System.setProperty("user.home", homeDirectory.absolutePath)
        System.setProperty("user.dir", homeDirectory.absolutePath)

        val resourceDir = File(context.filesDir, "resources")
        extractAssetsIfNeeded(context, resourceDir)
        System.setProperty("compose.application.resources.dir", resourceDir.absolutePath)

        isInitialized = true
        runCatching {
            Log.init()
            Log.info("Android environment: home=${homeDirectory.absolutePath}, resources=${resourceDir.absolutePath}")
        }.onFailure { it.printStackTrace() }
    }

    private fun extractAssetsIfNeeded(context: Context, resourceDir: File) {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val stamp = "${androidx.core.content.pm.PackageInfoCompat.getLongVersionCode(packageInfo)}-${packageInfo.lastUpdateTime}"
        val stampFile = File(resourceDir, ".extracted")
        if (stampFile.exists() && stampFile.readText() == stamp) return
        resourceDir.deleteRecursively()
        resourceDir.mkdirs()
        copyAssetDirectory(context, "common", resourceDir)
        stampFile.writeText(stamp)
    }

    private fun copyAssetDirectory(context: Context, assetPath: String, target: File) {
        val assets = context.assets
        val children = assets.list(assetPath).orEmpty()
        if (children.isEmpty()) {
            // a file (or an empty directory)
            target.parentFile?.mkdirs()
            runCatching {
                assets.open(assetPath).use { input -> target.outputStream().use { input.copyTo(it) } }
            }.onFailure { target.mkdirs() }
            return
        }
        target.mkdirs()
        for (child in children) {
            copyAssetDirectory(context, "$assetPath/$child", File(target, child))
        }
    }
}
