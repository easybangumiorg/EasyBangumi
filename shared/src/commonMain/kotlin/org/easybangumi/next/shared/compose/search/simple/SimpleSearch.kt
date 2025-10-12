package org.easybangumi.next.shared.compose.search.simple

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.launch
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
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SimpleSearchTabPager(
    modifier: Modifier = Modifier,
    searchItems: List<SimpleSearchViewModel.SimpleSearchItem>,
    pagerState: PagerState,
    onCartoonClick: (org.easybangumi.next.shared.data.cartoon.CartoonCover) -> Unit = {},
    onCartoonLongPress: (org.easybangumi.next.shared.data.cartoon.CartoonCover) -> Unit = {}
) {


    if (searchItems.isNotEmpty()) {
        val coroutineScope = rememberCoroutineScope()
//        val pagerState = rememberPagerState(
//            pageCount = { searchItems.size }
//        )
        
        Column(modifier = modifier.fillMaxWidth().fillMaxSize()) {
            // 顶部 Tab 行
            EasyTab(
                modifier = Modifier.fillMaxWidth(),
                size = searchItems.size,
                selection = pagerState.currentPage,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
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
                    modifier = Modifier.fillMaxSize()
                ) { pageIndex ->
                    val currentItem = searchItems[pageIndex]
                    SearchResultGrid(
                        modifier = Modifier.fillMaxSize(),
                        pagingFlow = currentItem.flow,
                        onCartoonClick = onCartoonClick,
                        onCartoonLongPress = onCartoonLongPress
                    )
                }
            }
            

        }
    } else {
        // 空状态
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Text("暂无搜索数据")
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SearchResultGrid(
    modifier: Modifier = Modifier,
    pagingFlow: org.easybangumi.next.lib.utils.PagingFlow<org.easybangumi.next.shared.data.cartoon.CartoonCover>,
    onCartoonClick: (org.easybangumi.next.shared.data.cartoon.CartoonCover) -> Unit,
    onCartoonLongPress: (org.easybangumi.next.shared.data.cartoon.CartoonCover) -> Unit
) {
    val lazyPagingItems = pagingFlow.collectAsLazyPagingItems()
    val cartoonHeight = EasyScheme.size.cartoonCoverHeight
    
    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Adaptive(EasyScheme.size.cartoonCoverWidth),
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
    val items = vm.searchItemList.collectAsState()
    items.value?.let { searchItems ->
        SimpleSearchTabPager(
            modifier = Modifier.fillMaxSize(),
            searchItems = searchItems,
            pagerState = vm.pagerState,
            onCartoonClick = { cartoonCover ->
                // 处理动漫点击事件
            },
            onCartoonLongPress = { cartoonCover ->
                // 处理动漫长按事件
            }
        )
    }
}