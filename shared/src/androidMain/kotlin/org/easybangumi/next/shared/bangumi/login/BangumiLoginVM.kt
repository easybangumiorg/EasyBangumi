package org.easybangumi.next.shared.bangumi.login

import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.easybangumi.next.shared.foundation.view_model.BaseViewModel
import org.easybangumi.next.shared.foundation.view_model.StateViewModel
import org.easybangumi.next.shared.source.bangumi.BangumiAppConfig
import org.easybangumi.next.shared.source.bangumi.BangumiConfig
import org.easybangumi.next.webkit.WebViewManager
import org.koin.core.component.inject
import androidx.core.net.toUri
import kotlinx.coroutines.Job
import org.easybangumi.next.shared.bangumi.account.BangumiAccountController
import org.easybangumi.next.shared.source.bangumi.business.BangumiApi

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
class BangumiLoginVM: StateViewModel<BangumiLoginVM.State>(State.Idle, false) {


    private val sta = this.toString()
    val webViewManager: WebViewManager by inject()
    private var webView: WebView? = null

    private val bangumiAccountController: BangumiAccountController by inject()
    private val bangumiApi: BangumiApi by inject()

    sealed class State {
        object Idle: State()
        data class ShowWebView(val webView: WebView): State()
        data class WaitingAccessToken(val callback: String): State()
        object GetAccountTokenSuccess: State()
        data class ErrorAndExit(val errorMsg: String): State()
    }

    private val webViewClient = object : WebViewClient() {
        override fun shouldInterceptRequest(
            view: WebView?,
            request: WebResourceRequest?
        ): WebResourceResponse? {
            request?.url?.toString()?.let { url ->
                onUrlHook(url)
            }
            return super.shouldInterceptRequest(view, request)
        }

        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            request?.url?.toString()?.let { url ->
                onUrlHook(url)
            }
            return super.shouldOverrideUrlLoading(view, request)
        }

        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            url?.let { u ->
                onUrlHook(u)
            }
            return super.shouldOverrideUrlLoading(view, url)
        }
        override fun shouldInterceptRequest(view: WebView?, url: String?): WebResourceResponse? {
            url?.let { u ->
                onUrlHook(u)
            }
            return super.shouldInterceptRequest(view, url)
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
        viewModelScope.launch(Dispatchers.Main) {
            val webView = webViewManager.getWebView()
            if (webView == null) {
                update {
                    State.ErrorAndExit("WebView 创建失败")
                }
                return@launch
            }

            update {
                State.ShowWebView(webView)
            }

            webView.webViewClient = webViewClient
            webView.loadUrl(bangumiApi.getLoginPageUrl(sta))
            this@BangumiLoginVM.webView = webView
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
        webView?.let {
            webViewManager.recycle(it)
        }
        super.onCleared()
    }

    fun reload() {
        webView?.loadUrl(bangumiApi.getLoginPageUrl(sta))
    }
}