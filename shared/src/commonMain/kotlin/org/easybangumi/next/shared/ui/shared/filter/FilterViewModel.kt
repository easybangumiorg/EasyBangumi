package org.easybangumi.next.shared.ui.shared.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.ViewModelFactoryDsl
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import app.cash.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.map
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.foundation.view_model.AbsViewModel
import org.easybangumi.next.shared.plugin.api.component.filter.Filter
import org.easybangumi.next.shared.plugin.api.component.filter.FilterComponent
import org.easybangumi.next.shared.plugin.api.toDataState
import org.easybangumi.next.shared.plugin.core.component.ComponentBusiness
import org.easybangumi.next.shared.plugin.paging.CartoonFilterPagingSource
import kotlin.reflect.KClass

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
class FilterViewModel(
    private val param: String = "",
    private val filterBusiness: ComponentBusiness<FilterComponent>,
): AbsViewModel<FilterViewModel.UIState, FilterViewModel.LogicState>() {

    companion object {

    }

    data class UIState(
        val filterList: DataState<List<Filter>> = DataState.none(),
        val pageFlow: DataState<Flow<PagingData<CartoonCover>>> = DataState.none(),
    )


    data class LogicState(
        val filterList: DataState<List<Filter>> = DataState.none(),
    )

    override val initUiState: UIState
        get() = UIState()

    override val initLogicState: LogicState
        get() = LogicState()

    override suspend fun logicToUi(logicState: LogicState): UIState {
        val pageFlow = logicState.filterList.map {
            val pagingSource = CartoonFilterPagingSource(it, filterBusiness)
            Pager(
                config = PagingConfig(
                    pageSize = 20,
                    enablePlaceholders = false,
                ),
                initialKey = filterBusiness.runDirect {
                    firstKey(it)
                },
                pagingSourceFactory = { pagingSource }
            ).flow.cachedIn(viewModelScope)
        }
        return UIState(
            filterList = logicState.filterList,
            pageFlow = pageFlow
        )
    }

    init {
        // 1. init filter
        viewModelScope.launch {
            update {
                it.copy(
                    filterList = DataState.loading()
                )
            }
            val res = filterBusiness.run {
                paramFilter(param).toDataState()
            }
            update {
                it.copy(
                    filterList = res
                )
            }
        }
    }

    fun changeFilter(
        origin: List<Filter>,
        change: Filter,
    ) {
        viewModelScope.launch {
            update {
                it.copy(
                    filterList = DataState.loading()
                )
            }
            val res = filterBusiness.run {
                refreshFilter(origin, change).toDataState()
            }
            update {
                it.copy(
                    filterList = res
                )
            }
        }

    }


}