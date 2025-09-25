package org.easybangumi.next.shared.playcon

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.libplayer.api.C
import org.easybangumi.next.libplayer.api.PlayerBridge
import org.easybangumi.next.libplayer.api.VideoSize
import org.easybangumi.next.libplayer.api.isMediaSet
import org.easybangumi.next.shared.foundation.view_model.BaseViewModel

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
open class BasePlayconViewModel<T: PlayerBridge>(
    protected val bridge: T,
) : BaseViewModel() {

    private val logger = logger()

    companion object {
        const val POSITION_LOOP_DELAY = 1000L
    }

    enum class ScreenMode {
        NORMAL, FULLSCREEN
    }

    var screenMode: ScreenMode by mutableStateOf<ScreenMode>(ScreenMode.NORMAL)


    protected var _playerState: C.State by mutableStateOf<C.State>(bridge.playStateFlow.value)
    val playerState: C.State
        get() = _playerState

    protected var _playWhenReady: Boolean by mutableStateOf(bridge.playWhenReadyFlow.value)
    val playWhenReady: Boolean
        get() = _playWhenReady


    protected var _videoSize: VideoSize by mutableStateOf(bridge.videoSizeFlow.value)
    val videoSize: VideoSize
        get() = _videoSize

    // 以下需要自己更新

    protected var _position: Long by mutableStateOf(bridge.positionMs)
    val position: Long
        get() = _position

    protected var _duration: Long by mutableStateOf(bridge.durationMs)
    val duration: Long
        get() = _duration

    protected var _bufferedPosition: Long by mutableStateOf(bridge.bufferedPositionMs)
    val bufferedPosition: Long
        get() = _bufferedPosition

    private var lopperJob: Job? = null
    private val needLoop = MutableStateFlow(false)

    fun needLoop() {
        needLoop.update { true }
    }

    fun noNeedLoop() {
        needLoop.update { false }
    }

    fun stopLoop() {
        lopperJob?.cancel()
        lopperJob = null
    }

    init {
        viewModelScope.launch {
            combine(
                bridge.playStateFlow,
                needLoop
            ) { state, needLoop ->
//                logger.info("loop job start playStateFlow: $state, needLoop: $needLoop, state.isMediaSet: ${state.isMediaSet()}")
                _playerState = state
                if (needLoop && state.isMediaSet() ) {
                    lopperJob?.cancel()
                    lopperJob = viewModelScope.launch {
                        while (isActive) {
                            yield()
                            _position = bridge.positionMs
                            _bufferedPosition = bridge.bufferedPositionMs
                            _duration = bridge.durationMs
//                            logger.info("loop job position: $_position, bufferedPosition: $_bufferedPosition, duration: $_duration")
                            delay(POSITION_LOOP_DELAY)
                        }
                    }
                }
                else {
                   stopLoop()
                }

            }.collect()
        }

        viewModelScope.launch {
            bridge.playWhenReadyFlow.collectLatest {
                if (_playWhenReady != it) {
                    _playWhenReady = it
                    logger.info("PlayWhenReady changed: $_playWhenReady")
                }
            }
        }

        viewModelScope.launch {
            bridge.videoSizeFlow.collectLatest {
                if (_videoSize != it) {
                    _videoSize = it
                    logger.info("VideoSize changed: $_videoSize")
                }
            }
        }

        viewModelScope.launch {
            bridge.playStateFlow.collectLatest {
                if (_playerState != it) {
                    _playerState = it
                    logger.info("PlayerState changed: $_playerState")
                }
            }
        }

    }

    fun setPlayWhenReady(playWhenReady: Boolean) {
        if (_playWhenReady != playWhenReady) {
            _playWhenReady = playWhenReady
        }
        bridge.setPlayWhenReady(playWhenReady)
    }

}