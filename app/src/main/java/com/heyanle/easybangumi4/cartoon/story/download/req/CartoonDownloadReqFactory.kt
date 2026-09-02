package com.heyanle.easybangumi4.cartoon.story.download.req

import com.heyanle.easybangumi4.cartoon.entity.CartoonDownloadReq
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.entity.CartoonLocalItem
import com.heyanle.easybangumi4.cartoon.entity.CartoonStoryItem
import com.heyanle.easybangumi4.cartoon.entity.DownloadDestination
import com.heyanle.easybangumi4.cartoon.story.bound.FlatFileNameSanitizer
import com.heyanle.easybangumi4.cartoon.story.download.action.AriaAction
import com.heyanle.easybangumi4.cartoon.story.download.action.CopyAndNfoAction
import com.heyanle.easybangumi4.cartoon.story.download.action.ParseAction
import com.heyanle.easybangumi4.cartoon.story.download.action.TranscodeAction
import com.heyanle.easybangumi4.cartoon.story.download.action.TransformerAction
import com.heyanle.easybangumi4.plugin.api.entity.Episode
import com.heyanle.easybangumi4.plugin.api.entity.PlayLine

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
        var nextTargetNumber = 1
        for ((i, episode) in episodeList.withIndex()){
            while (orderSet.contains(nextTargetNumber)) {
                nextTargetNumber++
            }
            val targetEpisode = nextTargetNumber

            orderSet.add(targetEpisode)
            nextTargetNumber++
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

    fun newFlatReqList(
        cartoonInfo: CartoonInfo,
        playLine: PlayLine,
        list: List<Episode>,
        quickMode: Boolean = true,
        existingNames: Set<String> = emptySet(),
    ): List<CartoonDownloadReq> {
        val episodeList = list.sortedBy { it.order }
        // 已被扁平目录占用或本批次已分配的文件名
        val usedNames = existingNames.toMutableSet()
        val reqList = mutableListOf<CartoonDownloadReq>()
        for ((i, episode) in episodeList.withIndex()) {
            var name = FlatFileNameSanitizer.defaultName(cartoonInfo.name, episode.label)
            if (usedNames.contains(name)) {
                name = FlatFileNameSanitizer.sanitize("$name-${episode.order}")
            }
            var attempt = 2
            while (usedNames.contains(name)) {
                name = FlatFileNameSanitizer.sanitize("$name-$attempt")
                attempt++
            }
            usedNames.add(name)
            reqList.add(
                CartoonDownloadReq(
                    uuid = "req-${System.currentTimeMillis()}-${i}",
                    fromCartoonInfo = cartoonInfo,
                    fromPlayLine = playLine,
                    fromEpisode = episode,
                    // FLAT 目的地不依赖本地番源条目，占位 localItem 仅携带番名/封面供任务列表展示
                    toLocalItemId = "",
                    localItem = CartoonLocalItem(
                        folderUri = "",
                        nfoUri = "",
                        itemId = "",
                        title = cartoonInfo.name,
                        desc = "",
                        cover = cartoonInfo.coverUrl,
                        genre = emptyList(),
                        episodes = emptyList(),
                    ),
                    toEpisodeTitle = episode.label,
                    toEpisode = episode.order,
                    quickMode = quickMode,
                    destination = DownloadDestination.FLAT,
                    flatFileName = name,
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
