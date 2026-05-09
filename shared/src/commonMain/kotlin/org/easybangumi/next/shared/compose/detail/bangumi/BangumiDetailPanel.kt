package org.easybangumi.next.shared.compose.detail.bangumi

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.easybangumi.next.shared.compose.common.collect_dialog.CartoonCollectDialog

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
fun BangumiDetailPanel(
    vm: BangumiDetailVM,
    onDismiss: () -> Unit,
) {
    val subjectState = vm.ui.value.subjectState
    val bgmCollectState = vm.ui.value.collectionState
    val cartoonInfoState = vm.ui.value.cartoonInfo
    val pagerState = rememberPagerState { vm.detailTabList.size }
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(0.dp)
    ) {
        // 顶部信息区（可滚动，无背景图片，无播放按钮，收藏按钮左对齐）
        item {
            BangumiDetailHeader(
                modifier = Modifier,
                coverUrl = vm.coverUrl,
                contentPaddingTop = 0.dp,
                isHeaderPin = true,
                subjectState = subjectState,
                bgmCollectionState = bgmCollectState,
                cartoonInfo = cartoonInfoState,
                onCollectClick = {
                    vm.openCollectDialog()
                },
                onPlayClick = {
                    // BottomSheet 模式下不使用
                },
                showPlayBtn = false,
                panelMode = true,
            )
        }

        // Tab 切换（固定在顶部）
        stickyHeader {
            BangumiDetailTab(
                modifier = Modifier.fillMaxWidth(),
                isPin = true,
                vm = vm,
                currentIndex = pagerState.currentPage,
                onSelected = {
                    scope.launch {
                        pagerState.animateScrollToPage(it)
                    }
                }
            )
        }

        // 内容区（填充剩余空间）
        item {
            BangumiContent(
                modifier = Modifier.fillParentMaxSize(),
                pagerState = pagerState,
                vm = vm,
                contentPadding = PaddingValues(0.dp),
                interactive = false,
            )
        }
    }

    // 收藏对话框
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
