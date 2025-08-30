package org.easybangumi.next.shared.compose.media_radar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.cash.paging.compose.collectAsLazyPagingItems
import org.easybangumi.next.shared.foundation.cartoon.CartoonCoverCard
import org.easybangumi.next.shared.foundation.cartoon.CartoonCoverCardRect
import org.easybangumi.next.shared.foundation.elements.LoadScaffold
import org.easybangumi.next.shared.foundation.lazy.pagingCommon
import org.easybangumi.next.shared.foundation.shimmer.ShimmerHost
import org.easybangumi.next.shared.foundation.stringRes
import org.easybangumi.next.shared.resources.Res
import org.easybangumi.next.shared.scheme.EasyScheme

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
fun MediaRadar(
    modifier: Modifier,
    vm: MediaRadarViewModel,
    onSelection: (MediaRadarViewModel.SelectionResult?) -> Unit = { _ -> }
) {

    val state = vm.ui


    Column (
        modifier = Modifier.fillMaxWidth().then(modifier)
    ) {
        MediaRadarHeader(
            Modifier.fillMaxWidth(),
            vm,
            state.value,
        )
        HorizontalDivider()
        MediaRadarList(
            Modifier.fillMaxWidth().weight(1f),
            vm,
            state.value,
            onSelection,
        )

    }

}

@Composable
fun MediaRadarHeader(
    modifier: Modifier,
    vm: MediaRadarViewModel,
    state: MediaRadarViewModel.State
) {
    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = state.keyword,
            onValueChange = { vm.onFieldChange(it) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                errorBorderColor = Color.Transparent,
                disabledBorderColor = Color.Transparent,

            )
        )
        TextButton(onClick = {
            vm.onSearchKeywordChange()
        }) {
            Text(stringRes(Res.strings.search))
        }
    }
}

@Composable
fun MediaRadarList(
    modifier: Modifier,
    vm: MediaRadarViewModel,
    state: MediaRadarViewModel.State,
    onSelection: (MediaRadarViewModel.SelectionResult?) -> Unit,
){
    val line = state.lineState
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Top
    ) {
        items(line) { line ->
            Column(
                verticalArrangement = Arrangement.Top
            ) {
                ListItem(
                    headlineContent = {
                        Text(
                            stringRes(line.business.first.source.manifest.label),
//                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = Color.Transparent
                    )
                )
                val height = EasyScheme.size.cartoonCoverSmallHeight

                LoadScaffold(
                    modifier = Modifier.fillMaxWidth()
                        .height(height),
                    data = line.pagingFlow,
                    onLoading = {
                        ShimmerHost(
                            modifier = Modifier.fillMaxWidth().height(height),
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
                ) {
                    val paging = it.data.collectAsLazyPagingItems()

                    LazyRow(
                        modifier = Modifier.fillMaxWidth()
                            .height(height),
                        contentPadding = PaddingValues(8.dp, 0.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (paging.itemCount > 0) {
                            items(paging.itemCount) {
                                val item = paging[it]
                                if (item != null) {
                                    CartoonCoverCard(
                                        modifier = Modifier,
                                        model = item.coverUrl,
                                        name = item.name,
                                        itemSize = height,
                                        itemIsWidth = false,
                                        coverAspectRatio = EasyScheme.size.cartoonCoverSmallAspectRatio,
                                        onClick = {
                                            onSelection.invoke(
                                                MediaRadarViewModel.SelectionResult(
                                                    playCover = item,
                                                    businessPair = line.business
                                                )
                                            )
                                        }
                                    )
                                }
                            }
                        }
                        pagingCommon(height, paging)
                    }
                }

            }
        }
    }
}

