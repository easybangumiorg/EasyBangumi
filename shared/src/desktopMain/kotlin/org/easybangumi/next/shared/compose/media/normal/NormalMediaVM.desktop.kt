package org.easybangumi.next.shared.compose.media.normal

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.easybangumi.next.shared.compose.media.MediaParam
import org.easybangumi.next.shared.foundation.view_model.BaseViewModel
import org.easybangumi.next.shared.playcon.BasePlayconViewModel
import org.easybangumi.next.shared.playcon.desktop.DesktopPlayerVM
import org.easybangumi.next.shared.window.DesktopWindowFullscreenStrategy
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

class DesktopNormalMediaVM(
    val param: MediaParam,
): BaseViewModel() {

    val commonVM: NormalMediaCommonVM by childViewModel {
        NormalMediaCommonVM(param)
    }

    private val windowState = mutableStateOf<EasyWindowState?>(null)

    val fullscreenStrategy = DesktopWindowFullscreenStrategy {
        windowState.value
    }

    // == 播放状态 =============================
    val playerVM: DesktopPlayerVM by childViewModel {
        DesktopPlayerVM(fullscreenStrategy)
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
                    )
                }
            }
        }
    }

    fun onLaunch(
        easyWindowState: EasyWindowState,
    ) {
        windowState.value = easyWindowState
    }
}
