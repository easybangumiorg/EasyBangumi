package org.easybangumi.next.shared.source.core.utils

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.headers
import io.ktor.util.toMap
import org.easybangumi.next.shared.ktor.KtorConfig
import org.easybangumi.next.shared.ktor.KtorFactory
import org.easybangumi.next.shared.source.api.utils.HttpHelper
import org.easybangumi.next.shared.source.api.utils.NetworkHelper

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
class HttpHelperImpl(
    ktorFactory: KtorFactory,
    private val networkHelper: NetworkHelper,
): HttpHelper {

    private val httpHelperConfig: KtorConfig by lazy {
        object : KtorConfig {
            override fun apply(config: HttpClientConfig<*>) {

            }
        }
    }

    private val client: HttpClient by lazy {
        ktorFactory.create(httpHelperConfig)
    }

    override suspend fun get(
        url: String,
        userAgent: String?,
        headers: Map<String, List<String>>
    ): HttpHelper.Response {
        val res= client.get {
            url(url)
            headers {
                headers.forEach { (key, values) ->
                    values.forEach { value ->
                        append(key, value)
                    }
                }
                if (userAgent != null) {
                    append("User-Agent", userAgent)
                } else {
                    append("User-Agent", networkHelper.randomUA)
                }
            }
        }
        val body = res.bodyAsText()
        return HttpHelper.Response(
            status = res.status.value,
            headers = res.headers.toMap(),
            body = body
        )
    }

    override suspend fun postJson(
        url: String,
        userAgent: String?,
        headers: Map<String, List<String>>,
        body: String
    ): HttpHelper.Response {
        val res= client.post {
            url(url)
            headers {
                headers.forEach { (key, values) ->
                    values.forEach { value ->
                        append(key, value)
                    }
                }
                if (userAgent != null) {
                    append("User-Agent", userAgent)
                } else {
                    append("User-Agent", networkHelper.randomUA)
                }
                append("Content-Type", "application/json")
            }
            setBody(body)
        }
        val resBody = res.bodyAsText()
        return HttpHelper.Response(
            status = res.status.value,
            headers = res.headers.toMap(),
            body = resBody
        )
    }

    override suspend fun postFormBody(
        url: String,
        userAgent: String?,
        headers: Map<String, List<String>>,
        body: Map<String, String>
    ): HttpHelper.Response {
        val res= client.post {
            url(url)
            headers {
                headers.forEach { (key, values) ->
                    values.forEach { value ->
                        append(key, value)
                    }
                }
                if (userAgent != null) {
                    append("User-Agent", userAgent)
                } else {
                    append("User-Agent", networkHelper.randomUA)
                }
                append("Content-Type", "application/x-www-form-urlencoded")
            }
            formData {
                body.forEach { (key, value) ->
                    append(key, value)
                }
            }
        }
        val resBody = res.bodyAsText()
        return HttpHelper.Response(
            status = res.status.value,
            headers = res.headers.toMap(),
            body = resBody
        )
    }
}