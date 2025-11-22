package org.easybangumi.next.shared.source.bangumi.business

import kotlinx.coroutines.Deferred
import org.easybangumi.next.shared.source.bangumi.model.BgmRsp
import org.easybangumi.next.shared.data.bangumi.BgmCalendarItem
import org.easybangumi.next.shared.data.bangumi.BgmCharacter
import org.easybangumi.next.shared.data.bangumi.BgmCollect
import org.easybangumi.next.shared.data.bangumi.BgmCollectRsp
import org.easybangumi.next.shared.data.bangumi.BgmEpisodeRsp
import org.easybangumi.next.shared.data.bangumi.BgmPerson
import org.easybangumi.next.shared.data.bangumi.BgmReviews
import org.easybangumi.next.shared.data.bangumi.BgmSubject
import org.easybangumi.next.shared.data.bangumi.BgmTrendsSubject
import org.easybangumi.next.shared.data.bangumi.User

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

    // get v0/me
    // header: Authorization: Bearer {token}
    fun getMe(
        token: String
    ): Deferred<BgmRsp<User>>

    // get /v0/users/{username}/collections/{subject_id}
    // header: Authorization: Bearer {token}
    fun getCollect(
        username: String,
        token: String,
        subjectId: String,
    ): Deferred<BgmRsp<BgmCollect>>

    // only quire anim type
    // get /v0/users/{username}/collections?type={type}
    // header: Authorization: Bearer {token}
    fun getCollectList(
        username: String,
        token: String,
        type: Int,
        offset: Int = 0,
        limit: Int = 100,
    ): Deferred<BgmRsp<BgmCollectRsp>>

    // only change type
    // post /v0/users/{username}/collections/{subject_id}
    fun changeCollectType(
        username: String,
        token: String,
        subjectId: String,
        type: Int,
    ): Deferred<BgmRsp<String?>>


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

    // get bangumi.proxy/search?keyword={keyword}&page={page}
    fun search(
        keyword: String,
        page: Int = 1,
    ): Deferred<BgmRsp<List<BgmTrendsSubject>>>

    fun coverUrl(
        subjectId: String,
        type: String = "large",
    ): String


}