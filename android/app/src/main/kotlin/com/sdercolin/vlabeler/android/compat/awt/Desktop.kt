package com.sdercolin.vlabeler.android.compat.awt

import com.sdercolin.vlabeler.android.AndroidPlatform
import java.io.File
import java.net.URI

/**
 * Minimal replacement of `java.awt.Desktop`, opening files/folders/URLs with Android intents.
 */
class Desktop private constructor() {

    fun open(file: File) {
        AndroidPlatform.openFile(file)
    }

    fun browse(uri: URI) {
        AndroidPlatform.openUrl(uri.toString())
    }

    fun edit(file: File) = open(file)

    fun isSupported(action: Action): Boolean = true

    enum class Action { OPEN, EDIT, BROWSE, MAIL, PRINT }

    companion object {
        private val instance = Desktop()

        @JvmStatic
        fun getDesktop(): Desktop = instance

        @JvmStatic
        fun isDesktopSupported(): Boolean = true
    }
}
