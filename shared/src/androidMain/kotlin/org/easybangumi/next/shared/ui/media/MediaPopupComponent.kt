package org.easybangumi.next.shared.ui.media

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.easybangumi.next.shared.foundation.elements.LoadScaffold
import org.easybangumi.next.shared.foundation.stringRes
import org.easybangumi.next.shared.resources.Res
import org.easybangumi.next.shared.ui.media_radar.MediaRadarBottomPanel

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
fun MediaRadarPopup(
    vm: MediaCommonViewModel
) {
    val radar = vm.ui.value.popup as? MediaCommonViewModel.Popup.MediaRadar
    MediaRadarBottomPanel(
        vm = vm.mediaRadarViewModel,
        show = radar != null,
        onDismissRequest = {
            vm.dismissPopup()
        },
        onSelection = {
            if (it != null) {
                vm.dismissPopup()
                vm.onMediaRadarResult(it)
            }
        }
    )
}

@Composable
fun MediaPlayListPopup(
    vm: MediaCommonViewModel
) {
    val playListPop = vm.ui.value.popup as? MediaCommonViewModel.Popup.PlayLineList
    if (playListPop != null) {
        val playLineListState = vm.ui.value.playIndex.playerLineList
        val currentIndex = vm.ui.value.playIndex.currentPlayerLine
        AlertDialog(
            onDismissRequest = {
                vm.dismissPopup()
            },
            modifier = Modifier,
            title = {
                Text(stringRes(Res.strings.choose_playline))
            },
            text = {
                LoadScaffold(
                    modifier = Modifier,
                    data = playLineListState) {
                    val playLineList = it.data

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        playLineList.forEachIndexed { index, it ->
                            OutlinedButton(
                                shape = RoundedCornerShape(8.dp),
                                onClick = {
                                vm.onPlayLineSelected(index)
                            }) {
                                Text(
                                    text = it.label,
//                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 1,
                                    color = if (currentIndex == index) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        }
                    }
                }

            },
            confirmButton = {
                TextButton(onClick = {
                    vm.dismissPopup()
                }) {
                    Text(stringRes(Res.strings.confirm))
                }
            }
        )
    }
}

