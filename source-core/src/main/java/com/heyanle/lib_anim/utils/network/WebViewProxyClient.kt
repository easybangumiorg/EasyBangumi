package com.heyanle.lib_anim.utils.network

import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.webkit.WebViewClientCompat
import okhttp3.CacheControl
import okhttp3.Headers
import okhttp3.Request
import java.io.ByteArrayInputStream

/**
 * Created by HeYanLe on 2023/2/4 12:41.
 * https://github.com/heyanLE
 */
open class WebViewProxyClient(
) : WebViewClientCompat() {

    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ): WebResourceResponse? {
        val req = request
        if(req.method == "GET"){
            val header = Headers.Builder()
            req.requestHeaders.iterator().forEach {
                if(it.key != "X-Request-With" && it.key != "x-request-with"){
                    header.add(it.key, it.value)
                }
            }
            //header.addAll(networkHelper.defaultLinuxHeader)
            Log.d("WebViewProxyClient", "ua -> ${header.get("User-Agent")}")
            // header.add("User-Agent", view.settings.userAgentString)
            val requ = Request.Builder().url(req.url.toString()).get()
                .headers(header.build()).cacheControl(CacheControl.Builder().noCache().build()).build()
            val resp = networkHelper.client.newCall(requ).execute()
            val contentType = resp.headers["Content-Type"]?:"text/html"
            val types = contentType.split(";")
            val type = types[0].trim()
            var charset = "utf-8"
            types.forEach {
                if(it.startsWith("charset=")){
                    charset = charset.substring(8)
                }
            }

            return WebResourceResponse(type, charset, resp.code, "ok", resp.headers.toMap(), ByteArrayInputStream(resp?.body?.string()?.toByteArray()))

        }


        return super.shouldInterceptRequest(view, request)
    }

}