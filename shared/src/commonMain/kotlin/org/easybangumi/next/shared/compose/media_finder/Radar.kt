package org.easybangumi.next.shared.compose.media_finder

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.easybangumi.next.shared.foundation.cartoon.CartoonCoverCard
import org.easybangumi.next.shared.foundation.image.AsyncImage
import org.easybangumi.next.shared.foundation.stringRes
import org.easybangumi.next.shared.scheme.EasyScheme
import org.easybangumi.next.shared.source.api.component.getManifest

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
fun Radar(
    vm: MediaFinderVM,
    modifier: Modifier = Modifier,
) {

    val state = vm.ui.value
    val radarState = state.radarState

    LaunchedEffect(state.keyword) {
        vm.radarV1VM.changeKeyword(state.keyword)
    }

    LazyColumn(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(16.dp, 0.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item {
            Row {
                Text("搜索关键词：")
                Text(state.keyword ?: "", modifier = Modifier.weight(1f))
                Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.clickable {
                    vm.showKeywordEditPopup()
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
                items(radarState.radarSourceTabList.size) {
                    val tab = radarState.radarSourceTabList[it]
                    val selected = tab == radarState.selectionSource
                    FilterChip(
                        elevation = null,
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
                            if (tab.error) {
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

        items(radarState.result.size) {
            val item = radarState.result[it]
            Row(
                modifier = Modifier.fillMaxWidth().clickable {
                    vm.onUserResultSelect(
                        MediaFinderVM.SelectionResult(
                            playCover = item.cover,
                            manifest = item.businessPair.getManifest(),
                            suggestPlayerLine = null,
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
                        vm.onUserResultSelect(
                            MediaFinderVM.SelectionResult(
                                playCover = item.cover,
                                manifest = item.businessPair.getManifest(),
                                suggestPlayerLine = null,
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
                            model = item.businessPair.getManifest().icon,
                            contentDescription = stringRes(item.businessPair.getManifest().label),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(stringRes(item.businessPair.getManifest().label))
                    }
                    item.playerLine?.let { lineList ->
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            items(lineList.size) {
                                val line = lineList[it]
                                FilterChip(
                                    elevation = null,
                                    selected = false,
                                    onClick = {
                                        vm.onUserResultSelect(
                                            MediaFinderVM.SelectionResult(
                                                playCover = item.cover,
                                                manifest = item.businessPair.getManifest(),
                                                suggestPlayerLine = line,
                                            )
                                        )
                                    },
                                    label = {
                                        Text(line.label)
                                    },
                                    trailingIcon = {
                                        Text(line.episodeList.size.toString())
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}