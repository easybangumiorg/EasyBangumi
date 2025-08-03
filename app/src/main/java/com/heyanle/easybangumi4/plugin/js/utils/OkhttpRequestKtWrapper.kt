package com.heyanle.easybangumi4.plugin.js.utils

import com.heyanle.easybangumi4.source_api.utils.core.network.GET
import com.heyanle.easybangumi4.source_api.utils.core.network.POST
import okhttp3.FormBody
import okhttp3.Headers
import okhttp3.Request
import org.jsoup.Jsoup

/**
 * Created by heyanle on 2025/8/3
 * https://github.com/heyanLE
 */
object OkhttpRequestKtWrapper {
    fun get(url: String): Request {
        return GET(url)
    }

    fun get(url: String, headerMap: Map<String, String>): Request {
        val hb = Headers.Builder()
        headerMap.forEach { (key, value) ->
            hb.add(key, value)
        }
        return GET(url, hb.build())
    }

    fun post(url: String): Request {
        return POST(url)
    }

    fun postFormBody(
        url: String,
        formBody: Map<String, Any> = emptyMap(),
        headerMap: Map<String, Any> = emptyMap()
    ): Request {
        val hb = Headers.Builder()
        headerMap.forEach { (key, value) ->
            hb.add(key, value.toString())
        }
        val fb = FormBody.Builder()
        formBody.forEach { (key, value) ->
            fb.add(key, value.toString())
        }


        return POST(
            url,
            hb.build(),
            fb.build()
        )
    }


    fun postFormBody(
        url: String,
        formBody: Map<String, Any> = emptyMap(),
    ): Request {

        val fb = FormBody.Builder()
        formBody.forEach { (key, value) ->
            fb.add(key, value.toString())
        }


        return POST(
            url,
            Headers.Builder().build(),
            fb.build()
        )
    }
}
