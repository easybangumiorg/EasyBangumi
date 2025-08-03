package org.easybangumi.next.shared.ui.media

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.data.cartoon.CartoonPlayCover
import org.easybangumi.next.shared.data.cartoon.Episode
import org.easybangumi.next.shared.data.cartoon.PlayerLine
import org.easybangumi.next.shared.foundation.view_model.StateViewModel
import org.easybangumi.next.shared.source.case.PlaySourceCase
import org.easybangumi.next.shared.ui.media_radar.MediaRadarParam
import org.easybangumi.next.shared.ui.media_radar.MediaRadarViewModel
import org.koin.core.component.inject

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
 *
 *  媒体页通用逻辑，作为各平台媒体 ViewModel 的子 ViewModel
 *  1. 媒体雷达
 *  2. 播放线路和集数
 *  3. 详情面板
 */
class MediaCommonViewModel (
    val cartoonCover: CartoonCover,
    val mediaRadarParam: MediaRadarParam? = null,
    var suggestEpisode: Int? = null,
): StateViewModel<MediaCommonViewModel.State>(State()) {

    private val logger = logger()

    private val playSourceCase: PlaySourceCase by inject()
    val mediaRadarViewModel: MediaRadarViewModel by childViewModel {
        MediaRadarViewModel(
            mediaRadarParam ?: MediaRadarParam(
                cover = cartoonCover,
            )
        )
    }

    data class State (
        val detail: DetailState = DetailState(),
        val playIndex: PlayIndexState = PlayIndexState(),
        val popup: Popup? = null,
    )

    data class DetailState(
        val radarResult: MediaRadarViewModel.SelectionResult? = null,
//        val playCover: CartoonPlayCover? = null,
        val showDetailFromPlay: Boolean = true,
    )
    data class PlayIndexState(
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
    sealed class Popup {
        data class MediaRadar(
            val cartoonCover: CartoonCover,
            val keyword: String? = null,
        ): Popup()

        data class MetaSourceDetail(
            val cartoonCover: CartoonCover,
        ): Popup()
    }

    fun onMediaRadarResult(result: MediaRadarViewModel.SelectionResult) {
        dismissPopup()
        update {
            it.copy(
                detail = it.detail.copy(
                    radarResult = result
                ),
            )
        }
        viewModelScope.launch {
            update {
                it.copy(
                    playIndex = it.playIndex.copy(
                        playerLineList = DataState.loading()
                    )
                )
            }
            val res = result.playBusiness.run {
                getPlayLines(result.playCover)
            }
            var targetPlaylineIndex: Int = state.value.playIndex.currentPlayerLine
            var targetEpisodeIndex: Int = state.value.playIndex.currentEpisode
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

            update {
                it.copy(
                    playIndex = it.playIndex.copy(
                        playerLineList = res,
                        currentPlayerLine = targetPlaylineIndex,
                        currentEpisode = targetEpisodeIndex,
                    )
                )
            }
        }
    }


    fun onPlayLineSelected(
        index: Int,
    ) {
        update {
            val playLine = it.playIndex.playerLineList.okOrNull()?.getOrNull(index)
            var currentEpisode = it.playIndex.currentEpisode
            if (playLine != null && currentEpisode !in playLine.episodeList.indices) {
                // 如果当前集数不在选中的线路中，则重置集数为 0
                currentEpisode = 0
            }
            it.copy(
                playIndex = it.playIndex.copy(
                    currentPlayerLine = index,
                    currentEpisode = currentEpisode,
                )
            )
        }
    }

    fun showMediaRadar(keyword: String? = null) {
        logger.info("showMediaRadar")
        update {
            val popup = (it.popup as? Popup.MediaRadar)?.copy(keyword = keyword)
                ?: Popup.MediaRadar(
                    cartoonCover = cartoonCover,
                    keyword = keyword
                )
            it.copy(
                popup = popup
            )
        }
    }

    fun dismissPopup() {
        logger.info("dismissPopup")
        update {
            it.copy(
                popup = null,
            )
        }
    }

    init {
//        viewModelScope.launch {
//            state.map { it.detail.radarResult }.distinctUntilChanged().collectLatest { result ->
//                if (result == null) {
//                    update {
//                        it.copy(
//                            popup = Popup.MediaRadar(
//                                cartoonCover = cartoonCover,
//                                keyword = null
//                            )
//                        )
//                    }
//                }
//            }
//        }
    }

}