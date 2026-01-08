package org.easybangumi.next.shared.compose.media_finder_old.radar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.easybangumi.next.shared.compose.media_finder_old.MediaFinderVM
import org.easybangumi.next.shared.foundation.cartoon.CartoonCoverCard
import org.easybangumi.next.shared.foundation.image.AsyncImage
import org.easybangumi.next.shared.foundation.stringRes
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
    vm: MediaRadarVM,
    state: MediaRadarVM.UiState,
    onResultSelect: (result: MediaFinderVM.SelectionResult) -> Unit,
) {

    LazyColumn(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(16.dp, 0.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item {
            Row {
                Text("搜索关键词：")
                Text(state.searchKeyword, modifier = Modifier.weight(1f))
                Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.clickable {
                    vm.showEditPopup()
                })
            }
        }
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
            ) {
                items(state.sourceTabList.size) {
                    val tab = state.sourceTabList[it]
                    val selected = tab == state.selectionSourceTab
                    FilterChip(
                        selected = selected,
                        leadingIcon = {
                            AsyncImage(
                                model = tab.sourceManifest.icon,
                                contentDescription = stringRes(tab.sourceManifest.label),
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(stringRes(tab.sourceManifest.label))

                        },
                        trailingIcon = {
                            if (tab.isError) {
                                Icon(Icons.Filled.Error, contentDescription = null)
                            } else if (tab.loading) {
                                // 加载中动画
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(tab.count.toString())
                            }
                        },
                        onClick = {

                        }
                    )
                }
            }
        }

        items(state.searchItemList.size) {
            val item = state.searchItemList[it]
            Row(
                modifier = Modifier.fillMaxWidth().clickable {
                    onResultSelect(
                        MediaFinderVM.SelectionResult(
                            playCover = item.cover,
                            businessPair = item.businessPair
                        )
                    )
                }
            ) {
                CartoonCoverCard(
                    modifier = Modifier,
                    model = item.cover.coverUrl,
                    name = null,
                    itemSize = EasyScheme.size.cartoonCoverSmallHeight,
                    itemIsWidth = false,
                    coverAspectRatio = EasyScheme.size.cartoonCoverSmallAspectRatio,
                    onClick = {
                        onResultSelect(
                            MediaFinderVM.SelectionResult(
                                playCover = item.cover,
                                businessPair = item.businessPair
                            )
                        )
                    }
                )
                Spacer(modifier = Modifier.size(4.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Text(item.cover.name, maxLines = 1, style = MaterialTheme.typography.titleMedium)
                    Row {
                        AsyncImage(
                            model = item.sourceManifest.icon,
                            contentDescription = stringRes(item.sourceManifest.label),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(stringRes(item.sourceManifest.label))
                    }

                }
            }
        }
    }
}