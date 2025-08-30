package org.easybangumi.next.shared.compose.media

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.shared.foundation.elements.ErrorElements
import org.easybangumi.next.shared.foundation.elements.LoadingElements


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
fun AndroidMediaPlayLineIndex(
    vm: PlayLineIndexViewModel
) {

}

fun LazyGridScope.androidMediaPlayLineIndex(
    vm: PlayLineIndexViewModel,
    state: PlayLineIndexViewModel.State,
){
    val data = state.playerLineList.okOrNull()
    if (state.playerLineList.isLoading()) {
        item(
            span = {
                GridItemSpan(this.maxLineSpan)
            }
        ) {
            LoadingElements(
                modifier = Modifier.fillMaxWidth().height(200.dp).padding(8.dp, 0.dp),
                isRow = true
            )
        }
    } else if (state.playerLineList is DataState.Error) {
        val errorMsg = state.playerLineList.errorMsg
        item(
            span = {
                GridItemSpan(this.maxLineSpan)
            }
        ) {
            ErrorElements(
                modifier = Modifier.fillMaxWidth().height(200.dp).padding(8.dp, 0.dp),
                errorMsg = errorMsg,
                isRow = true
            )
        }
    } else if(data != null) {
        item(
            span = {
                GridItemSpan(this.maxLineSpan)
            }
        ) {
            Row(
                modifier = Modifier.padding(8.dp, 0.dp)
            ) {
                LazyRow(
                    modifier = Modifier.weight(1f)
                ) {
                    val list = data
                    items(list.size) {
                        val item = list[it]
                        FilterChip(
                            selected = it == state.currentPlayerLine,
                            onClick = {
                                vm.onPlayLineSelected(it)
                            },
                            label = {
                                Text(item.label)
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
        val currentPlayLine = state.playLineOrNull
        if (currentPlayLine != null) {
            items(currentPlayLine.episodeList.size) {
                val item = currentPlayLine.episodeList[it]
                FilterChip(
                    selected = false,
                    onClick = {
//                        vm.onEpisodeSelected(it)
                    },
                    label = {
                        Text(item.label)
                    },
                    modifier = Modifier
                )
            }
        }
    }

}