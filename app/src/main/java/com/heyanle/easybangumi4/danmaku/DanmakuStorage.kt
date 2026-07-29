package com.heyanle.easybangumi4.danmaku

import com.heyanle.easybangumi4.base.json.JsonFileProvider

/**
 * File-backed persistence for bindings and cache entries. It is independent from the existing
 * Room schema, so no Room migration is needed for this feature.
 */
class DanmakuStorage(
    private val jsonFileProvider: JsonFileProvider,
) {
    fun binding(playbackKey: DanmakuPlaybackKey): DanmakuBinding? {
        return jsonFileProvider.danmakuBindings.getOrDef()
            .bindingFor(playbackKey)
    }

    fun saveBinding(binding: DanmakuBinding) {
        jsonFileProvider.danmakuBindings.update { bindings ->
            bindings.filterNot { it.playbackKey == binding.playbackKey } + binding
        }
    }

    fun removeBinding(playbackKey: DanmakuPlaybackKey) {
        jsonFileProvider.danmakuBindings.update { bindings ->
            bindings.filterNot { it.playbackKey == playbackKey }
        }
    }

    fun bangumiCache(sourceId: String, query: String, nowMillis: Long): DanmakuBangumiCacheEntry? {
        val cacheKey = "$sourceId:${query.trim().lowercase()}"
        return jsonFileProvider.danmakuBangumiCache.getOrDef()
            .firstOrNull { it.cacheKey == cacheKey && it.isValid(nowMillis) }
    }

    fun saveBangumiCache(entry: DanmakuBangumiCacheEntry) {
        jsonFileProvider.danmakuBangumiCache.update { entries ->
            entries.filterNot { it.cacheKey == entry.cacheKey } + entry
        }
    }

    fun episodeCache(sourceId: String, remoteAnimeId: Long, remoteBangumiId: String?, nowMillis: Long): DanmakuEpisodeCacheEntry? {
        val cacheKey = "$sourceId:${remoteBangumiId ?: remoteAnimeId}"
        return jsonFileProvider.danmakuEpisodeCache.getOrDef()
            .firstOrNull { it.cacheKey == cacheKey && it.isValid(nowMillis) }
    }

    fun saveEpisodeCache(entry: DanmakuEpisodeCacheEntry) {
        jsonFileProvider.danmakuEpisodeCache.update { entries ->
            entries.filterNot { it.cacheKey == entry.cacheKey } + entry
        }
    }

    fun commentCache(sourceId: String, remoteEpisodeId: Long, nowMillis: Long): DanmakuCommentCacheEntry? {
        val cacheKey = "$sourceId:$remoteEpisodeId"
        return jsonFileProvider.danmakuCommentCache.getOrDef()
            .firstOrNull { it.cacheKey == cacheKey && it.isValid(nowMillis) }
    }

    fun saveCommentCache(entry: DanmakuCommentCacheEntry) {
        jsonFileProvider.danmakuCommentCache.update { entries ->
            entries.filterNot { it.cacheKey == entry.cacheKey } + entry
        }
    }
}

internal fun List<DanmakuBinding>.bindingFor(playbackKey: DanmakuPlaybackKey): DanmakuBinding? {
    return firstOrNull { it.playbackKey == playbackKey }
}
