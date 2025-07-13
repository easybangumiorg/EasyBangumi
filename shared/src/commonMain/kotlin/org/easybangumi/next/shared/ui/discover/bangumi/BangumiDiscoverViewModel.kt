package org.easybangumi.next.shared.ui.discover.bangumi

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.PagingFlow
import org.easybangumi.next.lib.utils.ResourceOr
import org.easybangumi.next.lib.utils.newPagingFlow
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.foundation.view_model.StateViewModel
import org.easybangumi.next.shared.source.bangumi.business.BangumiApi
import org.easybangumi.next.shared.source.case.DiscoverSourceCase
import org.koin.core.component.inject
import kotlin.getValue

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

class BangumiDiscoverViewModel: StateViewModel<BangumiDiscoverViewModel.State>(State()) {

    data class RecommendTabState(
        val label: ResourceOr,
        val trendsFrom: BangumiApi.TrendsFrom,
        val pagingFlow: PagingFlow<CartoonCover>,
        val lazyGridState: LazyGridState = LazyGridState(0, 0),
    )


    data class State (
        val bannerData: DataState<List<CartoonCover>> = DataState.loading(),
        // Recommend
        val tabList: DataState<List<RecommendTabState>> = DataState.loading(),
    ) {

    }

    private val discoverSourceCase: DiscoverSourceCase by inject()

    init {
        //  Load discover
        viewModelScope.launch {
            async { loadBanner() }
            async { loadRecommendTabs() }
        }

    }



    private suspend fun loadBanner() {
        update { it.copy(bannerData = DataState.loading()) }
        val res = discoverSourceCase.getBangumiDiscoverBusiness().run {
            banner()
        }
        update { it.copy(bannerData = res) }
    }

    private suspend fun loadRecommendTabs() {
        update { it.copy(tabList = DataState.loading()) }
        val from =  discoverSourceCase.getBangumiDiscoverBusiness().runSuspendDirect {
            BangumiApi.TrendsFrom.entries.map {
                it to createTrendsPagingSource(it)
            }
        }.map {
            RecommendTabState(
                label = it.first.label,
                trendsFrom = it.first,
                pagingFlow = it.second.newPagingFlow().cachedIn(viewModelScope),
            )
        }
        update {
            it.copy(
                tabList = DataState.ok(from),
            )
        }
    }


}