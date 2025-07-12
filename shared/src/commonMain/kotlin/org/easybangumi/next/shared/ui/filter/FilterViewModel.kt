package org.easybangumi.next.shared.ui.filter
//
//import androidx.lifecycle.viewModelScope
//import app.cash.paging.PagingData
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.launch
//import org.easybangumi.next.lib.utils.DataState
//import org.easybangumi.next.lib.utils.map
//import org.easybangumi.next.lib.utils.newPagingFlow
//import org.easybangumi.next.shared.data.cartoon.CartoonCover
//import org.easybangumi.next.shared.foundation.view_model.LogicUIViewModel
//import org.easybangumi.next.shared.plugin.api.component.filter.Filter
//import org.easybangumi.next.shared.plugin.api.component.filter.FilterComponent
//import org.easybangumi.next.shared.plugin.api.toDataState
//import org.easybangumi.next.shared.plugin.api.component.ComponentBusiness
//import org.easybangumi.next.shared.plugin.paging.CartoonFilterPagingSource
//
///**
// *    https://github.com/easybangumiorg/EasyBangumi
// *
// *    Copyright 2025 easybangumi.org and contributors
// *
// *    Licensed under the Apache License, Version 2.0 (the "License");
// *    you may not use this file except in compliance with the License.
// *    You may obtain a copy of the License at
// *
// *        http://www.apache.org/licenses/LICENSE-2.0
// */
//class FilterViewModel(
//    private val param: String = "",
//    private val filterBusiness: ComponentBusiness<FilterComponent>,
//): LogicUIViewModel<FilterViewModel.UIState, FilterViewModel.LogicState>(
//    LogicState(),
//    UIState()
//) {
//
//    companion object {
//
//    }
//
//    data class UIState(
//        val filterList: DataState<List<Filter>> = DataState.none(),
//        val pageFlow: DataState<Flow<PagingData<CartoonCover>>> = DataState.none(),
//    )
//
//
//    data class LogicState(
//        val filterList: DataState<List<Filter>> = DataState.none(),
//    )
//
//    override suspend fun logicToUi(logicState: LogicState): UIState {
//        val pageFlow = logicState.filterList.map {
//            val pagingSource = CartoonFilterPagingSource(it, filterBusiness)
//            pagingSource.newPagingFlow()
//        }
//        return UIState(
//            filterList = logicState.filterList,
//            pageFlow = pageFlow
//        )
//    }
//
//    init {
//        // 1. init filter
//        viewModelScope.launch {
//            update {
//                it.copy(
//                    filterList = DataState.loading()
//                )
//            }
//            val res = filterBusiness.run {
//                paramFilter(param)
//            }.toDataState()
//            update {
//                it.copy(
//                    filterList = res
//                )
//            }
//        }
//    }
//
//    fun changeFilter(
//        origin: List<Filter>,
//        change: Filter,
//    ) {
//        viewModelScope.launch {
//            update {
//                it.copy(
//                    filterList = DataState.loading()
//                )
//            }
//            val res = filterBusiness.run {
//                refreshFilter(origin, change)
//            }.toDataState()
//            update {
//                it.copy(
//                    filterList = res
//                )
//            }
//        }
//
//    }
//
//
//}