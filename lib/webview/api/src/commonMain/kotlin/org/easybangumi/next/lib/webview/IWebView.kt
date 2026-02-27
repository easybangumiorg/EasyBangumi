package org.easybangumi.next.lib.webview

import kotlin.jvm.JvmOverloads

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
interface IWebView: AutoCloseable {

    suspend fun loadUrl(
        url: String,
        userAgent: String? = null,
        headers: Map<String, String> = emptyMap(),
        interceptResRegex: String? = ".*\\.(css|mp3|m4a|gif|jpg|png|webp).*",
        needBlob: Boolean = false,
    ): Boolean

    suspend fun waitingForPageLoaded(
        timeout: Long = 5000L
    ): Boolean

    suspend fun waitingForResourceLoaded(
        resourceRegex: String,
        sticky: Boolean = true,
        timeout: Long = 5000L
    ): String?

    suspend fun getContent(
        timeout: Long = 5000L
    ): String?

    // jcef 不支持返回值，后续有需求在研究
    suspend fun executeJavaScript(
        script: String,
        delay: Long = 100L,
    )

    fun getImpl(): Any?

}