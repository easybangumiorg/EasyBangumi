package com.heyanle.lib_anim.utils.network.webview_helper

/**
 * Created by HeYanLe on 2023/2/3 22:35.
 * https://github.com/heyanLE
 */

lateinit var webViewHelper: WebviewHelper

interface WebviewHelper {

    /**
     * 获取已渲染网页源码
     *
     * @param callBackRegex 回调正则。在检测到特定请求时返回结果。默认为空则在页面加载完成后自动回调（因为ajax等因素可能得到的源码不完整，另外注意超时）
     * @param actionJs 在页面加载完成后执行的js代码，可用于主动加载资源，如让视频加载出来以拦截
     * @param timeOut 加载超时。当超过超时时间后还没返回数据则会直接返回当前源码
     */
    suspend fun getRenderedHtmlCode(
        url: String,
        callBackRegex: String = "",
        encoding: String = "utf-8",
        userAgentString: String? = null,
        header: Map<String, String>? = null,
        actionJs: String? = null,
        timeOut: Long = 8000L
    ): String

    /**
     * 拦截资源
     *
     * @param regex 回调正则，在检测到特定请求时返回结果
     * @param actionJs 在页面加载完成后执行的js代码，可用于主动加载资源，如让视频加载出来以拦截
     * @param timeOut 加载超时。当超过超时时间后还没返回数据则会直接返回当前源码
     */
    suspend fun interceptResource(
        url: String,
        regex: String,
        userAgentString: String? = null,
        header: Map<String, String>? = null,
        actionJs: String? = null,
        timeOut: Long = 8000L
    ): String

    /**
     * 拦截Blob数据
     *
     * @param regex 回调正则，在检测到特定Blob数据符合时返回结果
     * @param actionJs 在页面加载完成后执行的js代码，可用于主动加载资源，如让视频加载出来以拦截
     * @param timeOut 加载超时。当超过超时时间后还没返回数据则会直接返回当前源码
     */
    suspend fun interceptBlob(
        url: String,
        regex: String,
        userAgentString: String? = null,
        header: Map<String, String>? = null,
        actionJs: String? = null,
        timeOut: Long = 8000L
    ): String


}