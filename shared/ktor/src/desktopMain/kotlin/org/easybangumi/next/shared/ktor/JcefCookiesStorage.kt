package org.easybangumi.next.shared.ktor

import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.http.Cookie
import io.ktor.http.Url
import io.ktor.util.date.GMTDate
import kotlinx.coroutines.suspendCancellableCoroutine
import org.cef.callback.CefCookieVisitor
import org.cef.misc.BoolRef
import org.cef.network.CefCookie
import org.cef.network.CefCookieManager
import org.easybangumi.next.jcef.JcefManager
import java.util.Date
import kotlin.coroutines.resume


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

class JcefCookiesStorage: CookiesStorage {
    override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {
        suspendCancellableCoroutine<Unit> { con ->
            JcefManager.runOnJcefContext(true) {
                CefCookieManager.getGlobalManager().setCookie(requestUrl.toString(), cookie.toCefCookie())
                if (!CefCookieManager.getGlobalManager().flushStore {
                    con.resume(Unit)
                }) {
                    con.resume(Unit)
                }
            }
        }

    }

    override suspend fun get(requestUrl: Url): List<Cookie> {
        return suspendCancellableCoroutine { con ->
            JcefManager.runOnJcefContext {
                val cookieList = arrayListOf<Cookie>()
                CefCookieManager.getGlobalManager().visitUrlCookies(requestUrl.toString(), true
                ) { p0, p1, p2, p3 ->
                    if (p0 != null) {
                        cookieList.add(p0.toCookie())
                    }
                    true
                }
                con.resume(cookieList)
            }
        }
    }

    override fun close() {

    }
}

private fun Cookie.toCefCookie(): CefCookie {
    val exp = this.expires
    val cefCookie = CefCookie(
        this.name,
        this.value,
        this.domain,
        this.path,
        this.secure,
        this.httpOnly,
        Date(System.currentTimeMillis()),
        Date(System.currentTimeMillis()),
        exp == null ||exp.timestamp > System.currentTimeMillis(),
        Date(this.expires?.timestamp ?: (System.currentTimeMillis() + 365L * 24 * 3600 * 1000))


    )
    return cefCookie
}

private fun CefCookie.toCookie(): Cookie {
    val exp = this.expires
    val cookie = Cookie(
        name = this.name,
        value = this.value,
        domain = this.domain,
        path = this.path,
        secure = this.secure,
        httpOnly = this.httponly,
        expires = if (this.hasExpires || exp == null) GMTDate(this.expires.time) else GMTDate(exp.time)
    )
    return cookie
}