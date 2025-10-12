package org.easybangumi.next.shared.compose.search

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.easybangumi.next.shared.data.store.StoreProvider
import org.easybangumi.next.shared.foundation.view_model.BaseViewModel

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
    defSearchWord: String,
): BaseViewModel() {

    // 展示在 toolbar 上的文字，不一定是真正搜索的 key
    val searchBarText = mutableStateOf(defSearchWord)

    // 真正搜索的 keyword
    private val _searchFlow = MutableStateFlow(defSearchWord)
    val searchFlow = _searchFlow.asStateFlow()

    private val searchHistoryHelper = StoreProvider.searchHistory

    // 搜索历史
    val searchHistory = searchHistoryHelper.flow().map {
        it.sortedByDescending { it.time }
    }

}