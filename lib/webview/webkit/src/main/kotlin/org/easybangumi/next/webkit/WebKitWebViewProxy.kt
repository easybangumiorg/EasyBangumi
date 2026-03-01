package org.easybangumi.next.webkit

import android.graphics.Bitmap
import android.webkit.*
import kotlinx.coroutines.*
import org.apache.commons.text.StringEscapeUtils
import org.easybangumi.next.lib.utils.coroutineProvider
import org.easybangumi.next.lib.utils.safeResume
import org.easybangumi.next.lib.webview.IWebView
import java.io.ByteArrayInputStream

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
class WebKitWebViewProxy(
    private val webViewManager: WebViewManager,
): IWebView {

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
        CoroutineScope(SupervisorJob() + coroutineProvider.single() + CoroutineName("WebKitWebViewProxy-single"))
    }
    private val mainScope by lazy {
        CoroutineScope(SupervisorJob() + coroutineProvider.main() + CoroutineName("WebKitWebViewProxy"))
    }

    private val blockWebResourceRequest =
        WebResourceResponse("text/html", "utf-8", ByteArrayInputStream("".toByteArray()))


    @Volatile
    private var url: String? = null
    @Volatile
    private var interceptResRegex: Regex? = null

    private var webView: WebView? = null

    private val resourceList = mutableListOf<String>()

    @Volatile
    private var userAgent: String? = null

    @Volatile
    private var headers: Map<String, String> = emptyMap()

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

    private val blobHookBridge = object : Any() {
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
    }


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
            if (url != this@WebKitWebViewProxy.url) {
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

    override suspend fun init(
        userAgent: String?,
        headers: Map<String, String>,
        needBlob: Boolean
    ): Boolean {
        this.userAgent = userAgent
        this.headers = headers
        this.needBlob = needBlob

        return mainScope.async {
            val currentWebView = webView ?: webViewManager.getWebView() ?: return@async false
            webView = currentWebView
            currentWebView.clearWeb()
            currentWebView.settings.userAgentString = userAgent
            currentWebView.webViewClient = webViewClient
            if (needBlob) {
                currentWebView.addJavascriptInterface(blobHookBridge, "blobHook")
            }
            true
        }.await()
    }

    override fun setInterceptResRegex(interceptResRegex: String?) {
        this.interceptResRegex = interceptResRegex?.takeIf { it.isNotBlank() }?.let { Regex(it) }
    }

    override suspend fun loadUrl(
        url: String,
        userAgent: String?,
        headers: Map<String, String>,
        interceptResRegex: String?,
        needBlob: Boolean
    ): Boolean {
        this.url = url
        setInterceptResRegex(interceptResRegex)
        if (!init(userAgent, headers, needBlob)) {
            return false
        }

        state?.continuation?.cancel()
        state = null
        isLoadEnd = false
        resourceList.clear()

        return mainScope.async {
            val currentWebView = webView ?: return@async false
            currentWebView.loadUrl(url, this@WebKitWebViewProxy.headers)
            true
        }.await()
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
        val res =  winner.await()
        delay(500)
        return res
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
        url = null
        interceptResRegex = null
        userAgent = null
        headers = emptyMap()
        needBlob = false
        isLoadEnd = false
        resourceList.clear()
    }

    override fun getImpl(): Any? {
        return webView
    }

    override fun addToEndpoint(): Boolean {
        return webView?.let {
            WebKitWindowEndpoint.addBrowserToWindow(it)
        } ?: false
    }

    override fun removeFromEndpoint(): Boolean {
        return webView?.let {
            WebKitWindowEndpoint.removeBrowserFromWindow(it)
            true
        } ?: false
    }
}
