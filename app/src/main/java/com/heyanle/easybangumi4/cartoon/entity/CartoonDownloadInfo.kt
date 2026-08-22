package com.heyanle.easybangumi4.cartoon.entity

import com.heyanle.easybangumi4.cartoon.story.download.DownloadTaskPlan
import com.heyanle.easybangumi4.cartoon.story.download.engine.QuickDownloadEngineIds
import com.heyanle.easybangumi4.cartoon.story.download.runtime.CartoonDownloadRuntime
import com.heyanle.easybangumi4.plugin.api.entity.Episode
import com.heyanle.easybangumi4.plugin.api.entity.PlayLine

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

    // 历史遗留问题，默认不使用快速模式
    val quickMode: Boolean = false,

    // 字段带默认值以兼容旧版 cartoon_download.json。
    // 完整模式忽略该字段，快速模式在创建任务时固化引擎选择。
    val quickDownloadEngineId: String = QuickDownloadEngineIds.ARIA,
){

    val stepChain: List<String> by lazy {
        DownloadTaskPlan.steps(quickMode)
    }
}

data class CartoonDownloadInfo (
    val req: CartoonDownloadReq,
    val runtime: CartoonDownloadRuntime?,
) {

}
