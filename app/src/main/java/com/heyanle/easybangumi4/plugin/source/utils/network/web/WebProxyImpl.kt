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
            let blobText = [];
            let blobUrl = [];
            let open_origin = window.URL.createObjectURL;
            window.URL.createObjectURL = function (t) {
                this.addEventListener("load", () => {
                    console.log("blob xhr hook", args[1], xhr.responseText);
                    try {
                        
                        blobUrl.push(args[1]);
                        blobText.push(xhr.responseText);
                        window.blobHook.handleWrapper(blobUrl, blobText);
                    } catch(e) {
                    console.log(e);
                    }
                });
                return open_origin.apply(this, args);
            }
            
            let text_origin = window.Response.prototype.text;
            window.Response.prototype.text = function () {
                return new Promise((resolve, reject) => {
                    text_origin.call(this).then((text) => {
                        console.log("blob text hook2", this.url, text);
                        blobUrl.push(this.url);
                        blobText.push(text);
                        resolve(text);
                        window.blobHook.handleWrapper(blobUrl, blobText);
                    }).catch(reject);
                });
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

    @Volatile
    private var webView: WebView? = null

    private val resourceList = mutableListOf<String>()
    // url to text
    private val blobResourceList = mutableListOf<Pair<String, String>>()

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

        class WaitingForBlobLoaded(
            val urlRegex: Regex?,
            val textRegex: Regex?,
            override val continuation: CancellableContinuation<Pair<String?, String?>>,
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
            }
            super.onPageStarted(view, url, favicon)

        }

        override fun onPageFinished(view: WebView?, url: String?) {
//            if (needBlob) {
//                "needBlob $webView".logi("WebProxyImpl")
//                webView?.evaluateJavascript(BLOB_XHR_HOOK_JS, null)
//            }
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

    override suspend fun href(url: String, cleanLoaded: Boolean) {
        mainScope.launch {
            if (cleanLoaded) {
                isLoadEnd = false
            }
            webView?.evaluateJavascript("window.location.href = '$url';", null)
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
                        fun handleWrapper(blobUrl: Array<String>, blobText: Array<String>) {
                            singleScope.launch {
                                val array = arrayListOf<Pair<String, String>>()
                                for (i in blobUrl.indices) {
                                    if (i >= blobText.size ) {
                                        break
                                    }
                                    array.add(Pair(blobUrl[i], blobText[i]))
                                }
                                blobResourceList.addAll(array)
                                "handleWrapper  ${blobResourceList.joinToString("\n")}".logi("WebProxyImpl")

                                (state as? State.WaitingForBlobLoaded)?.let { state ->
                                    for (pair in blobResourceList) {
                                        if (state.urlRegex != null && state.urlRegex.matches(pair.first)) {
                                            state.continuation.safeResume(pair)
                                            return@let
                                        } else if (state.textRegex != null && state.textRegex.matches(pair.second)) {
                                            state.continuation.safeResume(pair)
                                            return@let
                                        }
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
            winner.complete(isLoadEnd)
            return@async
        }
        mainScope.async {
            delay(timeout)
            winner.complete(isLoadEnd)
        }
        return winner.await()
    }

    override suspend fun waitingForResourceLoaded(
        urlRegex: String,
        sticky: Boolean,
        timeout: Long
    ): String? {
        if (sticky) {
            val res = resourceList.toList().firstOrNull { Regex(urlRegex).matches(it) }
            if (res != null) {
                return res
            }
        }
        state?.continuation?.cancel()
        val winner = CompletableDeferred<String?>()
        val regex = Regex(urlRegex)
        singleScope.launch {
            if (sticky) {
                val res = resourceList.toList().firstOrNull { Regex(urlRegex).matches(it) }
                if (res != null) {
                    winner.complete(res)
                    return@launch
                }
            }
            suspendCancellableCoroutine<String?> {
                state = State.WaitingForResourceLoaded(regex, it)
            }
            val res = resourceList.toList().firstOrNull { Regex(urlRegex).matches(it) }
            if (res != null) {
                winner.complete(res)
                return@launch
            }

        }
        mainScope.launch {
            delay(timeout)
            val res = resourceList.toList().firstOrNull { Regex(urlRegex).matches(it) }
            winner.complete(res)

        }
        return winner.await()
    }

    override suspend fun waitingForBlobText(
        urlRegex: String?,
        textRegex: String?,
        sticky: Boolean,
        timeout: Long
    ): Pair<String?, String?>? {
        if (!needBlob) {
            return null
        }
        if (sticky) {
            for (pair in blobResourceList) {
                if (urlRegex != null && Regex(urlRegex).matches(pair.first)) {
                    return pair
                } else if (textRegex != null && Regex(textRegex).matches(pair.second)) {
                    return pair
                }
            }
        }
        state?.continuation?.cancel()
        val winner = CompletableDeferred<Pair<String?, String?>?>()
        val urlR = urlRegex?.let { Regex(it) }
        val textR = textRegex?.let { Regex(it) }
        singleScope.launch {
            if (sticky) {
                for (pair in blobResourceList) {
                    if (urlRegex != null && Regex(urlRegex).matches(pair.first)) {
                        winner.complete(pair)
                        return@launch
                    } else if (textRegex != null && Regex(textRegex).matches(pair.second)) {
                        winner.complete(pair)
                        return@launch
                    }
                }
            }
            suspendCancellableCoroutine<Pair<String?, String?>?> {
                state = State.WaitingForBlobLoaded(urlR, textR, it)
            }
            for (pair in blobResourceList) {
                if (urlRegex != null && Regex(urlRegex).matches(pair.first)) {
                    winner.complete(pair)
                    return@launch
                } else if (textRegex != null && Regex(textRegex).matches(pair.second)) {
                    winner.complete(pair)
                    return@launch
                }
            }
        }
        mainScope.launch {
            delay(timeout)
            for (pair in blobResourceList) {
                if (urlRegex != null && Regex(urlRegex).matches(pair.first)) {
                    winner.complete(pair)
                    return@launch
                } else if (textRegex != null && Regex(textRegex).matches(pair.second)) {
                    winner.complete(pair)
                    return@launch
                }
            }
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

    override suspend fun executeJavaScript(script: String, delay: Long): String? {
        return mainScope.async {
            suspendCancellableCoroutine<String?> { con ->
                webView?.evaluateJavascript(script, object: ValueCallback<String> {
                    override fun onReceiveValue(value: String?) {
                        con.safeResume(value)
                    }
                })

                mainScope.launch {
                    delay(delay)
                    con.safeResume(null)
                }
            }
        }.await()
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
            (wb.parent as? ViewGroup)?.removeView(wb)
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