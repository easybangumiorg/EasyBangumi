package org.easybangumi.next.shared.source.bangumi.business.embed

import com.fleeksoft.ksoup.Ksoup
import io.ktor.client.plugins.api.TransformResponseBodyContext
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.path
import io.ktor.util.reflect.TypeInfo
import io.ktor.utils.io.ByteReadChannel
import org.easybangumi.next.shared.source.bangumi.BangumiConfig
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.shared.source.bangumi.model.BgmRsp
import org.easybangumi.next.shared.source.bangumi.model.BgmTrendsSubject

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
 *
 *    bangumi 列表页面相关
 *    1. bangumi.proxy/banners      首页 banner 使用 tv 热度排行
 *    2. bangumi.proxy/trends       首页 Tab 使用特定 类型（原创，漫画改，小说改） 的热度排行
 *    3. bangumi.proxy/search       纯搜索
 */

private val logger = logger("BangumiRankingHandler")

class BangumiRankingEmbedProxyHandler(
    private val bangumiConfig: BangumiConfig,
): EmbedProxyHandler {

    override fun onReq(builder: HttpRequestBuilder): Boolean {
        val path = builder.url.pathSegments.firstOrNull()
        if (path == "trends") {
            // 热度排行榜
            // 转到网页 https://chii.in/anime/browser/{from}?sort=trends&page={page}
            val page = builder.url.parameters.get("page")
            val from = builder.url.parameters.get("from")
            builder.url {
                host = bangumiConfig.bangumiHtmlHost
                if (from != null) {
                    path("anime", "browser", from)
                } else {
                    path("anime", "browser")
                }
                parameters.set("page", page ?: "1")
                parameters.set("sort", "trends")
            }
//            logger.info("trends url: ${builder.url}")
            return true
        } else if (path == "banners") {
            // 轮播图（tv 热度排行）
            // 转到网页 https://chii.in/anime/browser/tv/?sort=trends
            builder.url {
                host = bangumiConfig.bangumiHtmlHost
                path("anime", "browser", "tv")
                parameters.set("sort", "trends")
            }
//            logger.info("trends url: ${builder.url}")
            return true
        } else if (path == "search") {
            // 搜索
            // 转到网页 https://chii.in/subject_search/{keyword}?cat=2&page={page}
            val keyword = builder.url.parameters.get("keyword")
                ?: throw  IllegalArgumentException("keyword is required")
            val page = builder.url.parameters.get("page")
            builder.url {
                host = bangumiConfig.bangumiHtmlHost
                path("subject_search", keyword)
                parameters.set("cat", "2")
                parameters.set("page", page ?: "1")
            }
            return true
        }
        return false
    }

    override suspend fun onResp(
        context: TransformResponseBodyContext,
        response: HttpResponse,
        content: ByteReadChannel,
        requestedType: TypeInfo
    ): Any? {
        // 热度排行榜
        val ktype = requestedType.kotlinType ?: return null
        if (ktype.classifier != BgmRsp::class) {
            return null
        }
        val code = response.status.value
        val body = response.bodyAsText()

        if (code !in 200..299) {
            return BgmRsp.Error<Any?>(
                code = code,
                title = "website error",
                description = "website error",
                details = "website error",
                isProxy = true,
                raw = body,
            )
        } else {
            val doc = Ksoup.parse(body)
            val list = doc.toTrendsSubjectList(bangumiConfig)
//            logger.info("trends subject list size: ${list.size}")
            return BgmRsp.Success<List<BgmTrendsSubject>>(
                code = code,
                data = list,
                raw = body,
            )
        }
    }



}


