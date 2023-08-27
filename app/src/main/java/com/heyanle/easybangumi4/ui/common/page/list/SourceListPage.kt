package com.heyanle.easybangumi4.ui.common.page.list

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.heyanle.bangumi_source_api.api.component.page.SourcePage
import com.heyanle.bangumi_source_api.api.entity.CartoonCover
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.ui.common.CartoonCardWithCover
import com.heyanle.easybangumi4.ui.common.CartoonCardWithoutCover
import com.heyanle.easybangumi4.ui.common.FastScrollToTopFab
import com.heyanle.easybangumi4.ui.common.PagingCommon
import com.heyanle.easybangumi4.ui.common.pagingCommon
import com.heyanle.easybangumi4.ui.main.star.CoverStarViewModel
import com.heyanle.easybangumi4.navigationDetailed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/2/25 20:44.
 * https://github.com/heyanLE
 */

@OptIn(
    ExperimentalAnimationApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun SourceListPage(
    modifier: Modifier = Modifier,
    listPage: SourcePage.SingleCartoonPage,
    header: (@Composable () -> Unit)? = null
) {

    val vm = viewModel<SourceListViewModel>(factory = SourceListViewModelFactory(listPage))
    val coverStarVm = viewModel<CoverStarViewModel>()
    val scope = rememberCoroutineScope()


    val pi = vm.curPager.value.collectAsLazyPagingItems()

    when (listPage) {
        is SourcePage.SingleCartoonPage.WithCover -> {
            SourceListPageContentWithCover(
                vm = vm,
                coverStarVm = coverStarVm,
                pagingItems = pi,
                scope = scope,
                header = header
            )
        }

        is SourcePage.SingleCartoonPage.WithoutCover -> {
            SourceListPageContentWithoutCover(
                vm = vm,
                coverStarVm = coverStarVm,
                pagingItems = pi,
                scope = scope,
                header = header
            )
        }
    }


}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SourceListPageContentWithCover(
    modifier: Modifier = Modifier,
    vm: SourceListViewModel,
    coverStarVm: CoverStarViewModel,
    pagingItems: LazyPagingItems<CartoonCover>,
    scope: CoroutineScope,
    header: (@Composable () -> Unit)? = null,

    ) {
    val nav = LocalNavController.current
    var refreshing by remember { mutableStateOf(false) }
    val state = rememberPullRefreshState(refreshing, onRefresh = {
        scope.launch {
            refreshing = true
            vm.refresh()
            delay(500)
            refreshing = false
        }
    })

    val haptic = LocalHapticFeedback.current
    val lazyGridState = rememberLazyGridState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(state)
            .then(modifier)
    ) {
        pagingItems.let { items ->

            if (items.itemCount > 0) {
                LazyVerticalGrid(
                    modifier = Modifier
                        .fillMaxSize(),
                    state = lazyGridState,
                    columns = GridCells.Adaptive(100.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(4.dp, 4.dp, 4.dp, 88.dp)
                ) {
                    header?.let {
                        item(
                            span = {
                                // LazyGridItemSpanScope:
                                // maxLineSpan
                                GridItemSpan(maxLineSpan)
                            }
                        ) {
                            it()
                        }
                    }
                    items(items.itemCount) { int ->

                        items[int]?.let { cover ->
                            CartoonCardWithCover(
                                modifier = Modifier.fillMaxWidth(),
                                star = coverStarVm.isCoverStarted(cover) ,
                                cartoonCover = cover,
                                onClick = {
                                    nav.navigationDetailed(it)
                                },
                                onLongPress = {
                                    coverStarVm.star(it)
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                },
                            )
                        }

                    }

                    pagingCommon(items)
                }
            }
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                if (items.itemCount <= 0) {
                    Spacer(modifier = Modifier.size(4.dp))
                    header?.invoke()
                }
                PagingCommon(items = items)
            }
        }

        //MD3PullRefreshIndicator(refreshing, state, modifier)
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

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun SourceListPageContentWithoutCover(
    modifier: Modifier = Modifier,
    vm: SourceListViewModel,
    coverStarVm: CoverStarViewModel,
    pagingItems: LazyPagingItems<CartoonCover>,
    scope: CoroutineScope,
    header: (@Composable () -> Unit)? = null,

    ) {
    val nav = LocalNavController.current
    var refreshing by remember { mutableStateOf(false) }
    val state = rememberPullRefreshState(refreshing, onRefresh = {
        scope.launch {
            refreshing = true
            vm.refresh()
            delay(500)
            refreshing = false
        }
    })

    val lazyState = rememberLazyStaggeredGridState()

    val haptic = LocalHapticFeedback.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(state)
            .then(modifier)
    ) {

        pagingItems.let { items ->
            if (pagingItems.itemCount > 0) {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Adaptive(150.dp),
                    state = lazyState,
                    verticalItemSpacing = 4.dp,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(4.dp, 4.dp, 4.dp, 88.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    header?.let {
                        item(
                            span = StaggeredGridItemSpan.FullLine
                        ) {
                            it()
                        }
                    }
                    items(items.itemCount) { index ->
                        items[index]?.let { cover ->
                            CartoonCardWithoutCover(
                                cartoonCover = cover,
                                star =  coverStarVm.isCoverStarted(cover),
                                onClick = {
                                    nav.navigationDetailed(it)
                                },
                                onLongPress = {
                                    coverStarVm.star(it)
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                },
                            )
                        }

                    }
                    pagingCommon(items)
                }
            }
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                if (items.itemCount <= 0) {
                    Spacer(modifier = Modifier.size(4.dp))
                    header?.invoke()
                }
                PagingCommon(items = items)
            }

        }

        PullRefreshIndicator(
            refreshing,
            state,
            Modifier.align(Alignment.TopCenter),
            backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        FastScrollToTopFab(lazyState, after = 20)
    }
}