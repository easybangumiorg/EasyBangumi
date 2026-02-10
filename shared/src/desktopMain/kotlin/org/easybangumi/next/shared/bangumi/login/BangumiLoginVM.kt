package org.easybangumi.next.shared.bangumi.login

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
import org.easybangumi.next.jcef.JcefWebViewProxy
import org.easybangumi.next.jcef.safeClose
import org.easybangumi.next.jcef.safeDispose
import org.easybangumi.next.jcef.safeStopLoad
import org.easybangumi.next.lib.utils.safeResume
import org.easybangumi.next.shared.bangumi.account.BangumiAccountController
import org.easybangumi.next.shared.foundation.view_model.StateViewModel
import org.easybangumi.next.shared.source.bangumi.business.BangumiApi
import org.koin.core.component.inject
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.getValue

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
class BangumiLoginVM : StateViewModel<BangumiLoginVM.State>(State.Idle, true) {
    private val sta = this.toString()

    sealed class State {
        object Idle: State()
        object LoadingJcef: State()
        data class ShowJcef(
            val client: CefClient,
            val browser: CefBrowser,
        ): State()
        data class WaitingAccessToken(val callback: String): State()
        object GetAccountTokenSuccess: State()
        data class ErrorAndExit(val errorMsg: String): State()
    }

    val isBrowserLoading = mutableStateOf(false)

    private val bangumiAccountController: BangumiAccountController by inject()
    private val bangumiApi: BangumiApi by inject()

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
                "BangumiLoginVM WebViewClient onUrlHook: $url"
            )
            if (url != null ) {
                viewModelScope.launch(Dispatchers.Main) {
                    val code = bangumiApi.onAuthUrlHook(url)
                    if (code != null) {
                        if (code.isEmpty()) {
                            update {
                                State.ErrorAndExit("获取授权码失败 $url")
                            }
                        } else {
                            getAccessToken(code)
                        }
                    }

                }
            }
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
                            State.ErrorAndExit("JCEF 初始化失败")
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
                                State.ErrorAndExit("JCEF 创建浏览器客户端失败")
                            }
                            return@runOnJcefContext
                        }
                        client.addLoadHandler(loadHandler)
                        val browser = client.createBrowser(
                            bangumiApi.getLoginPageUrl(sta),
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

    private var getAccessTokenJob: Job? = null
    private fun getAccessToken(code: String) {
        getAccessTokenJob?.cancel()
        getAccessTokenJob = viewModelScope.launch {
            update {
                State.WaitingAccessToken(code)
            }

            bangumiApi.getAccessToken(code).await()
                .onFailure {  th ->
                    update {
                        State.ErrorAndExit("获取访问令牌失败：${th.message}")
                    }
                }.onSuccess {
                    bangumiAccountController.updateAccessToken(it)
                    update {
                        State.GetAccountTokenSuccess
                    }
                }
        }

    }

    override fun onCleared() {
        (state.value as? State.ShowJcef)?.let { jcef ->
            update {
                State.Idle
            }
            JcefManager.runOnJcefContext(false) {
                jcef.browser.safeStopLoad()
                jcef.browser.safeClose()
                jcef.client.safeDispose()
            }
        }
        super.onCleared()
    }

    fun reload() {
        (state.value as? State.ShowJcef)?.let {
            it.browser.loadURL(bangumiApi.getLoginPageUrl(sta))
        }
    }

}