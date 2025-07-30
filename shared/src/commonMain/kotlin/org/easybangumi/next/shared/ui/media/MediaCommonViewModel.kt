package org.easybangumi.next.shared.ui.media

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.data.cartoon.CartoonPlayCover
import org.easybangumi.next.shared.data.cartoon.Episode
import org.easybangumi.next.shared.data.cartoon.PlayerLine
import org.easybangumi.next.shared.foundation.view_model.StateViewModel
import org.easybangumi.next.shared.source.case.PlaySourceCase
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
 */
class MediaCommonViewModel (
    val cartoonCover: CartoonCover,
    var suggestEpisode: Episode? = null,
): StateViewModel<MediaCommonViewModel.State>(State()) {

    private val playSourceCase: PlaySourceCase by inject()

    data class State (
        val detail: DetailState = DetailState(),
        val playIndex: PlayIndexState = PlayIndexState(),
        val popup: Popup? = null,
    )

    data class DetailState(
        val playCover: CartoonPlayCover? = null,
        val showDetailFromPlay: Boolean = false,
    )
    data class PlayIndexState(
        val playerLineList: DataState<List<PlayerLine>> = DataState.none(),
        val currentPlayerLine: Int = 0,
        val currentEpisode: Int = 0,
    )
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
                    playCover = result.playCover
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
            res.onOK {

            }
            update {
                it.copy(
                    playIndex = it.playIndex.copy(
                        playerLineList = res,
                    )
                )
            }
        }
    }

    fun dismissPopup() {
        update {
            it.copy(
                popup = null,
            )
        }
    }

    init {
        viewModelScope.launch {
            state.map { it.detail.playCover }.distinctUntilChanged().collectLatest { playCover ->
                if (playCover == null) {
                    update {
                        it.copy(
                            popup = Popup.MediaRadar(
                                cartoonCover = cartoonCover,
                                keyword = null
                            )
                        )
                    }
                }
            }
        }
    }

}