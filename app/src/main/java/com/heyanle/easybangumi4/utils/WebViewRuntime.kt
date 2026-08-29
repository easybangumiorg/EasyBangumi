package com.heyanle.easybangumi4.utils

import android.app.Application
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView

/**
 * Owns every repository entry point that can initialize the Android WebView provider.
 *
 * Chromium caches its application package during the first provider call, which may be
 * CookieManager or WebSettings rather than the first WebView constructor. Keeping those
 * calls here guarantees the package-name spoof scope covers whichever one wins the race.
 */
class WebViewRuntime(
    private val application: Application,
    private val shouldSpoofPackageName: () -> Boolean,
) {

    private companion object {
        // The WebView provider is process-global, so the initialization lock must be too.
        // This remains safe even if a caller accidentally constructs another runtime.
        val providerLock = Any()
    }

    val cookieManager: CookieManager by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        withPackageNameSpoof { CookieManager.getInstance() }
    }

    fun createWebView(): WebView = withPackageNameSpoof {
        WebView(application)
    }

    fun getDefaultUserAgent(): String = withPackageNameSpoof {
        WebSettings.getDefaultUserAgent(application)
    }

    private fun <T> withPackageNameSpoof(block: () -> T): T {
        return synchronized(providerLock) {
            WebViewPackageNameScope.withSpoofing(
                enabled = shouldSpoofPackageName(),
                block = block,
            )
        }
    }
}
