package com.heyanle.easybangumi4.cartoon.story.download.req

import com.heyanle.easybangumi4.cartoon.entity.CartoonDownloadReq
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.entity.CartoonStoryItem
import com.heyanle.easybangumi4.cartoon.story.download.action.AriaAction
import com.heyanle.easybangumi4.cartoon.story.download.action.CopyAndNfoAction
import com.heyanle.easybangumi4.cartoon.story.download.action.ParseAction
import com.heyanle.easybangumi4.cartoon.story.download.action.TranscodeAction
import com.heyanle.easybangumi4.cartoon.story.download.action.TransformerAction
import com.heyanle.easybangumi4.source_api.entity.Episode
import com.heyanle.easybangumi4.source_api.entity.PlayLine

/**
 * Created by heyanle on 2024/7/9.
 * https://github.com/heyanLE
 */

object CartoonDownloadReqFactory {


    fun newReqList(
        cartoonInfo: CartoonInfo,
        playLine: PlayLine,
        list: List<Episode>,
        targetLocalInfo: CartoonStoryItem,
        quickMode: Boolean = true
    ): List<CartoonDownloadReq> {
        val episodeList = list.sortedBy { it.order }
        val orderSet = mutableSetOf<Int>()
        targetLocalInfo.downloadInfoList.forEach {
            orderSet.add(it.req.toEpisode)
        }
        targetLocalInfo.cartoonLocalItem.episodes.forEach {
            orderSet.add(it.episode)
        }
        val reqList = mutableListOf<CartoonDownloadReq>()
        for ((i, episode) in episodeList.withIndex()){
            var targetEpisode = episode.order
            while (orderSet.contains(targetEpisode)){
                targetEpisode ++
            }

            orderSet.add(targetEpisode)
            reqList.add(
                CartoonDownloadReq(
                    uuid = "req-${System.currentTimeMillis()}-${i}",
                    fromCartoonInfo = cartoonInfo,
                    fromPlayLine = playLine,
                    fromEpisode = episode,
                    toLocalItemId = targetLocalInfo.cartoonLocalItem.itemId,
                    localItem = targetLocalInfo.cartoonLocalItem,
                    toEpisodeTitle = episode.label,
                    toEpisode = targetEpisode,
                    quickMode = quickMode
                )
            )
        }
        return reqList
    }

    fun changeReqListMode(
        list: Collection<CartoonDownloadReq>,
        quickMode: Boolean = false
    ): List<CartoonDownloadReq> {
        return list.map {
            it.copy(
                quickMode = quickMode
            )
        }
    }

}