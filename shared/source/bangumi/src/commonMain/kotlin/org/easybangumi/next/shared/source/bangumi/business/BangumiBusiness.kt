package org.easybangumi.next.shared.source.bangumi.business

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.api.*
import io.ktor.client.statement.*
import io.ktor.http.URLBuilder
import io.ktor.http.path
import io.ktor.util.reflect.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.serializer
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.utils.coroutineProvider
import org.easybangumi.next.platformInformation
import org.easybangumi.next.shared.ktor.KtorConfig
import org.easybangumi.next.shared.ktor.KtorFactory
import org.easybangumi.next.shared.source.bangumi.BangumiAppConfig
import org.easybangumi.next.shared.source.bangumi.BangumiConfig
import org.easybangumi.next.shared.source.bangumi.business.embed.BangumiEmbedProxy
import org.easybangumi.next.shared.source.bangumi.model.BgmNetException
import org.easybangumi.next.shared.source.bangumi.model.BgmRsp
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection

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

class BangumiBusiness(
    ktorFactory: KtorFactory,
    private val bangumiConfig: BangumiConfig,
) {


    private val logger = logger()

    // 不为空时所有请求都会改成这个 url
    var debugHookUrl: String?
        get() = bangumiCaller.debugHookUrl
        set(value) {
            bangumiCaller.debugHookUrl = value
        }

    private val dispatcher = coroutineProvider.io()
    private val scope = CoroutineScope(SupervisorJob() + dispatcher + CoroutineName("BangumiHttp"))
    private val defaultUserAgent = "EasyBangumi/${platformInformation.versionName} (${platformInformation.platformName}) (https://github.com/easybangumiorg/EasyBangumi)"

    init {
        logger.info("BangumiBusiness initialized with config: $bangumiConfig userAgent: $defaultUserAgent")
    }
    private val proxy = BangumiEmbedProxy(config = bangumiConfig)

    private val json = Json {
        ignoreUnknownKeys = !platformInformation.isDebug
    }

    @OptIn(InternalSerializationApi::class)
    private val ktorBangumiPlugin by lazy {
        createClientPlugin("bangumi") {
            onRequest {  req, _ ->
                if (!req.headers.contains("User-Agent")) {
                    req.headers["User-Agent"] = defaultUserAgent
                }
            }
            transformResponseBody { response: HttpResponse,
                                    content: ByteReadChannel,
                                    requestedType: TypeInfo ->
                val type = requestedType.kotlinType ?: return@transformResponseBody null
                if (type.classifier != BgmRsp::class) {
                    return@transformResponseBody null
                }
                val code = response.status.value
                val body = response.bodyAsText()
                if (code == 401) {

                }
                if (code !in 200..299) {
                    val jsonObj = runCatching {
                        json.parseToJsonElement(body)
                    }.getOrNull()
                    return@transformResponseBody BgmRsp.Error<Any?>(
                        code = code,
                        title = runCatching { jsonObj?.jsonObject["title"]?.toString() }.getOrNull(),
                        description = runCatching { jsonObj?.jsonObject["message"]?.toString() }.getOrNull(),
                        details = runCatching { jsonObj?.jsonObject["details"]?.toString() }.getOrNull(),
                        raw = body,
                    )
                } else {
                    val genericTypes: List<KTypeProjection>? = requestedType.kotlinType?.arguments
                    val firstGenericType: KType? = genericTypes?.getOrNull(0)?.type

                    val genericType = firstGenericType ?: return@transformResponseBody null
                    val kSerializer = serializer(genericType)
                    val data = runCatching {
                        json.decodeFromString(kSerializer, body)
                    }.getOrElse {
                        it.printStackTrace()
                        null
                    }
                    if (!genericType.isMarkedNullable && data == null ) {
                        return@transformResponseBody null
                    }

                    return@transformResponseBody BgmRsp.Success(
                        code = code,
                        data = data,
                        raw = body,
                    )
                }
            }

        }
    }
    private val ktorConfig: KtorConfig by lazy {
        object : KtorConfig {
            override fun apply(config: HttpClientConfig<*>) {
                config.install(proxy.bangumHtmlProxyPlugin)
                config.install(ktorBangumiPlugin)
                config.HttpResponseValidator {
                    handleResponseException { cause, req ->
                        cause.printStackTrace()
                        throw BgmNetException(code = BgmRsp.Error.INNER_ERROR_CODE, url = req.url.toString(), netCause = cause)
                    }
                }
            }
        }
    }
    private val httpClient: HttpClient by lazy {
        ktorFactory.create(ktorConfig)
    }

    private val bangumiCaller = object: BangumiApiImpl.BangumiCaller {
        override var debugHookUrl: String? = null

        override fun <T> request(block: suspend HttpClient.() -> BgmRsp<T>): Deferred<BgmRsp<T>> {
            return scope.async {
                try {
                    httpClient.block()
                } catch (e: BgmNetException) {
                    e.printStackTrace()
                    e.rsp as BgmRsp<T>
                } catch (e: Exception) {
                    e.printStackTrace()
                    BgmRsp.Error<T>(
                        code = BgmRsp.Error.INNER_ERROR_CODE,
                        title = "网络错误",
                        description = e.message ?: "未知错误",
                        throwable = e
                    ) as BgmRsp<T>
                }
            }
        }

        override fun <T> requestNormal(block: suspend HttpClient.() -> T): Deferred<Result<T>> {
            return scope.async {
                runCatching {
                    httpClient.block()
                }
            }
        }
    }


    val api: BangumiApi by lazy {
        BangumiApiImpl(bangumiCaller, bangumiConfig)
    }



}