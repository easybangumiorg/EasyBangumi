package org.easybangumi.next.shared.source.api.component.play

import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.shared.source.api.component.Component
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.data.cartoon.Episode
import org.easybangumi.next.shared.data.cartoon.EpisodeSimple
import org.easybangumi.next.shared.data.cartoon.PlayInfo
import org.easybangumi.next.shared.data.cartoon.PlayLineSimple
import org.easybangumi.next.shared.data.cartoon.PlayerLine

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
interface IPlayComponent {

    // 搜索播放线路
    suspend fun getPlayLines(
        cartoonIndex: CartoonIndex
    ): DataState<List<PlayerLine>>



    suspend fun getPlayInfo(
        cartoonIndex: CartoonIndex,
        playerLine: PlayerLine,
        episode: Episode,
    ): DataState<PlayInfo>

    suspend fun getPlayInfoWithCheck(
        cartoonIndex: CartoonIndex,
        playerLine: PlayerLine,
        episode: Episode,
    ): DataState<PlayInfo> {
        return DataState.error("unsupported get play info with check")
    }

    // ═══════════════════════════════════════════════════════════════════
    // 剧集优先模式（可选实现）
    // ═══════════════════════════════════════════════════════════════════

    /**
     * 判断是否为剧集优先模式，默认返回 false（线路优先模式）
     * 无需重试逻辑
     */
    suspend fun isEpisodeFirstMode(cartoonIndex: CartoonIndex): Boolean = false

    /**
     * 获取剧集列表（剧集优先模式专用）
     * 需要重试逻辑
     */
    suspend fun getEpisodeList(
        cartoonIndex: CartoonIndex
    ): DataState<List<EpisodeSimple>>? = null

    /**
     * 根据剧集获取播放线路（剧集优先模式专用）
     * 需要重试逻辑
     */
    suspend fun getPlayLineSimpleForEpisode(
        cartoonIndex: CartoonIndex,
        episode: EpisodeSimple
    ): DataState<List<PlayLineSimple>>? = null

    /**
     * 获取播放信息（剧集优先模式专用）
     * 复用 PlayInfo 结构
     * 需要重试逻辑
     */
    suspend fun getPlayInfoSimple(
        cartoonIndex: CartoonIndex,
        playLineSimple: PlayLineSimple,
        episodeSimple: EpisodeSimple
    ): DataState<PlayInfo>? = null
}

interface PlayComponent: Component, IPlayComponent