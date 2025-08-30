package org.easybangumi.next.shared.compose.discover.bangumi

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import app.cash.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.ResourceOr
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.foundation.EasyTab
import org.easybangumi.next.shared.foundation.InputMode
import org.easybangumi.next.shared.foundation.LocalUIMode
import org.easybangumi.next.shared.foundation.carousel.EasyHorizontalMultiBrowseCarousel
import org.easybangumi.next.shared.foundation.carousel.rememberEasyCarouselState
import org.easybangumi.next.shared.foundation.cartoon.CartoonCardWithCover
import org.easybangumi.next.shared.foundation.cartoon.CartoonCoverCardRect
import org.easybangumi.next.shared.foundation.elements.LoadScaffold
import org.easybangumi.next.shared.foundation.lazy.pagingCommon
import org.easybangumi.next.shared.foundation.scroll_header.DiscoverScrollHeaderBehavior
import org.easybangumi.next.shared.foundation.scroll_header.ScrollableHeaderBehavior
import org.easybangumi.next.shared.foundation.scroll_header.ScrollableHeaderScaffold
import org.easybangumi.next.shared.foundation.scroll_header.rememberScrollableHeaderState
import org.easybangumi.next.shared.foundation.shimmer.ShimmerHost
import org.easybangumi.next.shared.foundation.stringRes
import org.easybangumi.next.shared.resources.Res
import org.easybangumi.next.shared.scheme.EasyScheme
import org.easybangumi.next.shared.compose.UI
import org.easybangumi.next.shared.data.cartoon.CartoonCover

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
private val logger = logger("BangumiDiscover")
@Composable
fun BangumiDiscover(
    modifier: Modifier,
    vm: BangumiDiscoverViewModel,
    scrollHeaderBehavior: DiscoverScrollHeaderBehavior = ScrollableHeaderBehavior.discoverScrollHeaderBehavior(
        state = rememberScrollableHeaderState()),
    headerContainerColor: Color? = null,
    pinHeaderContainerColor: Color? = null,
    contentContainerColor: Color? = null,
    clipContent: Boolean = true,
    onCoverClick: (CartoonCover) -> Unit,
    onTimelineClick: () -> Unit,
) {

    val uiState = vm.ui.value
    val ui = uiState


    val tabList = uiState.tabList.okOrNull()
    val pagerState = rememberPagerState { tabList?.size?:0 }


    val scope = rememberCoroutineScope()


    ScrollableHeaderScaffold(
        modifier = modifier,
        behavior = scrollHeaderBehavior,
    ) {
        if (tabList != null) {
            HorizontalPager(
                pagerState,
                modifier = Modifier.fillMaxSize().run {
                    if (contentContainerColor != null) {
                        background(contentContainerColor)
                    } else {
                        this
                    }
                },
                contentPadding = contentPadding,
                userScrollEnabled = false,
            ) {
                val tab = tabList.getOrNull(it)
                if (tab != null) {

                    val lazyPageState =  tab.pagingFlow.collectAsLazyPagingItems()
                    logger.info(lazyPageState.loadState.toString())
                    val cartoonHeight = EasyScheme.size.cartoonCoverHeight
                    val pagingCommonModifier = remember {
                        Modifier.height(cartoonHeight).fillMaxWidth()
                    }
                    LazyVerticalGrid(
                        modifier = Modifier.fillMaxSize().contentPointerScrollOpt(LocalUIMode.current.inputMode == InputMode.POINTER).run {
                            if (clipContent) {
                                clip(RoundedCornerShape(16.dp))
                            } else {
                                this
                            }
                        },
                        state = tab.lazyGridState,
                        columns = GridCells.Adaptive(EasyScheme.size.cartoonCoverWidth),
                        overscrollEffect = rememberOverscrollEffect(),
                        contentPadding = PaddingValues(16.dp, 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (lazyPageState.itemCount > 0) {
                            items(lazyPageState.itemCount) {
                                val item = lazyPageState[it]
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
                                            onClick = onCoverClick,
                                            onLongPress = {

                                            }
                                        )
                                    }

                                }
                            }
                        }


                        pagingCommon(height = 200.dp,lazyPageState)
                    }
                }
            }

        }



        key(
            uiState.bannerData,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().header().run {
                    if (headerContainerColor != null) {
                        background(headerContainerColor)
                    } else {
                        this
                    }
                }
            ) {
                BannerHeadline(
                    modifier = Modifier.fillMaxWidth(),
                    title = "最热番剧",
                    onTimelineClick = onTimelineClick,
                )
                Banner(
                    modifier = Modifier.fillMaxWidth().padding(16.dp, 16.dp),
                    data = ui.bannerData,
                    onClick = onCoverClick
                )

            }
        }
        Column (modifier = Modifier
            .fillMaxWidth().pinHeader().run {
                if (pinHeaderContainerColor != null) {
                    background(pinHeaderContainerColor)
                } else {
                    this
                }
            }
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
//                HorizontalDivider()
        }

    }


}

@Composable
fun BannerHeadline(
    modifier: Modifier = Modifier,
    title: ResourceOr,
    onTimelineClick: () -> Unit,
) {

    ListItem(
        modifier = modifier,
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        ),
        headlineContent = {
            Text(
                text = stringRes(title),
//                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        trailingContent = {
            TextButton(
                onClick = onTimelineClick
            ) {
                Icon(Icons.Default.Timeline, contentDescription = stringRes(Res.strings.anim_timeline))
                Spacer(Modifier.size(4.dp))
                Text(
                    text = stringRes(Res.strings.anim_timeline),
                    fontSize = 12.sp,
                )
            }
        }
    )
}
@Composable
fun Banner(
    modifier: Modifier,
    data: DataState<List<CartoonCover>>,
    onClick: (CartoonCover) -> Unit,
) {
    LoadScaffold(
        modifier,
        data = data,
        onLoading = {
            ShimmerHost(
                modifier = Modifier.fillMaxWidth().height(EasyScheme.size.cartoonCoverHeight),
                visible = true,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    repeat(3) {
                        CartoonCoverCardRect(
                            modifier = Modifier.drawRectWhenShimmerVisible(),
//                            cardBackgroundColor = Color.Black
                        )
                    }
                }
            }

        }
    ) { ok ->
        val carouselState = rememberEasyCarouselState {
            ok.data.size
        }
        EasyHorizontalMultiBrowseCarousel(
            easyCarouselState = carouselState,
            modifier = Modifier,
            showArc = !UI.isTouchMode(),
            userScrollEnabled = true,
        ) { index ->
            val cover = ok.data[index]

            CartoonCardWithCover(
                modifier = Modifier.fillMaxWidth().maskClip(RoundedCornerShape(16.dp)).clickable {
                    onClick(cover)
                },
                onClick = {
                    onClick(cover)
                },
                onLongPress = {

                },
                cartoonCover = cover
            )
        }
    }

}


@Composable
fun RecommendTab(
    modifier: Modifier,
    data: DataState<List<BangumiDiscoverViewModel.RecommendTabState>>,
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
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
            EasyTab(
                modifier = Modifier.width(it.data.size * EasyScheme.size.tabWidth),
                size = data.size,
                selection = selection,
                containerColor = Color.Transparent,
                onSelected = {
                    if (it in data.indices) {
                        onSelected(it)
                    }
                }
            ) { index, selected ->
                val tab = data[index]
                Text(
                    text = stringRes(tab.label),
                )
            }
        }
    }

}
