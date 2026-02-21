package org.easybangumi.next.shared.ktor

import android.webkit.CookieManager
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.http.Cookie
import io.ktor.http.Url


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

class WebkitCookiesStorage(

): CookiesStorage {
    override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {


        TODO("Not yet implemented")
    }

    override suspend fun get(requestUrl: Url): List<Cookie> {
        TODO("Not yet implemented")
    }

    override fun close() {
        TODO("Not yet implemented")
    }
}