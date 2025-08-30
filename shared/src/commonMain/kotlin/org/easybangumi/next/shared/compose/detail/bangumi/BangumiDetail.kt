package org.easybangumi.next.shared.compose.detail.bangumi

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.easybangumi.next.shared.debug.DebugPage
import org.easybangumi.next.shared.debug.media_radar.mediaRadarDebugCover
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.platformInformation
import org.easybangumi.next.shared.LocalNavController
import org.easybangumi.next.shared.RouterPage
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.foundation.InputMode
import org.easybangumi.next.shared.foundation.LocalUIMode
import org.easybangumi.next.shared.foundation.scroll_header.ScrollableHeaderBehavior
import org.easybangumi.next.shared.foundation.scroll_header.ScrollableHeaderScaffold
import org.easybangumi.next.shared.foundation.scroll_header.rememberScrollableHeaderState
import org.easybangumi.next.shared.foundation.shimmer.rememberShimmerState
import org.easybangumi.next.shared.foundation.view_model.vm
import org.easybangumi.next.shared.scheme.EasyScheme
import org.easybangumi.next.shared.source.bangumi.model.BgmSubject
import kotlin.math.absoluteValue

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
private val logger = logger("BangumiDetail")
@Composable
fun BangumiDetail(
    modifier: Modifier = Modifier,
    cartoonIndex: CartoonIndex,
    contentPaddingTop: Dp = 0.dp,
    onBack: (() -> Unit)? = null,
) {
    val vm = vm(::BangumiDetailViewModel, cartoonIndex)
    LaunchedEffect(Unit) {
        vm.loadSubject()
    }
    val scope = rememberCoroutineScope()
    val scrollableHeaderState = rememberScrollableHeaderState()
    val scrollHeaderBehavior = ScrollableHeaderBehavior.discoverScrollHeaderBehavior(
        state = scrollableHeaderState,
    )

    val currentTab = vm.ui.value.currentTab

    val subjectState = vm.ui.value.subjectState
    val subjectShimmerState = rememberShimmerState(
        subjectState.isLoading()
    )

    val characterState = vm.ui.value.characterState
    val personState = vm.ui.value.personState

    val pagerState = rememberPagerState { vm.detailTabList.size }

    val isTopAppBarPin = remember( scrollableHeaderState.offset) {
        scrollableHeaderState.offset.absoluteValue > 20
    }
    val density = LocalDensity.current
    val topAppBarHeight = EasyScheme.size.topAppBarHeight
    val isPinHeaderPin = remember(
        scrollableHeaderState.offset, scrollableHeaderState.offsetLimit
    ) {
        scrollableHeaderState.offset.absoluteValue >= (scrollableHeaderState.offsetLimit.absoluteValue - 20)
    }

    val nav = LocalNavController.current
    Box(modifier) {
        ScrollableHeaderScaffold(
            modifier = modifier,
            behavior = scrollHeaderBehavior,
        ) {


            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.colorScheme.onSurface
            ) {
                BangumiContent(
                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainerLow).contentPointerScrollOpt(LocalUIMode.current.inputMode == InputMode.POINTER),
                    pagerState = pagerState,
                    vm = vm,
                    contentPadding = contentPadding
                )
            }


            BangumiDetailHeader(
                modifier = Modifier.height(
                    EasyScheme.size.cartoonCoverHeight + topAppBarHeight + contentPaddingTop + 20.dp,
                    ).header(topAppBarHeight + contentPaddingTop),
                coverUrl = vm.coverUrl,
                contentPaddingTop = topAppBarHeight + contentPaddingTop,
                isHeaderPin = isPinHeaderPin,
                subjectState = subjectState,
            )

            BangumiDetailTab(
                modifier = Modifier.fillMaxWidth().pinHeader(),
                isPin = isPinHeaderPin,
                vm = vm,
                currentIndex = pagerState.currentPage,
                onSelected = {
                    scope.launch {
                        logger.info("onSelected: $it, currentPage: ${pagerState.currentPage}")
                        pagerState.animateScrollToPage(it)
                    }
                }
            )

        }
        BangumiTopAppBar(
            modifier = Modifier.fillMaxWidth(),
            isPin = isTopAppBarPin,
            subjectState = subjectState,
            onBack = onBack,
        )

        if (platformInformation.isDebug) {
            Text(modifier = Modifier.padding(64.dp).clickable {
                mediaRadarDebugCover = vm.ui.value.subjectState.okOrNull()?.cartoonCover
                nav.navigate(RouterPage.Debug(DebugPage.MEDIA_RADAR.name))
            },text = "debug")
        }

    }





}

@Composable
fun BangumiTopAppBar(
    modifier: Modifier,
    isPin: Boolean,
    subjectState: DataState<BgmSubject>,
    onBack: (() -> Unit)?
) {
    val title = remember(subjectState) {
        subjectState.okOrNull()?.displayName
    }
    TopAppBar(
        modifier = modifier,
        title = {
            if (isPin && title != null) {
                Text(
                    text = title,
                    maxLines = 1,
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = {
                onBack?.invoke()
            }) {
                Icon(Icons.Filled.ArrowBack, "")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors().copy(
            containerColor = if (isPin) MaterialTheme.colorScheme.surfaceContainerHigh else Color.Transparent
        )
    )
}

