package org.easybangumi.next.source.inner.debug

import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.PagingFrame
import org.easybangumi.next.lib.utils.UrlUtils
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.data.cartoon.Episode
import org.easybangumi.next.shared.data.cartoon.PlayInfo
import org.easybangumi.next.shared.data.cartoon.PlayerLine
import org.easybangumi.next.shared.source.api.component.BaseComponent
import org.easybangumi.next.shared.source.api.component.play.PlayComponent
import org.easybangumi.next.shared.source.api.component.search.SearchComponent

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
class DebugComponent: BaseComponent(), SearchComponent, PlayComponent {

    override fun firstKey(): String {
        return ""
    }

    override suspend fun search(
        keyword: String,
        key: String
    ): DataState<PagingFrame<CartoonCover>> {
        if (key.isEmpty()) {
            return DataState.Ok(
                PagingFrame(
                    "2",
                    listOf(
                        CartoonCover(
                            id = "1",
                            source = source.key,
                            name = "恋人不行",
                            coverUrl = "https://lain.bgm.tv/pic/cover/l/ae/03/524707_1quxk.jpg",
                            intro = "",
                            webUrl = ""
                        )
                    ),
                )
            )
        }
        return DataState.Ok(
            PagingFrame(
                null,
                emptyList(),
            )
        )
    }

    override suspend fun getPlayLines(cartoonIndex: CartoonIndex): DataState<List<PlayerLine>> {
        return DataState.Ok(
            listOf(
                PlayerLine(
                    id = "1",
                    label = "测试线路",
                    episodeList = listOf(
                        Episode(
                            id = "1",
                            label = "测试1",
                            order = 1
                        ),
                        Episode(
                            id = "2",
                            label = "测试2",
                            order = 2,
                        ),
                    )
                )
            )
        )
    }

    override suspend fun getPlayInfo(
        cartoonIndex: CartoonIndex,
        playerLine: PlayerLine,
        episode: Episode
    ): DataState<PlayInfo> {
        return DataState.Ok(
            PlayInfo(
                url = "https://apn.moedot.net/d/wo/2507/我们01z.mp4",
                type = PlayInfo.TYPE_NORMAL
            )
        )
    }
}