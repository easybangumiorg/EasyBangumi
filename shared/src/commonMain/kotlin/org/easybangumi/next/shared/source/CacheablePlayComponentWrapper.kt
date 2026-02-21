package org.easybangumi.next.shared.source

import kotlinx.serialization.Serializable
import org.easybangumi.next.lib.serialization.deserialize
import org.easybangumi.next.lib.serialization.jsonSerializer
import org.easybangumi.next.lib.serialization.serialize
import org.easybangumi.next.lib.store.JournalMapHelper
import org.easybangumi.next.lib.unifile.UFD
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.pathProvider
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.data.cartoon.Episode
import org.easybangumi.next.shared.data.cartoon.PlayInfo
import org.easybangumi.next.shared.data.cartoon.PlayerLine
import org.easybangumi.next.shared.source.api.component.play.IPlayComponent
import org.easybangumi.next.shared.source.api.component.play.PlayComponent


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

class CacheablePlayComponentWrapper (
    private val playComponent: PlayComponent,
) {
    val cacheUfd = pathProvider.getCachePath("source")
    val tempMap = JournalMapHelper(cacheUfd, playComponent.source.key + playComponent.source.manifest.version)

    @Serializable
    data class PlayInfoCacheItem(
        val cartoonIndex: CartoonIndex,
        val playerLine: PlayerLine,
        val episode: Episode,
        val playInfo: PlayInfo,
    ) {
        fun isFrom(
            cartoonIndex: CartoonIndex,
            playerLine: PlayerLine,
            episode: Episode,
        ): Boolean {
            return this.cartoonIndex == cartoonIndex && this.playerLine == playerLine && this.episode == episode
        }
    }

    suspend fun getPlayInfo(
        cartoonIndex: CartoonIndex,
        playerLine: PlayerLine,
        episode: Episode,
        cache: Boolean,
    ): DataState<PlayInfo> {
        val key = getKey(cartoonIndex, playerLine, episode)
        if (cache) {

            val cache = tempMap.get(key)
            if (cache.isNotEmpty()) {
                val cacheItem = runCatching { jsonSerializer.deserialize<PlayInfoCacheItem>(cache, null) }.getOrNull()
                if (cacheItem != null && cacheItem.isFrom(cartoonIndex, playerLine, episode)) {
                    return DataState.ok(cacheItem.playInfo, true)
                }
            }
        }
        return playComponent.getPlayInfo(cartoonIndex, playerLine, episode).apply {
            if (this is DataState.Ok) {
                val cacheItem = PlayInfoCacheItem(cartoonIndex, playerLine, episode, this.data)
                tempMap.put(key, jsonSerializer.serialize(cacheItem))
            }
        }
    }

    fun getKey(
        cartoonIndex: CartoonIndex,
        playerLine: PlayerLine,
        episode: Episode,
    ): String {
        return "$cartoonIndex|${playerLine.id}|${episode.id}"

    }
}

fun PlayComponent.cacheable(): CacheablePlayComponentWrapper {
    return this.source.extMap.getOrPut(this) {
        CacheablePlayComponentWrapper(this)
    } as CacheablePlayComponentWrapper
}