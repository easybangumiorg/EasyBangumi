package org.easybangumi.shared.plugin.bangumi.business

import kotlinx.coroutines.Deferred
import org.easybangumi.shared.plugin.bangumi.business.embed.BangumiEmbedProxy
import org.easybangumi.shared.plugin.bangumi.business.embed.BangumiRankingEmbedProxyHandler
import org.easybangumi.shared.plugin.bangumi.model.BgmRsp
import org.easybangumi.shared.plugin.bangumi.model.CalendarItem
import org.easybangumi.shared.plugin.bangumi.model.Subject
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

interface BangumiApi {
    // get subjects/{subjectId}
    fun getSubject(subjectId: String): Deferred<BgmRsp<Subject>>

    // get calendar
    fun calendar(): Deferred<BgmRsp<List<CalendarItem>>>

    enum class TrendsFrom(
        val path: String,
        val label: String,
    ) {
        ORIGINAL("原创", "原创"),
        MANGA("漫画改", "漫画改"),
        NOVEL("小说改", "小说改"),
    }
    // get bangumi.proxy/trends?page={page}&from={type}
    fun getTrends(page: Int, from: TrendsFrom): Deferred<BgmRsp<List<TrendsSubject>>>

    // get bangumi.proxy/banners
    fun getBanners(): Deferred<BgmRsp<List<TrendsSubject>>>
}