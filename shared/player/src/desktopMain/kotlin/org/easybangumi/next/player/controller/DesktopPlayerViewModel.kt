package org.easybangumi.next.player.controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.easybangumi.next.libplayer.api.PlayerBridge
import org.easybangumi.next.shared.player.BasePlayerViewModel

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
class DesktopPlayerViewModel(
    bridge: PlayerBridge,
) : BasePlayerViewModel(bridge) {

    companion object {
        const val CONTROL_HIDE_DELAY = 4000L
    }

    var hideDelayJob: Job? = null

    var isLoading by mutableStateOf(false)

    // 负数表示非滑动状态
    var seekPosition by mutableLongStateOf(-1L)

    var isShowController by mutableStateOf(false)

    var isLocked by mutableStateOf(false)

    fun lock() {
        isLocked = true
        restartHideDelayJob()
    }

    fun unlock() {
        isLocked = false
        restartHideDelayJob()
    }

    fun toggleLock() {
        if (isLocked) {
            unlock()
        } else {
            lock()
        }
    }

    fun startSeek() {
        seekPosition = position
        endHideDelayJob()
    }

    fun changeSeekPosition(position: Long) {
        seekPosition = position
        endHideDelayJob()
    }

    fun endSeek() {
        bridge.seekTo(seekPosition)
        seekPosition = -1
        restartHideDelayJob()
    }


    fun showController() {
        isShowController = true
        restartHideDelayJob()
    }

    fun hideController() {
        isShowController = false
    }

    fun toggleController() {
        if (isShowController) {
            hideController()
        } else {
            showController()
        }
    }

    fun restartHideDelayJob(){
        hideDelayJob?.cancel()
        hideDelayJob = viewModelScope.launch {
            delay(CONTROL_HIDE_DELAY)
            if (isActive && isShowController && seekPosition < 0) {
                hideController()
            }
        }
    }

    fun endHideDelayJob() {
        hideDelayJob?.cancel()
        hideDelayJob = null
    }



}