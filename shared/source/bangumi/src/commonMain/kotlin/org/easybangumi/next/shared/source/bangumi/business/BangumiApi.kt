package org.easybangumi.next.shared.source.bangumi.business

import kotlinx.coroutines.Deferred
import org.easybangumi.next.shared.source.bangumi.model.BgmRsp
import org.easybangumi.next.shared.source.bangumi.model.BgmCalendarItem
import org.easybangumi.next.shared.source.bangumi.model.BgmCharacter
import org.easybangumi.next.shared.source.bangumi.model.BgmEpisodeRsp
import org.easybangumi.next.shared.source.bangumi.model.BgmPerson
import org.easybangumi.next.shared.source.bangumi.model.BgmReviews
import org.easybangumi.next.shared.source.bangumi.model.BgmSubject
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

interface BangumiApi {
    // ====== Bangumi api v0 =====

    // get v0/subjects/{subjectId}
    fun getSubject(subjectId: String): Deferred<BgmRsp<BgmSubject>>

    // get calendar
    fun calendar(): Deferred<BgmRsp<List<BgmCalendarItem>>>

    // get v0/episodes?subject_id={subjectId}&offset={offset}&limit={limit}
    fun getEpisodeList(
        subjectId: String,
        offset: Int = 0,
        limit: Int = 100,
    ): Deferred<BgmRsp<BgmEpisodeRsp>>

    // get v0/subjects/{subjectId}/persons?subject_id={subjectId}
    fun getPersonList(
        subjectId: String,
    ): Deferred<BgmRsp<List<BgmPerson>>>

    // get v0/subjects/{subjectId}/characters?subject_id={subjectId}
    fun getCharacterList(
        subjectId: String,
    ): Deferred<BgmRsp<List<BgmCharacter>>>


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
    ): Deferred<BgmRsp<List<BgmTrendsSubject>>>

    // get bangumi.proxy/banners
    fun getBanners(): Deferred<BgmRsp<List<BgmTrendsSubject>>>

    // get bangumi.proxy/comments?subject_id={subjectId}&page={page}
    fun getComments(
        subjectId: String,
        page: Int,
    ): Deferred<BgmRsp<List<BgmReviews>>>

    fun search(
        keyword: String,
        page: Int = 1,
    ): Deferred<BgmRsp<List<BgmTrendsSubject>>>

    fun coverUrl(
        subjectId: String,
        type: String = "large",
    ): String


}