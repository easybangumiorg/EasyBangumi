package org.easybangumi.next.shared.compose.search.simple

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.utils.PagingFlow
import org.easybangumi.next.shared.LocalNavController
import org.easybangumi.next.shared.RouterPage
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.foundation.EasyTab
import org.easybangumi.next.shared.foundation.cartoon.CartoonCardWithCover
import org.easybangumi.next.shared.foundation.lazy.pagingCommon
import org.easybangumi.next.shared.foundation.stringRes
import org.easybangumi.next.shared.scheme.EasyScheme


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
internal val logger = logger("SimpleSearch")
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SimpleSearchTabPager(
    modifier: Modifier = Modifier,
    searchItems: List<SimpleSearchViewModel.SimpleSearchItem>,
    pagerState: PagerState,
    onScrollingChange: (Boolean) -> Unit = {},
    onCartoonClick: (CartoonCover) -> Unit = {},
    onCartoonLongPress: (CartoonCover) -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    if (searchItems.isNotEmpty()) {
        Column(modifier = modifier.fillMaxSize()) {
            // 顶部 Tab 行
            EasyTab(
                modifier = Modifier.fillMaxWidth(),
                size = searchItems.size,
                selection = pagerState.currentPage,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface,
                scrollable = true,
                onSelected = { index ->
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                }
            ) { index, selected ->
                val item = searchItems[index]
                Text(
                    text = stringRes(item.searchBusiness.source.manifest.label)
                )
            }

            Box(
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                // 底部 Horizontal Pager
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                ) { pageIndex ->
                    val currentItem = searchItems[pageIndex]

                    val lazyGridState = rememberLazyGridState()

                    LaunchedEffect(lazyGridState) {
                        snapshotFlow { lazyGridState.isScrollInProgress }
                            .collect { isScrolling ->
                                onScrollingChange(isScrolling)
                            }
                    }
                    SearchResultGrid(
                        modifier = Modifier.fillMaxSize(),
                        lazyGridState = lazyGridState,
                        pagingFlow = currentItem.flow,
                        onCartoonClick = onCartoonClick,
                        onCartoonLongPress = onCartoonLongPress
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SearchResultGrid(
    modifier: Modifier = Modifier,
    lazyGridState: LazyGridState,
    pagingFlow: PagingFlow<CartoonCover>,
    onCartoonClick: (CartoonCover) -> Unit,
    onCartoonLongPress: (CartoonCover) -> Unit
) {
    val lazyPagingItems = pagingFlow.collectAsLazyPagingItems()
    val cartoonHeight = EasyScheme.size.cartoonCoverHeight
    
    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Adaptive(EasyScheme.size.cartoonCoverWidth),
        state = lazyGridState,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (lazyPagingItems.itemCount > 0) {
            items(lazyPagingItems.itemCount) { index ->
                val item = lazyPagingItems[index]
                if (item != null) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        CartoonCardWithCover(
                            modifier = Modifier.fillMaxWidth(),
                            coverAspectRatio = null,
                            itemSize = EasyScheme.size.cartoonCoverHeight,
                            itemIsWidth = false,
                            cartoonCover = item,
                            onClick = { onCartoonClick(item) },
                            onLongPress = { onCartoonLongPress(item) }
                        )
                    }
                }
            }
        }
        
        // 添加分页状态处理
        pagingCommon(
            height = cartoonHeight,
            pagingItems = lazyPagingItems,
            isShowLoading = true,
            canRetry = true
        )
    }
}

@Composable
fun SimpleSearch(
    vm: SimpleSearchViewModel
) {
    val nav = LocalNavController.current
    val items = vm.searchItemList.collectAsState()
    val ite = items.value
    items.value?.let { searchItems ->
        SimpleSearchTabPager(
            modifier = Modifier.fillMaxSize(),
            searchItems = searchItems,
            pagerState = vm.pagerState,
            onScrollingChange = {
                vm.tryFreeFocus()
            },
            onCartoonClick = { cartoonCover ->
                // 处理动漫点击事件
                nav.navigate(RouterPage.Detail.fromCartoonIndex(cartoonCover.toCartoonIndex()))
            },
            onCartoonLongPress = { cartoonCover ->
                // 处理动漫长按事件
            }
        )
    }
}