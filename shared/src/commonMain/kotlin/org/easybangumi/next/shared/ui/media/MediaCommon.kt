package org.easybangumi.next.shared.ui.media

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.data.cartoon.Episode
import org.easybangumi.next.shared.ui.detail.DetailPreview
import org.easybangumi.next.shared.ui.media_radar.MediaRadar
import org.easybangumi.next.shared.ui.media_radar.MediaRadarBottomPanel
import org.easybangumi.next.shared.ui.media_radar.MediaRadarState
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
fun Media (
    cartoonCover: CartoonCover,
    suggestEpisode: Episode? = null,
) {

}

@Composable
fun MediaDetailPreview(
    modifier: Modifier,
    vm: MediaCommonViewModel,
) {
    Box(modifier) {
        val showPlayDetail = vm.ui.value.detail.showDetailFromPlay
        if (showPlayDetail) {

        } else {
            DetailPreview(
                modifier = Modifier.fillMaxWidth(),
                cartoonIndex = remember(vm.cartoonCover) {
                    vm.cartoonCover.toCartoonIndex()
                },
            )
        }
    }

}

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
fun MediaEpisodeList(
    vm: MediaCommonViewModel
) {

}