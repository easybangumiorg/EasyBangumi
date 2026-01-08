package org.easybangumi.next.shared.cartoon.search

import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.utils.PagingFlow
import org.easybangumi.next.lib.utils.newPagingFlow
import org.easybangumi.next.shared.compose.search.simple.SimpleSearchViewModel.SimpleSearchItem
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.foundation.view_model.BaseViewModel
import org.easybangumi.next.shared.foundation.view_model.StateViewModel
import org.easybangumi.next.shared.source.SourceCase
import org.easybangumi.next.shared.source.api.component.ComponentBusiness
import org.easybangumi.next.shared.source.api.component.search.SearchComponent
import org.easybangumi.next.shared.source.api.component.search.createPagingSource
import org.koin.core.component.inject
import kotlin.collections.map

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
class CartoonSearchVM: StateViewModel<CartoonSearchVM.State>(State()) {

    data class State(
        val searchKeyword: String? = null,
        val searchBusiness: List<ComponentBusiness<SearchComponent>> = emptyList(),
        // sourceKey to paging flow
        val pagingFlowMap: Map<String, PagingFlow<CartoonCover>> = emptyMap(),
    )

    // sourceKey to Pair<keyword, flow>
    private val pagingSourceTemp = mutableMapOf<String, Pair<String, PagingFlow<CartoonCover>>>()


    val sourceCase: SourceCase by inject()

    init {
        viewModelScope.launch {
            combine(
                sourceCase.searchBusiness(),
                state.map { it.searchKeyword }.distinctUntilChanged(),
            ) { searchBusinessList, keyword ->
                if (keyword != null) {
                    val pagingFlowMap = mutableMapOf<String, PagingFlow<CartoonCover>>()
                    searchBusinessList.forEach { business ->
                        val key = business.source.key
                        val temp = pagingSourceTemp[key]
                        val pagingFlow = if (temp != null && temp.first == keyword) {
                            temp.second
                        } else {
                            business.createPagingSource(keyword).newPagingFlow().cachedIn(viewModelScope)
                        }
                        pagingSourceTemp[key] = Pair(keyword, pagingFlow)
                        pagingFlowMap[key] = pagingFlow
                    }
                    update {
                        it.copy(
                            searchBusiness = searchBusinessList,
                            pagingFlowMap = pagingFlowMap,
                        )
                    }
                } else {
                    update {
                        it.copy(
                            searchBusiness = searchBusinessList,
                            pagingFlowMap = emptyMap(),
                        )
                    }
                }
            }.collect()

        }
    }

    fun changeKeyword(
        keyword: String?,
    ) {
        update {
            it.copy(
                searchKeyword = keyword,
            )
        }
    }



}