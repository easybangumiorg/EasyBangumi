package com.heyanle.easybangumi4.plugin.source.utils.network.web

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
@OptIn(ExperimentalStdlibApi::class)
interface IWebProxy: AutoCloseable {

    suspend fun href(url: String, cleanLoaded: Boolean)

    suspend fun loadUrl(
        url: String,
        userAgent: String? = null,
        headers: Map<String, String> = emptyMap(),
        interceptResRegex: String? = ".*\\.(css|mp3|m4a|gif|jpg|png|webp).*",
        needBlob: Boolean = false,
    ): Unit

    suspend fun waitingForPageLoaded(
        timeout: Long = 5000L
    ): Boolean

    suspend fun waitingForResourceLoaded(
        urlRegex: String,
        sticky: Boolean = true,
        timeout: Long = 5000L
    ): String?

    suspend fun waitingForBlobText(
        urlRegex: String? = null,
        textRegex: String? = null,
        sticky: Boolean = true,
        timeout: Long = 5000L
    ): Pair<String?, String?>?

    suspend fun getContent(
        timeout: Long = 5000L
    ): String?

    suspend fun getContentWithIframe(
        timeout: Long
    ): String?

    suspend fun executeJavaScript(
        script: String,
        delay: Long = 100L,
    ): String?

    fun addToWindow(show: Boolean)



}