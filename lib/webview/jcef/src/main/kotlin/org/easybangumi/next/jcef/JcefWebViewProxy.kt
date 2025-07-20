package org.easybangumi.next.jcef

import kotlinx.coroutines.*
import org.cef.CefClient
import org.cef.browser.*
import org.cef.callback.CefQueryCallback
import org.cef.handler.CefLoadHandler
import org.cef.handler.CefMessageRouterHandlerAdapter
import org.cef.handler.CefResourceRequestHandlerAdapter
import org.cef.network.CefRequest
import org.easybangumi.next.lib.webview.IWebView
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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
class JcefWebViewProxy : IWebView {

    companion object {
        val BLOB_HOOK_JS = """
            let origin = window.URL.createObjectURL
            window.URL.createObjectURL = function (t) {
                let blobUrl = origin(t)
                let xhr = new XMLHttpRequest()
                xhr.onload = function () {
                    window.cefQuery({
                        request: "blob:" + xhr.responseText,
                        onSuccess: function(response) { /* ... */ },
                        onFailure: function(error_code, error_message) { /* ... */ }
                    });
                   
                }
                xhr.open('get', blobUrl)
                xhr.send();
                return blobUrl
            }
        """.trimIndent()

        val GET_CONTENT_JS = """
            window.cefQuery({
                request: "content:" + document.documentElement.outerHTML,
                onSuccess: function(response) { /* ... */ },
                onFailure: function(error_code, error_message) { /* ... */ }
            });
        """.trimIndent()
    }

    @Volatile
    private var url: String? = null
    @Volatile
    private var userAgent: String? = null
    @Volatile
    private var headers: Map<String, String>? = null

    @Volatile
    private var needBlob: Boolean = false
    private val hasInjectBlobJs = AtomicBoolean(false)


    @Volatile
    private var interceptResRegex: Regex? = null

    @Volatile
    private var cefClient: CefClient? = null

    @Volatile
    private var browser: CefBrowser? = null

    private val hasLoad = AtomicBoolean(false)

    private val resourceList = mutableListOf<String>()

    @Volatile
    private var isLoadEnd = false

    @Volatile
    private var lastWaitingForPageLoaded: CancellableContinuation<Unit>? = null

    @Volatile
    private var resourceRegex: Regex? = null
    @Volatile
    private var lastWaitingForResourceLoaded: CancellableContinuation<String?>? = null

    @Volatile
    private var lastGetContent: CancellableContinuation<String?>? = null

    private val messageRouterHandlerAdapter = object: CefMessageRouterHandlerAdapter() {
        override fun onQuery(
            browser: CefBrowser?,
            frame: CefFrame?,
            queryId: Long,
            request: String?,
            persistent: Boolean,
            callback: CefQueryCallback?
        ): Boolean {
            request ?: return super.onQuery(browser, frame, queryId, request, persistent, callback)
            when {
                request.startsWith("blob:") -> {
                    if (needBlob) {
                        val blobUrl = request.removePrefix("blob:")
                        JcefManager.runOnJcefContext(false) {
                            resourceList.add(blobUrl)
                            val cal = lastWaitingForResourceLoaded
                            val regex = resourceRegex
                            if (cal != null && regex != null) {
                                if (regex.matches(blobUrl)) {
                                    cal.resume(blobUrl)
                                }
                            }
                        }
                        return true
                    }
                }
                request.startsWith("content:") -> {
                    val content = request.removePrefix("content:")
                    JcefManager.runOnJcefContext(false) {
                        lastGetContent?.resume(content)
                    }
                    return true
                }
            }
            return super.onQuery(browser, frame, queryId, request, persistent, callback)
        }
    }

    private val requestHandlerAdapter = object : CefResourceRequestHandlerAdapter() {
        override fun onBeforeResourceLoad(browser: CefBrowser?, frame: CefFrame?, request: CefRequest?): Boolean {
            request ?: return super.onBeforeResourceLoad(browser, frame, request)
            if (request.url == url) {
                userAgent?.let {
                    request.setHeaderByName("User-Agent", it, true)
                }
                headers?.forEach { (key, value) ->
                    request.setHeaderByName(key, value, true)
                }
            } else {
                resourceList.add(request.url)
                val regex = interceptResRegex
                if (regex != null && regex.matches(request.url)) {
                    JcefManager.runOnJcefContext(false) {
                        val cal = lastWaitingForResourceLoaded
                        cal?.resume(request.url)
                    }
                }
            }
            return super.onBeforeResourceLoad(browser, frame, request)
        }
    }

