package org.easybangumi.next.jcef

import kotlinx.coroutines.*
import org.cef.CefClient
import org.cef.browser.*
import org.cef.callback.CefQueryCallback
import org.cef.handler.CefLoadHandler
import org.cef.handler.CefMessageRouterHandlerAdapter
import org.cef.handler.CefResourceRequestHandlerAdapter
import org.cef.network.CefRequest
import org.easybangumi.next.lib.utils.coroutineProvider
import org.easybangumi.next.lib.utils.safeResume
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

        private val singleDispatcher: CoroutineDispatcher by lazy {
            coroutineProvider.newSingle()
        }
    }


    private val ioScope by lazy {
        CoroutineScope(SupervisorJob() + coroutineProvider.io() + CoroutineName("JcefWebViewProxy-io"))
    }
    private val singleScope = CoroutineScope(SupervisorJob() + singleDispatcher + CoroutineName("JcefWebViewProxy-single"))

    @Volatile
    private var url: String? = null
    private var interceptResRegex: Regex? = null
    @Volatile
    private var userAgent: String? = null
    @Volatile
    private var headers: Map<String, String>? = null

    @Volatile
    private var needBlob: Boolean = false
    private val hasInjectBlobJs = AtomicBoolean(false)



    @Volatile
    private var cefClient: CefClient? = null

    @Volatile
    private var browser: CefBrowser? = null

    private val hasLoad = AtomicBoolean(false)

    private val resourceList = mutableListOf<String>()

    @Volatile
    private var isLoadEnd = false

    sealed class State {
        abstract val continuation: CancellableContinuation<*>
        class WaitingForPageLoaded(
            override val continuation: CancellableContinuation<Unit>,
        ): State()

        class WaitingForGetContent(
            override val continuation: CancellableContinuation<String?>,
        ): State()

        class WaitingForResourceLoaded(
            val resourceRegex: Regex,
            override val continuation: CancellableContinuation<String?>,
        ): State()
    }

    private var state: State? = null



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
                    singleScope.launch {
                        val blobUrl = request.removePrefix("blob:")
                        resourceList.add(blobUrl)
                        (state as? State.WaitingForResourceLoaded)?.let {
                            if (it.resourceRegex.matches(blobUrl)) {
                                it.continuation.safeResume(blobUrl)
                            }
                        }
                    }
                    return true
                }
                request.startsWith("content:") -> {
                    singleScope.launch {
                        (state as? State.WaitingForGetContent)?.let {
                            val content = request.removePrefix("content:")
                            it.continuation.safeResume(content)
                        }
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
            val url = request.url ?: return super.onBeforeResourceLoad(browser, frame, request)
            if (request.url == this@JcefWebViewProxy.url) {
                userAgent?.let {
                    request.setHeaderByName("User-Agent", it, true)
                }
                headers?.forEach { (key, value) ->
                    request.setHeaderByName(key, value, true)
                }
            } else {
                singleScope.launch {
                    resourceList.add(url)
                    (state as? State.WaitingForResourceLoaded)?.let {
                        if (it.resourceRegex.matches(url)) {
                            it.continuation.safeResume(url)
                        }
                    }
                }
                if (interceptResRegex?.matches(url) == true) {
                    return true // Intercept the request
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
                singleScope.launch {
                    (state as? State.WaitingForPageLoaded)?.continuation?.safeResume(Unit)
                }
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
                            continuation.safeResume(true)
                        } catch (e: Exception) {
                            // there is something wrong
                            continuation.safeResume(false)
                        }

                    } else {
                        // If the CefApp is not initialized, we cannot proceed.
                        continuation.safeResume(false)
                    }
                }
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
        ioScope.async {
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
        ioScope.launch {
            delay(timeout)
            winner.complete(null)
        }

        return winner.await()
    }

    override suspend fun getContent(timeout: Long): String? {
        state?.continuation?.cancel()
        val winner = CompletableDeferred<String?>()
        singleScope.launch {
            val res = suspendCancellableCoroutine<String?> {
                state = State.WaitingForGetContent(it)
                JcefManager.runOnJcefContext(false) {
                    browser?.executeJavaScript(GET_CONTENT_JS, "", 0)
                }
            }
            winner.complete(res)
        }
        ioScope.launch {
            delay(timeout)
            winner.complete(null)
        }
        return winner.await()
    }

    override suspend fun executeJavaScript(script: String, delay: Long) {
        suspendCoroutine<Unit> { continuation ->
            JcefManager.runOnJcefContext(false) {
                browser?.executeJavaScript(script, "", 0)
                continuation.safeResume(Unit)
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
        state?.continuation?.cancel()
        state = null
    }
}