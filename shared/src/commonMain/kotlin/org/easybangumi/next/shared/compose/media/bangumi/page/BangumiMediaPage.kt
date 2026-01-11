package org.easybangumi.next.shared.compose.media.bangumi.page

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.easybangumi.next.shared.compose.media.bangumi.BangumiMediaCommonVM
import org.easybangumi.next.shared.foundation.EasyTab
import org.easybangumi.next.shared.foundation.stringRes
import org.easybangumi.next.shared.resources.Res
import kotlin.collections.get
import kotlin.invoke


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
sealed class BangumiMediaSubPage(
    val label: @Composable () -> Unit,
    val content: @Composable (BangumiMediaCommonVM) -> Unit,
) {
    data object Detail: BangumiMediaSubPage (
        label = { Text(stringRes(Res.strings.detailed)) },
        content = {
            BangumiMediaDetailSubPage(it)
        }
    )

    data object Comment: BangumiMediaSubPage (
        label = { Text(stringRes(Res.strings.comment) )},
        content = {
            BangumiMediaCommentSubPage(it)
        }
    )
}

private val bangumiMediaSubPageList = listOf(
    BangumiMediaSubPage.Detail,
    BangumiMediaSubPage.Comment
)

@Composable
fun BangumiMediaPage(
    commonVM: BangumiMediaCommonVM,
    modifier: Modifier
) {
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState {
        bangumiMediaSubPageList.size
    }
    Column (modifier) {
        EasyTab(
            modifier = Modifier.fillMaxWidth(),
            scrollable = true,
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
            size = bangumiMediaSubPageList.size,
            selection = pagerState.currentPage,
            onSelected = {
                scope.launch {
                    pagerState.animateScrollToPage(it)
                }
            },
            tabs = { index, selected ->
                val tab = bangumiMediaSubPageList[index]
                tab.label.invoke()
            }
        )
        HorizontalDivider(modifier = Modifier.fillMaxWidth())
        HorizontalPager(
            pagerState,
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            val tab = bangumiMediaSubPageList[it]
            Box(
                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                tab.content(commonVM)
            }
        }
    }
}