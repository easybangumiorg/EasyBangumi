package org.easybangumi.next.libplayer.exoplayer

import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.update
import org.easybangumi.next.libplayer.api.AbsPlayerBridge
import org.easybangumi.next.libplayer.api.MediaItem
import org.easybangumi.next.libplayer.api.action.Action
import kotlin.reflect.KClass

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
class ExoPlayerBridge(
    exoPlayerBuilder: ExoPlayer.Builder
): AbsPlayerBridge() {

    private val exoPlayerLazy = lazy {
        exoPlayerBuilder.build().apply {
            addListener(playerListener)
        }
    }
    private val exoPlayer: ExoPlayer by exoPlayerLazy

    override val positionMs: Long
        get() {
            if (exoPlayerLazy.isInitialized()) {
                val exoTime = exoPlayer.currentPosition
                if (exoTime == C.TIME_UNSET) {
                    return org.easybangumi.next.libplayer.api.C.TIME_UNSET
                }
                return exoTime
            } else {
                return org.easybangumi.next.libplayer.api.C.TIME_UNSET
            }
        }
    override val bufferedPositionMs: Long
        get() {
            if (exoPlayerLazy.isInitialized()) {
                val exoTime = exoPlayer.bufferedPosition
                if (exoTime == C.TIME_UNSET) {
                    return org.easybangumi.next.libplayer.api.C.TIME_UNSET
                }
                return exoTime
            } else {
                return org.easybangumi.next.libplayer.api.C.TIME_UNSET
            }
        }
    override val durationMs: Long
        get() {
            if (exoPlayerLazy.isInitialized()) {
                val exoTime = exoPlayer.duration
                if (exoTime == C.TIME_UNSET) {
                    return org.easybangumi.next.libplayer.api.C.TIME_UNSET
                }
                return exoTime
            } else {
                return org.easybangumi.next.libplayer.api.C.TIME_UNSET
            }
        }

    override fun prepareAction(): Map<KClass<out Action>, Action> {
        return mapOf(

        )
    }

    override val impl: Any
        get() = exoPlayer

    override fun prepare(mediaItem: MediaItem) {
        exoPlayer.contentPosition
    }

    override fun setPlayWhenReady(playWhenReady: Boolean) {
        exoPlayer.playWhenReady = playWhenReady
    }

    override fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
    }

    override fun setScaleType(scaleType: org.easybangumi.next.libplayer.api.C.RendererScaleType) {
        innerScaleTypeFlow.update { scaleType }
    }

    override fun close() {

    }

    private val playerListener: Player.Listener = object: Player.Listener {
        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            super.onPlayWhenReadyChanged(playWhenReady, reason)
            innerPlayWhenReadyFlow.update { playWhenReady }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            when (playbackState) {
                Player.STATE_IDLE -> innerPlayStateFlow.update { org.easybangumi.next.libplayer.api.C.State.IDLE }
                Player.STATE_BUFFERING -> innerPlayStateFlow.update { org.easybangumi.next.libplayer.api.C.State.BUFFERING }
                Player.STATE_READY -> innerPlayStateFlow.update { org.easybangumi.next.libplayer.api.C.State.READY }
                Player.STATE_ENDED -> innerPlayStateFlow.update { org.easybangumi.next.libplayer.api.C.State.ENDED }
            }
        }

        override fun onVideoSizeChanged(videoSize: VideoSize) {
            super.onVideoSizeChanged(videoSize)
            innerVideoSizeFlow.update {
                org.easybangumi.next.libplayer.api.VideoSize(
                    width = videoSize.width,
                    height = videoSize.height,
                )
            }
        }

        override fun onAvailableCommandsChanged(availableCommands: Player.Commands) {
            super.onAvailableCommandsChanged(availableCommands)
            if (availableCommands.contains(Player.COMMAND_PREPARE)) {
                innerPlayStateFlow.update { org.easybangumi.next.libplayer.api.C.State.PREPARING }
            }
        }
    }
}