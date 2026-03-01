package org.easybangumi.next.shared.compose.browser

import android.graphics.Bitmap
import android.webkit.*
import kotlinx.coroutines.*
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.utils.coroutineProvider
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
class BrowserPageVMAndroid(
    param: BrowserPageParam,
) : BrowserPageVM(param) {

    private val coroutineScope = CoroutineScope(SupervisorJob() + coroutineProvider.main())
    
    var webView: WebView? = null
        private set

    init {
        initializeBrowser()
    }

    private fun initializeBrowser() {
        update { State.Loading }
        // WebView 将在 Compose 中初始化
    }

    fun createWebView(androidWebView: WebView) {
        if (webView == null) {
            webView = androidWebView
            // 将 WebView 添加到窗口
            WebKitWindowEndpoint.addBrowserToWindow(androidWebView)
            setupWebView(androidWebView)
        }
    }

    private fun setupWebView(webView: WebView) {
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                coroutineScope.launch {
                    isLoading.value = true
                    url?.let {
                        currentUrl.value = it
                        urlInput.value = it
                    }
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                coroutineScope.launch {
                    isLoading.value = false
                    canGoBack.value = webView.canGoBack()
                    canGoForward.value = webView.canGoForward()
                    update { 
                        State.BrowserReady(
                            canGoBack = canGoBack.value,
                            canGoForward = canGoForward.value
                        )
                    }
                }
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                coroutineScope.launch {
                    isLoading.value = false
                    logger.error("Page load error: ${error?.description} for URL: ${request?.url}")
                    update { State.Error("Page load error: ${error?.description}") }
                }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                coroutineScope.launch {
                    title?.let {
                        pageTitle.value = it
                    }
                }
            }

            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                // 可以在这里更新进度条
            }
        }

        webView.settings.apply {
            javaScriptEnabled = param.enableJavaScript
            domStorageEnabled = true
            databaseEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = true
            displayZoomControls = false
            supportZoom()
            
            param.userAgent?.let {
                userAgentString = it
            }
        }

        // 加载初始 URL
        webView.loadUrl(param.url)
        
        update { 
            State.BrowserReady(
                canGoBack = false,
                canGoForward = false
            )
        }
    }

    override fun goBack() {
        webView?.goBack()
    }

    override fun goForward() {
        webView?.goForward()
    }

    override fun reload() {
        webView?.reload()
    }

    override fun stopLoading() {
        webView?.stopLoading()
    }

    override fun loadUrl(url: String) {
        val finalUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
            "https://$url"
        } else {
            url
        }
        webView?.loadUrl(finalUrl)
    }

    override fun onCleared() {
        coroutineScope.cancel()
        // 从窗口移除 WebView
        webView?.let {
            WebKitWindowEndpoint.removeBrowserFromWindow(it)
        }
        webView = null
    }
}