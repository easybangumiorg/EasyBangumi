package com.heyanle.easybangumi4.utils

import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.annotation.UiThread
import com.heyanle.easybangumi4.APP
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Created by heyanle on 2024/7/27.
 * https://github.com/heyanLE
 */
class WebViewManager(
    private val cookieManager: CookieManager
) {

    companion object {
        const val CORE_WEB_VIEW_COUNT = 1
    }

    private val lock = Object()
    private val scope = MainScope()
    private val coreWebViewList = mutableListOf<WebView>()


    fun getWebViewOrNull(): WebView? {
        synchronized(lock) {
            if (coreWebViewList.isNotEmpty()) {
                return coreWebViewList.removeLast()
            }

            if (isMainThread()) {
                return newWebView()
            }
            lock.notifyAll()
        }
        val countDownLatch = CountDownLatch(1)
        var webView: WebView? = null
        scope.launch {
            webView = newWebView()
            countDownLatch.countDown()
        }
        countDownLatch.await(3000, TimeUnit.MILLISECONDS)
        return webView
    }

    fun recycle(webView: WebView) {
        if (coreWebViewList.size >= CORE_WEB_VIEW_COUNT) {
            scope.launch {
                try {
                    webView.removeAllViews()
                    webView.destroy()
                } catch (e: Throwable) {
                    e.printStackTrace()
                }

            }

            return
        }
        synchronized(lock) {
            if (coreWebViewList.size >= CORE_WEB_VIEW_COUNT) {
                scope.launch {
                    try {
                        webView.removeAllViews()
                        webView.destroy()
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }

                }
                return
            }
            coreWebViewList.add(webView)
            lock.notifyAll()
        }
    }

    @UiThread
    private fun newWebView(): WebView? {
        return try {
            WebView(APP).apply {
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


}