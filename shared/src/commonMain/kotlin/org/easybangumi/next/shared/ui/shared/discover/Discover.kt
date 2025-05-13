package org.easybangumi.next.shared.ui.shared.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.carousel.CarouselDefaults
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.icerock.moko.resources.compose.stringResource
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.ResourceOr
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.data.cartoon.CartoonInfo
import org.easybangumi.next.shared.foundation.carousel.EasyHorizontalMultiBrowseCarousel
import org.easybangumi.next.shared.foundation.carousel.EasyHorizontalUncontainedCarousel
import org.easybangumi.next.shared.foundation.carousel.rememberEasyCarouselState
import org.easybangumi.next.shared.foundation.cartoon.CartoonCardWithCover
import org.easybangumi.next.shared.foundation.elements.ErrorElements
import org.easybangumi.next.shared.foundation.elements.LoadScaffold
import org.easybangumi.next.shared.foundation.elements.LoadingElements
import org.easybangumi.next.shared.foundation.image.AsyncImage
import org.easybangumi.next.shared.foundation.stringRes
import org.easybangumi.next.shared.foundation.view_model.vm
import org.easybangumi.next.shared.plugin.api.component.discover.DiscoverColumnJumpRouter
import org.easybangumi.next.shared.plugin.api.component.discover.DiscoverComponent
import org.easybangumi.next.shared.plugin.core.component.ComponentBusiness
import org.easybangumi.next.shared.resources.Res
import org.easybangumi.next.shared.ui.UI
import kotlin.text.ifEmpty

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
@Composable
fun Discover(
    discoverBusiness: ComponentBusiness<DiscoverComponent>,

    // 跳转详情页
    onJumpDetail: (CartoonIndex) -> Unit,

    // 发现页 【查看更多】区域点击跳转
    onJumpRouter: (DiscoverColumnJumpRouter) -> Unit,
) {
    val viewModel = vm(::DiscoverViewModel, discoverBusiness)

    val uiState = viewModel.ui.value

    val discoverColumnListState = viewModel.ui.value.discoverColumns

    LazyColumn {
        item {
            Banner(
                modifier = Modifier.fillMaxWidth().height(198.dp),
                data = uiState.banner,
                onClick = {

                },
            )
        }
        item {
            History(
                modifier = Modifier.fillMaxWidth(),
                data = uiState.history,
                onHistoryClick = {

                }
            )
        }

        discoverColumnListState.onOK {
            items(it) { state ->
                CartoonColumn(
                    modifier = Modifier.fillMaxWidth(),
                    state = state,
                    onCartoonClick = {

                    },
                    onJumpRouter = onJumpRouter
                )
            }
        }.onError {
            item {
                ErrorElements(
                    Modifier.fillMaxWidth(),
                    isRow = true,
                    errorMsg = it.errorMsg.ifEmpty { stringResource(Res.strings.net_error) },
                    onClick = {
                        viewModel.refreshColumnList()
                    },
                    other = {
                        Spacer(Modifier.size(12.dp))
                        Text(
                            text = stringResource(Res.strings.retry),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            fontStyle = FontStyle.Italic
                        )
                    }
                )

            }
        }.onLoading {
            item {
                LoadingElements(
                    Modifier.fillMaxWidth(),
                    isRow = true,
                    loadingMsg = it.loadingMsg.ifEmpty { stringResource(Res.strings.loading) })
            }
        }

    }


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
            data = data,
            onClick = onHistoryClick
        )
    }

}

@Composable
fun CartoonColumn(
    modifier: Modifier = Modifier,
    state: DiscoverViewModel.DiscoverColumnState,
    onCartoonClick: (CartoonCover) -> Unit,
    onJumpRouter: (DiscoverColumnJumpRouter) -> Unit,
) {
    Column(
        modifier = modifier
    ) {
        ColumnHeadline(
            text = state.column.label,
            action = {
                TextButton(onClick = {
                    onJumpRouter(state.column.jumpRouter)
                }) {
                    Text(
                        text = state.column.jumpTitle,
                        fontSize = 12.sp,
                    )
                }
            }
        )
        CartoonCoverRow(
            data = state.cartoonCovers,
            onClick = onCartoonClick
        )
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
    LoadScaffold(modifier, data = dataState) { ok ->
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