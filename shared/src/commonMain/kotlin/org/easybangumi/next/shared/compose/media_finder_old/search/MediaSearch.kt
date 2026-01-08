package org.easybangumi.next.shared.compose.media_finder_old.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.cash.paging.compose.collectAsLazyPagingItems
import org.easybangumi.next.shared.compose.media_finder_old.MediaFinderVM
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
fun MediaSearcher(
    modifier: Modifier,
    vm: MediaSearchVM,
    state: MediaSearchVM.State,
    onResultSelect: (result: MediaFinderVM.SelectionResult) -> Unit,
) {

    LazyColumn(
        modifier = Modifier.fillMaxWidth().then(modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = state.fieldText,
                    onValueChange = { vm.onFieldChange(it) },
                    singleLine = true,
//                    colors = OutlinedTextFieldDefaults.colors(
//                        focusedBorderColor = Color.Transparent,
//                        unfocusedBorderColor = Color.Transparent,
//                        errorBorderColor = Color.Transparent,
//                        disabledBorderColor = Color.Transparent,
//
//                    )
                    label = {
                        Text("搜索关键词")
                    }
                )
            }
        }

        item {
            Button(onClick = {
                vm.onSearchKeywordChange()
            }) {
                Text(stringRes(Res.strings.search))
            }
        }

        items(state.lineState) { line ->

            Column {
                ListItem(
                    headlineContent = {
                        Text(stringRes(line.searchBusiness.source.manifest.label))
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
                                        nameShowOutside = true,
                                        itemSize = height,
                                        itemIsWidth = false,
                                        coverAspectRatio = EasyScheme.size.cartoonCoverSmallAspectRatio,
                                        onClick = {
                                            onResultSelect(
                                                MediaFinderVM.SelectionResult(
                                                    playCover = item,
                                                    businessPair = line.businessPair
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