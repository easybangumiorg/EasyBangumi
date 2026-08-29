package com.heyanle.easybangumi4.utils

/**
 * Marks the synchronous WebView construction window in which Chromium reads the
 * application package for its X-Requested-With header.
 *
 * A thread identity check replaces the previous main-thread stack trace allocation on
 * every application package-name lookup. The previous marker is restored for nested calls.
 */
internal object WebViewPackageNameScope {

    @Volatile
    private var spoofingThread: Thread? = null

    fun shouldSpoof(): Boolean = spoofingThread === Thread.currentThread()

    fun <T> withSpoofing(enabled: Boolean, block: () -> T): T {
        if (!enabled) return block()

        val previousThread = spoofingThread
        spoofingThread = Thread.currentThread()
        return try {
            block()
        } finally {
            spoofingThread = previousThread
        }
    }
}
