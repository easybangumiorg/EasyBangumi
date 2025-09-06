package org.easybangumi.next.shared.playcon.pointer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.logger.logger
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
 *
 *    鼠标模式播放器 UI
 */
class PointerPlayconViewModel(
    bridge: PlayerBridge,
) : BasePlayerViewModel<PlayerBridge>(bridge) {

    companion object {
        const val CONTROL_HIDE_DELAY = 4000L
    }

    private val logger = logger()

    var hideDelayJob: Job? = null

//    var isLoading by mutableStateOf(false)

    var isShowController by mutableStateOf(false)

    var isLocked by mutableStateOf(false)

    init {
        viewModelScope.launch {

        }
    }

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


    fun seekTo(position: Long) {
        bridge.seekTo(position)
//        restartHideDelayJob()
    }

    fun onSeekEnd(seekPosition: Long) {
        bridge.seekTo(seekPosition)
        restartHideDelayJob()
    }


    fun showController(needHideDelay : Boolean = true) {
//        logger.info("PointerPlayerViewModel: show controller $needHideDelay")
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
//            logger.info("PointerPlayerViewModel: hide controller delay start")
            delay(CONTROL_HIDE_DELAY)
//            logger.info("PointerPlayerViewModel: hide controller after delay")
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