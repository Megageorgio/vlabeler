package com.sdercolin.vlabeler.android

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.DocumentsContract
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import com.sdercolin.vlabeler.env.Log
import java.io.File
import java.lang.ref.WeakReference

/**
 * Access to Android platform services from the shared code.
 */
object AndroidPlatform {

    lateinit var context: Context
        private set

    private var activityRef: WeakReference<Activity>? = null

    val activity: Activity? get() = activityRef?.get()

    private val mainHandler = Handler(Looper.getMainLooper())

    fun init(context: Context) {
        this.context = context.applicationContext
    }

    fun attachActivity(activity: Activity) {
        activityRef = WeakReference(activity)
    }

    fun detachActivity(activity: Activity) {
        if (activityRef?.get() === activity) activityRef = null
    }

    fun runOnMain(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) block() else mainHandler.post(block)
    }

    fun toast(message: String) = runOnMain {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private fun startActivity(intent: Intent) {
        val activity = activity
        if (activity != null) {
            activity.startActivity(intent)
        } else {
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    fun openUrl(url: String) = runOnMain {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: ActivityNotFoundException) {
            Log.error(e)
            toast(url)
        }
    }

    fun copyToClipboard(text: String) = runOnMain {
        val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        manager.setPrimaryClip(ClipData.newPlainText("vLabeler", text))
    }

    /**
     * Opens a file with an external app, or a directory with the system file manager.
     */
    fun openFile(file: File) = runOnMain {
        try {
            if (file.isDirectory) {
                openDirectory(file)
            } else {
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
                val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension.lowercase())
                    ?: "text/plain"
                val intent = Intent(Intent.ACTION_VIEW)
                    .setDataAndType(uri, mime)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                startActivity(Intent.createChooser(intent, file.name))
            }
        } catch (t: Throwable) {
            Log.error(t)
            copyToClipboard(file.absolutePath)
            toast("${file.absolutePath}\n(path copied to clipboard)")
        }
    }

    private fun openDirectory(directory: File) {
        val externalRoot = Environment.getExternalStorageDirectory().absolutePath
        val path = directory.absolutePath
        if (path.startsWith(externalRoot)) {
            val relative = path.removePrefix(externalRoot).trim('/')
            val documentId = "primary:$relative"
            val uri = DocumentsContract.buildDocumentUri("com.android.externalstorage.documents", documentId)
            val intent = Intent(Intent.ACTION_VIEW)
                .setDataAndType(uri, DocumentsContract.Document.MIME_TYPE_DIR)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            try {
                startActivity(intent)
                return
            } catch (e: ActivityNotFoundException) {
                Log.debug(e)
            }
        }
        copyToClipboard(path)
        toast("$path\n(path copied to clipboard)")
    }

    val hasAllFilesAccess: Boolean
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            context.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
        }
}
