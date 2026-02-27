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
import org.easybangumi.next.shared.source.api.component.WebViewCheckParam

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
            val browser: CefBrowser,
            val tips: String?,
        ): State()
        data class Error(val errorMsg: String): State()
    }

    val isBrowserLoading = mutableStateOf(false)


    var webViewCheckParam: WebViewCheckParam? = null


    init {
        viewModelScope.launch {
            update {
                State.LoadingJcef
            }
            val paramKey = param.needWebViewCheckKey
            val param = needWebViewCheckParamMap.remove(paramKey)?.targetOrNull
            this@WebPageVM.webViewCheckParam = param
            val webView = param?.iWebView?.getImpl() as? CefBrowser
            if (webView != null) {
                update {
                    State.ShowJcef(
                        browser = webView,
                        tips = param?.tips
                    )
                }
            }

        }
    }

    fun onDisposableEffect() {
        webViewCheckParam?.onFinish?.invoke()
        webViewCheckParam = null
    }

    suspend fun onCheck(): Boolean {
        return webViewCheckParam?.let {
            it.check?.invoke(it.iWebView)
        } ?:false

    }

}