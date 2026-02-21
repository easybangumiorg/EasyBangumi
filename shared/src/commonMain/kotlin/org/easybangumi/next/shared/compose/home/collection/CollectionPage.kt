package org.easybangumi.next.shared.compose.home.collection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import app.cash.paging.compose.collectAsLazyPagingItems
import org.easybangumi.next.shared.cartoon.collection.CartoonCollectionController
import org.easybangumi.next.shared.data.bangumi.BangumiConst
import org.easybangumi.next.shared.data.cartoon.CartoonInfo
import org.easybangumi.next.shared.foundation.cartoon.CartoonCoverCard
import org.easybangumi.next.shared.foundation.cartoon.CartoonCardWithCover
import org.easybangumi.next.shared.foundation.elements.EmptyElements
import org.easybangumi.next.shared.foundation.lazy.pagingCommon
import org.easybangumi.next.shared.data.bangumi.BgmCollect
import org.easybangumi.next.shared.foundation.FastScrollToTopFab
import org.easybangumi.next.shared.foundation.scroll_header.ScrollableHeaderBehavior
import org.easybangumi.next.shared.foundation.scroll_header.ScrollableHeaderScaffold
import org.easybangumi.next.shared.foundation.scroll_header.rememberScrollableHeaderState
import org.easybangumi.next.shared.foundation.stringRes

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
fun BangumiCollectionList(
    nestedScrollConnection: NestedScrollConnection? = null,
    data: CartoonCollectionController.CollectionData.BangumiCollection,
    selectionType: BangumiConst.BangumiCollectType,
    onTypeSelected: (BangumiConst.BangumiCollectType) -> Unit,
    selectionSet: Set<CartoonInfo>,
    isHapticFeedback: Boolean = true,
    onRefresh: () -> Unit,
    onClick: (CartoonInfo) -> Unit,
    onLongPress: (CartoonInfo) -> Unit,
) {

    val lazyGridState = rememberLazyGridState()
    val haptic = LocalHapticFeedback.current
    val lazyPagingItems = data.type2Collect[selectionType]?.collectAsLazyPagingItems()
    val showFilterBackground by remember {
        derivedStateOf {
            lazyGridState.firstVisibleItemIndex > 0 || lazyGridState.firstVisibleItemScrollOffset > 0
        }
    }

    val scrollableHeaderState = rememberScrollableHeaderState()
    val scrollHeaderBehavior = ScrollableHeaderBehavior.discoverScrollHeaderBehavior(
        state = scrollableHeaderState,
    )


    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        if (lazyPagingItems == null) {
            EmptyElements(
                modifier = Modifier
                    .fillMaxSize()
            )
        } else {
            LazyVerticalGrid(
                modifier = Modifier
                    .fillMaxSize(),
                state = lazyGridState,
                columns = GridCells.Adaptive(100.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(4.dp, 4.dp, 4.dp, 88.dp)
            ) {
                item(
                    span = {
                        // LazyGridItemSpanScope:
                        // maxLineSpan
                        GridItemSpan(maxLineSpan)
                    }
                ) {
                    BangumiFilterTabRow(
                        modifier = Modifier.fillMaxWidth(),
                        typeList = data.typeList,
                        selectionType = selectionType,
                        onTypeSelected = onTypeSelected,
                        useSurfaceBackground = false,
                    )
                }

                items(lazyPagingItems.itemCount) { index ->
                    val collect = lazyPagingItems[index]
                    val subject = collect?.subject
                    val cartoonInfo = collect?.toCartoonInfo()
                    if (collect != null && subject != null && cartoonInfo != null) {
                        CartoonCardWithCover(
                            cartoonCover = subject.cartoonCover,
                            star = selectionSet.any {
                                it.fromId == cartoonInfo.fromId &&
                                        it.fromSourceKey == cartoonInfo.fromSourceKey
                            },
                            onClick = {
                                onClick(cartoonInfo)
                            },
                            onLongPress = {
                                if (isHapticFeedback) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                onLongPress(cartoonInfo)
                            }
                        )
                    }
                }

                pagingCommon(height = 200.dp, pagingItems = lazyPagingItems)

                // 给回到顶部按钮留点空间
                item {
                    Spacer(modifier = Modifier.size(88.dp))
                }
            }

            FastScrollToTopFab(lazyGridState)
        }
    }

}

