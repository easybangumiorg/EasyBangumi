package org.easybangumi.next.shared.ui.shared.page

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import app.cash.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.map
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.foundation.view_model.AbsViewModel
import org.easybangumi.next.shared.plugin.api.component.page.CartoonPage
import org.easybangumi.next.shared.plugin.api.component.page.PageComponent
import org.easybangumi.next.shared.plugin.api.component.page.PageTab
import org.easybangumi.next.shared.plugin.api.toDataState
import org.easybangumi.next.shared.plugin.core.component.ComponentBusiness
import org.easybangumi.next.shared.plugin.paging.CartoonPagePagingSource
import org.koin.core.component.KoinComponent

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
class CartoonPageViewModel(
    private val cartoonPage: CartoonPage,
    private val pageBusiness: ComponentBusiness<PageComponent>
): AbsViewModel<CartoonPageViewModel.UIState, CartoonPageViewModel.LogicState>() {

    data class UIState(
        val tabList: DataState<List<PageTab>> = DataState.none(),
        val pageMap: DataState<Map<PageTab,  Flow<PagingData<CartoonCover>>>> = DataState.none()
    )


    data class LogicState(
        val tabList: DataState<List<PageTab>> = DataState.none(),
    )

    override val initUiState: UIState
        get() = UIState()
    override val initLogicState: LogicState
        get() = LogicState()

    override suspend fun logicToUi(logicState: LogicState): UIState {
        val pageFlow = logicState.tabList.map {
            it.associateWith {
                val pagingSource = CartoonPagePagingSource(it, pageBusiness)
                Pager(
                    config = PagingConfig(
                        pageSize = 20,
                        enablePlaceholders = false,
                    ),
                    initialKey = it.initKey,
                    pagingSourceFactory = { pagingSource }
                ).flow.cachedIn(viewModelScope)
            }
        }
        return UIState(
            tabList = logicState.tabList,
            pageMap = pageFlow
        )
    }

    init {

        // 1. load tab
        viewModelScope.launch {
            update { it.copy(tabList = DataState.loading()) }
            val res = pageBusiness.run {
                getPageTab(cartoonPage).toDataState()
            }
            update { it.copy(tabList = res) }
        }
    }



}