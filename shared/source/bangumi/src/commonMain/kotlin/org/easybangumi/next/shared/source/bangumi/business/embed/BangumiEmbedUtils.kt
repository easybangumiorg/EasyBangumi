package org.easybangumi.next.shared.source.bangumi.business.embed

import com.fleeksoft.ksoup.nodes.Document
import io.ktor.http.URLBuilder
import io.ktor.http.path
import org.easybangumi.next.shared.source.bangumi.BangumiConfig
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
 */
fun Document.toTrendsSubjectList(
    bangumiConfig: BangumiConfig
): List<BgmTrendsSubject> {
    val ul = select("div ul#browserItemList").firstOrNull()
    if (ul == null) {
        return emptyList()
    }
    val ulList = ul.children()
    if (ulList.isEmpty()) {
        return emptyList()
    }
    val subjectList = arrayListOf<BgmTrendsSubject>()
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
        val imageUrl = URLBuilder().run {
            host = bangumiConfig.bangumiApiHost
            path("v0", "subjects", id.toString(), "image")
            parameters.set("subject_id", id.toString())
            parameters.set("type", "common")
            toString()
        }
//            logger.info("imageUrl: $imageUrl")


        val jumpUrl = bangumiConfig.makeUrl(href)

        val rankSpan = inner.select("span.rank").firstOrNull()
        val rank = rankSpan?.text()?.replace("Rank", "")?.trim()?.toIntOrNull()

        val infoTipP = inner.select("p.info.tip").firstOrNull()
        val infoTipText = infoTipP?.text()?.split("/")?.map { it.trim() } ?: emptyList()
        val fadeSmall = inner.select("small.fade")?.firstOrNull()
        val score = fadeSmall?.text()?.trim()?.toIntOrNull()

        val tipJSpan = inner.select("span.tip_j").firstOrNull()
        val scoreTotal = tipJSpan?.text()?.trim()?.replace("(", "")?.replace("人评分)", "")?.trim()?.toIntOrNull()
        val bgmTrendsSubject = BgmTrendsSubject(
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
        subjectList.add(bgmTrendsSubject)
    }
    return subjectList.toList()
}