@Composable
private fun BangumiFilterTabRow(
    modifier: Modifier = Modifier,
    typeList: List<BangumiConst.BangumiCollectType>,
    selectionType: BangumiConst.BangumiCollectType,
    onTypeSelected: (BangumiConst.BangumiCollectType) -> Unit,
    useSurfaceBackground: Boolean = false,
) {
    val listState = rememberLazyListState()
    LazyRow(
        modifier = modifier.then(
            if (useSurfaceBackground) {
                Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)
            } else {
                modifier
            }
        ),
        state = listState,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(typeList) { type ->
            FilterClip(
                text = stringRes(type.label),
                selected = type == selectionType,
                onClick = {
                    if (type != selectionType) {
                        onTypeSelected(type)
                    }
                }
            )
        }
    }
}

@Composable
private fun FilterClip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        elevation = null,
        selected = selected,
        onClick = onClick,
        label = { Text(text) },
        modifier = modifier
    )
}

@Composable
fun LocalStarList(
    nestedScrollConnection: NestedScrollConnection? = null,
    starCartoon: List<CartoonInfo>,
    selectionSet: Set<CartoonInfo>,
    isHapticFeedback: Boolean = true,
    onRefresh: () -> Unit,
    onClick: (CartoonInfo) -> Unit,
    onLongPress: (CartoonInfo) -> Unit,
) {
    val lazyGridState = rememberLazyGridState()
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val refreshing = remember {
        mutableStateOf(false)
    }
//    val state = rememberPullRefreshState(refreshing.value, onRefresh = {
//        scope.launch {
//            refreshing.value = true
//            onRefresh()
//            delay(500)
//            refreshing.value = false
//        }
//
//    })

    Box(
        modifier = Modifier
            .fillMaxSize()
//            .pullRefresh(state)
    ) {
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize()
                .run {
                    if (nestedScrollConnection != null) {
                        nestedScroll(nestedScrollConnection)
                    } else {
                        this
                    }
                },
            state = lazyGridState,
            columns = GridCells.Adaptive(100.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(4.dp, 4.dp, 4.dp, 88.dp)
        ) {
            if (starCartoon.isEmpty()) {
                item(span = {
                    // LazyGridItemSpanScope:
                    // maxLineSpan
                    GridItemSpan(maxLineSpan)
                }) {
                    EmptyElements(
                        modifier = Modifier.height(256.dp),

                        )
                }
            }
            items(starCartoon) { info ->
                CartoonCoverCard(
                    modifier = Modifier,
                    model = info.coverUrl,
                    name = info.name,
                    onClick = {
                        onClick.invoke(info)
                    },
                    onLongPress = {
                        if (isHapticFeedback) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        onLongPress(info)
                    }
                )
            }
        }
//        PullRefreshIndicator(
//            refreshing.value,
//            state,
//            Modifier.align(Alignment.TopCenter),
//            backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
//            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
//        )
//        FastScrollToTopFab(listState = lazyGridState)
    }


}



private fun BgmCollect.toCartoonInfo(): CartoonInfo? {
    val subject = subject ?: return null
    val cover = subject.cartoonCover
    if (cover.id.isEmpty()) {
        return null
    }
    return CartoonInfo(
        fromId = cover.id,
        fromSourceKey = cover.source,
        name = cover.name,
        coverUrl = cover.coverUrl,
        detailedUrl = cover.webUrl,
        intro = cover.intro,
    )
}

