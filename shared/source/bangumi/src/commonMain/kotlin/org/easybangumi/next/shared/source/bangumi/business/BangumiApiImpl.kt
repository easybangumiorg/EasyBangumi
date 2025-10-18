package org.easybangumi.next.shared.source.bangumi.business

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.http.URLBuilder
import io.ktor.http.path
import kotlinx.coroutines.Deferred
import org.easybangumi.next.shared.source.bangumi.BangumiConfig
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

class BangumiApiImpl(
    private val caller: BangumiCaller,
    private val bangumiConfig: BangumiConfig
) : BangumiApi {

    interface BangumiCaller {
        var debugHookUrl: String?
        fun <T> request(block: suspend HttpClient.() -> BgmRsp<T>): Deferred<BgmRsp<T>>
    }

    // api host
    private fun HttpRequestBuilder.bgmUrl(block: URLBuilder.() -> Unit) {
        url {
            host = caller.debugHookUrl ?: bangumiConfig.bangumiApiHost
            block(this)
        }
    }

    // 嵌入代理 host
    private fun HttpRequestBuilder.proxyUrl(block: URLBuilder.() -> Unit) {
        url {
            host = caller.debugHookUrl ?: bangumiConfig.bangumiEmbedProxyHost
            block(this)
        }
    }

    // get subjects/{subjectId}
    override fun getSubject(subjectId: String): Deferred<BgmRsp<BgmSubject>> {
        return caller.request {
            get {
                bgmUrl {
                    path("v0", "subjects", subjectId)
                }
            }.body()
        }
    }

    // get calendar
    override fun calendar(): Deferred<BgmRsp<List<BgmCalendarItem>>> {
        return caller.request {
            get {
                bgmUrl {
                    path("calendar")
                }
            }.body()
        }
    }

    override fun getEpisodeList(
        subjectId: String,
        offset: Int,
        limit: Int
    ): Deferred<BgmRsp<BgmEpisodeRsp>> {
        return caller.request {
            get {
                bgmUrl {
                    path("v0", "episodes")
                    parameters.append("subject_id", subjectId)
                    parameters.append("offset", offset.toString())
                    parameters.append("limit", limit.toString())
                }
            }.body()
        }
    }

    override fun getPersonList(subjectId: String): Deferred<BgmRsp<List<BgmPerson>>> {
        return caller.request {
            get {
                bgmUrl {
                    path("v0", subjectId, "persons")
                    parameters.append("subject_id", subjectId)
                }
            }.body()
        }
    }

    override fun getCharacterList(subjectId: String): Deferred<BgmRsp<List<BgmCharacter>>> {
        return caller.request {
            get {
                bgmUrl {
                    path("v0", subjectId, "characters")
                    parameters.append("subject_id", subjectId)
                }
            }.body()
        }
    }

    override fun getTrends(page: Int, from: BangumiApi.TrendsFrom): Deferred<BgmRsp<List<BgmTrendsSubject>>> {
        return caller.request {
            get {
                proxyUrl {
                    path("trends")
                    parameters.append("page", page.toString())
                    parameters.append("from", from.path)
                }
            }.body()
        }
    }

    override fun getBanners(): Deferred<BgmRsp<List<BgmTrendsSubject>>> {
        return caller.request {
            get {
                proxyUrl {
                    path("banners")
                }
            }.body()
        }
    }

    override fun getComments(
        subjectId: String,
        page: Int
    ): Deferred<BgmRsp<List<BgmReviews>>> {
        return caller.request {
            get {
                proxyUrl {
                    path("comments")
                    parameters.append("subject_id", subjectId)
                    parameters.append("page", page.toString())
                }
            }.body()
        }
    }

    override fun search(keyword: String, page: Int): Deferred<BgmRsp<List<BgmTrendsSubject>>> {
        return caller.request {
            get {
                proxyUrl {
                    path("search")
                    parameters.append("keyword", keyword)
                    parameters.append("page", page.toString())
                }
            }.body()
        }
    }

    override fun coverUrl(
        subjectId: String,
        type: String
    ): String {
        return URLBuilder().run {
            host = bangumiConfig.bangumiApiHost
            path("v0", "subjects", subjectId, "image")
            parameters.append("subject_id", subjectId)
            parameters.append("type", type)
            buildString()
        }
    }
}