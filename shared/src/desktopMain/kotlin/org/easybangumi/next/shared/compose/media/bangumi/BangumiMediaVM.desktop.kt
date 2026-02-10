package org.easybangumi.next.shared.compose.media.bangumi

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.utils.coroutineProvider
import org.easybangumi.next.libplayer.api.C
import org.easybangumi.next.shared.foundation.view_model.BaseViewModel
import org.easybangumi.next.shared.compose.media.MediaParam
import org.easybangumi.next.shared.playcon.BasePlayconViewModel
import org.easybangumi.next.shared.playcon.desktop.DesktopPlayerVM
import org.easybangumi.next.shared.playcon.desktop.FullscreenStrategy
import org.easybangumi.next.shared.window.EasyWindowState

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
class DesktopBangumiMediaVM(
    val param: MediaParam,
): BaseViewModel() {

    val showMediaPage = mutableStateOf(true)

    val commonVM: BangumiMediaCommonVM by childViewModel {
        BangumiMediaCommonVM(param)
    }

    // == 播放状态 =============================
    val playerVM: DesktopPlayerVM by childViewModel {
        DesktopPlayerVM(fullscreenStrategy)
    }

    val fullscreenStrategy = object: FullscreenStrategy {
        override fun enterFullscreen() {
            windowState.value?.let {
                it.state.placement = WindowPlacement.Fullscreen
            }
        }

        override fun exitFullscreen() {
            windowState.value?.let {
                it.state.placement = WindowPlacement.Floating
            }
        }

        override fun isFullscreen(): Boolean {
            return windowState.value?.state?.placement == WindowPlacement.Fullscreen
        }
    }

    init {
        // 播放链接变化 -> 播放器
        viewModelScope.launch {
            commonVM.playIndexState.map { it.playInfo }.collectLatest {
                playerVM.onPlayInfoChange(it)
            }
        }
        viewModelScope.launch {
            snapshotFlow {
                playerVM.playconVM.screenMode
            }.collectLatest { screenMode ->
                commonVM.sta.update {
                    it.copy(
                        isFullscreen = screenMode == BasePlayconViewModel.ScreenMode.FULLSCREEN,
//                        isTableMode = playerVM.screenMode == DesktopPlayerVM.ScreenMode.TABLE
                    )
                }
            }

        }
    }
    private var windowState = mutableStateOf<EasyWindowState?>(null)
    private var saveJob: Job? = null


    fun onLaunch(
        easyWindowState: EasyWindowState,
    ) {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            while (true) {
                delay(10000)
                trySaveHistory()
            }
        }
        windowState.value = easyWindowState
    }

    fun onDispose() {
        saveJob?.cancel()
        trySaveHistory()
    }


    fun trySaveHistory() {
        val state = playerVM.vlcjPlayerBridge.playStateFlow.value
        if (state == C.State.BUFFERING || state == C.State.READY || state == C.State.ENDED) {
            val pos = playerVM.vlcjPlayerBridge.positionMs
            if (pos != C.TIME_UNSET && pos > 0) {
                viewModelScope.launch(coroutineProvider.io()) {
                    commonVM.trySaveHistory(pos)
                }
            }
        }


    }
}