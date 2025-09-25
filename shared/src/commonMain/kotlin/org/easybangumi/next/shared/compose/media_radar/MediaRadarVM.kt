package org.easybangumi.next.shared.compose.media_radar

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
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.foundation.view_model.StateViewModel
import org.easybangumi.next.shared.source.api.component.ComponentBusinessPair
import org.easybangumi.next.shared.source.api.component.play.PlayComponent
import org.easybangumi.next.shared.source.api.component.search.SearchComponent
import org.easybangumi.next.shared.source.api.component.search.createPagingSource
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

class MediaRadarVM (
    private val param: MediaRadarParam,
): StateViewModel<MediaRadarVM.State>(
    State(param.defaultKeyword)
) {

    data class SelectionResult(
        val playCover: CartoonCover,
        val businessPair: ComponentBusinessPair<SearchComponent, PlayComponent>,
    ) {

        val searchBusiness = businessPair.first
        val playBusiness = businessPair.second

    }

    data class State(
        val keyword: String,
        val searchKeyword: String? = null,
        val selectionResult: SelectionResult? = null,
        val playSourceLoading: Boolean = true,
        val lineState: List<LineState> = listOf(),
    )

    data class LineState(
        val business: ComponentBusinessPair<SearchComponent, PlayComponent>,
        val pagingFlow: DataState<PagingFlow<CartoonCover>> = DataState.none(),
    )

    private val playSourceCase: PlaySourceCase by inject()

    private var pagingTemp: Pair<String, Map<String, PagingFlow<CartoonCover>>>? = null

    init {
        viewModelScope.launch {
            combine(
                state.map { it.searchKeyword }.distinctUntilChanged(),
                playSourceCase.searchBusinessWithPlayFlow().distinctUntilChanged()
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
                    val map = hashMapOf<String, PagingFlow<CartoonCover>>()
                    val temp = pagingTemp
                    if (temp != null && temp.first == keyword) {
                        map.putAll(temp.second)
                    }
                    val res = playBusiness.business.map {
                        val t = map[it.first.source.key]
                        if (t != null) {
                            LineState(it, DataState.ok(t))
                        } else {
                            val pagingSource =   it.first.createPagingSource(keyword)

                            val pagingFlow = pagingSource.newPagingFlow().cachedIn(viewModelScope)
                            map[it.first.source.key] = pagingFlow
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