package org.easybangumi.next.shared.ui.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cash.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.ResourceOr
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.data.cartoon.CartoonInfo
import org.easybangumi.next.shared.foundation.EasyTab
import org.easybangumi.next.shared.foundation.InputMode
import org.easybangumi.next.shared.foundation.LocalUIMode
import org.easybangumi.next.shared.foundation.carousel.EasyHorizontalMultiBrowseCarousel
import org.easybangumi.next.shared.foundation.carousel.EasyHorizontalUncontainedCarousel
import org.easybangumi.next.shared.foundation.carousel.rememberEasyCarouselState
import org.easybangumi.next.shared.foundation.cartoon.CartoonCardWithCover
import org.easybangumi.next.shared.foundation.elements.LoadScaffold
import org.easybangumi.next.shared.foundation.image.AsyncImage
import org.easybangumi.next.shared.foundation.lazy.pagingCommon
import org.easybangumi.next.shared.foundation.scroll_header.ScrollableHeaderBehavior
import org.easybangumi.next.shared.foundation.scroll_header.ScrollableHeaderScaffold
import org.easybangumi.next.shared.foundation.scroll_header.rememberScrollableHeaderState
import org.easybangumi.next.shared.foundation.stringRes
import org.easybangumi.next.shared.foundation.view_model.vm
import org.easybangumi.next.shared.plugin.api.component.discover.BannerHeadline
import org.easybangumi.next.shared.plugin.api.component.discover.DiscoverComponent
import org.easybangumi.next.shared.plugin.api.component.ComponentBusiness
import org.easybangumi.next.shared.resources.Res
import org.easybangumi.next.shared.scheme.EasyScheme
import org.easybangumi.next.shared.ui.UI
import org.easybangumi.next.shared.ui.discover.DiscoverViewModel.RecommendTabState

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
 *
 *  发现页模块，列表，从上到下排列
 *  【banner】from DiscoverComponent
 *  【history】from HistoryDatabase
 *  【column1】from DiscoverComponent
 *  【column2】from DiscoverComponent
 *  【column3】from DiscoverComponent
 */
private val logger = logger("Discover")

@Composable
fun Discover(
    modifier: Modifier = Modifier,
    discoverBusiness: ComponentBusiness<DiscoverComponent>,

    // 跳转详情页
    onJumpDetail: (CartoonIndex) -> Unit,
) {
    val viewModel = vm(::DiscoverViewModel, discoverBusiness)

    val uiState = viewModel.ui.value

    val tabList = uiState.tabList.okOrNull()

    val pagerState = rememberPagerState { tabList?.size?:0 }


    val scope = rememberCoroutineScope()


    val scrollableHeaderState = rememberScrollableHeaderState()
    val behavior = ScrollableHeaderBehavior.discoverScrollHeaderBehavior(
        state = scrollableHeaderState,

    )
    ScrollableHeaderScaffold(
        modifier = modifier,
        behavior = behavior,
    ) {
        if (tabList != null) {
            HorizontalPager(
                pagerState,
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
                contentPadding = contentPadding,
                userScrollEnabled = false,
            ) {
                val tab = tabList.getOrNull(it)
                LaunchedEffect(Unit) {
                    snapshotFlow {
                        tab?.lazyGridState?.firstVisibleItemIndex
                    }.collectLatest {
                        logger.info(it?.toString())
                    }
                }
                if (tab != null) {
                    val lazyPageState =  tab.pagingFlow.collectAsLazyPagingItems()
                    LazyVerticalGrid(
                        modifier = Modifier.fillMaxSize().contentPointerScrollOpt(LocalUIMode.current.inputMode == InputMode.POINTER),
                        state = tab.lazyGridState,
                        columns = GridCells.Adaptive(EasyScheme.size.cartoonCoverWidth + 4.dp),
                        overscrollEffect = rememberOverscrollEffect(),
                        contentPadding = PaddingValues(4.dp, 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (lazyPageState.itemCount > 0) {
                            items(lazyPageState.itemCount) {
                                val item = lazyPageState[it]
                                if (item != null) {
                                    CartoonCardWithCover(
                                        cartoonCover = item,
                                        onClick = {

                                        },
                                        onLongPress = {

                                        }
                                    )
                                }
                            }
                        }
                        pagingCommon(lazyPageState)
                    }

                }
            }

        }

        key(
            uiState.bannerHeadline, uiState.history
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().header()
            ) {
                BannerHeadline(
                    modifier = Modifier.fillMaxWidth(),
                    data = uiState.bannerHeadline,
                    onJumpTimeline = {

                    }
                )
                Banner(
                    modifier = Modifier.fillMaxWidth().height(EasyScheme.size.cartoonCoverHeight),
                    data = uiState.bannerData,
                    onClick = {

                    },
                )
                History(
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface),
                    data = uiState.history,
                    onHistoryClick = {

                    }
                )

            }
        }

        key(
            uiState.tabList, pagerState.currentPage
        ) {
            Column (modifier = Modifier
                .fillMaxWidth()
                .pinHeader()
            ) {
                RecommendTab(
                    modifier = Modifier.fillMaxWidth(),
                    data = uiState.tabList,
                    selection = pagerState.currentPage,
                    onSelected = {
                        scope.launch {
                            pagerState.animateScrollToPage(it)
                        }
                    },
                    onRetry = {

                    }
                )
                HorizontalDivider()
            }
        }

    }



}

