package org.easybangumi.next.shared.compose.media.bangumi

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.easybangumi.next.shared.compose.detail.bangumi.BangumiDetailVM
import org.easybangumi.next.shared.compose.media.MediaParam
import org.easybangumi.next.shared.compose.media.PlayLineIndexVM
import org.easybangumi.next.shared.compose.media_radar.MediaRadarParam
import org.easybangumi.next.shared.compose.media_radar.MediaRadarVM
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.foundation.view_model.BaseViewModel
import kotlin.getValue


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

class BangumiMediaCommonVM (
    val param: MediaParam,
): BaseViewModel() {

    val cartoonIndex: CartoonIndex = param.cartoonIndex
    var suggestEpisode: Int? = param.suggestEpisode

    // == 数据状态 =============================
    data class State(
        val detailNamePreview: String = "",
        val radarResult: MediaRadarVM.SelectionResult? = null,
        val showDetailFromPlay: Boolean = true,
        val isFullscreen: Boolean = false,
        val isTableMode: Boolean = false,
    )

    internal val sta = MutableStateFlow(State(
        // 先使用 cartoonCover 中的 name
        detailNamePreview = param.cartoonCover?.name?:""
    ))
    val state = sta.asStateFlow()

    // == 弹窗状态 =============================
    sealed class Popup {
        data object MediaRadarPanel: Popup()

        data object BangumiDetailPanel: Popup()
    }
    private val _popupState = MutableStateFlow<Popup?>(null)
    val popupState = _popupState.asStateFlow()

    // == 播放线路状态 =============================
    val playLineIndexVM: PlayLineIndexVM by childViewModel {
        PlayLineIndexVM(
//            cartoonIndex = cartoonIndex.toCartoonIndex(),
            suggestEpisode = suggestEpisode,
        )
    }
    val playIndexState = playLineIndexVM.logic


    // == 视频雷达状态 =============================
    val mediaRadarVM: MediaRadarVM by childViewModel {
        MediaRadarVM(
            param.suggestMediaRadarParam ?: MediaRadarParam(
                defaultKeyword = param.cartoonCover?.name?:""
            )
        )
    }

    // == Bangumi 番剧详情面板状态 =============================
    val bangumiDetailVM: BangumiDetailVM by childViewModel {
        BangumiDetailVM(cartoonIndex = cartoonIndex,)
    }

    init {
        // 媒体雷达结果 -> 播放线路状态
        viewModelScope.launch {
            state.map { it.radarResult }.distinctUntilChanged().collectLatest {
                if (it != null) {
                    playLineIndexVM.loadPlayLine(it.playCover.toCartoonIndex(), it.playBusiness)
                }
            }
        }


        // bangumi 番剧信息 -> state
        viewModelScope.launch {
            bangumiDetailVM.subjectRepository.flow.collectLatest {
                val name = it.okOrNull()?.displayName
                if (name != null) {
                    sta.update { s->
                        s.copy(
                            detailNamePreview = name
                        )
                    }
                }
            }
        }

        // 这里只是为了更新番剧名称，用最弱的刷新方式
        bangumiDetailVM.subjectRepository.refreshIfNone()
    }

    // state change ============================
    fun setShowDetailFromPlay(show: Boolean) {
        sta.update {
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


    fun onMediaRadarSelect(result: MediaRadarVM.SelectionResult?) {
        sta.update {
            it.copy(
                radarResult = result
            )
        }
    }

}