package com.heyanle.easybangumi4.plugin.source.utils.network.web

import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import com.heyanle.easybangumi4.ActivityManager
import com.heyanle.easybangumi4.utils.CoroutineProvider
import com.heyanle.easybangumi4.utils.WebViewManager
import com.heyanle.easybangumi4.utils.logi
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
        val BLOB_XHR_HOOK_JS = """
let origin = window.URL.createObjectURL;
window.URL.createObjectURL = function (t) {
    let blobUrl = origin(t);
    let xhr = new XMLHttpRequest();
    xhr.onload = function () {
        console.log("blobt: " + xhr.responseText);
        window.blobHook.handleWrapper(xhr.responseText);

    };
    xhr.open('get', blobUrl);
    xhr.send();
    return blobUrl;
}


        """.trimIndent()
    }
    val BLOB_FETCH_HOOK_JS = """
        

        let originalFetch = window.fetch;
        window.fetch = function (url, options) {
            options = options || {};
            let method = options.method || 'GET';
            let headers = options.headers ? JSON.stringify(options.headers) : '{}';
            let body = options.body ? String(options.body) : null;

            // 调用 Android 接口
            //                    window.AndroidInterceptor.onFetchIntercepted(method, url, body, headers);
            window.blobHook.handleWrapper(url)
            console.log("blobt: " + url)
            return originalFetch.apply(this, arguments);
        };
    """.trimIndent()
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

    @Volatile
    private var webView: WebView? = null

    private val resourceList = mutableListOf<String>()

    @Volatile
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


    private val webChromeClient = object: WebChromeClient() {
        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
            consoleMessage?.message()?.logi("WebProxyImpl")
            return super.onConsoleMessage(consoleMessage)
        }

    }
    private val webViewClient = object: WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            if (needBlob) {
                "needBlob $webView".logi("WebProxyImpl")
                webView?.evaluateJavascript(BLOB_XHR_HOOK_JS, null)
                webView?.evaluateJavascript(BLOB_FETCH_HOOK_JS, null)
            }
            super.onPageStarted(view, url, favicon)

        }

        override fun onPageFinished(view: WebView?, url: String?) {
            if (needBlob) {
                "needBlob $webView".logi("WebProxyImpl")
                webView?.evaluateJavascript(BLOB_XHR_HOOK_JS, null)
                webView?.evaluateJavascript(BLOB_FETCH_HOOK_JS, null)
            }
            super.onPageFinished(view, url)
            singleScope.launch {
                isLoadEnd = true
                (state as? State.WaitingForPageLoaded)?.continuation?.safeResume(Unit)
            }
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            onUrl(url)
            return super.shouldOverrideUrlLoading(view, request)
        }


        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            onUrl(url)
            return super.shouldOverrideUrlLoading(view, url)
        }

        override fun shouldInterceptRequest(view: WebView?, url: String?): WebResourceResponse? {
            return onUrl(url) ?: super.shouldInterceptRequest(view, url)
        }

        override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
            val url = request?.url?.toString() ?: return super.shouldInterceptRequest(view, request)
            return onUrl(url) ?: super.shouldInterceptRequest(view, request)
        }

        private fun onUrl(url: String?): WebResourceResponse?{
            url ?: return null
            url.logi("WebProxyImpl")
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
            return null
        }

        override fun onLoadResource(view: WebView?, url: String?) {
            super.onLoadResource(view, url)
        }


    }

    override suspend fun loadUrl(
        url: String,
        userAgent: String?,
        headers: Map<String, String>,
        interceptResRegex: String?,
        needBlob: Boolean
    ) {
        if (hasLoad.compareAndSet(false, true)) {
            close()
            this@WebProxyImpl.url = url
            this@WebProxyImpl.interceptResRegex = interceptResRegex?.let { Regex(it) }


            mainScope.async {
                this@WebProxyImpl.needBlob = needBlob
                val wb = webViewManager.getWebViewOrNull()
                if (wb == null) {
                    return@async false
                }
                webView = wb
                wb.clearWeb()
                wb.settings.userAgentString = userAgent
                wb.webViewClient = webViewClient
                wb.webChromeClient = webChromeClient
                if (needBlob) {
                    wb.addJavascriptInterface(object : Any() {
                        @JavascriptInterface
                        fun handleWrapper(blobTextData: String) {
                            "blobTextData: $blobTextData".logi("WebProxyImpl")
                            singleScope.launch {
                                "blobTextData: $blobTextData".logi("WebProxyImpl")
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
            val res = resourceList.toList().firstOrNull { Regex(resourceRegex).matches(it) }
            winner.complete(res)

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

    override suspend fun getContentWithIframe(timeout: Long): String? {
        if (webView == null) {
            throw IllegalStateException("WebView is not initialized, please call loadUrl first.")
        }

        state?.continuation?.cancel()
        val winner = CompletableDeferred<String?>()
        singleScope.launch {
            val res = suspendCancellableCoroutine<String?> { continuation ->
                mainScope.launch {
                    webView?.evaluateJavascript("""
                        function getIframeContent(iframe) {
                            try {
                                const doc = iframe.contentDocument || iframe.contentWindow.document;
                                return doc.documentElement.outerHTML;
                            } catch (e) {
                                return "<!-- 跨域iframe内容无法访问 -->";
                            }
                        }

                        function getFullHTMLWithIframes() {
                            let html = document.documentElement.outerHTML;
                            const iframes = document.querySelectorAll('iframe');
                            
                            for (const iframe of iframes) {
                                const iframeContent = getIframeContent(iframe);
                                html = html.replace(iframe.outerHTML, `<iframe-wrapper>`+ iframe.outerHTML + iframeContent + `</iframe-wrapper>`);
                            }
                            
                            return html;
                        }
                        getFullHTMLWithIframes()
                    """.trimIndent()) {
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

    override suspend fun executeJavaScript(script: String, delay: Long): Any {
        return mainScope.launch {
            val res = webView?.evaluateJavascript(script, null)
            delay(delay)
            res
        }
    }

    override fun close() {
        state?.continuation?.cancel()
        state = null
        webView?.let {
            webViewManager.recycle(it)
            (it.parent as? ViewGroup)?.removeView(it)
        }
        webView = null
    }

    override fun addToWindow(show: Boolean) {
        mainScope.launch {
            val act = ActivityManager.getTopActivity() ?: return@launch
            val wb = webView ?: return@launch
            val viewGroup = act.window.decorView.findViewById<ViewGroup>(android.R.id.content)

            if (show) {
                viewGroup.addView(wb, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                wb.visibility = View.VISIBLE
                wb.bringToFront()
            } else {
                viewGroup.addView(wb, 0, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
                wb.visibility = View.VISIBLE
            }


        }
    }
}