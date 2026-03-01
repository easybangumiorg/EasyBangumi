package org.easybangumi.next.shared.ktor

import io.ktor.http.Cookie
import io.ktor.http.Url
import io.ktor.util.date.GMTDate
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.suspendCancellableCoroutine
import org.cef.network.CefCookie
import org.easybangumi.next.jcef.JcefManager
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.utils.safeResume
import java.util.Date
import java.util.concurrent.atomic.AtomicBoolean


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

object JcefCookiesStorage {

    private const val COOKIE_SYNC_TIMEOUT = 5000L

    val easyCookiesStorage by lazy { EasyCookiesStorage() }
    private val logger = logger("JcefCookiesStorage")

    suspend fun storageToJcef(url: String) {
        val targetUrl = runCatching { Url(url) }.getOrNull() ?: return
        val storageCookies = easyCookiesStorage.get(targetUrl)
        if (storageCookies.isEmpty()) {
            return
        }

        val synced = withTimeoutOrNull(COOKIE_SYNC_TIMEOUT) {
            suspendCancellableCoroutine<Unit> { continuation ->
                JcefManager.runOnJcefContext(true) { state ->
                    if (state !is JcefManager.CefAppState.Initialized) {
                        continuation.safeResume(Unit)
                        return@runOnJcefContext
                    }

                    val manager = JcefManager.getSharedCookieManager()
                    if (manager == null) {
                        logger.warn("Shared JCEF cookie manager is null in storageToJcef.")
                        continuation.safeResume(Unit)
                        return@runOnJcefContext
                    }

                    storageCookies.forEach { cookie ->
                        val cookieUrl = cookie.toSetCookieUrl(url)
                        manager.setCookie(cookieUrl, cookie.toCefCookie())
                    }

                    if (!manager.flushStore {
                            continuation.safeResume(Unit)
                        }) {
                        continuation.safeResume(Unit)
                    }
                }
            }
            true
        } ?: false
        if (!synced) {
            logger.warn("storageToJcef timed out for url: $url")
        }

    }

    suspend fun jcefToStorage() {
        val jcefCookies = withTimeoutOrNull(COOKIE_SYNC_TIMEOUT) {
            suspendCancellableCoroutine<List<Cookie>> { continuation ->
                JcefManager.runOnJcefContext(true) { state ->
                    if (state !is JcefManager.CefAppState.Initialized) {
                        continuation.safeResume(emptyList())
                        return@runOnJcefContext
                    }

                    val manager = JcefManager.getSharedCookieManager()
                    if (manager == null) {
                        logger.warn("Shared JCEF cookie manager is null in jcefToStorage.")
                        continuation.safeResume(emptyList())
                        return@runOnJcefContext
                    }

                    val cookieList = arrayListOf<Cookie>()
                    val hasResume = AtomicBoolean(false)
                    val visited = manager.visitAllCookies { cefCookie, index, total, _ ->
                        cefCookie?.let {
                            cookieList.add(it.toCookie())
                        }
                        if (total <= 0 || index >= total - 1) {
                            if (hasResume.compareAndSet(false, true)) {
                                continuation.safeResume(cookieList)
                            }
                        }
                        true
                    }

                    if (!visited && hasResume.compareAndSet(false, true)) {
                        continuation.safeResume(cookieList)
                    }
                }
            }
        } ?: run {
            logger.warn("jcefToStorage timed out, keep current EasyCookiesStorage.")
            return
        }

        easyCookiesStorage.replaceAllCookies(jcefCookies)

    }


}

private fun Cookie.toSetCookieUrl(defaultUrl: String): String {
    val host = this.domain?.removePrefix(".")?.trim().orEmpty()
    if (host.isBlank()) {
        return defaultUrl
    }
    val path = this.path?.ifBlank { "/" } ?: "/"
    val scheme = if (this.secure) "https" else "http"
    return "$scheme://$host$path"
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
        expires = run {
            if (this.hasExpires || exp == null) GMTDate(this.expires?.time?:return@run null) else GMTDate(exp?.time?:return@run null)
        }
    )
    return cookie
}
