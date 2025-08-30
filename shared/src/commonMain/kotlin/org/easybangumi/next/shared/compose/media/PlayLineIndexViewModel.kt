package org.easybangumi.next.shared.compose.media

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.data.cartoon.Episode
import org.easybangumi.next.shared.data.cartoon.PlayerLine
import org.easybangumi.next.shared.foundation.view_model.StateViewModel
import org.easybangumi.next.shared.source.api.component.ComponentBusiness
import org.easybangumi.next.shared.source.api.component.play.PlayComponent


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
 */

class PlayLineIndexViewModel(
    var suggestEpisode: Int? = null,
): StateViewModel<PlayLineIndexViewModel.State>(State()) {

    private val logger = logger()

    data class State(
        val playerLineList: DataState<List<PlayerLine>> = DataState.none(),
        val currentPlayerLine: Int = 0,
        val currentEpisode: Int = 0,
    ) {
        val playLineOrNull: PlayerLine? by lazy {
            playerLineList.okOrNull()?.getOrNull(currentPlayerLine)
        }
        val currentEpisodeOrNull: Episode? by lazy {
            playLineOrNull?.episodeList?.getOrNull(currentEpisode)
        }
    }

    fun onPlayLineSelected(
        index: Int,
    ) {
        update {
            val playLine = it.playerLineList.okOrNull()?.getOrNull(index)
            var currentEpisode = it.currentEpisode
            if (playLine != null && currentEpisode !in playLine.episodeList.indices) {
                // 如果当前集数不在选中的线路中，则重置集数为 0
                currentEpisode = 0
            }
            it.copy(
                currentPlayerLine = index,
                currentEpisode = currentEpisode,
            )
        }
    }

    fun loadPlayLine(
        cartoonIndex: CartoonIndex,
        business: ComponentBusiness<PlayComponent>,
    ) {
        viewModelScope.launch {
            update {
                it.copy(
                    playerLineList = DataState.loading()
                )
            }
            val res = business.run {
                getPlayLines(cartoonIndex)
            }
            var targetPlaylineIndex: Int = state.value.currentPlayerLine
            var targetEpisodeIndex: Int = state.value.currentEpisode
            res.onOK {
                if (targetPlaylineIndex !in it.indices) {
                    targetPlaylineIndex = 0
                }
                val playLine = it.getOrNull(targetPlaylineIndex)
                if (playLine != null) {
                    val suggestIndex = playLine.episodeList.indexOfFirst { it.order == suggestEpisode }
                    if (suggestIndex in playLine.episodeList.indices) {
                        targetEpisodeIndex = suggestIndex
                    }
                } else {
                    targetPlaylineIndex = 0
                    targetEpisodeIndex = 0
                }
                // 只生效一次
                suggestEpisode = null
            }
            logger.info("loadPlayLine: $res, targetPlaylineIndex=$targetPlaylineIndex, targetEpisodeIndex=$targetEpisodeIndex")

            update {
                it.copy(
                    playerLineList = res,
                    currentPlayerLine = targetPlaylineIndex,
                    currentEpisode = targetEpisodeIndex,
                )
            }
        }
    }

}