package org.easybangumi.next.shared.ui.media

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.shared.foundation.EasyTab
import org.easybangumi.next.shared.foundation.elements.ErrorElements
import org.easybangumi.next.shared.foundation.elements.LoadScaffold
import org.easybangumi.next.shared.foundation.stringRes
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
        item {
            LoadScaffold(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                data = playLineState
            ) { }
        }
        val playLine = playLineState.okOrNull()
        if (playLine != null) {
            item {
                EasyTab(
                    modifier = Modifier.fillMaxWidth(),
                    selection = playIndexState.currentPlayerLine,
                    size = playLine.size,
                    onSelected = {
                        vm.mediaCommonVM.onPlayLineSelected(it)
                    },
                ) { index, selected ->
                    val tab = playLine[index]
                    Text(
                        text = stringRes(tab.label),
                    )
                }
            }
            items(playLine.size) {

            }
        }

    }
}


