package com.sdercolin.vlabeler.env

import com.sdercolin.vlabeler.android.BuildConfig

/**
 * The current runtime version. On Android, the Android version and API level are reported.
 */
val runtimeVersion: String? get() = "Android ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})"

/**
 * Whether the application is running in debug mode (debug build type on Android).
 */
val isDebug: Boolean by lazy { BuildConfig.DEBUG }
