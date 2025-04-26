package org.easybangumi.next.shared.ui.shared.discover

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.map
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.data.cartoon.CartoonInfo
import org.easybangumi.next.shared.data.room.dao.CartoonInfoDao
import org.easybangumi.next.shared.foundation.view_model.LogicUIViewModel
import org.easybangumi.next.shared.plugin.api.component.discover.DiscoverColumn
import org.easybangumi.next.shared.plugin.api.component.discover.DiscoverComponent
import org.easybangumi.next.shared.plugin.api.toDataState
import org.easybangumi.next.shared.plugin.core.component.ComponentBusiness
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
class DiscoverViewModel (
    private val discoverBusiness: ComponentBusiness<DiscoverComponent>
): LogicUIViewModel<DiscoverViewModel.UIState, DiscoverViewModel.LogicState>() {

    companion object {
        const val HISTORY_LIMIT = 16
    }

    private val cartoonInfoDao: CartoonInfoDao by inject()

    // ==== UI State ==
    data class DiscoverColumnState (
        val column: DiscoverColumn,
        val cartoonCovers: DataState<List<CartoonCover>> = DataState.none(),
    )

    data class UIState (
        val banner: DataState<List<CartoonCover>> = DataState.none(),
        val history: List<CartoonInfo> = emptyList(),
        val discoverColumns: DataState<List<DiscoverColumnState>> = DataState.none()
    )

    // ==== Logic State ==

    data class LogicState(
        val banner: DataState<List<CartoonCover>> = DataState.none(),
        val history: List<CartoonInfo> = emptyList(),
        val discoverColumns: DataState<List<DiscoverColumn>> = DataState.none(),
        val discoverColumnDataMap: Map<DiscoverColumn, DataState<List<CartoonCover>>> = emptyMap(),
    )

    override val initLogicState: LogicState
        get() = LogicState()

    override val initUiState: UIState
        get() = UIState()

    override suspend fun logicToUi(logicState: LogicState): UIState {
        return UIState(
            banner = logicState.banner,
            history = logicState.history,
            discoverColumns = logicState.discoverColumns.map { columnList ->
                columnList.map { column ->
                    DiscoverColumnState(
                        column = column,
                        cartoonCovers = logicState.discoverColumnDataMap[column] ?: DataState.none()
                    )
                }
            }
        )
    }

    init {
        // 1. collect history
        viewModelScope.launch {
            cartoonInfoDao.flowHistory(HISTORY_LIMIT).collectLatest { list ->
                update {
                    it.copy(history = list)
                }
            }
        }

        // 2. Load discover
        viewModelScope.launch {
            async { loadBanner() }
            async { loadColumnList() }
        }

    }



    private suspend fun loadBanner() {
        update { it.copy(banner = DataState.loading()) }
        val res = discoverBusiness.run {
            banner().toDataState()
        }
        update { it.copy(banner = res) }
    }

    private suspend fun loadColumnList() {
        update { it.copy(discoverColumns = DataState.loading()) }
        val res = discoverBusiness.run {
            columnList().toDataState()
        }
        update { it.copy(discoverColumns = res) }
        res.onOK { columnList ->
            columnList.forEach {
                viewModelScope.async { loadColumn(it) }
            }
        }
    }

    private suspend fun loadColumn(column: DiscoverColumn) {
        update { it.copy(discoverColumnDataMap = it.discoverColumnDataMap + (column to DataState.loading())) }
        val res = discoverBusiness.run {
            loadColumn(column).toDataState()
        }
        update { it.copy(discoverColumnDataMap = it.discoverColumnDataMap + (column to res)) }
    }


}