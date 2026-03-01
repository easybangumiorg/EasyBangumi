package org.easybangumi.next.shared.compose.web

import android.webkit.WebView
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.easybangumi.next.shared.foundation.view_model.StateViewModel
import org.easybangumi.next.shared.source.api.component.WebViewCheckParam
import org.easybangumi.next.webkit.WebKitWindowEndpoint

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
class WebPageVMAndroid(
    private val param: WebPageParam,
) : StateViewModel<WebPageVMAndroid.State>(State.Idle, true) {

    sealed class State {
        object Idle : State()
        object LoadingWebView : State()
        data class ShowWebView(
            val webView: WebView,
            val tips: String?,
        ) : State()
        data class Error(val errorMsg: String) : State()
    }

    val isBrowserLoading = mutableStateOf(false)
    var webViewCheckParam: WebViewCheckParam? = null
    var webView: WebView? = null
        private set

    init {
        viewModelScope.launch {
            update {
                State.LoadingWebView
            }
            val paramKey = param.needWebViewCheckKey
            val param = needWebViewCheckParamMap.remove(paramKey)?.targetOrNull
            val webView = param?.iWebView?.getImpl() as? WebView
            this@WebPageVMAndroid.webViewCheckParam = param
            if (webView != null) {
                update {
                    State.ShowWebView(
                        webView = webView,
                        tips = param.tips?:""
                    )
                }
            } else {
                update {
                    State.Error("webview 获取失败")
                }
            }
        }
    }

//    fun createWebView(androidWebView: WebView) {
//        if (webView == null) {
//            webView = androidWebView
//            // 将 WebView 添加到窗口
//            WebKitWindowEndpoint.addBrowserToWindow(androidWebView)
//
//            val checkParam = webViewCheckParam
//            if (checkParam != null) {
//                update {
//                    State.ShowWebView(
//                        webView = androidWebView,
//                        tips = checkParam.tips
//                    )
//                }
//            } else {
//                update {
//                    State.Error("WebView check parameter is null")
//                }
//            }
//        }
//    }

    fun onDisposableEffect() {
        webViewCheckParam?.onFinish?.invoke()
        webViewCheckParam = null
    }

    suspend fun onCheck(): Boolean {
        return webViewCheckParam?.let {
            it.check?.invoke(it.iWebView)
        } ?: false
    }
}