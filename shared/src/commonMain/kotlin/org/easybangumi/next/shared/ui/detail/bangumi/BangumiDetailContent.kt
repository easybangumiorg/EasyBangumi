package org.easybangumi.next.shared.ui.detail.bangumi

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import kotlinx.datetime.format.Padding
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.shared.foundation.EasyTab
import org.easybangumi.next.shared.foundation.elements.LoadScaffold
import org.easybangumi.next.shared.foundation.stringRes
import org.easybangumi.next.shared.resources.Res
import org.easybangumi.next.shared.scheme.EasyScheme
import org.easybangumi.next.shared.scheme.LocalSizeScheme
import org.easybangumi.next.shared.source.bangumi.model.BgmSubject

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
private val logger = logger("BangumiDetailContent")

@Composable
fun BangumiDetailTab(
    modifier: Modifier,
    isPin: Boolean,
    vm: BangumiDetailViewModel,
    currentIndex: Int,
    onSelected: (Int) -> Unit,
){

    logger.info("isPin: $isPin, currentIndex: $currentIndex, detailTabList: ${vm.detailTabList.size}")
    Box(modifier = modifier.fillMaxWidth().background(if (isPin) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surfaceContainerLowest), contentAlignment = Alignment.TopCenter) {
        EasyTab(
            modifier = Modifier.width((vm.detailTabList.size * EasyScheme.size.tabWidth)),
            size = vm.detailTabList.size,
            selection = currentIndex,
            containerColor = Color.Transparent,
//            containerColor = if (isPin) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.surfaceContainerLowest,
//        contentColor = if (isPin) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurface,
            onSelected = {
                if (it in vm.detailTabList.indices) {
                    onSelected(it)
                }
            }
        ) { index, selected ->
            val tab = vm.detailTabList[index]
            Text(
                text = stringRes(tab.title),
            )
        }
    }


}

@Composable
fun BangumiContent(
    modifier: Modifier,
    pagerState: PagerState,
    vm: BangumiDetailViewModel,
    contentPadding: PaddingValues,
){
    val subjectState = vm.ui.value.subjectState
    HorizontalPager(
        pagerState,
        modifier = modifier,
        userScrollEnabled = false,
        contentPadding = contentPadding
    ){
        Box(Modifier.background(Color.Red))
        val tab = vm.detailTabList.getOrNull(it)
        if (tab == null) {
            Text(
                modifier = Modifier.fillMaxSize(),
                text = "Tab not found",
                color = Color.Red,
            )
        } else {
            when (tab) {
                BangumiDetailViewModel.DetailTab.DETAIL -> {
                    BangumiDetailDetail(
                        modifier = Modifier.fillMaxSize(),
                        vm = vm,
                        subjectState = subjectState,
                    )
                }
                BangumiDetailViewModel.DetailTab.EPISODE -> {

                }
                BangumiDetailViewModel.DetailTab.COMMENT -> {

                }

            }
        }
    }
}

@Composable
fun BangumiDetailDetail(
    modifier: Modifier = Modifier,
    vm: BangumiDetailViewModel,
    subjectState: DataState<BgmSubject>,
) {
    val isDetailShowAll = remember {
        mutableStateOf(false)
    }
    val isTabShowAll = remember {
        mutableStateOf(false)
    }
    LoadScaffold(
        modifier, data = subjectState
    ) {

        LazyColumn(
            Modifier.fillMaxSize(),
//            horizontalAlignment = Alignment.CenterHorizontally
            contentPadding = PaddingValues(16.dp)
        ) {

            item(
//                key = it.data
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().clickable {
                        isDetailShowAll.value = !isDetailShowAll.value
                    },
//                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = it.data.summary?:"",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = if (isDetailShowAll.value) Int.MAX_VALUE else 5,
                        lineHeight = 26.sp
                    )
                    if (!isDetailShowAll.value) {
                        Text(
                            text = "...",
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 26.sp
                        )
                    }

                }

            }

            item {
                Spacer(Modifier.size(16.dp))
            }

            item {
                val maxSize = remember(isTabShowAll.value) {
                    if (isTabShowAll.value) Int.MAX_VALUE else 12
                }
                Column {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (i in 0..maxSize.coerceAtMost(it.data.tags.size)) {
                            val tag = it.data.tags.getOrNull(i) ?: continue
                            Row(modifier = Modifier
//                                .widthIn(max = 120.dp)

                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
//                                    vm.onTagClick(tag.name)
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = tag.name ?: "",
                                    maxLines = 1,
                                    style = MaterialTheme.typography.bodySmall,
//                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = tag.count?.toString() ?: "",
                                    maxLines = 1,
                                    style = MaterialTheme.typography.bodySmall,
//                                color = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }

                    }
                    TextButton(
                        modifier = Modifier.align(Alignment.End),
                        onClick = {
                            isTabShowAll.value = !isTabShowAll.value
                        }
                    ) {
                        Text(
                            text = if (isTabShowAll.value) stringRes(Res.strings.show_less) else stringRes(Res.strings.show_more),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

            }
        }
    }

}
