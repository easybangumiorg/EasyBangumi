package com.heyanle.easybangumi4.plugin.source.utils.network.interceptor


import com.heyanle.easybangumi4.source_api.utils.api.NetworkHelper
import okhttp3.Cookie
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import java.util.Collections

/**
 * Created by HeYanLe on 2023/2/5 17:23.
 * https://github.com/heyanLE
 */
class CookieInterceptor(
    private val networkHelper: NetworkHelper
) : Interceptor {

    companion object {
        fun parseAll(url: HttpUrl, headers: Headers): List<Cookie> {
            val cookieStrings = headers.values("Cookie")
            var cookies: MutableList<Cookie>? = null

            for (element in cookieStrings) {
                val els = element.split(";")
                for (el in els) {
                    val cookie = Cookie.parse(url, el) ?: continue
                    if (cookies == null) cookies = mutableListOf()
                    cookies.add(cookie)
                }


            }

            return if (cookies != null) {
                Collections.unmodifiableList(cookies)
            } else {
                emptyList()
            }
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val old = originalRequest.header("Cookie")
        if (old != null) {
            val cookies = parseAll(originalRequest.url, originalRequest.headers)
            networkHelper.cookieManager.saveFromResponse(originalRequest.url, cookies)
        }
        return chain.proceed(originalRequest)
    }
}