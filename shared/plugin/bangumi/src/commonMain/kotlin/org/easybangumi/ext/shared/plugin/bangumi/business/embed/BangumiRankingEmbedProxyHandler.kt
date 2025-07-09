package org.easybangumi.ext.shared.plugin.bangumi.business.embed

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import io.ktor.client.plugins.api.TransformResponseBodyContext
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.URLBuilder
import io.ktor.http.path
import io.ktor.util.reflect.TypeInfo
import io.ktor.utils.io.ByteReadChannel
import org.easybangumi.ext.shared.plugin.bangumi.business.BangumiConfig
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.ext.shared.plugin.bangumi.model.BgmRsp
import org.easybangumi.ext.shared.plugin.bangumi.model.TrendsSubject

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
 *    bangumi 排行榜页面相关
 *    1. bangumi.proxy/banners      首页 banner 使用 tv 热度排行
 *    2. bangumi.proxy/trends       首页 Tab 使用特定 类型（原创，漫画改，小说改） 的热度排行
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
            logger.info("trends url: ${builder.url}")
            return true
        } else if (path == "banners") {
            // 轮播图（tv 热度排行）
            // 转到网页 https://chii.in/anime/browser/tv/?sort=trends
            builder.url {
                host = bangumiConfig.bangumiHtmlHost
                path("anime", "browser", "tv")
                parameters.set("sort", "trends")
            }
            logger.info("trends url: ${builder.url}")
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
                raw = body,
            )
        } else {
            val doc = Ksoup.parse(body)
            val list = doc.toTrendsSubjectList()
            logger.info("trends subject list size: ${list.size}")
            return BgmRsp.Success<List<TrendsSubject>>(
                code = code,
                data = list,
                raw = body
            )
        }
        return null
    }

    private fun Document.toTrendsSubjectList(): List<TrendsSubject> {
        val ul = select("div.section ul#browserItemList").firstOrNull()
        if (ul == null) {
            return emptyList()
        }
        val ulList = ul.children()
        if (ulList.isEmpty()) {
            return emptyList()
        }
        val subjectList = arrayListOf<TrendsSubject>()
        for (index in ulList.indices) {
            val li = ulList.getOrNull(index) ?: continue
            val inner = li.select("div.inner").firstOrNull() ?: continue
            val a = inner.select("a.l").firstOrNull() ?: continue
            val href = a.attr("href")
            val small = inner.select("small.grey").firstOrNull()

            val id = href.split("/").lastOrNull()?.toIntOrNull() ?: continue
            val nameCN = a.text()
            val name = small?.text()
            val image = li.select("img.cover").firstOrNull()
//            var imageUrl = image?.attr("src")
//            if (imageUrl?.startsWith("//") == true) {
//                imageUrl = "https:$imageUrl"
//            } else if (imageUrl?.startsWith("/") == true) {
//                imageUrl = "https://$bangumiHtmlHost$imageUrl"
//            }
//
//            if (imageUrl?.contains("/pic/cover/c") == true) {
//                imageUrl = imageUrl.replace("/pic/cover/c", "/r/400/pic/cover/l")
//            }
            val imageUrl = URLBuilder().run {
                host = bangumiConfig.bangumiApiHost
                path("v0", "subjects", id.toString(), "image")
                parameters.set("subject_id", id.toString())
                parameters.set("type", "large")
                toString()
            }
            logger.info("imageUrl: $imageUrl")


            val jumpUrl = bangumiConfig.makeUrl(href)

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
                scoreTotal = scoreTotal,
                jumpUrl = jumpUrl
            )
            subjectList.add(trendsSubject)
        }
        return subjectList.toList()
    }
}


