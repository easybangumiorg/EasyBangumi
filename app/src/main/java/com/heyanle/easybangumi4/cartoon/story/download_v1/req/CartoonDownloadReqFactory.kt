package com.heyanle.easybangumi4.cartoon.story.download_v1.req

import androidx.media3.common.util.UnstableApi
import com.heyanle.easybangumi4.cartoon.entity.CartoonDownloadReq
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.story.download_v1.step.CopyAndNfoStep
import com.heyanle.easybangumi4.cartoon.story.download_v1.step.ParseStep
import com.heyanle.easybangumi4.cartoon.story.download_v1.step.TransformerStep
import com.heyanle.easybangumi4.cartoon.entity.CartoonStoryItem
import com.heyanle.easybangumi4.cartoon.story.download_v1.step.DownloadStep
import com.heyanle.easybangumi4.source_api.entity.Episode
import com.heyanle.easybangumi4.source_api.entity.PlayLine

/**
 * Created by heyanle on 2024/7/9.
 * https://github.com/heyanLE
 */

object CartoonDownloadReqFactory {

    @UnstableApi
    fun newReqList(
        cartoonInfo: CartoonInfo,
        playLine: PlayLine,
        list: List<Episode>,
        targetLocalInfo: CartoonStoryItem,
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
                    stepChain = listOf(
                        ParseStep.NAME,
                        DownloadStep.NAME,
                        TransformerStep.NAME,
                        CopyAndNfoStep.NAME
                    )
                )
            )
        }
        return reqList
    }

}