package com.heyanle.easybangumi4.source_api.utils.api

import android.webkit.WebView

/**
 * Created by heyanlin on 2024/6/4.
 */
interface WebViewHelperV2 {

    data class RenderedStrategy(
        // 网址
        val url: String,

        // 回调正则。在检测到特定请求时返回结果。默认为空则在页面加载完成后自动回调（因为ajax等因素可能得到的源码不完整，另外注意超时）
        val callBackRegex: String = "",

        // 编码
        val encoding: String = "utf-8",

        // UA
        val userAgentString: String? = null,

        // 请求头
        val header: Map<String, String>? = null,

        // 在页面加载完成后执行的js代码，可用于主动加载资源，如让视频加载出来以拦截
        val actionJs: String? = null,

        // 是否是拦截 blob 模式
        val isBlockBlob: Boolean = false,

        // 加载超时。当超过超时时间后还没返回数据则会直接返回当前源码
        val timeOut: Long = 8000L,
    )

    data class RenderedResult (

        // 策略
        val strategy: RenderedStrategy,

        // 网址
        val url: String,

        // 是否是超时
        val isTimeout: Boolean,

        // 网页源码
        val content: String,

        // 拦截资源
        val interceptResource: String,
    )

    suspend fun renderedHtml(strategy: RenderedStrategy): RenderedResult

    fun getGlobalWebView(): WebView

    fun openWebPage(
        onCheck: (WebView) -> Boolean,
        onStop: (WebView) -> Unit,
    )

    fun openWebPage(
        webView: WebView,
        onCheck: (WebView) -> Boolean,
        onStop: (WebView) -> Unit,
    )


}