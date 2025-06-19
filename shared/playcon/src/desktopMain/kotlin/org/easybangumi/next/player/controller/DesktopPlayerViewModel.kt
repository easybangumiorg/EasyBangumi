package org.easybangumi.next.player.controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.easybangumi.next.libplayer.api.PlayerBridge
import org.easybangumi.next.shared.playcon.BasePlayerViewModel

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

    var isShowController by mutableStateOf(true)

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


    fun onSeekEnd(seekPosition: Long) {
        bridge.seekTo(seekPosition)
        restartHideDelayJob()
    }


    fun showController(needHideDelay : Boolean = true) {
        isShowController = true
        if (needHideDelay) {
            restartHideDelayJob()
        } else {
            endHideDelayJob()
        }
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
            if (isActive && isShowController) {
                hideController()
            }
        }
    }

    fun endHideDelayJob() {
        hideDelayJob?.cancel()
        hideDelayJob = null
    }



}