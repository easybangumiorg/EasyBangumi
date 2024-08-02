package com.heyanle.easybangumi4.cartoon.entity

import com.heyanle.easybangumi4.cartoon.story.download_v1.runtime.CartoonDownloadRuntime

/**
 * Created by heyanle on 2024/7/12.
 * https://github.com/heyanLE
 */
data class CartoonStoryItem (
    val cartoonLocalItem: CartoonLocalItem,
    val downloadInfoList: List<CartoonDownloadInfo>,
) {

    // 不能提交下载任务的集数，包括
    // 1. 本地已经下载的集数
    // 2. runtime 为空的集数（下载未完成时重启 app），当用户点击重试时，必会触发 Runtime 刷新
    val cantReqEpisode: Set<Int> by lazy {
        cartoonLocalItem.episodes.map { it.episode }.toSet()
    }



    // 对于下载错误的信息，这里无法稳定，需要每次重新获取
    val errorDownloadEpisode: Set<Int>
        get() =  downloadInfoList.filter { it.runtime?.state == CartoonDownloadRuntime.STATE_ERROR }.map { it.req.toEpisode }.toSet()

}