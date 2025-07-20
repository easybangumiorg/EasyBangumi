package org.easybangumi.next.shared.media_radar

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.PagingFlow
import org.easybangumi.next.shared.data.cartoon.CartoonPlayCover
import org.easybangumi.next.shared.foundation.view_model.StateViewModel
import org.easybangumi.next.shared.source.api.component.ComponentBusiness
import org.easybangumi.next.shared.source.api.component.play.PlayComponent
import org.easybangumi.next.shared.source.bangumi.model.BgmReviews
import org.easybangumi.next.shared.source.case.PlaySourceCase
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

class MediaRadarViewModel (
    private val param: MediaRadarParam,
): StateViewModel<MediaRadarViewModel.State>(
    State(param.userKeyword)
) {

    data class State(
        val keyword: String,
        val searchKeyword: String? = null,
        val selectionPlayCover: CartoonPlayCover? = null,
        val playSourceLoading: Boolean = true,
        val lineState: Map<String, LineState> = mapOf(),
    )

    data class LineState(
        val playBusiness: ComponentBusiness<PlayComponent>,
        val pagingFlow: DataState<PagingFlow<CartoonPlayCover>> = DataState.none(),
    )

    private val playSourceCase: PlaySourceCase by inject()

    init {
        viewModelScope.launch {
            combine(
                state.map { it.searchKeyword }.distinctUntilChanged(),
                playSourceCase.playBusinessFlow().distinctUntilChanged()
            ) { keyword, playBusiness ->

            }.collect()

        }
    }

    fun onFieldChange(
        text: String,
    ){
        update {
            it.copy(keyword = text)
        }
    }

    fun onSearchKeywordChange(
        text: String,
    ){
        update {
            it.copy(searchKeyword = text)
        }
    }

}