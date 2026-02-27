package org.easybangumi.next.shared.playcon.android

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import org.easybangumi.next.libplayer.api.C
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
        const val LONG_PRESS_SPEED = 2.0f
        const val FAST_SEEK_MS = 10_000L
    }

    private val logger = logger()

    var hideDelayJob: Job? = null

    var isShowController by mutableStateOf(false)

    var isLocked by mutableStateOf(false)

    var isLoadingShow by mutableStateOf(false)

    var isEnded by mutableStateOf(false)

    // -- position scrolling --
    var isPositionScrolling: Boolean by mutableStateOf(false)
    var scrollingPosition: Long by mutableStateOf(0L)

    // -- brightness / volume overlay --
    val showBrightVolumeUi: MutableState<Boolean> = mutableStateOf(false)
    val brightVolumeType: MutableState<DragType> = mutableStateOf(DragType.VOLUME)
    val brightVolumePercent: MutableState<Int> = mutableStateOf(0)

    // -- speed control --
    var curSpeed: Float by mutableFloatStateOf(1f)
        private set

    // -- long-press speed --
    var isLongPressing by mutableStateOf(false)
        private set
    private var speedBeforeLongPress: Float = 1f

    // -- fast forward/rewind tap feedback --
    var isFastForwardWinShow by mutableStateOf(false)
    var isFastRewindWinShow by mutableStateOf(false)

    // -- title --
    var title: String by mutableStateOf("")

    init {
        viewModelScope.launch {
            snapshotFlow {
                playerState
            }.collectLatest {
                isLoadingShow = (it == C.State.PREPARING || it == C.State.BUFFERING)
                isEnded = (it == C.State.ENDED)
            }
        }
    }

    // -- speed --

    fun setSpeed(speed: Float) {
        curSpeed = speed
        bridge.impl.setPlaybackSpeed(speed)
    }

    // -- lock --

    fun lock() {
        isLocked = true
        restartHideDelayJob()
    }

    fun unlock() {
        isLocked = false
        restartHideDelayJob()
    }

    fun toggleLock() {
        if (isLocked) unlock() else lock()
    }

    // -- seek --

    fun seekTo(position: Long) {
        bridge.seekTo(position)
    }

    fun onSeekEnd(seekPosition: Long) {
        bridge.seekTo(seekPosition)
        restartHideDelayJob()
    }

    // -- controller visibility --

    fun showController(needHideDelay: Boolean = true) {
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
        if (isShowController) hideController() else showController()
    }

    fun restartHideDelayJob() {
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

    // -- gestures --

    fun onSingleClick() {
        if (isLocked) {
            toggleController()
        } else {
            toggleController()
        }
    }

    fun onPlayPause(playWhenReady: Boolean) {
        setPlayWhenReady(playWhenReady)
        restartHideDelayJob()
    }

    fun fastForward() {
        if (!bridge.isMedia()) return
        val target = (position + FAST_SEEK_MS).coerceAtMost(duration)
        bridge.seekTo(target)
        isFastForwardWinShow = true
        viewModelScope.launch {
            delay(500)
            isFastForwardWinShow = false
        }
    }

    fun fastRewind() {
        if (!bridge.isMedia()) return
        val target = (position - FAST_SEEK_MS).coerceAtLeast(0)
        bridge.seekTo(target)
        isFastRewindWinShow = true
        viewModelScope.launch {
            delay(500)
            isFastRewindWinShow = false
        }
    }

    fun onLongPress() {
        speedBeforeLongPress = curSpeed
        isLongPressing = true
        setSpeed(LONG_PRESS_SPEED)
    }

    fun onLongPressRelease() {
        isLongPressing = false
        setSpeed(speedBeforeLongPress)
    }

    fun onActionUP() {
        if (isLongPressing) {
            onLongPressRelease()
        }
        viewModelScope.launch {
            if (isPositionScrolling) {
                seekTo(scrollingPosition)
                isPositionScrolling = false
            }
            restartHideDelayJob()
        }
    }

    fun onGesturePositionChange(position: Long) {
        if (!bridge.isMedia()) return
        scrollingPosition = position.coerceIn(0L, duration.coerceAtLeast(0))
        isPositionScrolling = true
    }

    fun onSeekBarPositionChange(position: Long) {
        if (!bridge.isMedia()) return
        scrollingPosition = position.coerceIn(0L, duration.coerceAtLeast(0))
        isPositionScrolling = true
    }
}