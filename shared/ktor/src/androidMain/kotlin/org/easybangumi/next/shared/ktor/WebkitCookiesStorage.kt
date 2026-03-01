package org.easybangumi.next.shared.ktor

import android.webkit.CookieManager
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.http.Cookie
import io.ktor.http.Url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

object WebkitCookiesStorage : CookiesStorage {

    private val cookieManager : CookieManager by lazy {
        CookieManager.getInstance()
    }

    init {
//        // 确保 CookieManager 已接受 Cookie
//        cookieManager.setAcceptCookie(true)
//        cookieManager.acceptCookie()
//        cookieManager.setAcceptThirdPartyCookies(null, true)
    }

    override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {
        withContext(Dispatchers.Main) {
            try {
                val cookieString = cookieToString(cookie)
                cookieManager.setCookie(requestUrl.toString(), cookieString)
                // 确保 Cookie 立即生效
                cookieManager.flush()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override suspend fun get(requestUrl: Url): List<Cookie> {
        return withContext(Dispatchers.Main) {
            try {
                val cookieString = cookieManager.getCookie(requestUrl.toString())
                if (cookieString.isNullOrEmpty()) {
                    return@withContext emptyList()
                }
                return@withContext parseCookies(cookieString, requestUrl)
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    override fun close() {
        // CookieManager 是单例，不需要关闭
    }

    /**
     * 将 Ktor Cookie 转换为 CookieManager 格式的字符串
     */
    private fun cookieToString(cookie: Cookie): String {
        val builder = StringBuilder()
        
        // 基础属性
        builder.append("${cookie.name}=${cookie.value}")
        
        // Domain
        cookie.domain?.let {
            builder.append("; Domain=$it")
        }
        
        // Path
        cookie.path?.let {
            builder.append("; Path=$it")
        }
        
        // Expires
        cookie.expires?.let { expires ->
            val expiresString = expires.toString()
            builder.append("; Expires=$expiresString")
        }
        
        // Max-Age
        cookie.maxAge?.let { maxAge ->
            builder.append("; Max-Age=$maxAge")
        }
        
        // Secure
        if (cookie.secure) {
            builder.append("; Secure")
        }
        
        // HttpOnly
        if (cookie.httpOnly) {
            builder.append("; HttpOnly")
        }
        
        // SameSite
        cookie.extensions["SameSite"]?.let { sameSite ->
            builder.append("; SameSite=$sameSite")
        }
        
        return builder.toString()
    }

    /**
     * 解析 CookieManager 返回的字符串为 Ktor Cookie 列表
     */
    private fun parseCookies(cookieString: String, requestUrl: Url): List<Cookie> {
        val cookies = mutableListOf<Cookie>()
        
        // CookieManager 返回的格式: "name1=value1; name2=value2"
        val cookiePairs = cookieString.split("; ")
        
        for (pair in cookiePairs) {
            val parts = pair.split("=", limit = 2)
            if (parts.size == 2) {
                val name = parts[0].trim()
                val value = parts[1].trim()
                
                try {
                    val cookie = Cookie(
                        name = name,
                        value = value,
                        domain = requestUrl.host,
                        path = requestUrl.encodedPath,
                        secure = requestUrl.protocol.isSecure(),
                        httpOnly = false, // CookieManager 不返回 HttpOnly 信息
                        extensions = emptyMap()
                    )
                    cookies.add(cookie)
                } catch (e: Exception) {
                    // 跳过格式错误的 Cookie
                    e.printStackTrace()
                }
            }
        }
        
        return cookies
    }
}

/**
 * 扩展函数：判断 URL 协议是否安全
 */
private fun io.ktor.http.URLProtocol.isSecure(): Boolean {
    return this.name == "https"
}