package org.easybangumi.next.shared.ui.shared.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.CarouselDefaults
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.foundation.carousel.EasyHorizontalMultiBrowseCarousel
import org.easybangumi.next.shared.foundation.carousel.rememberEasyCarouselState
import org.easybangumi.next.shared.foundation.elements.LoadScaffold
import org.easybangumi.next.shared.foundation.image.AsyncImage
import org.easybangumi.next.shared.foundation.view_model.vm
import org.easybangumi.next.shared.plugin.api.component.discover.DiscoverColumnJumpRouter
import org.easybangumi.next.shared.plugin.api.component.discover.DiscoverComponent
import org.easybangumi.next.shared.plugin.core.component.ComponentBusiness
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

    Column {
        Banner(modifier = Modifier.fillMaxWidth().height(200.dp), data = uiState.banner) {

        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Banner(
    modifier: Modifier,
    data: DataState<List<CartoonCover>>,
    onClick: (CartoonCover) -> Unit,
){
    LoadScaffold(Modifier, data = data) { ok ->
        val carouselState = rememberEasyCarouselState {
            ok.data.size
        }
        EasyHorizontalMultiBrowseCarousel(
            easyCarouselState = carouselState,
            modifier = modifier,
            showArc = !UI.isTouchMode(),

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

                Text(cover.name,
                    modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).background(Color.Black.copy(0.6f)).padding(16.dp, 0.dp),
                    color = Color.White,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

        }
    }

}