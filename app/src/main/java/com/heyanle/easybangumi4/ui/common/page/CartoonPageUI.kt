package com.heyanle.easybangumi4.ui.common.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easybangumi4.source_api.component.page.SourcePage
import com.heyanle.easybangumi4.ui.common.FastScrollToTopFab
import com.heyanle.easybangumi4.ui.common.page.list.SourceListPage
import com.heyanle.easybangumi4.ui.common.page.list.SourceListViewModel
import com.heyanle.easybangumi4.ui.common.page.list.SourceListViewModelFactory
import com.heyanle.easybangumi4.ui.common.page.listgroup.SourceListPageGroup
import com.heyanle.easybangumi4.ui.main.star.CoverStarViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Created by heyanlin on 2024/2/9 10:28.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CartoonPageUI(
    cartoonPage: SourcePage
) {
    when (cartoonPage) {
        is SourcePage.SingleCartoonPage -> {
            val coverStarViewModel = viewModel<CoverStarViewModel>()
            val scope = rememberCoroutineScope()
            var refreshing by remember { mutableStateOf(false) }
            val lazyGridState = rememberLazyGridState()
            val vm =
                viewModel<SourceListViewModel>(factory = SourceListViewModelFactory(cartoonPage))
            val state = rememberPullRefreshState(refreshing, onRefresh = {
                scope.launch {
                    refreshing = true
                    vm.refresh()
                    delay(500)
                    refreshing = false
                }
            })

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pullRefresh(state)
            ) {
                SourceListPage(
                    coverStarVm = coverStarViewModel,
                    page = cartoonPage,
                    lazyGridState = lazyGridState,
                    vm = vm
                )
                PullRefreshIndicator(
                    refreshing,
                    state,
                    Modifier.align(Alignment.TopCenter),
                    backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                FastScrollToTopFab(listState = lazyGridState, after = 20)
            }
        }

        is SourcePage.Group -> {
            SourceListPageGroup(cartoonPage)
        }

        else -> {}
    }
}


@Composable
fun CartoonPageListTab(
    cartoonPage: List<SourcePage>,
    selectionIndex: Int,
    onPageClick: (Int) -> Unit,
) {

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(8.dp, 0.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        itemsIndexed(cartoonPage) { i, page ->
            //val selectPage = (vm.currentSourceState as? SourceHomeViewModel.CurrentSourcePageState.Page )?.cartoonPage
            val select = selectionIndex == i

            FilterChip(
                selected = select,
                onClick = {
                    onPageClick(i)
                },
                label = { Text(text = page.label) },
                colors = FilterChipDefaults.filterChipColors(),
            )

        }
    }
}
