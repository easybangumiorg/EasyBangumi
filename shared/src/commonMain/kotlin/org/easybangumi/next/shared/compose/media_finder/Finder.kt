package org.easybangumi.next.shared.compose.media_finder

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.easybangumi.next.shared.compose.media_finder_old.logger
import org.easybangumi.next.shared.compose.media_finder_old.radar.MediaRadar
import org.easybangumi.next.shared.compose.media_finder_old.search.MediaSearcher

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
fun MediaFinderHost(
    vm: MediaFinderVM
) {
    val state = vm.ui.value
    val bottomSheet = rememberModalBottomSheetState(false)
    LaunchedEffect(state.panelShow, bottomSheet.isVisible) {
        logger.info("MediaRadarBottomPanel LaunchedEffect show: ${state.panelShow}")
        if (state.panelShow && !bottomSheet.isVisible) {
            bottomSheet.show()
        } else if (!state.panelShow) {
            bottomSheet.hide()
        }
    }

    val coroutineScope = rememberCoroutineScope()
    if (state.panelShow) {
        ModalBottomSheet(
            onDismissRequest = {
                vm.hidePanel()
            },
            sheetState = bottomSheet,
            contentWindowInsets = {
                WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Top)
            }
        ) {

            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                SegmentedButton(
                    selected = vm.pagerState.currentPage == 0,
                    onClick = {
                        coroutineScope.launch {
                            vm.pagerState.scrollToPage(0)
                        }
                    },
                    label = {
                        Text(text = vm.labelList[0])
                    },
                    shape = RoundedCornerShape(4.dp, 0.dp, 0.dp, 4.dp)
                )
                SegmentedButton(
                    selected = vm.pagerState.currentPage == 1,
                    onClick = {
                        coroutineScope.launch {
                            vm.pagerState.scrollToPage(1)
                        }
                    },
                    label = {
                        Text(text = vm.labelList[1])
                    },
                    shape = RoundedCornerShape(0.dp, 4.dp, 4.dp, 0.dp)
                )
            }

            HorizontalPager(
                modifier = Modifier.fillMaxSize(),
                state = vm.pagerState,
                userScrollEnabled = false
            ) {
                when(it) {
                    0 -> {
                        Radar(
                            modifier = Modifier.fillMaxSize(),
                            vm = vm
                        )
                    }
                    1 -> {
                        Search(
                            modifier = Modifier.fillMaxSize(),
                            vm = vm
                        )
                    }
                    else -> {}
                }
            }

        }
    }

}

