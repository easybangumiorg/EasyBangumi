package org.easybangumi.next.shared.compose.search

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.focus.FocusRequester
import androidx.lifecycle.viewModelScope
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.flow.*
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.shared.compose.search.simple.SimpleSearchViewModel
import org.easybangumi.next.shared.data.store.StoreProvider
import org.easybangumi.next.shared.foundation.view_model.BaseViewModel
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
 *
 *  搜索历史
 *  分页模式 (simple)
 *  聚合模式
 *  Bangumi 模式（带检索）
 */
class SearchViewModel(
    defSearchWord: String,
    defSourceKey: String? = null,
): BaseViewModel() {

    val logger = logger()

    // 展示在 toolbar 上的文字，不一定是真正搜索的 key
    val searchBarText = mutableStateOf(defSearchWord)

    // 真正搜索的 keyword
    private val _searchFlow = MutableStateFlow(defSearchWord)
    val searchFlow = _searchFlow.asStateFlow()

    private val searchHistoryHelper = StoreProvider.searchHistory

    // 搜索历史
    val searchHistory = searchHistoryHelper.flow().map {
        it.sortedByDescending { it.time }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val focusFirstRequest = atomic(false)
    val focusRequester: FocusRequester = FocusRequester()


    private val sourceCase: SourceCase by inject()

    // 搜索业务
    val searchBusiness = sourceCase.searchBusiness().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {

    }

    // 普通模式
    val simpleVM: SimpleSearchViewModel by childViewModel {
        SimpleSearchViewModel(
            keywordFlow = _searchFlow,
            searchBusiness = searchBusiness,
            focusRequester = focusRequester,
            defSourceKey = defSourceKey,
        )
    }

    fun onRequestFocusFirst() {
        if (focusFirstRequest.compareAndSet(expect = false, update = true)) {
            try {
                focusRequester.requestFocus()
            }catch (e: Exception) {
                logger.error("request focus error", e)
            }

        }
    }

    fun search(keyword: String) {
        _searchFlow.update {
            keyword
        }
    }

}