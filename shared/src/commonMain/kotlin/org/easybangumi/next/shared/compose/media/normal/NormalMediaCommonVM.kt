package org.easybangumi.next.shared.compose.media.normal

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.easybangumi.next.shared.compose.media.MediaParam
import org.easybangumi.next.shared.compose.media.PlayLineIndexVM
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.foundation.view_model.BaseViewModel
import org.easybangumi.next.shared.source.api.component.ComponentBusiness
import org.easybangumi.next.shared.source.api.component.play.PlayComponent
import org.easybangumi.next.shared.source.SourceCase
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
class NormalMediaCommonVM(
    val param: MediaParam,
): BaseViewModel() {

    val cartoonIndex: CartoonIndex = param.cartoonIndex
    var suggestEpisode: Int? = param.suggestEpisode

    // == 数据状态 =============================
    data class State(
        val isFullscreen: Boolean = false,
        val isTableMode: Boolean = false,
        val playBusiness: ComponentBusiness<PlayComponent>? = null,
    )

    internal val sta = MutableStateFlow(State())
    val state = sta.asStateFlow()


    // == 弹窗状态 =============================
    sealed class Popup {

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

    // == 播放源状态 =============================
    val sourceCase: SourceCase by inject()

    init {
        viewModelScope.launch {
            sourceCase.playComponentFlow(param.cartoonIndex.source).stateIn(
                viewModelScope
            ).collectLatest { business ->
                sta.update {
                    it.copy(
                        playBusiness = business
                    )
                }
            }
        }

        viewModelScope.launch {
            state.map { it.playBusiness }.distinctUntilChanged().collectLatest {
                if (it != null) {
                    playLineIndexVM.loadPlayLine(
                        cartoonIndex = cartoonIndex,
                        business = it,
                    )
                }
            }
        }
    }


}