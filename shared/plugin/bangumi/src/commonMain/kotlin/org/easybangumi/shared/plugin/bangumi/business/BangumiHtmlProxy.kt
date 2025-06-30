package org.easybangumi.shared.plugin.bangumi.business

import com.fleeksoft.ksoup.Ksoup
import io.ktor.client.plugins.api.TransformResponseBodyContext
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.path
import io.ktor.util.AttributeKey
import io.ktor.util.reflect.TypeInfo
import io.ktor.utils.io.ByteReadChannel
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.shared.plugin.bangumi.model.BgmRsp
import org.easybangumi.shared.plugin.bangumi.model.TrendsSubject

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
class BangumiHtmlProxy(
    private val bangumiHtmlHost : String = "chii.in",
) {

    companion object {
        const val PROXY_HOST = "bangumi.proxy"
        val proxyRespAttrKey = AttributeKey<String>("proxyResp")


    }

    private val logger = logger()



    val reqHandlerMap = mapOf<String, (HttpRequestBuilder)->Unit>(
        "trends" to {
            // 热度排行榜
            // 转到网页 https://chii.in/anime/browser/?sort=trends&page=1
            val page = it.url.parameters.get("page")
            it.url {
                host = bangumiHtmlHost
                path("anime", "browser")
                parameters.set("page", page ?: "1")
                parameters.set("sort", "trends")
            }
        }
    )

    private fun transformRespError(
        code: Int,
        body: String,
    ): BgmRsp.Error<Any?> {
        return BgmRsp.Error<Any?>(
            code = code,
            title = "website error",
            description = "website error",
            details = "website error",
            raw = body,
        )
    }

    private interface RespHandler {
        suspend fun handleResponse(
            context: TransformResponseBodyContext,
            response: HttpResponse,
            content: ByteReadChannel,
            requestedType: TypeInfo
        ): Any?
    }

    private val respHandlerMap = mapOf<String, RespHandler>(
        "trends" to object: RespHandler {
            override suspend fun handleResponse(
                context: TransformResponseBodyContext,
                response: HttpResponse,
                content: ByteReadChannel,
                requestedType: TypeInfo
            ): Any? {
                // 热度排行榜
                // 转到网页 https://chii.in/anime/browser/?sort=trends&page=1
                val ktype = requestedType.kotlinType ?: return null
                if (ktype.classifier != BgmRsp::class) {
                    return null
                }
                val code = response.status.value
                val body = response.bodyAsText()

                if (code !in 200..299) {
                    return transformRespError(code, body)
                } else {
                    val doc = Ksoup.parse(body)
                    val ul = doc.select("div.section ul#browserItemList")
                    if (ul.isEmpty()) {
                        return BgmRsp.Success<List<TrendsSubject>>(code = code, data = emptyList(), raw = body)
                    }
                    val subjectList = arrayListOf<TrendsSubject>()
                    for (index in ul.indices) {
                        val li = ul.getOrNull(index) ?: continue
                        val inner = li.select("div.inner").firstOrNull() ?: continue
                        val a = inner.select("a.l").firstOrNull() ?: continue
                        val href = a.attr("href")
                        val small = inner.select("small.grey").firstOrNull()

                        val id = href.split("/").lastOrNull()?.toIntOrNull() ?: continue
                        val nameCN = a.text()
                        val name = small?.text()
                        val image = li.select("img.cover").firstOrNull()
                        var imageUrl = image?.attr("src")
                        if (imageUrl?.startsWith("//") == true) {
                            imageUrl = "https:$imageUrl"
                        } else if (imageUrl?.startsWith("/") == true) {
                            imageUrl = "https://$bangumiHtmlHost$imageUrl"
                        } else if (imageUrl?.startsWith("http") != true) {
                            imageUrl = "https://$bangumiHtmlHost/anime/browser/$imageUrl"
                        }

                        val rankSpan = inner.select("span.rank").firstOrNull()
                        val rank = rankSpan?.text()?.replace("Rank", "")?.trim()?.toIntOrNull()

                        val infoTipP = inner.select("p.info.tip").firstOrNull()
                        val infoTipText = infoTipP?.text()?.split("/")?.map { it.trim() } ?: emptyList()
                        val fadeSmall = inner.select("small.fade")?.firstOrNull()
                        val score = fadeSmall?.text()?.trim()?.toIntOrNull()

                        val tipJSpan = inner.select("span.tip_j").firstOrNull()
                        val scoreTotal = tipJSpan?.text()?.trim()?.replace("(", "")?.replace("人评分)", "")?.trim()?.toIntOrNull()
                        val trendsSubject = TrendsSubject(
                            id = id,
                            name = name,
                            nameCn = nameCN,
                            image = imageUrl,
                            info = infoTipText,
                            rank = rank,
                            score = score,
                            scoreTotal = scoreTotal
                        )
                        subjectList.add(trendsSubject)
                    }

                    return BgmRsp.Success<List<TrendsSubject>>(
                        code = code,
                        data = subjectList.toList(),
                        raw = body
                    )

                }

                return null
            }
        }
    )

    val bangumHtmlProxyPlugin = createClientPlugin("bangumi_html_proxy") {
        onRequest {   req, _ ->

            if (req.url.host.equals(PROXY_HOST)) {
                val path = req.url.pathSegments.firstOrNull()
                logger.info(path)
                if (path.isNullOrEmpty()) {
                    return@onRequest
                }

                reqHandlerMap[path]?.let {
                    it.invoke(req)
                    req.attributes.put(proxyRespAttrKey, path)
                }
            }
        }
        transformResponseBody {  response: HttpResponse,
                                content: ByteReadChannel,
                                requestedType: TypeInfo ->
            val proxyPath = response.request.attributes.getOrNull(proxyRespAttrKey)
            if (proxyPath.isNullOrEmpty()) {
                return@transformResponseBody null
            }
            respHandlerMap[proxyPath]?.let {
                return@transformResponseBody it.handleResponse(this, response, content, requestedType)
            }
            return@transformResponseBody null
        }
    }



}