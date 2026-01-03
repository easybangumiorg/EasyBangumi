package org.easybangumi.next.shared.compose.media_radar.search

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
import org.easybangumi.next.shared.source.SourceCase
import org.easybangumi.next.shared.source.api.component.ComponentBusinessPair
import org.easybangumi.next.shared.source.api.component.play.PlayComponent
import org.easybangumi.next.shared.source.api.component.search.SearchComponent
import org.easybangumi.next.shared.source.api.component.search.createPagingSource
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
 *  搜索模式的媒体雷达
 */
class SearchRadarVM(
    private val keywordSuggest: List<String>,
): StateViewModel<SearchRadarVM.State>(
    initState = State(
        keywordSuggest = keywordSuggest,
        fieldText = keywordSuggest.firstOrNull() ?: "",
        searchKeyword = keywordSuggest.firstOrNull() ?: ""
    )
) {

    data class SelectionResult(
        val playCover: CartoonCover,
        val businessPair: ComponentBusinessPair<SearchComponent, PlayComponent>,
    ) {

        val searchBusiness = businessPair.first
        val playBusiness = businessPair.second

    }

    data class State(
        val keywordSuggest: List<String> = listOf(),    // 搜索建议 - 别名等
        val fieldText: String,                          // 搜索框显示的文字
        val searchKeyword: String? = null,              // 真正搜索的关键字
        val selectionResult: SelectionResult? = null,   // 选中结果
        val playSourceLoading: Boolean = true,          // 加载中
        val lineState: List<LineState> = listOf(),      // 各搜索源的状态
    )

    data class LineState(
        val business: ComponentBusinessPair<SearchComponent, PlayComponent>,
        val pagingFlow: DataState<PagingFlow<CartoonCover>> = DataState.none(),
    )


    private val sourceCase: SourceCase by inject()


    private var pagingTemp: Pair<String, Map<String, PagingFlow<CartoonCover>>>? = null

    init {
        viewModelScope.launch {
            combine(
                state.map { it.searchKeyword }.distinctUntilChanged(),
                sourceCase.searchBusinessWithPlayFlow().distinctUntilChanged()
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
            it.copy(fieldText = text)
        }
    }

    fun onSearchKeywordChange(){
        update {
            it.copy(searchKeyword = it.fieldText)
        }
    }


}