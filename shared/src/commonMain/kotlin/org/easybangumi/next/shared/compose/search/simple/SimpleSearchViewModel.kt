package org.easybangumi.next.shared.compose.search.simple

import androidx.compose.foundation.pager.PagerState
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.utils.PagingFlow
import org.easybangumi.next.lib.utils.newPagingFlow
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.foundation.view_model.BaseViewModel
import org.easybangumi.next.shared.source.api.component.ComponentBusiness
import org.easybangumi.next.shared.source.api.component.search.SearchComponent
import org.easybangumi.next.shared.source.api.component.search.createPagingSource
import kotlin.collections.emptyList


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

class SimpleSearchViewModel(
    val keywordFlow: StateFlow<String?>,
    val searchBusiness: StateFlow<List<ComponentBusiness<SearchComponent>>>,
    var defSourceKey: String? = null
): BaseViewModel() {

    private val enable: MutableStateFlow<Boolean> = MutableStateFlow(true)

    val pagerState = PagerState(0, ) {
        searchItemList.value?.size ?: 0
    }

    data class SimpleSearchItem(
        val searchBusiness: ComponentBusiness<SearchComponent>,
        val flow: PagingFlow<CartoonCover>
    )

    private val _searchItemList = MutableStateFlow<List<SimpleSearchItem>?>(emptyList())
    val searchItemList = _searchItemList.asStateFlow()

    // sourceKey to Pair<keyword, flow>
    val pagingSourceTemp = mutableMapOf<String, Pair<String, PagingFlow<CartoonCover>>>()

    init {
        viewModelScope.launch {
            combine(
                keywordFlow,
                searchBusiness,
                enable
            ) {keyword, searchBusiness, enable ->
                if (enable && !keyword.isNullOrBlank()) {
                    searchBusiness.map { business ->
                        val key = business.source.key
                        val temp = pagingSourceTemp[key]
                        val pagingFlow = if (temp != null && temp.first == keyword) {
                            temp.second
                        } else {
                            business.createPagingSource(keyword).newPagingFlow()
                        }
                        pagingSourceTemp[key] = Pair(keyword, pagingFlow)
                        SimpleSearchItem(
                            searchBusiness = business,
                            flow = pagingFlow
                        )
                    }
                } else {
                    emptyList()

                }
            }.collectLatest { res ->
                var index = pagerState.currentPage
                _searchItemList.update {
                    res
                }
                if (index !in res.indices) {
                    index = 0
                }
                if (defSourceKey != null) {
                    val defIndex = res.indexOfFirst { it.searchBusiness.source.key == defSourceKey }
                    if (defIndex != -1) {
                        index = defIndex
                    }
                    defSourceKey = null
                }
                if (pagerState.currentPage != index) {
                    pagerState.scrollToPage(index)
                }
            }
        }
    }

    fun enable() {
        enable.update { true }
    }

    fun disable() {
        enable.update { false }
    }


}