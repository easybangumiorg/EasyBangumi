package org.easybangumi.shared.plugin.bangumi.business

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.api.*
import io.ktor.client.statement.*
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
import org.easybangumi.shared.plugin.bangumi.model.BgmNetException
import org.easybangumi.shared.plugin.bangumi.model.BgmRsp
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
    private val bangumiApiHost: String = "api.bgm.tv",
    private val bangumiHtmlHost: String = "chii.in",
): BangumiApiImpl.BangumiCaller {

    private val logger = logger()

    override var hookDebugUrl: String? = null

    private val dispatcher = coroutineProvider.io()
    private val scope = CoroutineScope(SupervisorJob() + dispatcher + CoroutineName("BangumiHttp"))
    private val userAgen = "org.easybangumi/EasyBangumi/${platformInformation.versionName} (${platformInformation.platformName}) (https://github.com/easybangumiorg/EasyBangumi)"

    private val proxy = BangumiHtmlProxy(bangumiHtmlHost)
    @OptIn(InternalSerializationApi::class)
    private val ktorBangumiPlugin by lazy {
        createClientPlugin("bangumi") {
            onRequest {  req, _ ->
                req.headers["User-Agent"] = userAgen
            }
            transformResponseBody { response: HttpResponse,
                                    content: ByteReadChannel,
                                    requestedType: TypeInfo ->
//                val proxyPath = response.request.attributes.getOrNull(BangumiHtmlProxy.proxyRespAttrKey)
//                if (proxyPath.isNotEmpty()) {
//                    return@transformResponseBody null
//                }
                val ktype = requestedType.kotlinType ?: return@transformResponseBody null
                if (ktype.classifier != BgmRsp::class) {
                    return@transformResponseBody null
                }
                val code = response.status.value
                val body = response.bodyAsText()
                if (code !in 200..299) {
                    val jsonObj = Json.parseToJsonElement(body)
                    return@transformResponseBody BgmRsp.Error<Any?>(
                        code = code,
                        title = runCatching { jsonObj.jsonObject["title"]?.toString() }.getOrNull(),
                        description = runCatching { jsonObj.jsonObject["message"]?.toString() }.getOrNull(),
                        details = runCatching { jsonObj.jsonObject["details"]?.toString() }.getOrNull(),
                        raw = body,
                    )
                } else {
                    val genericTypes: List<KTypeProjection>? = requestedType.kotlinType?.arguments
                    val firstGenericType: KType? = genericTypes?.getOrNull(0)?.type
                    val genericClazz = firstGenericType?.classifier as? KClass<*> ?: return@transformResponseBody null
                    val data = Json.decodeFromString(genericClazz.serializer(), body)
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
                    handleResponseException { cause, _ ->
                        throw BgmNetException(code = BgmRsp.Error.INNER_ERROR_CODE, cause = cause)
                    }
                }
            }
        }
    }
    private val httpClient: HttpClient by lazy {
        ktorFactory.create(ktorConfig)
    }


    override fun <T> request(block: suspend HttpClient.() -> BgmRsp<T>): Deferred<BgmRsp<T>> {
        return scope.async {
            try {
                httpClient.block()
            } catch (e: BgmNetException) {
                e.rsp as BgmRsp<T>
            }
        }
    }

    val api: BangumiApi by lazy {
        BangumiApiImpl(this, bangumiApiHost)
    }


}