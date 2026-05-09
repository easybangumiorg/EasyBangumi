package org.easybangumi.next.shared.compose.media.bangumi

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import org.easybangumi.next.shared.compose.common.collect_dialog.CartoonCollectDialog
import org.easybangumi.next.shared.compose.detail.bangumi.BangumiDetailPanel
import org.easybangumi.next.shared.compose.detail.bangumi.BangumiDetailVM
import org.easybangumi.next.shared.compose.media_finder.MediaFinderHost


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
fun BangumiPopup(
    vm: BangumiMediaCommonVM,
){
    val state = vm.popupState.collectAsState()
    val popup = state.value
    when (val po = popup) {
        is BangumiMediaCommonVM.Popup.BangumiDetailPanel -> {
            val sheetState = rememberModalBottomSheetState()
            ModalBottomSheet(
                onDismissRequest = {
                    vm.dismissPopup()
                },
                sheetState = sheetState,
                contentWindowInsets = {
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Top)
                }
            ) {
                BangumiDetailPanel(
                    vm = vm.bangumiDetailVM,
                    onDismiss = {
                        vm.dismissPopup()
                    }
                )
            }
        }
        is BangumiMediaCommonVM.Popup.CollectionDialog -> {
            CartoonCollectDialog(
                po.cartoonCover,
                onDismissRequest = {
                    vm.dismissPopup()
                }
            )
        }
//        is BangumiMediaCommonVM.Popup.MediaRadarPanel -> {
//            logger.info("show media radar panel")
//            MediaRadarBottomPanel(
//                vm = vm.mediaSearchVM,
//                onDismissRequest = {
//                    vm.dismissPopup()
//                },
//            )
//
//        }
        else -> {}
    }

    MediaFinderHost(
        vm.mediaFinderVM
    )
}