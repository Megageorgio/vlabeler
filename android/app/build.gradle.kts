import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
}

/**
 * The Android port reuses the desktop source code in `../../src/jvmMain` directly.
 *
 * The task [prepareSharedSources] copies the shared sources into the build directory, while:
 *  - skipping files that are re-implemented for Android in `src/main/kotlin` (see `excludedSharedFiles`),
 *  - remapping imports of desktop-only APIs (AWT, Apache HTTP engine, etc.) to Android compatible ones,
 *  - applying a few small textual patches (see `sharedPatches`).
 *
 * Desktop-only Compose APIs (DialogWindow, MenuBar, scrollbars, tooltips, context menus, onPointerEvent, ...) are
 * provided by shims under `src/main/kotlin` using the original package names, so most of the shared UI code compiles
 * without modification.
 */
val repoRoot: File = rootDir.parentFile
val sharedKotlinDir: File = repoRoot.resolve("src/jvmMain/kotlin")
val sharedResourcesDir: File = repoRoot.resolve("src/jvmMain/resources")
val sharedAppResourcesDir: File = repoRoot.resolve("resources/common")
val generatedSharedKotlinDir = layout.buildDirectory.dir("generated/shared/kotlin")
val generatedAssetsDir = layout.buildDirectory.dir("generated/shared/assets")
val generatedJavaResourcesDir = layout.buildDirectory.dir("generated/shared/javaResources")

val appVersion: String = Properties().apply {
    repoRoot.resolve("gradle.properties").inputStream().use { load(it) }
}.getProperty("app.version")

/**
 * Version code from `app.version`: "1.7.0" -> 1070099, "1.7.0-beta2" -> 1070002, so that pre-releases are ordered
 * before the release and every version can be installed over the previous one.
 */
fun appVersionCode(version: String): Int {
    val match = Regex("""^(\d+)\.(\d+)\.(\d+)(?:-\D*(\d+)?)?""").find(version)
        ?: throw GradleException("Unsupported app.version: $version")
    val (major, minor, patch, pre) = match.destructured
    val preNumber = if (match.value.contains('-')) pre.toIntOrNull()?.coerceIn(0, 98) ?: 0 else 99
    return ((major.toInt() * 100 + minor.toInt()) * 100 + patch.toInt()) * 100 + preNumber
}

val sharedPackagePath = "com/sdercolin/vlabeler"

/** Shared files that are replaced by Android implementations in src/main/kotlin (same package & file name). */
val excludedSharedFiles = listOf(
    "Main.kt",
    "audio/conversion/WaveConverter.kt",
    "env/Keyboard.kt",
    "env/Runtime.kt",
    "ipc/IpcServer.kt",
    "repository/ToolCursorRepository.kt",
    "tracking/TrackingService.kt",
    "ui/dialog/FileDialog.kt",
    "util/Clipboard.kt",
    "util/Encoding.kt",
    "util/JavaScript.kt",
    "util/Url.kt",
    "video/NewWindowVideo.kt",
    "video/VideoPanel.kt",
    "video/VideoPlayer.kt",
).map { "$sharedPackagePath/$it" }

/** Import lines remapped in every shared file. Key: regex, value: replacement. */
val importRemaps = listOf(
    "\\bjava\\.awt\\." to "com.sdercolin.vlabeler.android.compat.awt.",
    "import io\\.ktor\\.client\\.engine\\.apache\\.Apache\\b" to "import io.ktor.client.engine.okhttp.OkHttp as Apache",
    "import com\\.segment\\.analytics\\.kotlin\\.core\\.utilities\\." to "import com.sdercolin.vlabeler.android.compat.",
    "import javax\\.imageio\\.ImageIO\\s*\\n" to "",
    "import androidx\\.compose\\.ui\\.graphics\\.asSkiaBitmap" to "import androidx.compose.ui.graphics.asAndroidBitmap",
    "import org\\.jetbrains\\.skiko\\.toBufferedImage\\s*\\n" to "",
    "androidx\\.compose\\.desktop\\.ui\\.tooling\\.preview\\.Preview" to "androidx.compose.ui.tooling.preview.Preview",
    // `PointerEvent.button` is a member on desktop, but an extension (shim) on Android
    "import androidx\\.compose\\.ui\\.input\\.pointer\\.PointerButton\\n" to
        "import androidx.compose.ui.input.pointer.PointerButton\nimport androidx.compose.ui.input.pointer.button\n",
)

