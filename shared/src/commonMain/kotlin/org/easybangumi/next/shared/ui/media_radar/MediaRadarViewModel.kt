package org.easybangumi.next.shared.ui.media_radar

import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.PagingFlow
import org.easybangumi.next.lib.utils.newPagingFlow
import org.easybangumi.next.shared.data.cartoon.CartoonPlayCover
import org.easybangumi.next.shared.foundation.view_model.StateViewModel
import org.easybangumi.next.shared.source.api.component.ComponentBusiness
import org.easybangumi.next.shared.source.api.component.play.IPlayComponent
import org.easybangumi.next.shared.source.api.component.play.PlayComponent
import org.easybangumi.next.shared.source.api.component.play.createSearchPlayPagingSource
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
    State(param.userKeyword.ifEmpty { param.cover.name})
) {

    data class SelectionResult(
        val playCover: CartoonPlayCover,
        val playBusiness: ComponentBusiness<PlayComponent>,
    )

    data class State(
        val keyword: String,
        val searchKeyword: String? = null,
        val selectionResult: SelectionResult? = null,
        val playSourceLoading: Boolean = true,
        val lineState: List<LineState> = listOf(),
    )

    data class LineState(
        val playBusiness: ComponentBusiness<PlayComponent>,
        val pagingFlow: DataState<PagingFlow<CartoonPlayCover>> = DataState.none(),
    )

    private val playSourceCase: PlaySourceCase by inject()

    private var pagingTemp: Pair<String, Map<String, PagingFlow<CartoonPlayCover>>>? = null

    init {
        viewModelScope.launch {
            combine(
                state.map { it.searchKeyword }.distinctUntilChanged(),
                playSourceCase.playBusinessFlow().distinctUntilChanged()
            ) { keyword, playBusiness ->

                if (playBusiness.isLoading) {
                    update {
                        it.copy(playSourceLoading = true)
                    }
                } else if (keyword == null) {
                    update {
                        it.copy(
                            playSourceLoading = false,
                            lineState = listOf(),
                        )
                    }
                } else {
                    val map = hashMapOf<String, PagingFlow<CartoonPlayCover>>()
                    val temp = pagingTemp
                    if (temp != null && temp.first == keyword) {
                        map.putAll(temp.second)
                    }
                    val res = playBusiness.businessList.map {
                        val t = map[it.source.key]
                        if (t != null) {
                            LineState(it, DataState.ok(t))
                        } else {
                            val pagingSource = it.runSuspendDirect {
                                val param = IPlayComponent.PlayLineSearchParam(
                                    cartoonCover = param.cover,
                                    keyword = keyword
                                )
                                createSearchPlayPagingSource(param)
                            }
                            val pagingFlow = pagingSource.newPagingFlow().cachedIn(viewModelScope)
                            map[it.source.key] = pagingFlow
                            LineState(it, DataState.ok(pagingFlow))
                        }
                    }
                    pagingTemp = keyword to map
                    update {
                        it.copy(
                            playSourceLoading = false,
                            lineState = res,
                        )
                    }
                }
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

    fun onSearchKeywordChange(){
        update {
            it.copy(searchKeyword = it.keyword)
        }
    }

}