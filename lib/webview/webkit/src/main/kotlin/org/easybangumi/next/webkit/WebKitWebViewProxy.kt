package org.easybangumi.next.webkit

import android.graphics.Bitmap
import android.webkit.*
import kotlinx.coroutines.*
import org.easybangumi.next.lib.utils.coroutineProvider
import org.easybangumi.next.lib.utils.global
import org.easybangumi.next.lib.webview.IWebView
import java.io.ByteArrayInputStream
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume

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
class WebKitWebViewProxy: IWebView {

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

    private val webViewManager: WebViewManager by lazy {
        WebViewManager(3, CookieManager.getInstance(), global.appContext)
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

    private val hasLoad = AtomicBoolean(false)

    private var webView: WebView? = null

    private val resourceList = mutableListOf<String>()

    private var needBlob: Boolean = false
    @Volatile
    private var isLoadEnd = false

    @Volatile
    private var lastGetContent: CancellableContinuation<String?>? = null
    @Volatile
    private var lastWaitingForPageLoaded: CancellableContinuation<Unit>? = null

    @Volatile
    private var resourceRegex: Regex? = null
    @Volatile
    private var lastWaitingForResourceLoaded: CancellableContinuation<String?>? = null

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
                lastWaitingForPageLoaded?.resume(Unit)
            }
        }

        override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
            val url = request?.url?.toString() ?: return super.shouldInterceptRequest(view, request)
            if (url != this@WebKitWebViewProxy.url) {
                singleScope.launch {
                    resourceList.add(url)
                    if (resourceRegex?.matches(url) == true) {
                        lastWaitingForResourceLoaded?.resume(url)
                    }
                }
            }

            if (resourceRegex?.matches(url) != true && interceptResRegex?.matches(url) == true) {
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
            this@WebKitWebViewProxy.url = url
            this@WebKitWebViewProxy.interceptResRegex = interceptResRegex?.let { Regex(it) }

            mainScope.async {
               val wb = webViewManager.getWebView()
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
                                if (resourceRegex?.matches(blobTextData) == true) {
                                    lastWaitingForResourceLoaded?.resume(url)
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

    override suspend fun waitingForPageLoaded(timeout: Long) {
        if (isLoadEnd) {
            return
        }
        if (lastWaitingForPageLoaded != null) {
            throw IllegalStateException("waitingForPageLoaded is already in progress, please wait for it to complete.")
        }
        var continuationTemp: CancellableContinuation<Unit>? = null
        withTimeout(timeout) {
            suspendCancellableCoroutine<Unit> { continuation ->
                continuationTemp = continuation
                if (isLoadEnd) {
                    continuation.resume(Unit)
                    return@suspendCancellableCoroutine
                }
                singleScope.launch {
                    if (isLoadEnd) {
                        continuation.resume(Unit)
                    } else {
                        lastWaitingForPageLoaded = continuation
                    }
                }

            }
        }
        if (lastWaitingForPageLoaded == continuationTemp) {
            lastWaitingForPageLoaded = null
        }
    }

    override suspend fun waitingForResourceLoaded(
        resourceRegex: String,
        sticky: Boolean,
        timeout: Long
    ): String? {
        val regex = Regex(resourceRegex)
        this.resourceRegex = regex
        val sourceListTemp = resourceList.toList()
        if (sticky) {
            val res = sourceListTemp.firstOrNull { Regex(resourceRegex).matches(it) }
            if (res != null) {
                return res
            }
        }
        if (lastWaitingForResourceLoaded != null) {
            throw IllegalStateException("waitingForResourceLoaded is already in progress, please wait for it to complete.")
        }
        var continuationTemp: CancellableContinuation<String?>? = null
        val res = withTimeoutOrNull(timeout) {
            suspendCancellableCoroutine<String?> { continuation ->
                continuationTemp = continuation
                singleScope.launch {
                    if (sticky) {
                        val sourceListTemp = resourceList.toList()
                        val res = sourceListTemp.firstOrNull { Regex(resourceRegex).matches(it) }
                        if (res != null) {
                            continuation.resume(res)
                            return@launch
                        } else {
                            lastWaitingForResourceLoaded = continuation
                        }
                    }
                }
            }
        }
        if (lastWaitingForResourceLoaded == continuationTemp) {
            lastWaitingForResourceLoaded = null
        }
        return res
    }

    override suspend fun getContent(timeout: Long): String? {
        if (webView == null) {
            throw IllegalStateException("WebView is not initialized, please call loadUrl first.")
        }
        return withTimeoutOrNull(timeout) {
            suspendCancellableCoroutine<String?> { continuation ->
                lastGetContent = continuation
                lastWaitingForPageLoaded
                webView?.evaluateJavascript(
                    "(function() { return document.documentElement.outerHTML })()",
                ) {
                    continuation.resume(it)
                }
            }
        }
    }

    override suspend fun executeJavaScript(script: String, delay: Long) {
        mainScope.launch {
            webView?.evaluateJavascript(script, null)
            delay(delay)
        }
    }

    override fun close() {
        lastWaitingForPageLoaded?.cancel()
        lastWaitingForResourceLoaded?.cancel()
        lastGetContent?.cancel()
        lastWaitingForPageLoaded = null
        lastWaitingForResourceLoaded = null
        lastGetContent = null
        webView?.let {
            webViewManager.recycle(it)
        }
        webView = null
    }
}