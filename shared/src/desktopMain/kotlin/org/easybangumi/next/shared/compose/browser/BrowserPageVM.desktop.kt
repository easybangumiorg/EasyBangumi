package org.easybangumi.next.shared.compose.browser

import kotlinx.coroutines.*
import org.cef.CefClient
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.browser.CefRendering
import org.cef.browser.CefRequestContext
import org.cef.handler.CefLoadHandler
import org.cef.network.CefRequest
import org.easybangumi.next.jcef.JcefCookieSyncEndpoint
import org.easybangumi.next.jcef.JcefManager
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.utils.coroutineProvider
import java.awt.Component
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
 */
class BrowserPageVMDesktop(
    param: BrowserPageParam,
) : BrowserPageVM(param) {

    private val coroutineScope = CoroutineScope(SupervisorJob() + coroutineProvider.main())
    
    private var cefClient: CefClient? = null
    private var browser: CefBrowser? = null
    var browserComponent: Component? = null
        private set

    private val isInitialized = AtomicBoolean(false)

    init {
        initializeBrowser()
    }

    private fun initializeBrowser() {
        if (isInitialized.compareAndSet(false, true)) {
            update { State.Loading }
            
            coroutineScope.launch {
                try {
                    JcefManager.runOnJcefContext(true) { state ->
                        if (state is JcefManager.CefAppState.Initialized) {
                            try {
                                val client = state.cefApp.createClient()
                                val requestContext = JcefManager.getSharedRequestContext() ?: CefRequestContext.getGlobalContext()
                                val cefBrowser = client.createBrowser(
                                    "",
                                    CefRendering.DEFAULT,
                                    true,
                                    requestContext
                                )
                                
//                                cefBrowser.setCloseAllowed()
//                                cefBrowser.createImmediately()
                                cefBrowser.wasResized(1080, 720)
                                
                                client.addLoadHandler(object : CefLoadHandler {

                                    override fun onLoadingStateChange(
                                        browser: CefBrowser?,
                                        isLoading: Boolean,
                                        canGoBack: Boolean,
                                        canGoForward: Boolean
                                    ) {
                                        coroutineScope.launch {
                                            this@BrowserPageVMDesktop.isLoading.value = isLoading
                                            this@BrowserPageVMDesktop.canGoBack.value = canGoBack
                                            this@BrowserPageVMDesktop.canGoForward.value = canGoForward
                                        }
                                    }

                                    override fun onLoadStart(
                                        browser: CefBrowser?,
                                        frame: CefFrame?,
                                        transitionType: CefRequest.TransitionType?
                                    ) {
                                        coroutineScope.launch {
                                            this@BrowserPageVMDesktop.isLoading.value = true
                                        }
                                    }

                                    override fun onLoadEnd(browser: CefBrowser?, frame: CefFrame?, httpStatusCode: Int) {
                                        coroutineScope.launch {
                                            this@BrowserPageVMDesktop.isLoading.value = false
                                            browser?.url?.let { url ->
                                                currentUrl.value = url
                                                urlInput.value = url
                                            }
                                            update { 
                                                State.BrowserReady(
                                                    canGoBack = canGoBack.value,
                                                    canGoForward = canGoForward.value
                                                )
                                            }
                                        }
                                    }

                                    override fun onLoadError(
                                        browser: CefBrowser?,
                                        frame: CefFrame?,
                                        errorCode: CefLoadHandler.ErrorCode?,
                                        errorText: String?,
                                        failedUrl: String?
                                    ) {
                                        coroutineScope.launch {
                                            this@BrowserPageVMDesktop.isLoading.value = false
                                            logger.error("Page load error: $errorText for URL: $failedUrl")
                                            update { State.Error("Page load error: $errorText") }
                                        }
                                    }
                                })
                                
                                cefClient = client
                                this@BrowserPageVMDesktop.browser = cefBrowser
                                browserComponent = cefBrowser.uiComponent
                                
                                update { 
                                    State.BrowserReady(
                                        canGoBack = false,
                                        canGoForward = false
                                    )
                                }
                                
                            } catch (e: Exception) {
                                logger.error("Error initializing browser", e)
                                update { State.Error("Failed to initialize browser: ${e.message}") }
                            }
                        } else {
                            update { State.Error("CEF not initialized") }
                        }
                    }
                } catch (e: Exception) {
                    logger.error("Error in browser initialization", e)
                    update { State.Error("Failed to initialize browser: ${e.message}") }
                }
            }
        }
    }

    override fun goBack() {
        browser?.goBack()
    }

    override fun goForward() {
        browser?.goForward()
    }

    override fun reload() {
        browser?.reload()
    }

    override fun stopLoading() {
        browser?.stopLoad()
    }

    override fun loadUrl(url: String) {
        val finalUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
            "https://$url"
        } else {
            url
        }
        JcefCookieSyncEndpoint.storageToJcefBlocking(finalUrl)
        browser?.loadURL(finalUrl)
    }

    override fun onCleared() {
        try {
            val targetBrowser = browser
            val targetClient = cefClient
            JcefCookieSyncEndpoint.jcefToStorageBlocking()
            JcefManager.runOnJcefContext(false) {
                targetBrowser?.safeClose()
                JcefManager.flushCookieStoreSync()
                targetClient?.safeDispose()
            }
            browser = null
            cefClient = null
            browserComponent = null
            coroutineScope.cancel()
        } catch (e: Exception) {
            logger.error("Error disposing browser resources", e)
        }
        super.onCleared()
    }

    // CEF 扩展函数
    private fun CefBrowser.safeClose() {
        try {
            this.close(true)
        } catch (e: Exception) {
            // 忽略关闭错误
        }
    }

    private fun CefClient.safeDispose() {
        try {
            this.dispose()
        } catch (e: Exception) {
            // 忽略释放错误
        }
    }
}
