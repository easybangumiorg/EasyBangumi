package org.easybangumi.next.shared.ui.media

import androidx.lifecycle.viewModelScope
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.utils.global
import org.easybangumi.next.libplayer.exoplayer.ExoPlayerBridge
import org.easybangumi.next.libplayer.exoplayer.ExoPlayerFrameState
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.foundation.view_model.BaseViewModel
import org.easybangumi.next.shared.playcon.android.AndroidPlayconViewModel
import org.easybangumi.next.shared.ui.media_radar.MediaRadarParam
import org.koin.core.component.inject

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
class AndroidMediaViewModel(
    cartoonCover: CartoonCover,
    suggestEpisode: Int? = null,
    val mediaRadarParam: MediaRadarParam? = null
): BaseViewModel() {

    companion object {
        const val MEDIA_COMPONENT_ASPECT = 16f / 9f
    }

    // 媒体通用
    val mediaCommonVM: MediaCommonViewModel by childViewModel {
        MediaCommonViewModel(cartoonCover, mediaRadarParam, suggestEpisode)
    }

    // 视频播放
    val exoBuilder: ExoPlayer.Builder by inject()
    val exoBridge = ExoPlayerBridge(global.appContext, exoBuilder)
    val exoPlayerFrameState = ExoPlayerFrameState()

    // 播放控制
    val playconVM: AndroidPlayconViewModel by childViewModel {
        AndroidPlayconViewModel(exoBridge)
    }


    private var hasShowRadarFirst = false

    init {
        exoPlayerFrameState.bindBridge(exoBridge)
        addCloseable(exoPlayerFrameState)

        viewModelScope.launch {
            // 没选定播放片源时强制竖屏
            mediaCommonVM.logic.filter { it.detail.radarResult == null }.collectLatest {
                playconVM.screenMode = AndroidPlayconViewModel.ScreenMode.NORMAL
                if (!hasShowRadarFirst) {
                    hasShowRadarFirst = true
                    mediaCommonVM.showMediaRadar()
                }
//                mediaCommonVM.showMediaRadar()
            }
        }
    }





}