    private val loadHandler = object : CefLoadHandler {
        override fun onLoadingStateChange(
            p0: CefBrowser?,
            p1: Boolean,
            p2: Boolean,
            p3: Boolean
        ) {

        }

        override fun onLoadStart(
            p0: CefBrowser?,
            p1: CefFrame?,
            p2: CefRequest.TransitionType?
        ) {
//            cefClient?.addMessageRouter()
            JcefManager.runOnJcefContext(false) {
                if (needBlob && hasInjectBlobJs.compareAndSet(false, true)) {
                    browser?.executeJavaScript(BLOB_HOOK_JS, "", 0)
                }
            }

        }

        override fun onLoadEnd(p0: CefBrowser?, p1: CefFrame?, p2: Int) {
            JcefManager.runOnJcefContext(false) {
                isLoadEnd = true
                lastWaitingForPageLoaded?.resume(Unit)
            }
        }

        override fun onLoadError(
            p0: CefBrowser?,
            p1: CefFrame?,
            p2: CefLoadHandler.ErrorCode?,
            p3: String?,
            p4: String?
        ) {

        }
    }

//    private var isInitialized = false

    override suspend fun loadUrl(
        url: String,
        userAgent: String?,
        headers: Map<String, String>,
        interceptResRegex: String?,
        needBlob: Boolean
    ): Boolean {
        if (hasLoad.compareAndSet(false, true)) {
            close()
            this.url = url
            this.userAgent = userAgent
            this.headers = headers
            this.needBlob = needBlob
            this.interceptResRegex = interceptResRegex?.let { Regex(it) }
            return suspendCoroutine<Boolean> { continuation ->
                JcefManager.runOnJcefContext(true) {
                    if (it is JcefManager.CefAppState.Initialized) {
                        try {
                            val cefApp = it.cefApp
                            val ct = cefApp.createClient()
                            val router = CefMessageRouter.create(messageRouterHandlerAdapter)
                            ct.addMessageRouter(router)
                            ct.addLoadHandler(loadHandler)
                            val bs = ct.createBrowser(
                                url,
                                CefRendering.DEFAULT,
                                true,
                                CefRequestContext.createContext { p0, p1, p2, p3, p4, p5, p6 ->
                                    requestHandlerAdapter
                                }
                            )
                            bs.setCloseAllowed()
                            bs.createImmediately()
                            bs.wasResized(1080, 1080)
                            cefClient = ct
                            browser = bs
                            continuation.resume(true)
                        } catch (e: Exception) {
                            // there is something wrong
                            continuation.resume(false)
                        }

                    } else {
                        // If the CefApp is not initialized, we cannot proceed.
                        continuation.resume(false)
                    }
                }
            }
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
                JcefManager.runOnJcefContext(false) {
                    if (isLoadEnd) {
                        continuation.resume(Unit)
                        return@runOnJcefContext
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
                JcefManager.runOnJcefContext(false) {
                    // double check
                    if (sticky) {
                        val sourceListTemp = resourceList.toList()
                        val res = sourceListTemp.firstOrNull { Regex(resourceRegex).matches(it) }
                        if (res != null) {
                            continuation.resume(res)
                            return@runOnJcefContext
                        }
                    }
                    lastWaitingForResourceLoaded = continuation
                }
            }
        }
        if (lastWaitingForResourceLoaded == continuationTemp) {
            lastWaitingForResourceLoaded = null
        }
        return res
    }

    override suspend fun getContent(timeout: Long): String? {
        if (lastGetContent != null) {
            throw IllegalStateException("getContent is already in progress, please wait for it to complete.")
        }
        var continuationTemp: CancellableContinuation<String?>? = null
        val content = suspendCancellableCoroutine<String?> { continuation ->
            continuationTemp = continuation
            JcefManager.runOnJcefContext(false) {
                lastGetContent = continuation
                browser?.executeJavaScript(GET_CONTENT_JS, "", 0)
            }
        }
        if (lastGetContent == continuationTemp) {
            lastGetContent = null
        }
        return content
    }

    override suspend fun executeJavaScript(script: String, delay: Long) {
        suspendCoroutine<Unit> { continuation ->
            JcefManager.runOnJcefContext(false) {
                browser?.executeJavaScript(script, "", 0)
                continuation.resume(Unit)
            }
        }
        delay(delay)
    }

    override fun close() {
        JcefManager.runOnJcefContext(false) {
            browser?.safeStopLoad()
            browser?.safeClose()
            cefClient?.safeDispose()
            browser = null
            cefClient = null
        }
        lastWaitingForPageLoaded?.cancel()
        lastWaitingForResourceLoaded?.cancel()
        lastGetContent?.cancel()
        lastWaitingForPageLoaded = null
        lastWaitingForResourceLoaded = null
        lastGetContent = null
    }
}