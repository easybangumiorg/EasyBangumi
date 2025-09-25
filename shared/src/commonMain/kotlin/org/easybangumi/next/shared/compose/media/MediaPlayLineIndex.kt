package org.easybangumi.next.shared.compose.media

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.shared.foundation.elements.ErrorElements
import org.easybangumi.next.shared.foundation.elements.LoadingElements
import org.easybangumi.next.shared.foundation.lazy.itemsFromGrid


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

fun LazyListScope.mediaPlayLineIndex(
    vm: PlayLineIndexVM,
    state: PlayLineIndexVM.State,
    gridCount: Int = 2,
){
    val data = state.playerLineList.okOrNull()
    if (state.playerLineList.isLoading()) {
        item {
            LoadingElements(
                modifier = Modifier.fillMaxWidth().height(200.dp).padding(8.dp, 0.dp),
                isRow = true
            )
        }
    } else if (state.playerLineList is DataState.Error) {
        val errorMsg = state.playerLineList.errorMsg
        item{
            ErrorElements(
                modifier = Modifier.fillMaxWidth().height(200.dp).padding(8.dp, 0.dp),
                errorMsg = errorMsg,
                isRow = true
            )
        }
    } else if(data != null) {
        item{
            Row(
                modifier = Modifier.padding(8.dp, 0.dp)
            ) {
                LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val list = data
                    items(list.size) {
                        val item = list[it]
                        FilterChip(
                            selected = it == state.currentShowingPlayerLine,
                            onClick = {
                                vm.onShowingPlayLineSelected(it)
                            },
                            label = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (item == state.playLineOrNull) {
                                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                                        Spacer(Modifier.size(4.dp))
                                    }
                                    Text(item.label)
                                }

                            },
                            modifier = Modifier
                        )
                    }
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Sort, contentDescription = "")
                }
            }
        }
        val currentPlayLine = state.showingPlayerLine
        if (currentPlayLine != null) {
            itemsFromGrid(
                itemsCount = currentPlayLine.episodeList.size,
                girdCount = gridCount,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                rowModifier = Modifier.padding(8.dp, 0.dp)
            ) {
                val item = currentPlayLine.episodeList[it]
                Card(
                    modifier = Modifier.fillMaxWidth().padding(0.dp, 4.dp).clip(CardDefaults.shape).clickable {
                        vm.onEpisodeSelected(state.currentShowingPlayerLine, it)
                    },
                    colors = CardDefaults.cardColors().copy(
                        containerColor = if(item == state.currentEpisodeOrNull) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = if(item == state.currentEpisodeOrNull) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    ),
                ) {
                    Text(text = item.label, modifier = Modifier.padding(8.dp, 8.dp, 0.dp, 24.dp))
                }
            }
        }
    }

}