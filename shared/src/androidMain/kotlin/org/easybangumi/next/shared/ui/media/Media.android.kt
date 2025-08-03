package org.easybangumi.next.shared.ui.media

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.easybangumi.next.libplayer.exoplayer.ExoPlayerCompose
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.data.cartoon.Episode
import org.easybangumi.next.shared.foundation.view_model.vm
import org.easybangumi.next.shared.playcon.android.AndroidPlaycon
import org.easybangumi.next.shared.playcon.android.AndroidPlayconViewModel
import org.easybangumi.next.shared.ui.media_radar.MediaRadarParam

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
fun Media(
    cartoonCover: CartoonCover,
    suggestEpisode: Episode? = null,
    mediaRadarParam: MediaRadarParam? = null,
) {

    val vm = vm(::MediaViewModel, cartoonCover, suggestEpisode, mediaRadarParam)

    val screenMode = vm.playconVM.screenMode
//    val isFullScreen = (screenMode == AndroidPlayconViewModel.ScreenMode.FULLSCREEN) && vm.
    when (screenMode) {
        AndroidPlayconViewModel.ScreenMode.NORMAL -> {
            MediaNormal(vm)
        }
        AndroidPlayconViewModel.ScreenMode.FULLSCREEN -> {

        }
    }

    MediaRadarPopup(vm.mediaCommonVM)
}

@Composable
fun MediaNormal(
    viewModel: MediaViewModel
){
    Column {
        PlayerNormal(viewModel)
    }
}

@Composable
fun ColumnScope.PlayerNormal(
    viewModel: MediaViewModel
) {
    Box(
        modifier = Modifier.fillMaxWidth().aspectRatio(MediaViewModel.MEDIA_COMPONENT_ASPECT)
    ) {
        ExoPlayerCompose(
            modifier = Modifier.fillMaxWidth().aspectRatio(MediaViewModel.MEDIA_COMPONENT_ASPECT),
            state = viewModel.exoPlayerFrameState
        )
        AndroidPlaycon(
            modifier = Modifier.fillMaxWidth().aspectRatio(MediaViewModel.MEDIA_COMPONENT_ASPECT),
            viewModel = viewModel.playconVM
        )
    }
}


