package com.heyanle.lib_anim.utils.network

import android.util.Log
import android.webkit.CookieManager
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

/**
 * Created by HeYanLe on 2023/2/3 21:51.
 * https://github.com/heyanLE
 */
class AndroidCookieJar : CookieJar {
    private val manager = CookieManager.getInstance()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val urlString = url.toString()
        Log.d("AndroidCookieJar", urlString)
        cookies.forEach { manager.setCookie(urlString, it.toString()) }
        manager.flush()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return get(url)
    }

    fun get(url: HttpUrl): List<Cookie> {
        val cookies = manager.getCookie(url.toString())
        return if (cookies != null && cookies.isNotEmpty()) {
            cookies.split(";").mapNotNull { Cookie.parse(url, it) }
        } else {
            emptyList()
        }
    }

    fun remove(url: HttpUrl, cookieNames: List<String>? = null, maxAge: Int = -1): Int {
        val urlString = url.toString()
        val cookies = manager.getCookie(urlString) ?: return 0

        fun List<String>.filterNames(): List<String> {
            return if (cookieNames != null) {
                this.filter { it in cookieNames }
            } else {
                this
            }
        }

        return cookies.split(";")
            .map { it.substringBefore("=") }
            .filterNames()
            .onEach { manager.setCookie(urlString, "$it=;Max-Age=$maxAge") }
            .count()
    }

    fun put(url: String, vararg cookies: Cookie) {
        val urlString = url.toHttpUrl().toString()
        cookies.forEach { manager.setCookie(urlString, it.toString()) }
    }

    fun removeAll() {
        manager.removeAllCookies {}
    }

    fun flush() {
        manager.flush()
    }
}