@Composable
fun BannerHeadline(
    modifier: Modifier,
    data: BannerHeadline,
    onJumpTimeline: () -> Unit,
) {

    ListItem(
        modifier = modifier,
        headlineContent = {
            Text(
                text = stringRes(data.label),
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        trailingContent = if (data.hasTimelineEnter) {
            {
                TextButton(onClick = onJumpTimeline) {
                    Icon(Icons.Default.Timeline, contentDescription = stringRes(Res.strings.anim_timeline))
                    Text(
                        text = stringRes(Res.strings.anim_timeline),
                        fontSize = 12.sp,
                    )
                }
            }
        } else {
            null
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Banner(
    modifier: Modifier,
    data: DataState<List<CartoonCover>>,
    onClick: (CartoonCover) -> Unit,
) {
    LoadScaffold(Modifier, data = data) { ok ->
        val carouselState = rememberEasyCarouselState {
            ok.data.size
        }
        EasyHorizontalMultiBrowseCarousel(
            easyCarouselState = carouselState,
            modifier = modifier,
            showArc = !UI.isTouchMode(),
            userScrollEnabled = true,
        ) { index ->
            val cover = ok.data[index]
            Box(
                modifier = Modifier.fillMaxWidth().maskClip(RoundedCornerShape(16.dp)).clickable {
                    onClick(cover)
                }
            ) {
                AsyncImage(
                    cover.coverUrl,
                    modifier = Modifier.fillMaxSize(),
                    contentDescription = cover.name,
                    contentScale = ContentScale.FillWidth
                )

                Text(
                    cover.name,
                    modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).background(Color.Black.copy(0.6f))
                        .padding(16.dp, 0.dp),
                    color = Color.White,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

        }
    }

}

@Composable
fun History(
    modifier: Modifier,
    data: List<CartoonInfo>,
    onHistoryClick: (CartoonInfo) -> Unit,
) {
    Column (modifier) {
        ColumnHeadline(
            text = Res.strings.history,
            action = {
                TextButton(onClick = {}) {
                    Text(
                        text = stringRes(Res.strings.more),
                        fontSize = 12.sp,
                    )
                }
            }
        )
        CartoonCoverRow(
            modifier = Modifier.fillMaxWidth(),
            data = data,
            onClick = onHistoryClick
        )
    }

}

@Composable
fun RecommendTab(
    modifier: Modifier,
    data: DataState<List<RecommendTabState>>,
    selection: Int,
    onSelected: (Int) -> Unit,
    onRetry: () -> Unit,
) {
    LoadScaffold(
        modifier = modifier,
        data = data,
        isRow = false,
        errorRetry = {
            onRetry()
        },
        checkEmpty = true,

    ) {
        val data = it.data
        EasyTab(
            modifier = Modifier.fillMaxWidth(),
            size = data.size,
            selection = selection,
            onSelected = {
                if (it in data.indices) {
                    onSelected(it)
                }
            }
        ) { index, selected ->
            val tab = data[index]
            Text(
                text = stringRes(tab.tab.name),
            )
        }
    }

}

@Composable
fun ColumnHeadline(
    modifier: Modifier = Modifier,
    text: ResourceOr,
    action: (@Composable () -> Unit)? = null,
) {
    ListItem(
        modifier = modifier,
        headlineContent = {
            Text(
                text = stringRes(text),
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        trailingContent = action,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartoonCoverRow(
    modifier: Modifier = Modifier,
    data: DataState<List<CartoonCover>>,
    onClick: (CartoonCover) -> Unit,
) {
    LoadScaffold(modifier, data = data) { ok ->
        val carouselState = rememberEasyCarouselState {
            ok.data.size
        }
        EasyHorizontalUncontainedCarousel(
            easyCarouselState = carouselState,
            itemWidth = 154.dp,
            modifier = Modifier.fillMaxWidth(),
            showArc = !UI.isTouchMode(),
            userScrollEnabled = UI.isTouchMode(),
        ) { index ->
            val cover = ok.data[index]
            CartoonCardWithCover(
                modifier = Modifier.fillMaxWidth(),
                cartoonCover = cover,
                itemSize = 154.dp,
                itemIsWidth = true,
                onClick = {
                    onClick(cover)
                },
                onLongPress = {

                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartoonCoverRow(
    modifier: Modifier = Modifier,
    data: List<CartoonInfo>,
    onClick: (CartoonInfo) -> Unit,
) {
    val dataState = remember(data) {
        if (data.isEmpty()) {
            DataState.empty()
        } else {
            DataState.Ok(data)
        }
    }
    LoadScaffold(modifier, data = dataState, isRow = true) { ok ->
        val carouselState = rememberEasyCarouselState {
            ok.data.size
        }
        EasyHorizontalUncontainedCarousel(
            easyCarouselState = carouselState,
            itemWidth = 154.dp,
            modifier = Modifier.fillMaxWidth(),
            showArc = !UI.isTouchMode(),
            userScrollEnabled = UI.isTouchMode(),
        ) { index ->
            val cover = ok.data[index]
            CartoonCardWithCover(
                cartoonInfo = cover,
                itemSize = 198.dp,
                itemIsWidth = false,
                onClick = {
                    onClick(cover)
                },
                onLongPress = {

                }
            )
        }
    }
}