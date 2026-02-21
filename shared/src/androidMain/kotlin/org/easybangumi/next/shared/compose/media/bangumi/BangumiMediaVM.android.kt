package org.easybangumi.next.shared.compose.media.bangumi

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.easybangumi.next.libplayer.api.C
import org.easybangumi.next.shared.foundation.view_model.BaseViewModel
import org.easybangumi.next.shared.playcon.android.AndroidPlayerVM
import org.easybangumi.next.shared.compose.media.MediaParam

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
class AndroidBangumiMediaVM(
    val param: MediaParam,
): BaseViewModel() {

    // == common 逻辑
    val commonVM: BangumiMediaCommonVM by childViewModel {
        BangumiMediaCommonVM(param)
    }

    // == 播放状态 =============================
    val playerVM: AndroidPlayerVM by childViewModel {
        AndroidPlayerVM()
    }

    private var saveJob: Job? = null


    init {
        commonVM.attachBridge(playerVM.exoBridge)
        // 播放链接变化 -> 播放器
        viewModelScope.launch {
            commonVM.playIndexState.map { it.playInfo }.collectLatest {
                playerVM.onPlayInfoChange(it)
            }
        }

        viewModelScope.launch {
            playerVM.screenModeViewModel.logic.collectLatest { screenViewState ->
                commonVM.sta.update {
                    it.copy(
                        isFullscreen = screenViewState.isFullScreen,
                        isTableMode = screenViewState.isTabletMod
                    )
                }
            }
        }

        viewModelScope.launch {
            playerVM.exoBridge.playWhenReadyFlow.collectLatest {
                if (!it) {
                    trySaveHistory()
                }
            }
        }
        viewModelScope.launch {
            playerVM.exoBridge.playStateFlow.collectLatest {
                trySaveHistory()
            }
        }




    }

    fun onLaunch() {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            while (true) {
                delay(10000)
                trySaveHistory()
            }
        }
    }

    fun onDispose() {
        saveJob?.cancel()
        trySaveHistory()
    }

    fun trySaveHistory() {
        if (playerVM.exoBridge.isMedia()) {
            val pos = playerVM.exoBridge.positionMs
            if (pos != C.TIME_UNSET && pos > 0) {
                viewModelScope.launch {
                    commonVM.trySaveHistory(pos)
                }
            }
        }


    }



}