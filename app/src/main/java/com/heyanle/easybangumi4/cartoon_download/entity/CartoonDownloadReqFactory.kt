package com.heyanle.easybangumi4.cartoon_download.entity

import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon_download.step.CopyAndNfoStep
import com.heyanle.easybangumi4.cartoon_download.step.ParseStep
import com.heyanle.easybangumi4.cartoon_download.step.TransformerStep
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
        targetItemId: String,
    ): List<CartoonDownloadReq> {
        val episodeList = list.sortedBy { it.order }
        var max = -1
        val orderSet = mutableSetOf<Int>()
        val reqList = mutableListOf<CartoonDownloadReq>()
        for ((i, episode) in episodeList.withIndex()){
            var targetEpisode = episode.order
            if (orderSet.contains(targetEpisode)){
                targetEpisode = max + 1
            }
            max = targetEpisode
            if(episode.order > max){
                max = episode.order
            }
            orderSet.add(episode.order)
            reqList.add(
                CartoonDownloadReq(
                    uuid = "req-${System.currentTimeMillis()}-${i}",
                    fromCartoonInfo = cartoonInfo,
                    fromPlayLine = playLine,
                    fromEpisode = episode,
                    toLocalItemId = targetItemId,
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