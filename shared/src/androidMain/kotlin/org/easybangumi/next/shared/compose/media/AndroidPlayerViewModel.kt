package org.easybangumi.next.shared.compose.media

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.Global
import org.easybangumi.next.lib.utils.global
import org.easybangumi.next.libplayer.api.C
import org.easybangumi.next.libplayer.api.MediaItem
import org.easybangumi.next.libplayer.exoplayer.EasyTextureView
import org.easybangumi.next.libplayer.exoplayer.ExoPlayerBridge
import org.easybangumi.next.libplayer.exoplayer.ExoPlayerFrameState
import org.easybangumi.next.libplayer.exoplayer.LibC
import org.easybangumi.next.shared.data.cartoon.PlayInfo
import org.easybangumi.next.shared.foundation.view_model.BaseViewModel
import org.easybangumi.next.shared.playcon.android.AndroidPlayconViewModel
import org.koin.core.component.inject
import kotlin.getValue

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
class AndroidPlayerViewModel: BaseViewModel() {

    companion object {
        const val MEDIA_COMPONENT_ASPECT = 16f / 9f
    }

    val textureView = EasyTextureView(global.appContext)

    // 视频播放
    val exoBuilder: ExoPlayer.Builder by inject()
    val exoBridge = ExoPlayerBridge(global.appContext, exoBuilder)
    val exoPlayerFrameState = ExoPlayerFrameState()

    // 播放控制
    val playconVM: AndroidPlayconViewModel by childViewModel {
        AndroidPlayconViewModel(exoBridge)
    }


    val playInfo = mutableStateOf<DataState<PlayInfo>>(DataState.none())
    val isFinalLoading = mutableStateOf(false)

    init {
        exoPlayerFrameState.bindBridge(exoBridge)
        exoBridge.attachTextureView(textureView)
        addCloseable(exoPlayerFrameState)

        viewModelScope.launch {
            snapshotFlow {
                playconVM.playerState to playInfo.value
            }.collectLatest {
                isFinalLoading.value = (playconVM.playerState == C.State.PREPARING || playconVM.playerState == C.State.BUFFERING || playInfo.value.isLoading())
            }
        }

        viewModelScope.launch {
            snapshotFlow {
                playInfo.value.okOrNull()
            }.collectLatest {
                if (it != null) {
                    val mediaItem = MediaItem(
                        mediaType = when(it.type) {
                            PlayInfo.TYPE_HLS -> MediaItem.MEDIA_TYPE_HLS
                            PlayInfo.TYPE_NORMAL -> MediaItem.MEDIA_TYPE_NORMAL
                            else -> MediaItem.MEDIA_TYPE_UNKNOWN
                        },
                        uri = it.url,
                        header = it.header
                    )
                    exoBridge.prepare(mediaItem)
                    exoBridge.setPlayWhenReady(true)
                }
            }
        }

    }


    fun onPlayInfoChange(playerInfoState: DataState<PlayInfo>) {
        playInfo.value = playerInfoState
    }

    override fun onCleared() {
        super.onCleared()
        exoBridge.close()
    }
}