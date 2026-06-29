package org.easybangumi.next.webkit

import android.content.Context
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.UiThread
import kotlinx.coroutines.*
import org.easybangumi.next.lib.logger.Logger
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.utils.coroutineProvider

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
class WebViewManager(
    private val maxWebViewCount: Int = 2,
    private val cookieManager: CookieManager,
    private val context: Context,
) {

    private val nonClient = object: WebViewClient() {}

    private val logger: Logger by lazy {
        logger()
    }

    private val application = context.applicationContext

    private val mainScope by lazy {
        CoroutineScope(SupervisorJob() + coroutineProvider.main() + CoroutineName("WebViewManager"))
    }

    private val webViewList = arrayListOf<WebView?>()




    @UiThread
    private fun newWebView(): WebView? {
        return try {
            WebView(application).apply {
                // setDefaultSettings()
                with(settings) {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    cacheMode = WebSettings.LOAD_DEFAULT

                    // Allow zooming
                    setSupportZoom(true)
                    builtInZoomControls = true
                    displayZoomControls = false
                }
                cookieManager.also {
                    it.setAcceptCookie(true)
                    it.acceptCookie()
                    it.setAcceptThirdPartyCookies(this, true) // 跨域cookie读取
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            null
        }
    }


    suspend fun getWebView(): WebView? {
        return mainScope.async {
            val temp = webViewList.removeLastOrNull()
            if (temp != null) {
                return@async temp
            }

            val webView = newWebView()
            return@async webView
        }.await()
    }

    fun recycle(webView: WebView){
        mainScope.launch {
            runCatching { webView.removeAllViews() }
            runCatching { webView.webViewClient = nonClient }
            runCatching { webView.webChromeClient = null }
            runCatching { webView.stop() }
            if (webViewList.size < maxWebViewCount) {
                webViewList.add(webView)
            }
        }
    }



}