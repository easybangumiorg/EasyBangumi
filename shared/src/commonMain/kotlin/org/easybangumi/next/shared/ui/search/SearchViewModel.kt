package org.easybangumi.next.shared.ui.search

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import app.cash.paging.PagingData
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.easybangumi.next.lib.utils.newPagingFlow
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.data.store.StoreProvider
import org.easybangumi.next.shared.foundation.view_model.BaseViewModel
import org.easybangumi.next.shared.foundation.view_model.LogicUI
import org.easybangumi.next.shared.plugin.api.component.SearchComponent
import org.easybangumi.next.shared.plugin.api.component.ComponentBusiness
import org.easybangumi.next.shared.plugin.paging.CartoonSearchPagingSource

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
class SearchViewModel(
    private val initSearchKey: String = "",
    private val searchBusiness: ComponentBusiness<SearchComponent>
) : BaseViewModel(), LogicUI<SearchViewModel.UIState, SearchViewModel.LogicState> {

    private val searchHistoryStore = StoreProvider.searchHistory

    data class SearchUIState(
        val searchKeyword: String,
        val searchData: Flow<PagingData<CartoonCover>>
    )

    data class UIState(
        val showKeyword: String = "",
        val searchData: SearchUIState? = null,
        val searchHistory: List<String> = emptyList()
    )

    data class LogicState(
        val showKeyword: String = "",
        val searchKeyword: String? = null,
        val searchHistory: List<String> = emptyList(),
    )


    private val uiState = mutableStateOf<UIState>(UIState())
    override val ui: State<UIState> = uiState

    private val logicState = MutableStateFlow<LogicState>(
        LogicState(
            searchKeyword = initSearchKey.ifEmpty { null },
            showKeyword = initSearchKey,
        )
    )
    override val logic: StateFlow<LogicState> = logicState

    init {
        // showKeyword and searchHistory
        // logic to ui
        viewModelScope.launch {
            logic.collectLatest { logicState ->
                uiState.value = uiState.value.copy(
                    showKeyword = logicState.showKeyword,
                    searchHistory = logicState.searchHistory
                )
            }
        }

        // search logic to ui
        viewModelScope.launch {
            logic.map { it.searchKeyword }.distinctUntilChanged().collectLatest { key ->
                if (key == null) {
                    uiState.value = uiState.value.copy(
                        searchData = null
                    )
                } else {
                    val pagingSource = CartoonSearchPagingSource(key, searchBusiness)
                    val flow = pagingSource.newPagingFlow()
                    uiState.value = uiState.value.copy(
                        searchData = SearchUIState(key, flow)
                    )

                    searchHistoryStore.update {
                        it.filter { it.key != key } + StoreProvider.SearchHistory(key, Clock.System.now().toEpochMilliseconds())
                    }
                }
            }
        }

        // searchHistory
        viewModelScope.launch {
            searchHistoryStore.flow().collectLatest { histories ->
                logicState.update {
                    it.copy(searchHistory = histories.sortedByDescending { it.time }.map { it.key })
                }

            }
        }
    }

    fun changeShowSearchKeyword(keyword: String) {
        logicState.update {
            it.copy(
                showKeyword = keyword,
                searchKeyword = if (keyword.isEmpty()) null else it.searchKeyword
            )
        }
    }

    fun search() {
        logicState.update {
            it.copy(
                searchKeyword = it.showKeyword.ifEmpty { null }
            )
        }
    }
}