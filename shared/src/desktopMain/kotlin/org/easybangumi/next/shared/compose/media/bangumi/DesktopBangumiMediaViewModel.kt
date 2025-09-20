package org.easybangumi.next.shared.compose.media.bangumi

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.easybangumi.next.shared.compose.detail.bangumi.BangumiDetailViewModel
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.foundation.view_model.BaseViewModel
import org.easybangumi.next.shared.compose.media.MediaParam
import org.easybangumi.next.shared.compose.media.PlayLineIndexViewModel
import org.easybangumi.next.shared.compose.media_radar.MediaRadarParam
import org.easybangumi.next.shared.compose.media_radar.MediaRadarViewModel
import org.easybangumi.next.shared.playcon.desktop.DesktopPlayerViewModel
import org.easybangumi.next.shared.playcon.pointer.PointerPlayconViewModel

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
class DesktopBangumiMediaViewModel(
    val param: MediaParam,
): BaseViewModel() {

    val cartoonIndex: CartoonIndex = param.cartoonIndex
    var suggestEpisode: Int? = param.suggestEpisode

    // == 数据状态 =============================
    data class State(
        val detailNamePreview: String = "",
        val radarResult: MediaRadarViewModel.SelectionResult? = null,
        val showDetailFromPlay: Boolean = true,
        val isFullscreen: Boolean = false,
        val isTableMode: Boolean = false,
    )

    private val _state = MutableStateFlow(State(
        // 先使用 cartoonCover 中的 name
        detailNamePreview = param.cartoonCover?.name?:""
    ))
    val state = _state.asStateFlow()

    // == 弹窗状态 =============================
    sealed class Popup {
        data object MediaRadarPanel: Popup()

        data object BangumiDetailPanel: Popup()
    }
    private val _popupState = MutableStateFlow<Popup?>(null)
    val popupState = _popupState.asStateFlow()

    // == 播放线路状态 =============================
    val playLineIndexViewModel: PlayLineIndexViewModel by childViewModel {
        PlayLineIndexViewModel(
//            cartoonIndex = cartoonIndex.toCartoonIndex(),
            suggestEpisode = suggestEpisode,
        )
    }
    val playIndexState = playLineIndexViewModel.logic

    // == 播放状态 =============================
    val playerViewModel: DesktopPlayerViewModel by childViewModel {
        DesktopPlayerViewModel()
    }

    // == 视频雷达状态 =============================
    val mediaRadarViewModel: MediaRadarViewModel by childViewModel {
        MediaRadarViewModel(
            param.suggestMediaRadarParam ?: MediaRadarParam(
                defaultKeyword = param.cartoonCover?.name?:""
            )
        )
    }

    // == Bangumi 番剧详情面板状态 =============================
    val bangumiDetailViewModel: BangumiDetailViewModel by childViewModel {
        BangumiDetailViewModel(cartoonIndex = cartoonIndex,)
    }

    init {
        // 媒体雷达结果 -> 播放线路状态
        viewModelScope.launch {
            state.map { it.radarResult }.distinctUntilChanged().collectLatest {
                if (it != null) {
                    playLineIndexViewModel.loadPlayLine(it.playCover.toCartoonIndex(), it.playBusiness)
                }
            }
        }

        // 播放链接变化 -> 播放器
        viewModelScope.launch {
            playIndexState.map { it.playInfo }.collectLatest {
                playerViewModel.onPlayInfoChange(it)
            }
        }


        // bangumi 番剧信息 -> state
        viewModelScope.launch {
            bangumiDetailViewModel.subjectRepository.flow.collectLatest {
                val name = it.okOrNull()?.displayName
                if (name != null) {
                    _state.update { s->
                        s.copy(
                            detailNamePreview = name
                        )
                    }
                }
            }
        }

        // 这里只是为了更新番剧名称，用最弱的刷新方式
        bangumiDetailViewModel.subjectRepository.refreshIfNone()
    }

    // state change ============================
    fun setShowDetailFromPlay(show: Boolean) {
        _state.update {
            it.copy(
                showDetailFromPlay = show,
            )
        }
    }


    // popup =============================

    fun showMediaRadar() {
        _popupState.update { Popup.MediaRadarPanel }
    }

    fun showBangumiDetailPanel() {
        _popupState.update { Popup.BangumiDetailPanel }
    }
    fun dismissPopup() {
        _popupState.update { null }
    }


    fun onMediaRadarSelect(result: MediaRadarViewModel.SelectionResult?) {
        _state.update {
            it.copy(
                radarResult = result
            )
        }
    }

}