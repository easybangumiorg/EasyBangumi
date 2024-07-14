package com.heyanle.easybangumi4.cartoon.download.req

import com.heyanle.easybangumi4.cartoon.entity.CartoonDownloadReq
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.download.step.CopyAndNfoStep
import com.heyanle.easybangumi4.cartoon.download.step.ParseStep
import com.heyanle.easybangumi4.cartoon.download.step.TransformerStep
import com.heyanle.easybangumi4.cartoon.entity.CartoonLocalInfo
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
        targetLocalInfo: CartoonLocalInfo,
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

            orderSet.add(episode.order)
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
                    stepChain = listOf(
                        ParseStep.NAME,
                        TransformerStep.NAME,
                        CopyAndNfoStep.NAME
                    )
                )
            )
        }
        return reqList
    }

}