package org.easybangumi.next.shared.source.api.component.play

import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.shared.source.api.component.Component
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
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

    suspend fun getPlayCover(
        cartoonIndex: CartoonIndex
    ): DataState<CartoonPlayCover?> {
        return DataState.Ok(null)
    }

    // 搜索播放线路
    suspend fun getPlayLines(
        cartoonIndex: CartoonIndex
    ): DataState<List<PlayerLine>>



    suspend fun getPlayInfo(
        cartoonIndex: CartoonIndex,
        playerLine: PlayerLine,
        episode: Episode,
    ): DataState<PlayInfo>
}

interface PlayComponent: Component, IPlayComponent