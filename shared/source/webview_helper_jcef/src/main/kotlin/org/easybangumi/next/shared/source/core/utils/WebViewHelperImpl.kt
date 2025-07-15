package org.easybangumi.next.shared.source.core.utils

import kotlinx.coroutines.async
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.browser.CefRendering
import org.cef.browser.CefRequestContext
import org.cef.handler.CefLoadHandler
import org.cef.handler.CefRequestContextHandler
import org.cef.handler.CefResourceHandler
import org.cef.handler.CefResourceRequestHandler
import org.cef.handler.CefResourceRequestHandlerAdapter
import org.cef.misc.BoolRef
import org.cef.network.CefRequest
import org.easybangumi.next.jcef.JcefManager
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.utils.DataStateException
import org.easybangumi.next.lib.utils.coroutineProvider
import org.easybangumi.next.shared.source.api.source.Source
import org.easybangumi.next.shared.source.api.utils.WebViewHelper
import java.lang.ref.WeakReference
import kotlin.coroutines.Continuation
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
 */

class WebViewHelperImpl(
    private val source: Source
): WebViewHelper {

    private val logger = logger()

    companion object {
        private val blockRes: Array<String> = arrayOf(
            ".css",
            ".mp4", ".ts",
            ".mp3", ".m4a",
            ".gif", ".jpg", ".png", ".webp"
        )
    }

    override suspend fun renderedHtml(
        strategy: WebViewHelper.RenderedStrategy
    ): WebViewHelper.RenderedResult {
        return source.scope.async(
            coroutineProvider.io()
        ) {
            suspendCoroutine {
                renderedHtmlInternal(strategy, it)
            }
        }.await()
    }

    private fun renderedHtmlInternal(
        strategy: WebViewHelper.RenderedStrategy,
        continuation: Continuation<WebViewHelper.RenderedResult>
    ) {
        JcefManager.runOnJcefContext {
            if (it is JcefManager.CefAppState.Initialized) {

                val cefApp = it.cefApp
                logger.info("Rendering HTML with strategy: $strategy")
                val app = it.cefApp
                val client = app.createClient()
                val browser = client.createBrowser(
                    strategy.url,
                    CefRendering.DEFAULT,
                    true,
                    CefRequestContext.createContext { p0, p1, p2, p3, p4, p5, p6 ->
                        EasyCefResourceRequestHandlerAdapter(
                            strategy,
                            JcefResourceHandler(strategy, continuation)
                        )
                    }
                )
                client.addLoadHandler(object: CefLoadHandler {
                    override fun onLoadEnd(p0: CefBrowser?, p1: CefFrame?, p2: Int) {
                        JcefManager.runOnJcefContext(false) {
                            if (strategy.actionJs != null) {
                                p0?.executeJavaScript(strategy.actionJs, "", 1)
                                logger.info("Executing action JS: ${strategy.actionJs}")
                            } else {
                                logger.info("No action JS to execute")
                            }
                        }
                    }

                    override fun onLoadingStateChange(
                        p0: CefBrowser?,
                        p1: Boolean,
                        p2: Boolean,
                        p3: Boolean
                    ) { }

                    override fun onLoadStart(
                        p0: CefBrowser?,
                        p1: CefFrame?,
                        p2: CefRequest.TransitionType?
                    ) { }

                    override fun onLoadError(
                        p0: CefBrowser?,
                        p1: CefFrame?,
                        p2: CefLoadHandler.ErrorCode?,
                        p3: String?,
                        p4: String?
                    ) { }
                })

                browser.setCloseAllowed()
                browser.createImmediately()
            } else if (it is JcefManager.CefAppState.Error) {
                continuation.resumeWith(Result.failure(DataStateException("JCEF initialization failed")))
            } else {
                continuation.resumeWith(Result.failure(DataStateException("JCEF is not initialized yet")))
            }
        }

    }

    class EasyCefResourceRequestHandlerAdapter(
        private val strategy: WebViewHelper.RenderedStrategy,
        handler: JcefResourceHandler
    ): CefResourceRequestHandlerAdapter() {

        private val logger = logger()

        private val handlerRef: WeakReference<JcefResourceHandler> = WeakReference(handler)
        override fun onBeforeResourceLoad(
            browser: CefBrowser?,
            frame: CefFrame?,
            request: CefRequest?
        ): Boolean {
            if (browser == null || request == null) {
                return super.onBeforeResourceLoad(browser, frame, request)
            }

            request.setHeaderByName("User-Agent", strategy.userAgentString, true)
            if (request.url == strategy.url) {
                strategy.header?.map {
                    request.setHeaderByName(it.key, it.value, true)
                }
            }

            val handler = handlerRef.get()
            if (handler == null || handler.handleRequest(request.url, browser)) {
                try {
                    browser.stopLoad()
                    browser.close(true)
                } catch (e: Exception) {
                    logger.error("Error stopping browser load", e)
                }
                return true
            }

            if (strategy.needInterceptResource && blockRes.any { request.url.contains(it) }) {
                return true
            }
            return super.onBeforeResourceLoad(browser, frame, request)
        }

        override fun getResourceHandler(
            browser: CefBrowser?,
            frame: CefFrame?,
            request: CefRequest?
        ): CefResourceHandler? {
            return super.getResourceHandler(browser, frame, request)
        }

    }

    inner class JcefResourceHandler(
        private val strategy: WebViewHelper.RenderedStrategy,
        private val continuation: Continuation<WebViewHelper.RenderedResult>
    ) {

        fun handleRequest(
            url: String,
            browser: CefBrowser
        ): Boolean {
            val targetRegex = Regex(strategy.callBackRegex)
            if (targetRegex.matches(url)) {
                if (strategy.needContent) {
                    JcefManager.runOnJcefContext(false) {
                        browser.getSource {
                            val content = it ?: ""
//                            logger.info("Content fetched from browser: ${content.take(100)}...") // Log first 100 chars
                            continuation.resumeWith(Result.success(
                                WebViewHelper.RenderedResult(strategy, null, content, url)
                            ))
                        }
                    }
                } else {
                    continuation.resumeWith(Result.success(
                        WebViewHelper.RenderedResult(strategy, null, null, url)
                    ))
                }
                return true
            }
            return false
        }
    }
}