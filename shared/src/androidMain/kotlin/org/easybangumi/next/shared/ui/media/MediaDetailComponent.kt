package org.easybangumi.next.shared.ui.media

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.easybangumi.next.shared.foundation.EasyTab
import org.easybangumi.next.shared.foundation.elements.ErrorElements
import org.easybangumi.next.shared.foundation.elements.LoadScaffold
import org.easybangumi.next.shared.foundation.stringRes
import org.easybangumi.next.shared.resources.Res
import org.easybangumi.next.shared.ui.detail.DetailPreview
import org.easybangumi.next.shared.ui.media_radar.MediaRadarViewModel

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
fun MediaDetailPage(
    modifier: Modifier,
    vm: MediaViewModel,
) {
    val radarResult = vm.mediaCommonVM.ui.value.detail.radarResult
    Box(modifier) {
        if (radarResult == null) {
            MediaDetailEmptyPage(
                modifier = Modifier.fillMaxSize(),
                vm = vm,
            )
        } else {
            MediaDetailContentPage(
                modifier = Modifier.fillMaxSize(),
                vm = vm,
                radarResult = radarResult,
            )
        }
    }
}

@Composable
fun MediaDetailEmptyPage(
    modifier: Modifier,
    vm: MediaViewModel,
){
    Box(modifier) {
        Column {
            DetailPreview(
                modifier = Modifier.fillMaxWidth(),
                cartoonIndex = remember(vm.mediaCommonVM.cartoonCover) {
                    vm.mediaCommonVM.cartoonCover.toCartoonIndex()
                },
            )
            ErrorElements(
                modifier = Modifier.fillMaxWidth().weight(1f),
                isRow = false,
                errorMsg = "未选择播放源，点击搜索！",
                onClick = {
                    logger.info("onClick: showMediaRadar")
                    vm.mediaCommonVM.showMediaRadar()
                }
            )
        }
    }
}

@Composable
fun MediaDetailContentPage(
    modifier: Modifier,
    vm: MediaViewModel,
    radarResult: MediaRadarViewModel.SelectionResult
) {
    val playIndexState = vm.mediaCommonVM.ui.value.playIndex
    val playLineState = playIndexState.playerLineList
    LazyColumn(
        modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {
        item {
            MediaDetailPreview(
                modifier = Modifier.fillMaxWidth(),
                vm = vm.mediaCommonVM,
            )
        }
        if (!playLineState.isOk()) {
            item {
                LoadScaffold(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    data = playLineState
                ) { }
            }
        }
        val playRadarResult = vm.mediaCommonVM.ui.value.detail.radarResult
        if (playRadarResult != null) {
            item {
                Row (Modifier.fillMaxWidth()
                    .padding(8.dp, 4.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(8.dp))
                    .padding(16.dp, 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(stringRes(Res.strings.play_from))

                    AsyncImage(
                        model = playRadarResult.playBusiness.source.manifest.icon,
                        contentDescription = stringRes(playRadarResult.playBusiness.source.manifest.label)
                    )
                    Text(
                        text = stringRes(playRadarResult.playBusiness.source.manifest.label)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    OutlinedButton(
                        onClick = {
                            vm.mediaCommonVM.showMediaRadar()
                        }
                    ) {
                        Icon(Icons.Default.SyncAlt, "")
                        Text(stringRes(Res.strings.change_source))
                    }

                }
            }
        }


        val playLineList = playLineState.okOrNull()
        val currentPlayLine = playLineList?.getOrNull(playIndexState.currentPlayerLine)
        if (playLineList != null) {
            item {
                Row (
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(Modifier.size(8.dp))
                    LazyRow(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        itemsIndexed(playLineList) { index, it ->
                            FilterChip(
                                selected = currentPlayLine == it,
                                onClick = {
                                    vm.mediaCommonVM.onPlayLineSelected(index)
                                },
                                label = {
                                    Text(
                                        text = it.label,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            )
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Sort, "")
                    }
                }


            }
            if (currentPlayLine != null) {
                items(
                    currentPlayLine.episodeList.size
                ) {
                    val episode = currentPlayLine.episodeList[it]
                    Box(Modifier.fillMaxWidth()
                        .padding(8.dp, 4.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(8.dp))
                        .padding(16.dp, 8.dp)
                    ) {
                        Text(episode.label, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }

    }
}


