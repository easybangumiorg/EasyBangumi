package org.easybangumi.next.shared.source.bangumi.business

import kotlinx.coroutines.Deferred
import org.easybangumi.next.shared.source.bangumi.business.embed.BangumiEmbedProxy
import org.easybangumi.next.shared.source.bangumi.business.embed.BangumiRankingEmbedProxyHandler
import org.easybangumi.next.shared.source.bangumi.model.BgmRsp
import org.easybangumi.next.shared.source.bangumi.model.CalendarItem
import org.easybangumi.next.shared.source.bangumi.model.Character
import org.easybangumi.next.shared.source.bangumi.model.EpisodeRsp
import org.easybangumi.next.shared.source.bangumi.model.Person
import org.easybangumi.next.shared.source.bangumi.model.Reviews
import org.easybangumi.next.shared.source.bangumi.model.Subject
import org.easybangumi.next.shared.source.bangumi.model.TrendsSubject

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
    // ====== Bangumi api v0 =====

    // get v0/subjects/{subjectId}
    fun getSubject(subjectId: String): Deferred<BgmRsp<Subject>>

    // get calendar
    fun calendar(): Deferred<BgmRsp<List<CalendarItem>>>

    // get v0/episodes?subject_id={subjectId}&offset={offset}&limit={limit}
    fun getEpisodeList(
        subjectId: String,
        offset: Int = 0,
        limit: Int = 100,
    ): Deferred<BgmRsp<EpisodeRsp>>

    // get v0/subjects/{subjectId}/persons?subject_id={subjectId}
    fun getPersonList(
        subjectId: String,
    ): Deferred<BgmRsp<List<Person>>>

    // get v0/subjects/{subjectId}/characters?subject_id={subjectId}
    fun getCharacterList(
        subjectId: String,
    ): Deferred<BgmRsp<List<Character>>>


    // ====== embed proxy api =====

    enum class TrendsFrom(
        val path: String,
        val label: String,
    ) {
        ORIGINAL("原创", "原创"),
        MANGA("漫画改", "漫画改"),
        NOVEL("小说改", "小说改"),
    }
    // get bangumi.proxy/trends?page={page}&from={type}
    fun getTrends(
        page: Int,
        from: TrendsFrom,
    ): Deferred<BgmRsp<List<TrendsSubject>>>

    // get bangumi.proxy/banners
    fun getBanners(): Deferred<BgmRsp<List<TrendsSubject>>>

    // get bangumi.proxy/comments?subject_id={subjectId}&page={page}
    fun getComments(
        subjectId: String,
        page: Int,
    ): Deferred<BgmRsp<List<Reviews>>>

    fun coverUrl(
        subjectId: String,
        type: String = "large",
    ): String


}