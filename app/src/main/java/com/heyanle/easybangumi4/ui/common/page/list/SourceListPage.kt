package com.heyanle.easybangumi4.ui.common.page.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.navigationDetailed
import com.heyanle.easybangumi4.source_api.component.page.SourcePage
import com.heyanle.easybangumi4.source_api.entity.CartoonCover
import com.heyanle.easybangumi4.source_api.entity.toIdentify
import com.heyanle.easybangumi4.ui.common.CartoonCardWithCover
import com.heyanle.easybangumi4.ui.common.CartoonCardWithoutCover
import com.heyanle.easybangumi4.ui.common.PagingCommon
import com.heyanle.easybangumi4.ui.common.pagingCommon
import com.heyanle.easybangumi4.ui.main.star.CoverStarViewModel

/**
 * Created by heyanlin on 2024/2/9 10:29.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SourceListPage(
    coverStarVm: CoverStarViewModel,
    pageList: List<SourcePage.SingleCartoonPage>,
    lazyGridState: LazyGridState,
    lazyStaggeredGridState: LazyStaggeredGridState,
) {
    val star = coverStarVm.setFlow.collectAsState(initial = setOf<String>())
    val nav = LocalNavController.current
    val haptic = LocalHapticFeedback.current
    val vm =
        viewModel<SourceGroupListViewModel>(factory = SourceGroupListViewModelFactory(pageList))
    val paging = remember(vm.selected.intValue) {
        val index = vm.selected.intValue
        if (vm.pageList.isNotEmpty() && (index >= vm.pageList.size || index < 0)) {
            vm.selected.intValue = 0
            null
        } else if (vm.pageList.isNotEmpty()) {
            vm.pageList[vm.selected.intValue]
        } else {
            null
        }
    }
    val pagingItems = paging?.second?.collectAsLazyPagingItems()
    val lazyListState = rememberLazyListState(initialFirstVisibleItemIndex = vm.selected.intValue)

    if (paging?.first is SourcePage.SingleCartoonPage.WithCover) {
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize(),
            state = lazyGridState,
            columns = GridCells.Adaptive(100.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
            contentPadding = PaddingValues(4.dp, 4.dp, 4.dp, 88.dp)
        ) {
            item(
                span = {
                    GridItemSpan(maxLineSpan)
                }
            ) {
                SourceListGroupTab(
                    list = pageList,
                    curPage = vm.selected.intValue,
                    lazyListState = lazyListState,
                    onClick = {
                        vm.selected.intValue = it
                    }
                )
            }
            pagingItems?.let { pagingItems ->
                listPageWithCover(
                    pagingItems,
                    star.value,
                    onClick = {
                        nav.navigationDetailed(it)
                    },
                    onLongPress = {
                        coverStarVm.star(it)
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                )
                pagingCommon(pagingItems)
            }

        }


    } else {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(150.dp),
            state = lazyStaggeredGridState,
            verticalItemSpacing = 4.dp,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(4.dp, 4.dp, 4.dp, 88.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item(
                span = StaggeredGridItemSpan.FullLine
            ) {
                SourceListGroupTab(
                    list = pageList,
                    curPage = vm.selected.intValue,
                    lazyListState = lazyListState,
                    onClick = {
                        vm.selected.intValue = it
                    }
                )
            }
            pagingItems?.let {
                listPageWithoutCover(
                    pagingItems,
                    star.value,
                    onClick = {
                        nav.navigationDetailed(it)
                    },
                    onLongPress = {
                        coverStarVm.star(it)
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                )
                pagingCommon(pagingItems)
            }
        }
    }

    pagingItems?.let {
        PagingCommon(items = it)
    }


}

@Composable
fun SourceListPage(
    coverStarVm: CoverStarViewModel,
    page: SourcePage.SingleCartoonPage,
    lazyGridState: LazyGridState,
    lazyStaggeredGridState: LazyStaggeredGridState,
    vm: SourceListViewModel
) {
    val star = coverStarVm.setFlow.collectAsState(initial = setOf<String>())
    val nav = LocalNavController.current
    val haptic = LocalHapticFeedback.current
    val pagingItems = vm.curPager.value.collectAsLazyPagingItems()

    if (page is SourcePage.SingleCartoonPage.WithCover) {
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize(),
            state = lazyGridState,
            columns = GridCells.Adaptive(100.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
            contentPadding = PaddingValues(4.dp, 4.dp, 4.dp, 88.dp)
        ) {
            listPageWithCover(
                pagingItems,
                star.value,
                onClick = {
                    nav.navigationDetailed(it)
                },
                onLongPress = {
                    coverStarVm.star(it)
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            )
            pagingCommon(pagingItems)
        }
    } else {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(150.dp),
            state = lazyStaggeredGridState,
            verticalItemSpacing = 4.dp,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(4.dp, 4.dp, 4.dp, 88.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            listPageWithoutCover(
                pagingItems,
                star.value,
                onClick = {
                    nav.navigationDetailed(it)
                },
                onLongPress = {
                    coverStarVm.star(it)
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            )
            pagingCommon(pagingItems)
        }
    }


    PagingCommon(items = pagingItems)
}

@Composable
fun SourceListGroupTab(
    list: List<SourcePage.SingleCartoonPage>,
    curPage: Int,
    lazyListState: LazyListState,
    onClick: (Int) -> Unit,
) {
    //val state = rememberLazyListState(initialFirstVisibleItemIndex = curPage)

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        state = lazyListState
    ) {
        itemsIndexed(list) { index, item ->
            val selected = index == curPage
            Surface(
                shape = CircleShape,
                modifier =
                Modifier
                    .padding(2.dp, 8.dp),
                color = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
            ) {
                Text(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable {
                            onClick(index)
                        }
                        .padding(8.dp, 0.dp),
                    color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.W900,
                    text = item.label,
                    fontSize = 12.sp,
                )
            }
        }
    }
}

fun LazyGridScope.listPageWithCover(
    pagingItems: LazyPagingItems<CartoonCover>,
    starSet: Set<String>,
    onClick: (CartoonCover) -> Unit,
    onLongPress: (CartoonCover) -> Unit,
) {
    items(
        count = pagingItems.itemCount,
    ) {
        pagingItems[it]?.let { cover ->
            CartoonCardWithCover(
                modifier = Modifier.fillMaxWidth(),
                star = starSet.contains(cover.toIdentify()),
                cartoonCover = cover,
                onClick = onClick,
                onLongPress = onLongPress,
            )
        }
    }
}

fun LazyStaggeredGridScope.listPageWithoutCover(
    pagingItems: LazyPagingItems<CartoonCover>,
    starSet: Set<String>,
    onClick: (CartoonCover) -> Unit,
    onLongPress: (CartoonCover) -> Unit,
) {
    items(
        count = pagingItems.itemCount,
    ) {
        pagingItems[it]?.let { cover ->
            CartoonCardWithoutCover(
                cartoonCover = cover,
                star = starSet.contains(cover.toIdentify()),
                onClick = onClick,
                onLongPress = onLongPress,
            )
        }
    }
}