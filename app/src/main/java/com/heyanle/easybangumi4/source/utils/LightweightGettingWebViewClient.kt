package com.heyanle.easybangumi4.source.utils

import android.annotation.SuppressLint
import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.CallSuper]
import androidx.webkit.WebViewCompat
import java.io.ByteArrayInputStream

/**
 * Created by LoliBall on 2023/12/14 23:05.
 * https://github.com/WhichWho
 */

/**
 * @param blockRes 屏蔽资源，加快抓取速度
 */

abstract class LightweightGettingWebViewClient(
    private val targetRegex: Regex?,
    private val blockReqForward: Boolean = true,
    private val blockRes: Array<String> = arrayOf(
        ".css",
        ".mp4", ".ts",
        ".mp3", ".m4a",
        ".gif", ",jpg", ".png", ".webp"
    )
) : WebViewClient() {

    private val blockWebResourceRequest =
        WebResourceResponse("text/html", "utf-8", ByteArrayInputStream("".toByteArray()))

    @SuppressLint("WebViewClientOnReceivedSslError")
    override fun onReceivedSslError(
        view: WebView?,
        handler: SslErrorHandler?,
        error: SslError?
    ) {
        handler?.proceed()
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)
    }



    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        return false
    }


    /**
     * 拦截无关资源文件
     *
     * 注意，该方法运行在线程池内
     */
    @SuppressLint("RequiresFeature")
    @CallSuper
    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest?
    ) = run {
        val url = request?.url?.toString()
            ?: return@run super.shouldInterceptRequest(view, request)
        //阻止无关资源加载，加快获取速度
        if (targetRegex?.matches(url) != true && blockRes.any { url.contains(it) }) {
            //Log.d("禁止加载", url)
            //转发回onLoadResource
            if (blockReqForward)
                view.post { WebViewCompat.getWebViewClient(view).onLoadResource(view, url) }
            blockWebResourceRequest
        } else super.shouldInterceptRequest(view, request)
    }
}