package org.easybangumi.next.shared.compose.detail.bangumi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.shared.LocalNavController
import org.easybangumi.next.shared.compose.common.collect_dialog.CartoonCollectDialog
import org.easybangumi.next.shared.data.bangumi.BgmSubject
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.foundation.InputMode
import org.easybangumi.next.shared.foundation.LocalUIMode
import org.easybangumi.next.shared.foundation.scroll_header.ScrollableHeaderBehavior
import org.easybangumi.next.shared.foundation.scroll_header.ScrollableHeaderScaffold
import org.easybangumi.next.shared.foundation.scroll_header.rememberScrollableHeaderState
import org.easybangumi.next.shared.foundation.shimmer.rememberShimmerState
import org.easybangumi.next.shared.foundation.view_model.vm
import org.easybangumi.next.shared.scheme.EasyScheme
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
internal val logger = logger("BangumiDetail")
@Composable
fun BangumiDetail(
    modifier: Modifier = Modifier,
    cartoonIndex: CartoonIndex,
    contentPaddingTop: Dp = 0.dp,
    onBack: (() -> Unit)? = null,
) {
    val vm = vm(::BangumiDetailVM, cartoonIndex)
    LaunchedEffect(Unit) {
        vm.loadSubjectIfFirst()
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
    val bgmCollectState = vm.ui.value.collectionState
    val cartoonInfoState = vm.ui.value.cartoonInfo

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
                modifier = Modifier.heightIn(
                    min = EasyScheme.size.cartoonCoverHeight + topAppBarHeight + contentPaddingTop + 20.dp,)
                    .header(topAppBarHeight + contentPaddingTop),
                coverUrl = vm.coverUrl,
                contentPaddingTop = topAppBarHeight + contentPaddingTop,
                isHeaderPin = isPinHeaderPin,
                subjectState = subjectState,
                bgmCollectionState = bgmCollectState,
                cartoonInfo = cartoonInfoState,
                onCollectClick = {
                    vm.openCollectDialog()
                },
                onPlayClick = {
                    vm.onPlayClick(nav)
                }
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

    }



    when (val dia = vm.ui.value.dialog) {
        is BangumiDetailVM.Dialog.CollectDialog -> {
            CartoonCollectDialog(
                cartoonCover = dia.cartoonCover,
                onDismissRequest = {
                    vm.dialogDismiss()
                }
            )
        }
        else -> {}
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

