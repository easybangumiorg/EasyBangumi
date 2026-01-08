package org.easybangumi.next.shared.compose.media

import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.source.bangumi.source.BangumiInnerSource
import org.easybangumi.next.shared.compose.media.bangumi.BangumiMedia
import org.easybangumi.next.shared.compose.media.normal.NormalMedia
import org.easybangumi.next.shared.data.cartoon.CartoonCover

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
 *
 *  来自特殊 Meta 源的番的播放页带有搜索播放源功能
 *  并且 Meta 源只能为 Inner 源并且其页面单独实现
 *  来自普通 Player 源的番的播放页只允许自己作为播放源
 */
@Serializable
data class MediaParam(
    val cartoonIndex: CartoonIndex,
    val cartoonCover: CartoonCover? = null,
    val suggestEpisode: Int? = null,
    // only bangumi source have
    val radarKeywordSuggest: List<String> = emptyList(),
)

@Composable
fun Media(
    param: MediaParam,
) {
    when (param.cartoonIndex.source) {
        BangumiInnerSource.SOURCE_KEY -> {
            BangumiMedia(param)
        }
        else -> {
            NormalMedia(param)
        }
    }
}


