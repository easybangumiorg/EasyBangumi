package org.easybangumi.next.shared.source.quick.utils

import com.dokar.quickjs.QuickJs
import com.dokar.quickjs.binding.JsObject
import com.dokar.quickjs.binding.define
import kotlin.time.Clock
import org.easybangumi.next.lib.utils.safeMutableMapOf
import org.easybangumi.next.lib.webview.IWebView
import org.easybangumi.next.shared.source.api.utils.WebViewHelper

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
suspend fun QuickJs.register(webViewHelper: QuickWebViewHelper) {

    val wrapperCode = """
// 自动 close
let WebViewHelper = {
    use: async function(block) {
        let webViewId = await __Native_WebViewHelper.createWebView();
        let iWeb = {
            loadUrl: async function(url, userAgent = null, headers = {}, interceptResRegex = ".*\\.(css|mp3|m4a|gif|jpg|png|webp).*", needBlob = false) {
                return await __Native_WebViewHelper.loadUrl(webViewId, url, userAgent, headers, interceptResRegex, needBlob);
            },
            waitingForPageLoaded: async function(timeout = 5000) {
                return await __Native_WebViewHelper.waitingForPageLoaded(webViewId, timeout);
            },
            waitingForResourceLoaded: async function(resourceRegex, sticky = true, timeout = 5000) {
                return await __Native_WebViewHelper.waitingForResourceLoaded(webViewId, resourceRegex, sticky, timeout);
            },
            getContent: async function(timeout = 5000) {
                return await __Native_WebViewHelper.getContent(webViewId, timeout);
            },
            executeJavaScript: async function(script, delay = 100) {
                return await __Native_WebViewHelper.executeJavaScript(webViewId, script, delay);
            }
        }
        let res = await block(iWeb);
        await __Native_WebViewHelper.closeWebView(webViewId);
        return res;
    }
}
    """.trimIndent()

    evaluate<Unit>(wrapperCode, "WebViewHelperWrapper.js")

    // bridge 使用 WebView id
    define("__Native_WebViewHelper") {
        asyncFunction("createWebView") {
            val webViewId = webViewHelper.newWebView()
            webViewId
        }

        asyncFunction("closeWebView") {
            val webViewId: String = it[0].toString()
            webViewHelper.closeWebView(webViewId)
        }

        asyncFunction("loadUrl") {
            val webViewId: String = it[0].toString()
            val url: String = it[1].toString()
            val userAgent: String? = it.getOrNull(2)?.toString()
            val headersObj: JsObject = it.getOrNull(3) as? JsObject ?: JsObject(emptyMap())
            val headers: Map<String, String> = headersObj.toMap().mapValues { entry -> entry.value.toString() }
            val interceptResRegex: String? = it.getOrNull(4)?.toString()
            val needBlob: Boolean = it.getOrNull(5) as? Boolean ?: false
            webViewHelper.loadUrl(
                webViewId = webViewId,
                url = url,
                userAgent = userAgent,
                headers = headers,
                interceptResRegex = interceptResRegex,
                needBlob = needBlob
            )
        }

        asyncFunction("waitingForPageLoaded") {
            val webViewId: String = it[0].toString()
            val timeout: Long = (it.getOrNull(1) as? Number)?.toLong() ?: 5000L
            webViewHelper.waitingForPageLoaded(
                webViewId = webViewId,
                timeout = timeout
            )
        }

        asyncFunction("waitingForResourceLoaded") {
            val webViewId: String = it[0].toString()
            val resourceRegex: String = it[1].toString()
            val sticky: Boolean = it.getOrNull(2) as? Boolean ?: true
            val timeout: Long = (it.getOrNull(3) as? Number)?.toLong() ?: 5000L
            webViewHelper.waitingForResourceLoaded(
                webViewId = webViewId,
                resourceRegex = resourceRegex,
                sticky = sticky,
                timeout = timeout
            )
        }

        asyncFunction("getContent") {
            val webViewId: String = it[0].toString()
            val timeout: Long = (it.getOrNull(1) as? Number)?.toLong() ?: 5000L
            webViewHelper.getContent(
                webViewId = webViewId,
                timeout = timeout
            )
        }

        asyncFunction("executeJavaScript") {
            val webViewId: String = it[0].toString()
            val script: String = it[1].toString()
            val delay: Long = (it.getOrNull(2) as? Number)?.toLong() ?: 100L
            webViewHelper.executeJavaScript(
                webViewId = webViewId,
                script = script,
                delay = delay
            )
        }


    }
}

class QuickWebViewHelper(
    private val webViewHelper: WebViewHelper
) {

    private val webViewMap = safeMutableMapOf<String, IWebView>()

    suspend fun newWebView(): String {
        val webView = webViewHelper.newWebView()
        val webViewId = "webview_${Clock.System.now().toEpochMilliseconds()}_${(0..9999).random()}"
        webViewMap[webViewId] = webView
        return webViewId
    }

    suspend fun closeWebView(webViewId: String) {
        webViewMap[webViewId]?.let {
            it.close()
            webViewMap.remove(webViewId)
        }
    }

    suspend fun loadUrl(
        webViewId: String,
        url: String,
        userAgent: String? = null,
        headers: Map<String, String> = emptyMap(),
        interceptResRegex: String? = ".*\\.(css|mp3|m4a|gif|jpg|png|webp).*",
        needBlob: Boolean = false,
    ): Boolean {
        val webView = webViewMap[webViewId] ?: return false
        return webView.loadUrl(
            url = url,
            userAgent = userAgent,
            headers = headers,
            interceptResRegex = interceptResRegex,
            needBlob = needBlob
        )
    }

    suspend fun waitingForPageLoaded(
        webViewId: String,
        timeout: Long = 5000L
    ): Boolean {
        val webView = webViewMap[webViewId] ?: return false
        return webView.waitingForPageLoaded(
            timeout = timeout
        )
    }

    suspend fun waitingForResourceLoaded(
        webViewId: String,
        resourceRegex: String,
        sticky: Boolean = true,
        timeout: Long = 5000L
    ): String? {
        val webView = webViewMap[webViewId] ?: return null
        return webView.waitingForResourceLoaded(
            resourceRegex = resourceRegex,
            sticky = sticky,
            timeout = timeout
        )
    }

    suspend fun getContent(
        webViewId: String,
        timeout: Long = 5000L
    ): String? {
        val webView = webViewMap[webViewId] ?: return null
        return webView.getContent(
            timeout = timeout
        )
    }

    suspend fun executeJavaScript(
        webViewId: String,
        script: String,
        delay: Long = 100L,
    ) {
        val webView = webViewMap[webViewId] ?: return
        return webView.executeJavaScript(
            script = script,
            delay = delay
        )
    }



}