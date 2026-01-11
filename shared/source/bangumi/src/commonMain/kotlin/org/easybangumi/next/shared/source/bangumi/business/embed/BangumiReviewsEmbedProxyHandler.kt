package org.easybangumi.next.shared.source.bangumi.business.embed

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import io.ktor.client.plugins.api.TransformResponseBodyContext
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.URLProtocol
import io.ktor.http.path
import io.ktor.util.reflect.TypeInfo
import io.ktor.utils.io.ByteReadChannel
import org.easybangumi.next.shared.source.bangumi.BangumiConfig
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.shared.source.bangumi.model.BgmRsp
import org.easybangumi.next.shared.data.bangumi.BgmReviews

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
class BangumiReviewsEmbedProxyHandler(
    private val bangumiConfig: BangumiConfig,
): EmbedProxyHandler {

    private val logger = logger()

    override fun onReq(builder: HttpRequestBuilder): Boolean {
        val path = builder.url.pathSegments.firstOrNull()
        if (path == "comments") {
            val subjectId = builder.url.parameters["subject_id"]
                ?: throw  IllegalArgumentException("subject_id is required")
            val page = builder.url.parameters["page"] ?: "1"
            builder.url {
                protocol = URLProtocol.HTTPS
                host = bangumiConfig.bangumiHtmlHost
                // subject/509986/reviews/1.html
                path("subject", subjectId, "reviews", "$page.html")
                parameters.append("subjectId", subjectId)
                parameters.append("page", page)
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
        }else {
            val doc = Ksoup.parse(body)
            val list = doc.toCommendsList()
            logger.info("trends subject list size: ${list.size}")
            return BgmRsp.Success<List<BgmReviews>>(
                code = code,
                data = list,
                raw = body
            )
        }
    }

    private fun Document.toCommendsList(): List<BgmReviews> {
        val res = arrayListOf<BgmReviews>()
        val entryList = select("div#entry_list").firstOrNull() ?: return emptyList()
        val entryListChildren = entryList.children()
        for (i in entryListChildren.indices) {
            val child = entryListChildren[i]
            val cover = child.select("p.cover").firstOrNull()
            var img = cover?.select("img")?.firstOrNull()?.attr("src")
            if (img != null) {
                img = bangumiConfig.makeUrl(img)
            }
            val a = cover?.select("a")?.firstOrNull()
            val url = a?.attr("href") ?: ""
            val id = url.split("blog/").lastOrNull()
            val entry = child.select("div.entry").firstOrNull()
            val title = entry?.select("h2.title")?.firstOrNull()?.text()

            val divTime = entry?.select("div.time")?.firstOrNull()
            val authorA = divTime?.select("span.tip_j a")?.firstOrNull()
            val authorName = authorA?.text()
            val authorId = authorA?.attr("href")?.split("user/")?.lastOrNull()

            val time = divTime?.select("small.time")?.firstOrNull()?.text()
            var orange = entry?.select("small.orange")?.firstOrNull()?.text()
            orange = orange?.removePrefix("(+")
            orange = orange?.removeSuffix(")")
            val starCount = orange?.toIntOrNull()

            var divContent = entry?.select("div.content")?.firstOrNull()?.text()?.trim()
            divContent = divContent?.removeSuffix("(more)")

            val review = BgmReviews(
                id = id,
                title = title,
                authorId = authorId,
                author = authorName,
                date = time,
                starCount = starCount,
                cover = img,
                url = bangumiConfig.makeUrl(url),
                contentShort = divContent
            )
            res.add(review)
        }
        return res.toList()
    }
}