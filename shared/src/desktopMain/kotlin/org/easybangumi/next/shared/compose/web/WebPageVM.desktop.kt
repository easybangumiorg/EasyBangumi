package org.easybangumi.next.shared.compose.web

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.cef.CefClient
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.browser.CefRendering
import org.cef.browser.CefRequestContext
import org.cef.handler.CefLoadHandler
import org.cef.handler.CefResourceRequestHandlerAdapter
import org.cef.network.CefRequest
import org.easybangumi.next.jcef.JcefManager
import org.easybangumi.next.shared.bangumi.login.BangumiLoginVM
import org.easybangumi.next.shared.foundation.view_model.StateViewModel

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
class WebPageVM(
    private val param: WebPageParam,
) : StateViewModel<WebPageVM.State>(State.Idle, true) {

    sealed class State {
        object Idle: State()
        object LoadingJcef: State()
        data class ShowJcef(
            val client: CefClient,
            val browser: CefBrowser,
        ): State()
        data class Error(val errorMsg: String): State()
    }

    val isBrowserLoading = mutableStateOf(false)


    private val loadHandler = object: CefLoadHandler {
        override fun onLoadingStateChange(
            p0: CefBrowser?,
            p1: Boolean,
            p2: Boolean,
            p3: Boolean
        ) {
            if (p1 && p0 == (state.value as? State.ShowJcef)?.browser) {
                isBrowserLoading.value = true
            } else {
                isBrowserLoading.value = false
            }
        }

        override fun onLoadStart(
            p0: CefBrowser?,
            p1: CefFrame?,
            p2: CefRequest.TransitionType?
        ) {}

        override fun onLoadEnd(p0: CefBrowser?, p1: CefFrame?, p2: Int) {}

        override fun onLoadError(
            p0: CefBrowser?,
            p1: CefFrame?,
            p2: CefLoadHandler.ErrorCode?,
            p3: String?,
            p4: String?
        ) {}
    }

    private val requestHandlerAdapter = object : CefResourceRequestHandlerAdapter() {
        override fun onBeforeResourceLoad(browser: CefBrowser?, frame: CefFrame?, request: CefRequest?): Boolean {
            val url = request?.url
            onUrlHook(url)
            return super.onBeforeResourceLoad(browser, frame, request)
        }

        fun onUrlHook(
            url: String?,
        ) {
            logger.info(
                "WebViewClient onUrlHook: $url"
            )

        }
    }


    init {
        viewModelScope.launch {
            update {
                State.LoadingJcef
            }
            JcefManager.runOnJcefContext(true) { state ->
                when (state) {
                    is JcefManager.CefAppState.Error -> {
                        update {
                            State.Error("JCEF 初始化失败")
                        }
                    }
                    JcefManager.CefAppState.Initializing -> {
                        update {
                            State.LoadingJcef
                        }
                    }
                    is JcefManager.CefAppState.Initialized -> {
                        val jcefApp = state.cefApp

                        val client = jcefApp.createClient()
                        if (client == null) {
                            update {
                                State.Error("JCEF 创建浏览器客户端失败")
                            }
                            return@runOnJcefContext
                        }
                        client.addLoadHandler(loadHandler)
                        val browser = client.createBrowser(
                            param.url,
                            CefRendering.DEFAULT,
                            true,
                            CefRequestContext.createContext { p0, p1, p2, p3, p4, p5, p6 ->
                                requestHandlerAdapter
                            }
                        )
                        browser.setCloseAllowed()
                        browser.createImmediately()
                        update {
                            State.ShowJcef(
                                client = client,
                                browser = browser,
                            )
                        }
                    }
                }

            }
        }
    }

}