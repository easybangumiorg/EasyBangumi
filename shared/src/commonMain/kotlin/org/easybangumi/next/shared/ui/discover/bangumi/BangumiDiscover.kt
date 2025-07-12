package org.easybangumi.next.shared.ui.discover.bangumi

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cash.paging.compose.collectAsLazyPagingItems
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.ResourceOr
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.foundation.EasyTab
import org.easybangumi.next.shared.foundation.carousel.EasyHorizontalMultiBrowseCarousel
import org.easybangumi.next.shared.foundation.carousel.rememberEasyCarouselState
import org.easybangumi.next.shared.foundation.cartoon.CartoonCardWithCover
import org.easybangumi.next.shared.foundation.elements.LoadScaffold
import org.easybangumi.next.shared.foundation.lazy.pagingCommon
import org.easybangumi.next.shared.foundation.stringRes
import org.easybangumi.next.shared.resources.Res
import org.easybangumi.next.shared.scheme.EasyScheme
import org.easybangumi.next.shared.ui.UI

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
@Composable
fun BangumiDiscover(
    modifier: Modifier,
    vm: BangumiDiscoverViewModel,
    nestedScrollConnection: NestedScrollConnection? = null,
    onCoverClick: (CartoonCover) -> Unit,
    onTimelineClick: () -> Unit,
) {

    val uiState = vm.ui.value
    val ui = uiState
    val lazyGridState = rememberLazyGridState()

    val lazyPageState = ui.currentTab?.pagingFlow?.collectAsLazyPagingItems()

    LazyVerticalGrid(
        modifier = modifier.fillMaxWidth().run {
            if (nestedScrollConnection != null) {
                this.nestedScroll(nestedScrollConnection)
            } else {
                this
            }
        },
        state = lazyGridState,
        columns = GridCells.Adaptive(EasyScheme.size.cartoonCoverWidth),
        overscrollEffect = rememberOverscrollEffect(),
        contentPadding = PaddingValues(8.dp, 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {

        item(
            span = {
                GridItemSpan(maxLineSpan)
            },
            key = Unit
        ) {
            BannerHeadline(
                modifier = Modifier.fillMaxWidth(),
                title = "最热番剧",
                onTimelineClick = onTimelineClick,
            )
        }

        item(
            span = {
                GridItemSpan(maxLineSpan)
            },
            key = ui.bannerData
        ) {
            Banner(
                modifier = Modifier.fillMaxWidth(),
                data = ui.bannerData,
                onClick = onCoverClick
            )
        }

        item(
            span = {
                GridItemSpan(maxLineSpan)
            },
            key = uiState.tabList to uiState.currentIndex
        ) {
            Column (modifier = Modifier
                .fillMaxWidth()
            ) {
                RecommendTab(
                    modifier = Modifier.fillMaxWidth(),
                    data = uiState.tabList,
                    selection = ui.currentIndex,
                    onSelected = {
                       vm.changeCurrentIndex(it)
                    },
                    onRetry = {

                    }
                )
                HorizontalDivider()
            }
        }


        if (lazyPageState != null && lazyPageState.itemCount > 0) {
            items(lazyPageState.itemCount) {
                val item = lazyPageState[it]
                if (item != null) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        CartoonCardWithCover(
                            cartoonCover = item,
                            onClick = onCoverClick,
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
        EasyTab(
            modifier = Modifier.fillMaxWidth(),
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
