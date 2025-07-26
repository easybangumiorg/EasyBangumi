package org.easybangumi.next.libplayer.exoplayer

import android.app.Application
import android.view.TextureView
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
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
@OptIn(UnstableApi::class)
class ExoPlayerBridge(
    application: Application,
    exoPlayerBuilder: ExoPlayer.Builder,
): AbsPlayerBridge() {

    private val exoMediaSourceFactory = ExoMediaSourceFactory(application)

    private val exoPlayerLazy = lazy {
        exoPlayerBuilder.build().apply {
            addListener(playerListener)
        }
    }
    private var textureView: EasyTextureView? = null
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
        val player = exoPlayerLazy.value
        val exoItem = exoMediaSourceFactory.getMediaItem(mediaItem)
        val mediaSource = exoMediaSourceFactory.getMediaSourceFactory(mediaItem)
        player.setMediaSource(mediaSource.createMediaSource(exoItem))
        player.prepare()
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
        if (exoPlayerLazy.isInitialized()) {
            exoPlayer.removeListener(playerListener)
            exoPlayer.release()
        }
        innerPlayWhenReadyFlow.value = false
        innerPlayStateFlow.value = org.easybangumi.next.libplayer.api.C.State.IDLE
        innerVideoSizeFlow.value = org.easybangumi.next.libplayer.api.VideoSize(0, 0)
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

        override fun onVideoSizeChanged(videoSize: ExoVideoSize) {
            super.onVideoSizeChanged(videoSize)
            innerVideoSizeFlow.update {
                LibVideoSize(
                    width = videoSize.width,
                    height = videoSize.height,
                )
            }
            textureView?.setVideoSize(LibVideoSize(
                width = videoSize.width,
                height = videoSize.height,
            ))
        }

        override fun onAvailableCommandsChanged(availableCommands: Player.Commands) {
            super.onAvailableCommandsChanged(availableCommands)
            if (availableCommands.contains(Player.COMMAND_PREPARE)) {
                innerPlayStateFlow.update { org.easybangumi.next.libplayer.api.C.State.PREPARING }
            }
        }
    }

    fun attachVideoView(videoView: EasyTextureView) {
        exoPlayer.setVideoTextureView(videoView)
        videoView.setVideoSize(videoSizeFlow.value)
        textureView = videoView
    }

    fun detachVideoView(view: EasyTextureView) {
        exoPlayer.clearVideoTextureView(view)
        textureView = null
//        exoPlayer.setVideoTextureView(null)
    }
}