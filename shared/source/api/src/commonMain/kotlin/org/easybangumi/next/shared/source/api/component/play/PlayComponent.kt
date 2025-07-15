package org.easybangumi.next.shared.source.api.component.play

import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.shared.source.api.component.Component
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.data.cartoon.CartoonPlayCover
import org.easybangumi.next.shared.data.cartoon.Episode
import org.easybangumi.next.shared.data.cartoon.PlayInfo
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
    // 搜索播放实体
    data class PlayLineSearchParam(
        // 来源番的数据
        val cartoonCover: CartoonCover,
        // 用户手动输入的搜索关键字，可能为空
        val keyword: String? = null,
        // 用户手动输入的 url，可能为空
        val webUrl: String? = null,
    )
    suspend fun searchPlayCovers(
        param: PlayLineSearchParam,
        // 因为搜索播放一般只需要前几个结果，因此这里不用支持分页了直接指定 limit
        limit: Int = 0,
    ): DataState<List<CartoonPlayCover>>

    // 搜索播放线路
    suspend fun getPlayLines(
        cartoonCover: CartoonPlayCover,
    ): DataState<List<List<PlayerLine>>>


    suspend fun getPlayInfo(
        cartoonPlayCover: CartoonPlayCover,
        playerLine: PlayerLine,
        episode: Episode,
    ): DataState<PlayInfo>
}

interface PlayComponent: Component, IPlayComponent