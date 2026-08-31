package com.heyanle.easybangumi4.utils

import android.content.Context
import android.os.SystemClock
import java.io.File

/**
 * Persists short-lived WebView risk scopes outside the normal application data directories.
 *
 * A native WebView abort cannot run Kotlin finally blocks. In that case the active `.tag`
 * directory survives the process and is discovered on the next start, allowing the application
 * to fall back to WebView compatibility mode before sources initialize again.
 */
object WebViewCompatibilityModeGuard {

    private const val ROOT_DIRECTORY = "webview_compatibility_guard"
    private const val TAG_SUFFIX = ".tag"

    @Volatile
    private var rootDirectory: File? = null

    fun initialize(context: Context) = initialize(
        File(context.noBackupFilesDir, ROOT_DIRECTORY),
    )

    internal fun initialize(directory: File) {
        rootDirectory = directory
        directory.mkdirs()
    }

    fun newTag(prefix: String): String {
        val safePrefix = sanitize(prefix).ifBlank { "webview" }
        return "${safePrefix}_${Thread.currentThread().id}_${SystemClock.elapsedRealtimeNanos()}"
    }

    fun open(tag: String): Boolean {
        val directory = tagDirectory(tag) ?: return false
        return directory.mkdirs() || directory.isDirectory
    }

    fun close(tag: String): Boolean {
        val directory = tagDirectory(tag) ?: return false
        return !directory.exists() || directory.deleteRecursively()
    }

    /** A non-empty parent means the previous process did not close an active scope. */
    fun shouldEnableCompatibilityMode(): Boolean =
        rootDirectory?.listFiles()?.isNotEmpty() == true

    /** Called only after compatibility mode has been persisted successfully. */
    fun clear(): Boolean {
        val root = rootDirectory ?: return false
        return !root.exists() || root.deleteRecursively()
    }

    private fun tagDirectory(tag: String): File? {
        val root = rootDirectory ?: return null
        val safeTag = sanitize(tag)
        if (safeTag.isBlank()) return null
        return File(root, "$safeTag$TAG_SUFFIX")
    }

    private fun sanitize(tag: String): String = buildString(tag.length.coerceAtMost(96)) {
        tag.take(96).forEach { character ->
            append(
                when {
                    character.isLetterOrDigit() -> character
                    character == '_' || character == '-' || character == '.' -> character
                    else -> '_'
                },
            )
        }
    }
}
