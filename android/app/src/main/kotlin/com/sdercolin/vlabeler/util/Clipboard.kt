package com.sdercolin.vlabeler.util

import com.sdercolin.vlabeler.android.AndroidPlatform

object Clipboard {
    fun copyToClipboard(text: String) {
        AndroidPlatform.copyToClipboard(text)
    }
}