/** Textual patches applied to all shared files. */
val globalPatches = listOf(
    // Touch input does not set mouse buttons, so we use compat helpers that also consider touch pointers.
    "\\.buttons\\.areAnyPressed" to ".areAnyPressedCompat",
    "\\.buttons\\.isPrimaryPressed" to ".isPrimaryPressedCompat",
    "\\.buttons\\.isSecondaryPressed" to ".isSecondaryPressedCompat",
    // Modifier keys of the on-screen toolbar are also applied to pointer events (e.g. multi-selection in lists)
    "\\.keyboardModifiers\\b" to ".keyboardModifiersCompat",
)

/** Textual patches applied to specific shared files. */
val sharedPatches = mapOf(
    // PNG encoding: Skia is not available on Android, android.graphics.Bitmap is used instead
    "repository/ChartRepository.kt" to listOf(
        "import org.jetbrains.skia.EncodedImageFormat\n" to "",
        "import org.jetbrains.skia.Image as SkiaImage\n" to "",
        "val bitmap = image.asSkiaBitmap()" to "val bitmap = image.asAndroidBitmap()",
        "if (bitmap.peekPixels() == null) return" to "",
        "val skiaImage = SkiaImage.makeFromBitmap(bitmap)" to "",
        "val pngBytes = skiaImage.encodeToData(EncodedImageFormat.PNG)?.bytes ?: return" to
            "val pngBytes = java.io.ByteArrayOutputStream().also { " +
            "bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }.toByteArray()",
    ),
    "env/Log.kt" to listOf(
        "record.instant" to "java.time.Instant.ofEpochMilli(record.millis)",
    ),
    // Marker lines must be easy to hit with a finger
    "ui/editor/labeler/marker/MarkerState.kt" to listOf(
        "private const val NEAR_RADIUS_START_OR_END = 20f" to
            "private val NEAR_RADIUS_START_OR_END get() = com.sdercolin.vlabeler.android.TouchTolerance.dpToPx(16f)",
        "private const val NEAR_RADIUS_CUSTOM = 5f" to
            "private val NEAR_RADIUS_CUSTOM get() = com.sdercolin.vlabeler.android.TouchTolerance.dpToPx(12f)",
    ),
    // The desktop updater downloads desktop installers, which is not useful on Android
    "ui/App.kt" to listOf(
        "appState.checkUpdates(isAuto = true)" to "Unit // Android: no automatic update check",
    ),
    "ui/AppUpdaterState.kt" to listOf(
        "dialogState.openUpdaterDialog(it)" to
            "com.sdercolin.vlabeler.util.Url.open(com.sdercolin.vlabeler.util.Url.LATEST_RELEASE)",
    ),
    "repository/FontRepository.kt" to listOf(
        "originalData.readAllBytes()" to "originalData.readBytes()",
    ),
    // One finger is used as the mouse on the canvas, so the canvas itself must not be scrolled by touch drags.
    // Scrolling and zooming are done by two-finger gestures (see TouchNavigation.kt).
    "ui/editor/labeler/Canvas.kt" to listOf(
        ".horizontalScroll(horizontalScrollState)" to
            ".horizontalScroll(horizontalScrollState, enabled = com.sdercolin.vlabeler.android.isMouseScrollEnabled)",
    ),
    "ui/editor/labeler/Labeler.kt" to listOf(
        ".border(width = 0.5.dp, color = Black50)" to
            ".border(width = 0.5.dp, color = Black50)" +
            ".then(com.sdercolin.vlabeler.android.touchNavigation(horizontalScrollState, appState))",
    ),
)

