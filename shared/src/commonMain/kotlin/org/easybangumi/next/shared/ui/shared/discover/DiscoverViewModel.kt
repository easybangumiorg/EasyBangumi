package org.easybangumi.next.shared.ui.shared.discover

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.PagingFlow
import org.easybangumi.next.lib.utils.map
import org.easybangumi.next.lib.utils.newPagingFlow
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.data.cartoon.CartoonInfo
import org.easybangumi.next.shared.data.room.dao.CartoonInfoDao
import org.easybangumi.next.shared.foundation.view_model.StateViewModel
import org.easybangumi.next.shared.plugin.api.component.discover.BannerHeadline
import org.easybangumi.next.shared.plugin.api.component.discover.DiscoverComponent
import org.easybangumi.next.shared.plugin.api.component.discover.RecommendTab
import org.easybangumi.next.shared.plugin.api.toDataState
import org.easybangumi.next.shared.plugin.api.component.ComponentBusiness
import org.easybangumi.next.shared.plugin.paging.CartoonRecommendPagingSource
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
): StateViewModel<DiscoverViewModel.State>(
    State(discoverBusiness.runDirect { bannerHeadline() }),
) {

    companion object {
        const val HISTORY_LIMIT = 16
    }

    private val cartoonInfoDao: CartoonInfoDao by inject()

    // ==== State ==

    data class RecommendTabState(
        val tab: RecommendTab,
        val pagingFlow: PagingFlow<CartoonCover>,
        val lazyGridState: LazyGridState = LazyGridState(0, 0),
    )

    data class State (
        val bannerHeadline: BannerHeadline,
        val bannerData: DataState<List<CartoonCover>> = DataState.none(),

        val history: List<CartoonInfo> = emptyList(),

        // Recommend
        val tabList: DataState<List<RecommendTabState>> = DataState.none(),

    ) {

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
            async { loadRecommend() }
        }

    }

//    fun onTabSelected(index: Int) {
//        update { it.copy(selection = index) }
//    }



    private suspend fun loadBanner() {
        update { it.copy(bannerData = DataState.loading()) }
        val res = discoverBusiness.run {
            banner()
        }.toDataState()
        update { it.copy(bannerData = res) }
    }

    private suspend fun loadRecommend() {
        update { it.copy(tabList = DataState.loading()) }
        val res = discoverBusiness.run {
            recommendTab()
        }.toDataState().map {
            it.map {
                val pagingSource = CartoonRecommendPagingSource(it, discoverBusiness)
                val flow = pagingSource.newPagingFlow().cachedIn(viewModelScope)
                RecommendTabState(it, flow)
            }
        }
        update {
            it.copy(
                tabList = res,
            )
        }
    }


}