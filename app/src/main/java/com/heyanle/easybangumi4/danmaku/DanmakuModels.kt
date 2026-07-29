package com.heyanle.easybangumi4.danmaku

import com.squareup.moshi.Json

/** Stable identifiers and normalized data shared by source, persistence, UI, and renderer layers. */
const val DANDANPLAY_SOURCE_ID = "dandanplay"

data class DanmakuSourceMetadata(
    val id: String,
    val displayName: String,
    val attribution: String,
    val website: String,
    val isBuiltIn: Boolean = true,
    val isRemovable: Boolean = false,
)

/** Identifies the exact local playback episode that owns a remote danmaku binding. */
data class DanmakuPlaybackKey(
    val cartoonId: String,
    val cartoonSourceId: String,
    val playLineId: String,
    val episodeId: String,
) {
    val stableKey: String
        get() = listOf(cartoonId, cartoonSourceId, playLineId, episodeId).joinToString("|")
}

data class DanmakuBangumi(
    val remoteAnimeId: Long,
    val remoteBangumiId: String?,
    val title: String,
    val imageUrl: String? = null,
    val typeDescription: String? = null,
)

data class DanmakuEpisode(
    val remoteEpisodeId: Long,
    val remoteAnimeId: Long,
    val remoteBangumiId: String?,
    @Json(name = "animeTitle")
    val bangumiTitle: String,
    val episodeTitle: String,
    val episodeNumber: String? = null,
    val timeOffsetMillis: Long = 0L,
)

data class DanmakuBangumiMatch(
    val candidates: List<DanmakuBangumi>,
    val matchedBangumi: DanmakuBangumi?,
)

enum class DanmakuMatchOrigin {
    AUTOMATIC,
    MANUAL,
}

/**
 * Page-scoped selection of a remote bangumi. It intentionally lives separately from the
 * episode binding: changing playback targets reuses this selection and only rematches an episode.
 */
data class DanmakuBangumiSelection(
    val sourceId: String,
    val bangumi: DanmakuBangumi,
    val episodes: List<DanmakuEpisode>,
    val origin: DanmakuMatchOrigin,
)

data class DanmakuEpisodeMatchRequest(
    /** One-based position in the currently selected local sort order. */
    val sortedEpisodePosition: Int,
)

data class DanmakuEpisodeMatch(
    val episode: DanmakuEpisode?,
    val sortedEpisodePosition: Int,
)

fun DanmakuEpisodeMatch.matchedEpisodeOrNull(): DanmakuEpisode? {
    return episode
}

/**
 * A monotonically increasing playback-session token. The playback coordinator owns these tokens
 * and must discard any source result whose token is no longer current.
 */
data class DanmakuRequestSession(
    val playbackKey: DanmakuPlaybackKey,
    val generation: Long,
)

/** The durable binding schema. Storage adds timestamps but never changes this identity. */
data class DanmakuBinding(
    val playbackKey: DanmakuPlaybackKey,
    val sourceId: String,
    val remoteEpisodeId: Long,
    val remoteAnimeId: Long,
    val remoteBangumiId: String?,
    @Json(name = "animeTitle")
    val bangumiTitle: String,
    val episodeTitle: String,
    val timeOffsetMillis: Long,
    val origin: DanmakuMatchOrigin,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)

enum class DanmakuDisplayMode {
    SCROLL,
    BOTTOM,
    TOP,
}

data class DanmakuComment(
    val id: Long,
    val timeMillis: Long,
    val mode: DanmakuDisplayMode,
    val colorArgb: Int,
    val userId: String?,
    val text: String,
    /** DanDanPlay is the source; this field records any upstream provenance returned by it. */
    val provenance: String? = null,
)

data class DanmakuCommentCacheEntry(
    val sourceId: String,
    val remoteEpisodeId: Long,
    val createdAtMillis: Long,
    val expiresAtMillis: Long,
    val comments: List<DanmakuComment>,
) {
    val cacheKey: String
        get() = "$sourceId:$remoteEpisodeId"

    fun isValid(nowMillis: Long): Boolean = nowMillis < expiresAtMillis
}

data class DanmakuBangumiCacheEntry(
    val sourceId: String,
    val query: String,
    val createdAtMillis: Long,
    val expiresAtMillis: Long,
    @Json(name = "animes")
    val bangumis: List<DanmakuBangumi>,
) {
    val cacheKey: String
        get() = "$sourceId:${query.trim().lowercase()}"

    fun isValid(nowMillis: Long): Boolean = nowMillis < expiresAtMillis
}

data class DanmakuEpisodeCacheEntry(
    val sourceId: String,
    val remoteAnimeId: Long,
    val remoteBangumiId: String?,
    val createdAtMillis: Long,
    val expiresAtMillis: Long,
    val episodes: List<DanmakuEpisode>,
) {
    val cacheKey: String
        get() = "$sourceId:${remoteBangumiId ?: remoteAnimeId}"

    fun isValid(nowMillis: Long): Boolean = nowMillis < expiresAtMillis
}

/** Cache durations follow DanDanPlay's published guidance. */
object DanmakuCachePolicy {
    const val ACTIVE_SHOW_MILLIS = 60L * 60L * 1000L
    const val DEFAULT_MILLIS = 6L * 60L * 60L * 1000L
    const val ARCHIVE_MILLIS = 2L * 24L * 60L * 60L * 1000L

    fun ttlMillis(isActiveShow: Boolean, isArchivedShow: Boolean): Long = when {
        isActiveShow -> ACTIVE_SHOW_MILLIS
        isArchivedShow -> ARCHIVE_MILLIS
        else -> DEFAULT_MILLIS
    }
}

sealed interface DanmakuResult<out T> {
    data class Success<T>(val value: T, val fromCache: Boolean = false) : DanmakuResult<T>

    data object CredentialsMissing : DanmakuResult<Nothing>

    data class Unavailable(
        val message: String,
        val cause: Throwable? = null,
    ) : DanmakuResult<Nothing>

    data class InvalidResponse(val message: String) : DanmakuResult<Nothing>

    /** Internal completion state; callers must ignore it rather than altering current UI state. */
    data object Stale : DanmakuResult<Nothing>
}
