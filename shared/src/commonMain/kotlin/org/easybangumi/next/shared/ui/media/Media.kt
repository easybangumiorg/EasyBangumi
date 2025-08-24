package org.easybangumi.next.shared.ui.media

import androidx.compose.runtime.Composable
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.source.bangumi.source.BangumiInnerSource
import org.easybangumi.next.shared.ui.media_radar.MediaRadarParam

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

data class MediaParam(
    val cartoonCover: CartoonCover,
    val suggestEpisode: Int? = null,
    val mediaRadarParam: MediaRadarParam? = null,
)

@Composable
fun Media(
    param: MediaParam,
) {
    when (param.cartoonCover.source) {
        BangumiInnerSource.SOURCE_KEY -> {
            BangumiMedia(param)
        }
        else -> {
            NormalMedia(param)
        }
    }
}

@Composable
expect fun BangumiMedia (
    mediaParam: MediaParam
)

@Composable
expect fun NormalMedia (
    mediaParam: MediaParam
)