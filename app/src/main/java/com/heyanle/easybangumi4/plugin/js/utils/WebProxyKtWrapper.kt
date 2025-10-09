package com.heyanle.easybangumi4.plugin.js.utils

import com.heyanle.easybangumi4.plugin.source.utils.network.web.IWebProxy
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

/**
 * Created by heyanle on 2025/10/8
 * https://github.com/heyanLE
 */
class WebViewProxyKtWrapper(
    val webView: IWebProxy
) {

    @JvmOverloads
    fun loadUrl(
        url: String,
        userAgent: String? = null,
        headers: Map<String, String>? = null,
        interceptResRegex: String? = ".*\\.(css|mp3|m4a|gif|jpg|png|webp).*",
        needBlob: Boolean = false,
    ) {
        runBlocking {
            webView.loadUrl(
                url,
                userAgent,
                headers ?: emptyMap(),
                interceptResRegex,
                needBlob
            )
        }
    }

    @JvmOverloads
    fun waitingForPageLoaded(
        timeout: Long = 5000L
    ): Boolean {
        return runBlocking {
            webView.waitingForPageLoaded(timeout)
        }
    }

    @JvmOverloads
    fun waitingForResourceLoaded(
        resourceRegex: String,
        sticky: Boolean = true,
        timeout: Long = 5000L
    ): String? {
        return runBlocking {
            webView.waitingForResourceLoaded(
                resourceRegex,
                sticky,
                timeout
            )
        }
    }

    @JvmOverloads
    fun getContent(
        timeout: Long = 5000L
    ): String? {
        return runBlocking {
            webView.getContent(timeout)
        }
    }

    @JvmOverloads
    fun getContentWithIframe(
        timeout: Long = 5000L
    ): String? {
        return runBlocking {
            webView.getContentWithIframe(timeout)
        }
    }

    fun executeJavaScript(
        script: String,
        delay: Long = 100L,
    ): Any {
        return runBlocking {
            webView.executeJavaScript(
                script,
                delay
            )
        }
    }

    fun delay(delay: Long) {
        runBlocking {
            kotlinx.coroutines.delay(delay)
        }
    }


    fun close() {
        webView.close()
    }

    fun addToWindow(show: Boolean) {
        webView.addToWindow(show)
    }
}