package org.easybangumi.ext.shared.plugin.bangumi.business

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.http.URLBuilder
import io.ktor.http.path
import kotlinx.coroutines.Deferred
import org.easybangumi.ext.shared.plugin.bangumi.model.BgmRsp
import org.easybangumi.ext.shared.plugin.bangumi.model.CalendarItem
import org.easybangumi.ext.shared.plugin.bangumi.model.Character
import org.easybangumi.ext.shared.plugin.bangumi.model.EpisodeRsp
import org.easybangumi.ext.shared.plugin.bangumi.model.Person
import org.easybangumi.ext.shared.plugin.bangumi.model.Reviews
import org.easybangumi.ext.shared.plugin.bangumi.model.Subject
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
    override fun getSubject(subjectId: String): Deferred<BgmRsp<Subject>> {
        return caller.request {
            get {
                bgmUrl {
                    path("v0", "subjects", subjectId)
                }
            }.body()
        }
    }

    // get calendar
    override fun calendar(): Deferred<BgmRsp<List<CalendarItem>>> {
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
    ): Deferred<BgmRsp<EpisodeRsp>> {
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

    override fun getPersonList(subjectId: String): Deferred<BgmRsp<List<Person>>> {
        return caller.request {
            get {
                bgmUrl {
                    path("v0", subjectId, "persons")
                    parameters.append("subject_id", subjectId)
                }
            }.body()
        }
    }

    override fun getCharacterList(subjectId: String): Deferred<BgmRsp<List<Character>>> {
        return caller.request {
            get {
                bgmUrl {
                    path("v0", subjectId, "characters")
                    parameters.append("subject_id", subjectId)
                }
            }.body()
        }
    }

    override fun getTrends(page: Int, from: BangumiApi.TrendsFrom): Deferred<BgmRsp<List<TrendsSubject>>> {
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

    override fun getBanners(): Deferred<BgmRsp<List<TrendsSubject>>> {
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
    ): Deferred<BgmRsp<List<Reviews>>> {
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