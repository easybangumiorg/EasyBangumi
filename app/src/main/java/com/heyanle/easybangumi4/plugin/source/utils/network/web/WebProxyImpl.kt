package com.heyanle.easybangumi4.plugin.source.utils.network.web

import android.graphics.Bitmap
import android.webkit.*
import com.heyanle.easybangumi4.utils.CoroutineProvider
import com.heyanle.easybangumi4.utils.WebViewManager
import com.heyanle.easybangumi4.utils.safeResume
import kotlinx.coroutines.*
import org.apache.commons.text.StringEscapeUtils
import java.io.ByteArrayInputStream
import java.util.concurrent.atomic.AtomicBoolean

/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  这一坨逻辑有时间再来优化
 */
class WebProxyImpl(
    private val webViewManager: WebViewManager
): IWebProxy {

    companion object {
        val BLOB_HOOK_JS = """
            let origin = window.URL.createObjectURL
            window.URL.createObjectURL = function (t) {
                let blobUrl = origin(t)
                let xhr = new XMLHttpRequest()
                xhr.onload = function () {
                    window.cefQuery({
                        window.blobHook.handleWrapper(xhr.responseText)
                    });
                   
                }
                xhr.open('get', blobUrl)
                xhr.send();
                return blobUrl
            }
        """.trimIndent()
    }
    private val singleScope by lazy {
        CoroutineScope(SupervisorJob() + CoroutineProvider.SINGLE + CoroutineName("WebKitWebViewProxy-single"))
    }
    private val mainScope by lazy {
        CoroutineScope(SupervisorJob() + Dispatchers.Main + CoroutineName("WebKitWebViewProxy"))
    }

    private val blockWebResourceRequest =
        WebResourceResponse("text/html", "utf-8", ByteArrayInputStream("".toByteArray()))


    @Volatile
    private var url: String? = null
    @Volatile
    private var interceptResRegex: Regex? = null

    private val hasLoad = AtomicBoolean(false)

    private var webView: WebView? = null

    private val resourceList = mutableListOf<String>()

    private var needBlob: Boolean = false
    @Volatile
    private var isLoadEnd = false

    sealed class State {
        abstract val continuation: CancellableContinuation<*>
        class WaitingForPageLoaded(
            override val continuation: CancellableContinuation<Unit>,
        ): State()

        class WaitingForResourceLoaded(
            val resourceRegex: Regex,
            override val continuation: CancellableContinuation<String?>,
        ): State()
    }

    private var state: State? = null


    private val webViewClient = object: WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            if (needBlob) {
                webView?.evaluateJavascript(BLOB_HOOK_JS, null)
            }
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            singleScope.launch {
                isLoadEnd = true
                (state as? State.WaitingForPageLoaded)?.continuation?.safeResume(Unit)
            }
        }

        override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
            val url = request?.url?.toString() ?: return super.shouldInterceptRequest(view, request)
            if (url != this@WebProxyImpl.url) {
                singleScope.launch {
                    resourceList.add(url)
                    (state as? State.WaitingForResourceLoaded)?.let {
                        if (it.resourceRegex.matches(url) == true) {
                            it.continuation.safeResume(url)
                        }
                    }

                }
            }

            if (interceptResRegex?.matches(url) == true) {
                return blockWebResourceRequest
            }

            return super.shouldInterceptRequest(view, request)
        }


    }

    override suspend fun loadUrl(
        url: String,
        userAgent: String?,
        headers: Map<String, String>,
        interceptResRegex: String?,
        needBlob: Boolean
    ): Boolean {
        if (hasLoad.compareAndSet(false, true)) {
            close()
            this@WebProxyImpl.url = url
            this@WebProxyImpl.interceptResRegex = interceptResRegex?.let { Regex(it) }

            mainScope.async {
               val wb = webViewManager.getWebViewOrNull()
                if (wb == null) {
                    return@async false
                }
                webView = wb
                wb.clearWeb()
                wb.settings.userAgentString = userAgent
                wb.webViewClient = webViewClient
                if (needBlob) {
                    wb.addJavascriptInterface(object : Any() {
                        @JavascriptInterface
                        fun handleWrapper(blobTextData: String) {
                            singleScope.launch {
                                resourceList.add(blobTextData)
                                (state as? State.WaitingForResourceLoaded)?.let {
                                    if (it.resourceRegex.matches(blobTextData)) {
                                        it.continuation.safeResume(blobTextData)
                                    }
                                }
                            }
                        }
                    }, "blobHook")
                }
                wb.loadUrl(url, headers)
            }
            return false
        } else {
            throw IllegalStateException("IWebView can only be loaded once, and it has already been loaded.")
        }
    }

    override suspend fun waitingForPageLoaded(timeout: Long): Boolean {
        if (isLoadEnd) {
            return true
        }
        state?.continuation?.cancel()
        val winner = CompletableDeferred<Boolean>()
        singleScope.async {
            if (isLoadEnd) {
                winner.complete(isLoadEnd)
                return@async
            }

            suspendCancellableCoroutine<Unit> {
                state = State.WaitingForPageLoaded(it)
            }
        }
        mainScope.async {
            delay(timeout)
            winner.complete(isLoadEnd)
        }
        return winner.await()
    }

    override suspend fun waitingForResourceLoaded(
        resourceRegex: String,
        sticky: Boolean,
        timeout: Long
    ): String? {
        if (sticky) {
            val res = resourceList.toList().firstOrNull { Regex(resourceRegex).matches(it) }
            if (res != null) {
                return res
            }
        }
        state?.continuation?.cancel()
        val winner = CompletableDeferred<String?>()
        val regex = Regex(resourceRegex)
        singleScope.launch {
            if (sticky) {
                val res = resourceList.toList().firstOrNull { Regex(resourceRegex).matches(it) }
                if (res != null) {
                    winner.complete(res)
                    return@launch
                }
            }
            suspendCancellableCoroutine<String?> {
                state = State.WaitingForResourceLoaded(regex, it)
            }

        }
        mainScope.launch {
            delay(timeout)
            winner.complete(null)
        }

        return winner.await()
    }

    override suspend fun getContent(timeout: Long): String? {
        if (webView == null) {
            throw IllegalStateException("WebView is not initialized, please call loadUrl first.")
        }

        state?.continuation?.cancel()
        val winner = CompletableDeferred<String?>()
        singleScope.launch {
            val res = suspendCancellableCoroutine<String?> { continuation ->
                mainScope.launch {
                    webView?.evaluateJavascript("(function() { return document.documentElement.outerHTML })()") {
                        continuation.safeResume(StringEscapeUtils.unescapeEcmaScript(it))
                    }
                }
            }
            winner.complete(res)
        }
        mainScope.launch {
            delay(timeout)
            winner.complete(null)
        }
        return winner.await()
    }

    override suspend fun executeJavaScript(script: String, delay: Long) {
        mainScope.launch {
            webView?.evaluateJavascript(script, null)
            delay(delay)
        }
    }

    override fun close() {
        state?.continuation?.cancel()
        state = null
        webView?.let {
            webViewManager.recycle(it)
        }
        webView = null
    }
}