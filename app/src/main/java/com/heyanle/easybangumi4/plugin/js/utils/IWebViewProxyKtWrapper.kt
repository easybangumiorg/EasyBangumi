package com.heyanle.easybangumi4.plugin.js.utils

import com.heyanle.easybangumi4.plugin.source.utils.network.web.IWebProxy
import kotlinx.coroutines.runBlocking

/**
 * Created by heyanle on 2025/10/8
 * https://github.com/heyanLE
 */
class WebViewProxyKtWrapper(
    val webView: IWebProxy
) {

    fun loadUrl(
        url: String,
        userAgent: String? = null,
        headers: Map<String, String> = emptyMap(),
        interceptResRegex: String? = ".*\\.(css|mp3|m4a|gif|jpg|png|webp).*",
        needBlob: Boolean = false,
    ): Boolean {
        return runBlocking {
            webView.loadUrl(
                url,
                userAgent,
                headers,
                interceptResRegex,
                needBlob
            )
        }
    }

    fun waitingForPageLoaded(
        timeout: Long = 5000L
    ): Boolean {
        return runBlocking {
            webView.waitingForPageLoaded(timeout)
        }
    }

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

    fun getContent(
        timeout: Long = 5000L
    ): String? {
        return runBlocking {
            webView.getContent(timeout)
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
}