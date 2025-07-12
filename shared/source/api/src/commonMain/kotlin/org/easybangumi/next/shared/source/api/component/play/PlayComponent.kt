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

interface PlayComponent: Component {


    // 搜索播放线路
    data class PlayLineSearchParam(
        // 来源番的数据
        val cartoonCover: CartoonCover,
        // 用户手动输入的搜索关键字，可能为空
        val keyword: String? = null,
        // 用户手动输入的 url，可能为空
        val webUrl: String? = null,
    )

    data class PlayLineSearchResultItem(
        val fromCartoonCover: CartoonPlayCover,
        val playLineList: List<PlayerLine>,
    )

    // 搜索播放线路
    suspend fun searchPlayLines(
        param: PlayLineSearchParam,
    ): DataState<List<PlayLineSearchResultItem>>


    suspend fun getPlayInfo(
        cartoonPlayCover: CartoonPlayCover,
        playerLine: PlayerLine,
        episode: Episode,
    ): DataState<PlayInfo>

}