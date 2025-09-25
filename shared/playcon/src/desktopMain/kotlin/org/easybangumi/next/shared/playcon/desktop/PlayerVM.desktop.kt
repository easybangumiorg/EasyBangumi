package org.easybangumi.next.shared.playcon.desktop

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.libplayer.api.MediaItem
import org.easybangumi.next.libplayer.vlcj.VlcjBridgeManager
import org.easybangumi.next.libplayer.vlcj.VlcjPlayerBridge
import org.easybangumi.next.shared.data.cartoon.PlayInfo
import org.easybangumi.next.shared.foundation.view_model.BaseViewModel
import org.easybangumi.next.shared.playcon.pointer.PointerPlayconVM
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
class DesktopPlayerVM: BaseViewModel() {

    val vlcjManager: VlcjBridgeManager by inject()
    val vlcjPlayerBridge: VlcjPlayerBridge by lazy {
        vlcjManager.getOrCreateBridge(this.toString())
    }

    val playInfo = mutableStateOf<DataState<PlayInfo>>(DataState.none())
    val isFinalLoading = mutableStateOf(false)

    val playconVM: PointerPlayconVM by childViewModel {
        PointerPlayconVM(
            vlcjPlayerBridge,
        )
    }
    init {
        viewModelScope.launch {
            snapshotFlow {
                playInfo.value
            }.collectLatest {
                if (it.isLoading()) {
                    vlcjPlayerBridge.setPlayWhenReady(false)
                }
                val it = playInfo.value.okOrNull()
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
                    vlcjPlayerBridge.prepare(mediaItem)
                    vlcjPlayerBridge.setPlayWhenReady(true)
                }
            }
        }
    }

    fun onPlayInfoChange(playerInfoState: DataState<PlayInfo>) {
        playInfo.value = playerInfoState
    }

    override fun onCleared() {
        vlcjManager.release(this.toString())
        super.onCleared()
    }

}