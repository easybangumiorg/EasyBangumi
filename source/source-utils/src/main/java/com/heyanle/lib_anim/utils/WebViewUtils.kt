package com.heyanle.lib_anim.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView

/**
 * Created by HeYanLe on 2023/2/3 22:04.
 * https://github.com/heyanLE
 */

object WebViewUtil {
    const val SPOOF_PACKAGE_NAME = "org.chromium.chrome"

    const val MINIMUM_WEBVIEW_VERSION = 110

    fun supportsWebView(context: Context): Boolean {
        try {
            // May throw android.webkit.WebViewFactory$MissingWebViewPackageException if WebView
            // is not installed
            CookieManager.getInstance()
        } catch (e: Throwable) {
            e.printStackTrace()
            return false
        }

        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_WEBVIEW)
    }
}

@SuppressLint("SetJavaScriptEnabled")
fun WebView.setDefaultSettings() {
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
}

fun WebView.isOutdated(): Boolean {
    return getWebViewMajorVersion() < WebViewUtil.MINIMUM_WEBVIEW_VERSION
}

// 获取版本
private fun WebView.getWebViewMajorVersion(): Int {
    val uaRegexMatch = """.*Chrome/(\d+)\..*""".toRegex().matchEntire(getDefaultUserAgentString())
    return if (uaRegexMatch != null && uaRegexMatch.groupValues.size > 1) {
        uaRegexMatch.groupValues[1].toInt()
    } else {
        0
    }
}

// 获取手机自带浏览器的 UA
fun WebView.getDefaultUserAgentString(): String {
//    if(isOutdated()){
//        return "Mozilla/5.0 (Linux; Android 13; RMX3551 Build/SKQ1.220617.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/103.0.5060.129 Mobile Safari/537.36"
//    }

    val originalUA: String = settings.userAgentString

    // Next call to getUserAgentString() will get us the default
    settings.userAgentString = null
    val defaultUserAgentString = settings.userAgentString

    // Revert to original UA string
    settings.userAgentString = originalUA

    return defaultUserAgentString
}
