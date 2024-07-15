package com.heyanle.easybangumi4.cartoon.entity

import com.heyanle.easybangumi4.cartoon.story.download.runtime.CartoonDownloadRuntime
import com.heyanle.easybangumi4.source_api.entity.Episode
import com.heyanle.easybangumi4.source_api.entity.PlayLine

/**
 * 番剧下载请求
 * Created by heyanle on 2024/7/7.
 * https://github.com/heyanLE
 */
data class CartoonDownloadReq(
    val uuid: String,

    // 下载任务创建来自的番剧信息
    val fromCartoonInfo: CartoonInfo,
    val fromPlayLine: PlayLine,
    val fromEpisode: Episode,

    // 下载任务目标本地番剧信息
    val toLocalItemId: String,
    val localItem: CartoonLocalItem,

    val toEpisodeTitle: String,
    val toEpisode: Int,

    val stepChain: List<String>,
)

data class CartoonDownloadInfo (
    val req: CartoonDownloadReq,
    val runtime: CartoonDownloadRuntime?,
) {

}