val prepareSharedSources by tasks.registering {
    group = "build"
    description = "Copies and adapts the shared desktop sources for the Android build."
    inputs.dir(sharedKotlinDir)
    inputs.property("excluded", excludedSharedFiles)
    inputs.property("remaps", importRemaps.toString())
    inputs.property("globalPatches", globalPatches.toString())
    inputs.property("patches", sharedPatches.toString())
    outputs.dir(generatedSharedKotlinDir)
    doLast {
        val outDir = generatedSharedKotlinDir.get().asFile
        outDir.deleteRecursively()
        val remapRegexes = importRemaps.map { Regex(it.first) to it.second }
        val globalRegexes = globalPatches.map { Regex(it.first) to it.second }
        val usedPatches = mutableSetOf<String>()
        sharedKotlinDir.walkTopDown().filter { it.isFile && it.extension == "kt" }.forEach { file ->
            val relative = file.relativeTo(sharedKotlinDir).invariantSeparatorsPath
            if (relative in excludedSharedFiles) return@forEach
            // normalize line endings (Windows checkouts with core.autocrlf), so that patches with "\n" match
            var text = file.readText().replace("\r\n", "\n")
            remapRegexes.forEach { (regex, replacement) -> text = text.replace(regex, replacement) }
            val beforeGlobalPatches = text
            globalRegexes.forEach { (regex, replacement) -> text = text.replace(regex, replacement) }
            val needsCompatImports = text != beforeGlobalPatches
            val fileKey = relative.removePrefix("$sharedPackagePath/")
            sharedPatches[fileKey]?.forEach { (old, new) ->
                if (!text.contains(old)) {
                    throw GradleException("Patch target not found in $fileKey: $old")
                }
                text = text.replace(old, new)
                usedPatches += fileKey
            }
            if (needsCompatImports) {
                text = text.replaceFirst(
                    Regex("^package (.+)$", RegexOption.MULTILINE),
                    "package $1\n\nimport com.sdercolin.vlabeler.android.compat.areAnyPressedCompat\n" +
                        "import com.sdercolin.vlabeler.android.compat.isPrimaryPressedCompat\n" +
                        "import com.sdercolin.vlabeler.android.compat.isSecondaryPressedCompat\n" +
                        "import com.sdercolin.vlabeler.android.compat.keyboardModifiersCompat",
                )
            }
            val target = outDir.resolve(relative)
            target.parentFile.mkdirs()
            target.writeText(text)
        }
        val unused = sharedPatches.keys - usedPatches
        if (unused.isNotEmpty()) throw GradleException("Patched files not found: $unused")
    }
}

val prepareSharedAssets by tasks.registering(Sync::class) {
    group = "build"
    description = "Copies default labelers, plugins and app config into the APK assets."
    from(sharedAppResourcesDir) { into("common") }
    into(generatedAssetsDir)
}

val prepareAppProperties by tasks.registering {
    group = "build"
    description = "Generates app.properties containing the app version (read by the shared code)."
    inputs.property("version", appVersion)
    outputs.dir(generatedJavaResourcesDir)
    doLast {
        val file = generatedJavaResourcesDir.get().asFile.resolve("app.properties")
        file.parentFile.mkdirs()
        file.writeText("app.version=$appVersion\n")
    }
}

android {
    namespace = "com.sdercolin.vlabeler.android"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.sdercolin.vlabeler"
        minSdk = 26
        targetSdk = 34
        versionCode = appVersionCode(appVersion)
        versionName = appVersion
        buildConfigField("String", "APP_VERSION", "\"$appVersion\"")
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64", "x86")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-Xsuppress-version-warnings",
        )
    }

    sourceSets["main"].apply {
        java.srcDirs("src/main/kotlin", generatedSharedKotlinDir)
        resources.srcDirs(sharedResourcesDir, generatedJavaResourcesDir)
        assets.srcDirs(generatedAssetsDir)
    }

    packaging {
        resources {
            excludes += listOf(
                "META-INF/*.md",
                "META-INF/LICENSE*",
                "META-INF/NOTICE*",
                "META-INF/DEPENDENCIES",
                "META-INF/INDEX.LIST",
                "META-INF/*.kotlin_module",
                "META-INF/versions/9/previous-compilation-data.bin",
                "icon.icns",
            )
        }
    }
}

tasks.named("preBuild") { dependsOn(prepareSharedSources, prepareSharedAssets, prepareAppProperties) }
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    dependsOn(prepareSharedSources)
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.core:core-ktx:1.13.1")

    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")

    // Signal processing (only the FFT & window functions are used, so transitive deps are not needed)
    implementation("com.github.psambit9791:jdsp:1.0.0") { isTransitive = false }
    implementation("org.apache.commons:commons-math3:3.6.1")

    // JavaScript engine for labelers/plugins (replaces GraalJS)
    implementation("wang.harlon.quickjs:wrapper-android:3.2.3")

    // Charset detection (replaces Apache Tika)
    implementation("com.github.albfernandez:juniversalchardet:2.4.0")

    // Update checking
    implementation("io.ktor:ktor-client-core:2.1.0")
    implementation("io.ktor:ktor-client-okhttp:2.1.0")
    implementation("io.ktor:ktor-client-logging:2.1.0")
    implementation("io.ktor:ktor-client-content-negotiation:2.1.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.1.0")

    // File tree in the custom file dialog
    implementation("cafe.adriel.bonsai:bonsai-core:1.2.0")

    // Font parsing
    implementation("org.apache.pdfbox:fontbox:2.0.24")
}
