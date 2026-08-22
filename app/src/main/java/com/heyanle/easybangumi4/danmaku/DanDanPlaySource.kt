package com.heyanle.easybangumi4.danmaku

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Response
import okio.ByteString.Companion.encodeUtf8
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import kotlin.coroutines.resume

data class DanDanPlayCredentials(
    val appId: String,
    val appSecret: String,
) {
    val isConfigured: Boolean
        get() = appId.isNotBlank() && appSecret.isNotBlank()
}

/**
 * Built-in DanDanPlay adapter. It owns only API translation and signing; caches and the
 * current-playback cancellation policy live above this source.
 */
class DanDanPlaySource(
    private val credentials: DanDanPlayCredentials,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .build(),
) : DanmakuSource {

    override val metadata = DanmakuSourceMetadata(
        id = DANDANPLAY_SOURCE_ID,
        displayName = "弹弹play",
        attribution = "弹幕数据由弹弹play开放弹幕网络提供",
        website = "https://www.dandanplay.com",
    )

    override fun isAvailable(): Boolean = credentials.isConfigured

    override suspend fun searchBangumi(query: String): DanmakuResult<List<DanmakuBangumi>> {
        if (query.trim().length < 2) {
            return DanmakuResult.InvalidResponse("番名至少需要两个字符")
        }
        return requestJson(
            path = "/api/v2/search/anime",
            query = mapOf("keyword" to query.trim(), "v2" to "true"),
        ).mapPayload { json ->
            json.optJSONArray("animes").mapJson { item ->
                DanmakuBangumi(
                    remoteAnimeId = item.optLong("animeId"),
                    remoteBangumiId = item.optStringOrNull("bangumiId"),
                    title = item.optString("animeTitle"),
                    imageUrl = item.optStringOrNull("imageUrl"),
                    typeDescription = item.optStringOrNull("typeDescription"),
                )
            }.filter { it.remoteAnimeId != 0L && it.title.isNotBlank() }
        }
    }

    override suspend fun loadEpisodes(bangumi: DanmakuBangumi): DanmakuResult<List<DanmakuEpisode>> {
        val bangumiId = bangumi.remoteBangumiId ?: bangumi.remoteAnimeId.toString()
        return requestJson("/api/v2/bangumi/$bangumiId").mapPayload { json ->
            val bangumiPayload = json.optJSONObject("bangumi") ?: return@mapPayload emptyList()
            bangumiPayload.optJSONArray("episodes").mapJson { item ->
                DanmakuEpisode(
                    remoteEpisodeId = item.optLong("episodeId"),
                    remoteAnimeId = bangumi.remoteAnimeId,
                    remoteBangumiId = bangumi.remoteBangumiId,
                    bangumiTitle = bangumi.title,
                    episodeTitle = item.optString("episodeTitle"),
                    episodeNumber = item.optStringOrNull("episodeNumber"),
                )
            }.filter { it.remoteEpisodeId != 0L && it.episodeTitle.isNotBlank() }
        }
    }

    override suspend fun loadComments(remoteEpisodeId: Long): DanmakuResult<List<DanmakuComment>> {
        return requestJson(
            path = "/api/v2/comment/$remoteEpisodeId",
            query = mapOf("withRelated" to "true"),
        ).mapPayload { json ->
            json.optJSONArray("comments").mapJson { item ->
                item.toDanmakuComment()
            }.filterNotNull()
        }
    }

    private suspend fun requestJson(
        path: String,
        query: Map<String, String> = emptyMap(),
        method: String = "GET",
        body: String? = null,
    ): DanmakuResult<JSONObject> {
        if (!credentials.isConfigured) {
            return DanmakuResult.CredentialsMissing
        }

        val url = API_BASE_URL.toHttpUrl().newBuilder()
            .addPathSegments(path.removePrefix("/"))
            .apply { query.forEach { (key, value) -> addQueryParameter(key, value) } }
            .build()
        val timestamp = System.currentTimeMillis() / 1000L
        val signature = "${credentials.appId}$timestamp$path${credentials.appSecret}"
            .encodeUtf8()
            .sha256()
            .base64()
        val request = Request.Builder()
            .url(url)
            .header("X-AppId", credentials.appId)
            .header("X-Timestamp", timestamp.toString())
            .header("X-Signature", signature)
            .method(
                method,
                body?.toRequestBody(JSON_MEDIA_TYPE),
            )
            .build()

        return suspendCancellableCoroutine { continuation ->
            val call = client.newCall(request)
            continuation.invokeOnCancellation { call.cancel() }
            call.enqueue(object : Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    if (continuation.isActive) {
                        continuation.resume(DanmakuResult.Unavailable("无法连接弹弹play", e))
                    }
                }

                override fun onResponse(call: okhttp3.Call, response: Response) {
                    val result = runCatching {
                        response.use { currentResponse ->
                            val responseBody = currentResponse.body?.string().orEmpty()
                            when {
                                !currentResponse.isSuccessful -> DanmakuResult.Unavailable(
                                    message = "弹弹play请求失败（HTTP ${currentResponse.code}）",
                                )
                                else -> {
                                    val json = JSONObject(responseBody)
                                    if (json.has("success") && !json.optBoolean("success")) {
                                        DanmakuResult.Unavailable(
                                            message = json.optString("errorMessage", "弹弹play暂不可用"),
                                        )
                                    } else {
                                        DanmakuResult.Success(json)
                                    }
                                }
                            }
                        }
                    }.getOrElse {
                        DanmakuResult.InvalidResponse("弹弹play响应格式无效")
                    }
                    if (continuation.isActive) {
                        continuation.resume(result)
                    }
                }
            })
        }
    }

    private fun JSONObject.toDanmakuComment(): DanmakuComment? {
        val parameters = optString("p").split(',')
        if (parameters.size < 3) return null
        val timeMillis = (parameters[0].toDoubleOrNull()?.times(1000))?.toLong() ?: return null
        val mode = when (parameters[1].toIntOrNull()) {
            4 -> DanmakuDisplayMode.BOTTOM
            5 -> DanmakuDisplayMode.TOP
            else -> DanmakuDisplayMode.SCROLL
        }
        return DanmakuComment(
            id = optLong("cid"),
            timeMillis = timeMillis,
            mode = mode,
            colorArgb = parameters[2].toLongOrNull()?.toInt() ?: 0xFFFFFF,
            userId = parameters.getOrNull(3),
            text = optString("m"),
            provenance = DANDANPLAY_SOURCE_ID,
        )
    }

    private inline fun <T> DanmakuResult<JSONObject>.mapPayload(
        transform: (JSONObject) -> T,
    ): DanmakuResult<T> = when (this) {
        is DanmakuResult.Success -> runCatching { DanmakuResult.Success(transform(value), fromCache) }
            .getOrElse { DanmakuResult.InvalidResponse("弹弹play响应格式无效") }
        DanmakuResult.CredentialsMissing -> DanmakuResult.CredentialsMissing
        is DanmakuResult.Unavailable -> this
        is DanmakuResult.InvalidResponse -> this
        DanmakuResult.Stale -> DanmakuResult.Stale
    }

    private inline fun <T> JSONArray?.mapJson(transform: (JSONObject) -> T): List<T> {
        if (this == null) return emptyList()
        return buildList {
            for (index in 0 until length()) {
                optJSONObject(index)?.let { add(transform(it)) }
            }
        }
    }

    private fun JSONObject.optStringOrNull(key: String): String? = optString(key).takeIf(String::isNotBlank)

    private companion object {
        const val API_BASE_URL = "https://api.dandanplay.net/"
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}
