package com.heyanle.easybangumi4.utils

import java.util.concurrent.atomic.AtomicInteger

/**
 * Marks the synchronous WebView provider initialization window in which Chromium may read
 * the application package for its X-Requested-With header.
 *
 * The Chromium caller check is intentionally performed only inside this short-lived scope.
 * Returning the spoofed package for other provider calls (for example ApkInfo) makes
 * Chromium query PackageManager for a package that may not be installed and aborts the
 * first provider initialization.
 */
internal object WebViewPackageNameScope {

    private val spoofingDepth = AtomicInteger(0)

    fun shouldSpoofPackageName(): Boolean {
        if (spoofingDepth.get() <= 0) return false

        return Thread.currentThread().stackTrace.any {
            it.className.equals("org.chromium.base.BuildInfo", ignoreCase = true) &&
                it.methodName.equals("getAll", ignoreCase = true)
        }
    }

    fun <T> withSpoofing(enabled: Boolean, block: () -> T): T {
        if (!enabled) return block()

        spoofingDepth.incrementAndGet()
        return try {
            block()
        } finally {
            spoofingDepth.decrementAndGet()
        }
    }
}
