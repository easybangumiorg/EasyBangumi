package com.heyanle.easybangumi4.cartoon.entity

import com.heyanle.easybangumi4.cartoon.story.download.action.AriaAction
import com.heyanle.easybangumi4.cartoon.story.download.action.CopyAndNfoAction
import com.heyanle.easybangumi4.cartoon.story.download.action.ParseAction
import com.heyanle.easybangumi4.cartoon.story.download.action.TranscodeAction
import com.heyanle.easybangumi4.cartoon.story.download.action.TransformerAction
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

    // 历史遗留问题，默认不使用快速模式
    val quickMode: Boolean = false,
){

    companion object {
        private val quickActionName = listOf<String>(
            ParseAction.NAME,
            AriaAction.NAME,
            TranscodeAction.NAME,
            CopyAndNfoAction.NAME
        )

        private val normalActionNameList = listOf<String>(
            ParseAction.NAME,
            TransformerAction.NAME,
            CopyAndNfoAction.NAME
        )
    }


    val stepChain: List<String> by lazy {
        if(quickMode) quickActionName else normalActionNameList
    }
}

data class CartoonDownloadInfo (
    val req: CartoonDownloadReq,
    val runtime: CartoonDownloadRuntime?,
) {

}