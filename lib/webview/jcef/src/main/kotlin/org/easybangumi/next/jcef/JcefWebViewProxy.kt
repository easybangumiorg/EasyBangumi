package org.easybangumi.next.jcef

import kotlinx.coroutines.*
import org.cef.CefClient
import org.cef.browser.*
import org.cef.callback.CefQueryCallback
import org.cef.handler.CefLoadHandler
import org.cef.handler.CefMessageRouterHandlerAdapter
import org.cef.handler.CefRequestHandlerAdapter
import org.cef.handler.CefResourceRequestHandler
import org.cef.handler.CefResourceRequestHandlerAdapter
import org.cef.misc.BoolRef
import org.cef.network.CefRequest
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.utils.coroutineProvider
import org.easybangumi.next.lib.utils.safeResume
import org.easybangumi.next.lib.webview.IWebView
import java.awt.Component
import java.util.concurrent.FutureTask
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.SwingUtilities

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
        private const val EMPTY_BROWSER_URL = ""

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
                request: "content:" + window.document.documentElement.outerHTML,
                onSuccess: function(response) { /* ... */ },
                onFailure: function(error_code, error_message) { /* ... */ }
            });
        """.trimIndent()

        private const val MAX_TRACKED_RESOURCES = 1024

        private val singleDispatcher: CoroutineDispatcher by lazy {
            coroutineProvider.newSingle()
        }
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

    private val logger = logger(this.toString())

    @Volatile
    private var cefClient: CefClient? = null

    @Volatile
    private var messageRouter: CefMessageRouter? = null

    @Volatile
    private var browser: CefBrowser? = null

    private val resourceList = mutableListOf<String>()
    private val stateLock = Any()

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

    private fun replaceState(newState: State?) {
        val oldState = synchronized(stateLock) {
            val old = state
            state = newState
            old
        }
        if (oldState !== newState) {
            oldState?.continuation?.cancel()
        }
    }

    private fun clearState(continuation: CancellableContinuation<*>) {
        synchronized(stateLock) {
            if (state?.continuation === continuation) {
                state = null
            }
        }
    }

    private fun currentState(): State? = synchronized(stateLock) { state }

    private fun trackResource(resourceUrl: String) {
        synchronized(stateLock) {
            if (resourceList.size >= MAX_TRACKED_RESOURCES) {
                resourceList.removeAt(0)
            }
            resourceList.add(resourceUrl)
        }
    }

    private fun findTrackedResource(regex: Regex): String? = synchronized(stateLock) {
        resourceList.firstOrNull { regex.matches(it) }
    }

    private fun clearRuntimeState() {
        replaceState(null)
        synchronized(stateLock) {
            resourceList.clear()
        }
        url = null
        userAgent = null
        headers = null
        interceptResRegex = null
        needBlob = false
        isLoadEnd = false
        hasInjectBlobJs.set(false)
    }

    private fun resetPageState(targetUrl: String) {
        replaceState(null)
        synchronized(stateLock) {
            resourceList.clear()
        }
        url = targetUrl
        isLoadEnd = false
        hasInjectBlobJs.set(false)
    }



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
                        trackResource(blobUrl)
                        (currentState() as? State.WaitingForResourceLoaded)?.let {
                            if (it.resourceRegex.matches(blobUrl)) {
                                it.continuation.safeResume(blobUrl)
                                clearState(it.continuation)
                            }
                        }
                    }
                    return true
                }
                request.startsWith("content:") -> {
                    singleScope.launch {
                        (currentState() as? State.WaitingForGetContent)?.let {
                            val content = request.removePrefix("content:")
                            it.continuation.safeResume(content)
                            clearState(it.continuation)
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
            val requestUrl = request.url ?: return super.onBeforeResourceLoad(browser, frame, request)
            if (request.url == this@JcefWebViewProxy.url) {
                userAgent?.let {
                    request.setHeaderByName("User-Agent", it, true)
                }
                headers?.forEach { (key, value) ->
                    request.setHeaderByName(key, value, true)
                }
            } else {
                singleScope.launch {
                    trackResource(requestUrl)
                    (currentState() as? State.WaitingForResourceLoaded)?.let {
                        if (it.resourceRegex.matches(requestUrl)) {
                            it.continuation.safeResume(requestUrl)
                            clearState(it.continuation)
                        }
                    }
                }
                if (interceptResRegex?.matches(requestUrl) == true) {
                    return true // Intercept the request
                }
            }
            return super.onBeforeResourceLoad(browser, frame, request)
        }
    }

    private val requestHandler = object : CefRequestHandlerAdapter() {
        override fun getResourceRequestHandler(
            browser: CefBrowser?,
            frame: CefFrame?,
            request: CefRequest?,
            isNavigation: Boolean,
            isDownload: Boolean,
            requestInitiator: String?,
            disableDefaultHandling: BoolRef?
        ): CefResourceRequestHandler? {
            return requestHandlerAdapter
        }
    }

    private val loadHandler = object : CefLoadHandler {
        override fun onLoadingStateChange(
            p0: CefBrowser?,
            p1: Boolean,
            p2: Boolean,
            p3: Boolean
        ) {
            val browserUrl = p0?.url
            if (browserUrl == url && !p1) {
                singleScope.launch {
                    isLoadEnd = true
                    (currentState() as? State.WaitingForPageLoaded)?.let {
                        it.continuation.safeResume(Unit)
                        clearState(it.continuation)
                    }
                }
            }

        }

        override fun onLoadStart(
            p0: CefBrowser?,
            p1: CefFrame?,
            p2: CefRequest.TransitionType?
        ) {
            val targetUrl = url
            val frameUrl = p1?.url
            val browserUrl = p0?.url
            val isTargetPageLoad = targetUrl != null && (frameUrl == targetUrl || browserUrl == targetUrl)
            if (needBlob && isTargetPageLoad && hasInjectBlobJs.compareAndSet(false, true)) {
                p0?.executeJavaScript(BLOB_HOOK_JS, "", 0)
            }

        }

        override fun onLoadEnd(p0: CefBrowser?, p1: CefFrame?, p2: Int) {

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

    override suspend fun init(
        userAgent: String?,
        headers: Map<String, String>,
        needBlob: Boolean
    ): Boolean {
        this.userAgent = userAgent
        this.headers = headers
        this.needBlob = needBlob

        if (browser != null && cefClient != null) {
            return true
        }

        return suspendCancellableCoroutine { continuation ->
            JcefManager.runOnJcefContext(true) {
                if (it !is JcefManager.CefAppState.Initialized) {
                    continuation.safeResume(false)
                    return@runOnJcefContext
                }

                var tempClient: CefClient? = null
                var tempRouter: CefMessageRouter? = null
                var tempBrowser: CefBrowser? = null

                try {
                    val ct = it.cefApp.createClient()
                    tempClient = ct

                    val router = CefMessageRouter.create(messageRouterHandlerAdapter)
                    tempRouter = router
                    ct.addMessageRouter(router)
                    ct.addLoadHandler(loadHandler)
                    ct.addRequestHandler(requestHandler)

                    val requestContext = JcefManager.getSharedRequestContext() ?: CefRequestContext.getGlobalContext()

                    val bs = ct.createBrowser(
                        EMPTY_BROWSER_URL,
                        CefRendering.DEFAULT,
                        true,
                        requestContext,
                    )
                    tempBrowser = bs
                    bs.setCloseAllowed()
                    bs.wasResized(1080, 1080)

                    if (!JcefBrowserWindowEndpoint.addBrowserToWindow(bs.uiComponent)) {
                        logger.error("Failed to attach JCEF browser to desktop window endpoint.")
                        ct.safeRemoveMessageRouter(router)
                        bs.safeClose()
                        ct.safeDispose()
                        continuation.safeResume(false)
                        return@runOnJcefContext
                    }

                    cefClient = ct
                    messageRouter = router
                    browser = bs
                    continuation.safeResume(true)
                } catch (e: Exception) {
                    logger.error("Failed to create JCEF browser", e)
                    tempBrowser?.uiComponent?.let { JcefBrowserWindowEndpoint.removeBrowserFromWindow(it) }
                    runCatching {
                        tempBrowser?.safeClose()
                    }
                    runCatching {
                        if (tempClient != null && tempRouter != null) {
                            tempClient.safeRemoveMessageRouter(tempRouter)
                        }
                        tempClient?.safeDispose()
                    }
                    browser = null
                    cefClient = null
                    messageRouter = null
                    continuation.safeResume(false)
                }
            }
        }
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
        this.userAgent = userAgent
        this.headers = headers
        this.needBlob = needBlob
        setInterceptResRegex(interceptResRegex)

        if (!init(userAgent, headers, needBlob)) {
            return false
        }

        val targetBrowser = browser ?: return false
        if (!waitForBrowserAttached(targetBrowser.uiComponent, 5000L)) {
            logger.error("JCEF browser host component is not attached, skip loading url: $url")
            return false
        }

        JcefCookieSyncEndpoint.storageToJcef(url)
        resetPageState(url)
        // jcef 没有渲染回调，没辙了
        delay(500)
        return suspendCancellableCoroutine { continuation ->
            JcefManager.runOnJcefContext(false) {
                runCatching {
                    targetBrowser.loadURL(url)
                }.onSuccess {
                    continuation.safeResume(true)
                }.onFailure { e ->
                    logger.error("Failed to load url: $url", e)
                    continuation.safeResume(false)
                }
            }
        }
    }

    override suspend fun waitingForPageLoaded(timeout: Long): Boolean {
        if (isLoadEnd) {
            return true
        }
        return withTimeoutOrNull(timeout) {
            suspendCancellableCoroutine<Unit> { continuation ->
                replaceState(State.WaitingForPageLoaded(continuation))
                continuation.invokeOnCancellation {
                    clearState(continuation)
                }
                if (isLoadEnd) {
                    clearState(continuation)
                    continuation.safeResume(Unit)
                }
            }
            true
        }.let {
            // jcef 没有完美的 pageLoaded 回调，没辙了
            delay(1000)
            it
        } ?: false
    }

    override suspend fun waitingForResourceLoaded(
        resourceRegex: String,
        sticky: Boolean,
        timeout: Long
    ): String? {
        val regex = Regex(resourceRegex)

        if (sticky) {
            val res = findTrackedResource(regex)
            if (res != null) {
                return res
            }
        }
        return withTimeoutOrNull(timeout) {
            suspendCancellableCoroutine<String?> { continuation ->
                replaceState(State.WaitingForResourceLoaded(regex, continuation))
                continuation.invokeOnCancellation {
                    clearState(continuation)
                }

                if (sticky) {
                    val cachedRes = findTrackedResource(regex)
                    if (cachedRes != null) {
                        clearState(continuation)
                        continuation.safeResume(cachedRes)
                    }
                }
            }
        }
    }

    override suspend fun getContent(timeout: Long): String? {
        return withTimeoutOrNull(timeout) {
            suspendCancellableCoroutine<String?> { continuation ->
                replaceState(State.WaitingForGetContent(continuation))
                continuation.invokeOnCancellation {
                    clearState(continuation)
                }
                JcefManager.runOnJcefContext(false) {
                    browser?.executeJavaScript(GET_CONTENT_JS, "", 0)
                }
            }
        }
    }

    override suspend fun executeJavaScript(script: String, delay: Long) {
        suspendCancellableCoroutine<Unit> { continuation ->
            JcefManager.runOnJcefContext(false) {
                browser?.executeJavaScript(script, "", 0)
                continuation.safeResume(Unit)
            }
        }
        delay(delay)
    }

    override fun close() {
        logger.info("Closing JcefWebViewProxy")

        val targetClient = cefClient
        val targetBrowser = browser
        val targetRouter = messageRouter

        cefClient = null
        browser = null
        messageRouter = null

        targetBrowser?.uiComponent?.let { JcefBrowserWindowEndpoint.removeBrowserFromWindow(it) }

        JcefCookieSyncEndpoint.jcefToStorageBlocking()

        JcefManager.runOnJcefContext(false) {
            targetBrowser?.safeStopLoad()
            targetBrowser?.safeClose()
            JcefManager.flushCookieStoreSync()
            if (targetClient != null && targetRouter != null) {
                targetClient.safeRemoveMessageRouter(targetRouter)
            }
            targetClient?.safeDispose()
        }

        clearRuntimeState()
        singleScope.cancel()
    }

    override fun getImpl(): Any? {
        return browser
    }

    override fun addToEndpoint(): Boolean {
        return browser?.uiComponent?.let {
            JcefBrowserWindowEndpoint.addBrowserToWindow(
                it
            )
        } ?: false
    }

    override fun removeFromEndpoint(): Boolean {
        return browser?.uiComponent?.let {
            JcefBrowserWindowEndpoint.removeBrowserFromWindow(
                it
            )
            true
        } ?: false
    }

    private suspend fun waitForBrowserAttached(component: Component, timeout: Long): Boolean {
        return withTimeoutOrNull(timeout) {
            while (currentCoroutineContext().isActive) {
                if (isBrowserComponentAttached(component)) {
                    return@withTimeoutOrNull true
                }
                delay(20)
            }
            false
        } ?: false
    }

    private fun isBrowserComponentAttached(component: Component): Boolean {
        return runCatching {
            if (SwingUtilities.isEventDispatchThread()) {
                component.parent != null && component.isDisplayable
            } else {
                val task = FutureTask {
                    component.parent != null && component.isDisplayable
                }
                SwingUtilities.invokeAndWait(task)
                task.get()
            }
        }.getOrElse {
            false
        }
    }

    private fun CefClient.safeRemoveMessageRouter(router: CefMessageRouter) {
        try {
            removeMessageRouter(router)
        } catch (_: Exception) {
            // ignore
        }
    }
}
