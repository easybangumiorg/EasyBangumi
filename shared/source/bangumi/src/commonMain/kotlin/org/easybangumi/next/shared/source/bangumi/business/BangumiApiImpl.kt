package org.easybangumi.next.shared.source.bangumi.business

import coil3.toUri
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.http.path
import kotlinx.coroutines.Deferred
import org.easybangumi.next.shared.data.bangumi.AccessTokenInfo
import org.easybangumi.next.shared.source.bangumi.BangumiConfig
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

class BangumiApiImpl(
    private val caller: BangumiCaller,
    private val bangumiConfig: BangumiConfig,
) : BangumiApi {

    interface BangumiCaller {
        var debugHookUrl: String?
        fun <T> request(block: suspend HttpClient.() -> BgmRsp<T>): Deferred<BgmRsp<T>>
        fun <T> requestNormal(block: suspend HttpClient.() -> T): Deferred<Result<T>>
    }

    // api host
    private fun HttpRequestBuilder.bgmUrl(block: URLBuilder.() -> Unit) {
        url {
            protocol = URLProtocol.HTTPS
            host = caller.debugHookUrl ?: bangumiConfig.apiHost
            block(this)
        }
    }

    // 嵌入代理 host
    private fun HttpRequestBuilder.proxyUrl(block: URLBuilder.() -> Unit) {
        url {
            protocol = URLProtocol.HTTPS
            host = caller.debugHookUrl ?: bangumiConfig.embedProxyHost
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
                    path("v0", "subjects", subjectId, "persons")
                    parameters.append("subject_id", subjectId)
                }
            }.body()
        }
    }

    override fun getCharacterList(subjectId: String): Deferred<BgmRsp<List<BgmCharacter>>> {
        return caller.request {
            get {
                bgmUrl {
                    path("v0", "subjects", subjectId, "characters")
                    parameters.append("subject_id", subjectId)
                }
            }.body()
        }
    }

    override fun getMe(token: String): Deferred<BgmRsp<User>> {
        return caller.request {
            get {
                bgmUrl {
                    path("v0", "me")
                    headers.append("Authorization", "Bearer $token")
                }
            }.body()
        }
    }

    override fun getCollect(
        username: String,
        token: String,
        subjectId: String
    ): Deferred<BgmRsp<BgmCollect>> {
        return caller.request {
            get {
                bgmUrl {
                    path("v0", "users", username, "collections", subjectId)
                    headers.append("Authorization", "Bearer $token")
                }
            }.body()
        }
    }

    override fun getCollectList(
        username: String,
        token: String,
        type: Int,
        offset: Int,
        limit: Int
    ): Deferred<BgmRsp<BgmCollectRsp>> {
        return caller.request {
            get {
                bgmUrl {
                    path("v0", "users", username, "collections")
                    parameters.append("type", type.toString())
                    parameters.append("offset", offset.toString())
                    parameters.append("limit", limit.toString())
                    headers.append("Authorization", "Bearer $token")
                }
            }.body()
        }
    }

    override fun changeCollectType(username: String, token: String, subjectId: String, type: Int): Deferred<BgmRsp<String?>> {
        return caller.request {
            post {
                bgmUrl {
                    path("v0", "users", "-", "collections", subjectId)
                    headers.append("Authorization", "Bearer $token")
                }
                contentType(ContentType.Application.Json)
                setBody("{\"type\": $type}")
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
            protocol = URLProtocol.HTTPS
            host = bangumiConfig.apiHost
            path("v0", "subjects", subjectId, "image")
            parameters.append("subject_id", subjectId)
            parameters.append("type", type)
            buildString()
        }
    }

    override fun getLoginPageUrl(state: String): String {
        return URLBuilder().run {
            protocol = URLProtocol.HTTPS
            host = bangumiConfig.authApi
            path("oauth", "authorize")
            parameters.append("client_id", bangumiConfig.appId)
            parameters.append("response_type", "code")
            parameters.append("redirect_uri", bangumiConfig.callbackUrl)
            parameters.append("state", state)
            buildString()
        }
    }

    override fun onAuthUrlHook(url: String): String? {
        if (url.contains(bangumiConfig.callbackUrl)) {
            val uri = url.toUri()
            uri.query?.split("&")?.forEach { param ->
                val parts = param.split("=")
                if (parts.size == 2 && parts[0] == "code") {
                    return parts[1]
                }
            }
            return ""
        }
        return null
    }

    override fun getAccessToken(code: String): Deferred<Result<AccessTokenInfo>> {
        return caller.requestNormal {
            post {
                url {
                    protocol = URLProtocol.HTTPS
                    host = bangumiConfig.authApi
                    path("oauth", "access_token")
                }
                contentType(ContentType.Application.Json)
                setBody(
                    mapOf(
                        "client_id" to bangumiConfig.appId,
                        "client_secret" to bangumiConfig.appSecret,
                        "grant_type" to "authorization_code",
                        "code" to code,
                        "redirect_uri" to bangumiConfig.callbackUrl,
                    )
                )
            }.body()
        }
    }

    override fun refreshAccessToken(refreshToken: String): Deferred<Result<AccessTokenInfo>> {
        return caller.requestNormal {
            post {
                url {
                    protocol = URLProtocol.HTTPS
                    host = bangumiConfig.authApi
                    path("oauth", "access_token")
                }
                contentType(ContentType.Application.Json)
                setBody(
                    mapOf(
                        "grant_type" to "refresh_token",
                        "client_id" to bangumiConfig.appId,
                        "client_secret" to bangumiConfig.appSecret,
                        "refresh_token" to refreshToken,
                        "redirect_uri" to bangumiConfig.callbackUrl,
                    )
                )
            }.body()
        }
    }
}