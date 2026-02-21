package org.easybangumi.next.shared.playcon.android

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.libplayer.exoplayer.ExoPlayerBridge
import org.easybangumi.next.shared.playcon.BasePlayconViewModel

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
class AndroidPlayconVM(
    bridge: ExoPlayerBridge,
) : BasePlayconViewModel<ExoPlayerBridge>(bridge) {

    companion object {
        const val CONTROL_HIDE_DELAY = 4000L
    }

    private val logger = logger()

    var hideDelayJob: Job? = null

//    var isLoading by mutableStateOf(false)

    var isShowController by mutableStateOf(false)

    var isLocked by mutableStateOf(false)


    var isLoadingShow by mutableStateOf(false)

    var isPositionScrolling: Boolean by mutableStateOf(false)
    var scrollingPosition: Long by mutableStateOf(0L)

    val showBrightVolumeUi: MutableState<Boolean> = mutableStateOf(false)
    val brightVolumeType: MutableState<DragType> = mutableStateOf(DragType.VOLUME)
    val brightVolumePercent: MutableState<Int> = mutableStateOf(0)

    init {
        viewModelScope.launch {
            snapshotFlow {
                playerState
            }.collectLatest {
                isLoadingShow = (it == org.easybangumi.next.libplayer.api.C.State.PREPARING || it == org.easybangumi.next.libplayer.api.C.State.BUFFERING)
            }
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


    fun onSingleClick() {
        if (!isLocked) {
            toggleController()
        }
    }

    fun onPlayPause(playWhenReady: Boolean) {}

    fun fastRewind() {}

    fun fastForward() {}


    fun onLongPress() {}

    fun onActionUP() {
        viewModelScope.launch {
            if (isPositionScrolling) {
                seekTo(scrollingPosition)
                isPositionScrolling = false
            }
            restartHideDelayJob()
        }
    }

    fun onGesturePositionChange(position: Long) {
        logger.info("onGesturePositionChange: $position, duration: $duration, isMedia = ${bridge.isMedia()}")
        if (!bridge.isMedia()) {
            return
        }
        scrollingPosition = position.coerceIn(0L, duration.coerceAtLeast(Long.MAX_VALUE))
        isPositionScrolling = true
    }

    fun onSeekBarPositionChange(position: Long) {
        logger.info("onSeekBarPositionChange: $position, duration: $duration, isMedia = ${bridge.isMedia()}")
        if (!bridge.isMedia()) {
            return
        }
        scrollingPosition = position.coerceIn(0L, duration.coerceAtLeast(Long.MAX_VALUE))
        isPositionScrolling = true

    }


}