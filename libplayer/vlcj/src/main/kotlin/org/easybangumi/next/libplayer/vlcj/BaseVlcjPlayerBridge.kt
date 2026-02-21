package org.easybangumi.next.libplayer.vlcj

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.update
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.libplayer.api.AbsPlayerBridge
import org.easybangumi.next.libplayer.api.C
import org.easybangumi.next.libplayer.api.MediaItem
import org.easybangumi.next.libplayer.api.action.Action
import org.easybangumi.next.libplayer.api.action.SpeedAction
import org.easybangumi.next.libplayer.vlcj.action.VlcjSpeedAction
import uk.co.caprica.vlcj.media.MediaRef
import uk.co.caprica.vlcj.media.TrackType
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.MediaPlayerEventListener
import javax.swing.SwingUtilities
import kotlin.math.absoluteValue
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

open class BaseVlcjPlayerBridge(
    private val manager: VlcjBridgeManager,
): AbsPlayerBridge<MediaPlayer>(), AutoCloseable {

    protected val logger = logger()

    protected val mediaPlayer: MediaPlayer by lazy {
        manager.createMediaPlayer()
    }

    override val impl: MediaPlayer
        get() = mediaPlayer

//    private val mediaPlayerLazy = lazy {
//        val mp = reentrantLock.withLock {
//            mediaPlayerFactory.mediaPlayers().newMediaPlayer()
//        }
//        callbackVideoSurface.attach(mp)
//        mp.events().addMediaPlayerEventListener(mediaPlayerEventListener)
//        mp
//    }
//    private val mediaPlayer: MediaPlayer by mediaPlayerLazy

    override val positionMs: Long
        get() = if (seekingTargetTime < 0) mediaPlayer.status().time() else seekingTargetTime

    // unsupported
    override val bufferedPositionMs: Long
        get() = C.TIME_UNSET

    override val durationMs: Long
        get() = mediaPlayer.status().length()

    override fun prepare(mediaItem: MediaItem) {
        //logger.debug("Preparing media: ${mediaItem.uri}")
        if (innerPlayWhenReadyFlow.value) {
            val result = mediaPlayer.media().play(mediaItem.uri)
            //logger.debug("Media play result: $result")
        } else {
            val result = mediaPlayer.media().prepare(mediaItem.uri)
            //logger.debug("Media prepare result: $result")
        }
        innerErrorFlow.update { null }
    }

    override fun setPlayWhenReady(playWhenReady: Boolean) {
        mediaPlayer.controls().setPause(!playWhenReady)
        if (playWhenReady) {
            mediaPlayer.controls().play()
        }
        innerPlayWhenReadyFlow.update { playWhenReady }
    }

    @Volatile
    protected var seekingTargetTime = -1L

    // Throttle buffering() posts to EDT to reduce allocations (see buffering override)
    private val BUFFERING_THROTTLE_MS = 150L
    @Volatile
    private var lastBufferingPostTimeMs = 0L
    @Volatile
    private var lastPostedBufferingCache = 0f

    override fun seekTo(positionMs: Long) {
        SwingUtilities.invokeLater {
            mediaPlayer.controls().setTime(positionMs)
            seekingTargetTime = positionMs
        }
    }


    override fun setScaleType(scaleType: C.RendererScaleType) {
        innerScaleTypeFlow.update { scaleType }
    }


    override fun prepareAction(): Map<KClass<out Action<*>>, Action<*>> {
        return mapOf<KClass<out Action<*>>, Action<*>>(
            SpeedAction::class to VlcjSpeedAction().apply { onBind(this@BaseVlcjPlayerBridge) }
        )
    }

    override fun close() {
        actionMap.forEach { (klass, action) ->
            action.onUnbind()
        }
        mediaPlayer.events().removeMediaPlayerEventListener(mediaPlayerEventListener)
        mediaPlayer.release()
    }

    private val mediaPlayerEventListener = object: MediaPlayerEventListener {
        override fun mediaChanged(
            mediaPlayer: MediaPlayer?,
            media: MediaRef?
        ) {

        }

        override fun opening(mediaPlayer: MediaPlayer?) {
            SwingUtilities.invokeLater {
                //logger.debug("VLC MediaPlayer opening")
                innerPlayStateFlow.update { C.State.PREPARING }
            }
        }

        // vlc 如果 seek 超过多个关键帧，会先定位到对应关键帧走一遍 buffering，在定位到目标 走一遍 buffering
        // 因此哪怕 newCache 为 100 但是时间不对也需要视为在加载
        // 并且因为帧率问题最终可能不会精准定位到对应 ms，添加 1.5s 宽容度
        // Throttle: avoid posting to EDT on every buffering() call to reduce allocations and EDT load
        override fun buffering(mediaPlayer: MediaPlayer?, newCache: Float) {
            val now = System.currentTimeMillis()
            val time = mediaPlayer?.status()?.time()
            val transitionToFull = newCache >= 100f && lastPostedBufferingCache < 100f
            val throttleElapsed = now - lastBufferingPostTimeMs >= BUFFERING_THROTTLE_MS
            if (!transitionToFull && !throttleElapsed) return
            lastBufferingPostTimeMs = now
            lastPostedBufferingCache = newCache
            SwingUtilities.invokeLater {
                logger.debug("buffering: $newCache seekingTargetTime: $seekingTargetTime time: ${mediaPlayer?.status()?.time()}")
                if (newCache < 100f) {
                    innerPlayStateFlow.update { C.State.BUFFERING }
                } else {
                    val currentTime = mediaPlayer?.status()?.time()
                    if (currentTime == null || seekingTargetTime == -1L) {
                        innerPlayStateFlow.update { C.State.READY }
                        return@invokeLater
                    }
                    if ((currentTime - seekingTargetTime).absoluteValue < 15000) {
                        // Paused state needs to render target frame
                        if (!innerPlayWhenReadyFlow.value) {
                            mediaPlayer?.controls()?.nextFrame()
                        }
                        seekingTargetTime = -1L
                        innerPlayStateFlow.update { C.State.READY }
                    } else {
                        innerPlayStateFlow.update { C.State.BUFFERING }
                    }
                }
            }
        }

        override fun playing(mediaPlayer: MediaPlayer?) {
            SwingUtilities.invokeLater {
                //logger.debug("VLC MediaPlayer playing")
                innerPlayStateFlow.update { C.State.READY }
                innerPlayWhenReadyFlow.update { true }
            }
        }

        override fun paused(mediaPlayer: MediaPlayer?) {
            SwingUtilities.invokeLater {
                innerPlayWhenReadyFlow.update { false }
            }
        }

        override fun stopped(mediaPlayer: MediaPlayer?) {
            SwingUtilities.invokeLater {
                innerPlayStateFlow.update { C.State.IDLE }
            }
        }

        override fun forward(mediaPlayer: MediaPlayer?) {

        }

        override fun backward(mediaPlayer: MediaPlayer?) {

        }

        override fun finished(mediaPlayer: MediaPlayer?) {
            SwingUtilities.invokeLater {
                innerPlayStateFlow.update { C.State.ENDED }
            }
        }

        override fun timeChanged(mediaPlayer: MediaPlayer?, newTime: Long) {

        }

        override fun positionChanged(
            mediaPlayer: MediaPlayer?,
            newPosition: Float
        ) {

        }

        override fun seekableChanged(
            mediaPlayer: MediaPlayer?,
            newSeekable: Int
        ) {

        }

        override fun pausableChanged(
            mediaPlayer: MediaPlayer?,
            newPausable: Int
        ) {

        }

        override fun titleChanged(mediaPlayer: MediaPlayer?, newTitle: Int) {

        }

        override fun snapshotTaken(mediaPlayer: MediaPlayer?, filename: String?) {

        }

        override fun lengthChanged(mediaPlayer: MediaPlayer?, newLength: Long) {

        }

        override fun videoOutput(mediaPlayer: MediaPlayer?, newCount: Int) {

        }

        override fun scrambledChanged(
            mediaPlayer: MediaPlayer?,
            newScrambled: Int
        ) {

        }

        override fun elementaryStreamAdded(
            mediaPlayer: MediaPlayer?,
            type: TrackType?,
            id: Int
        ) {

        }

        override fun elementaryStreamDeleted(
            mediaPlayer: MediaPlayer?,
            type: TrackType?,
            id: Int
        ) {

        }

        override fun elementaryStreamSelected(
            mediaPlayer: MediaPlayer?,
            type: TrackType?,
            id: Int
        ) {

        }

        override fun corked(mediaPlayer: MediaPlayer?, corked: Boolean) {

        }

        override fun muted(mediaPlayer: MediaPlayer?, muted: Boolean) {

        }

        override fun volumeChanged(mediaPlayer: MediaPlayer?, volume: Float) {

        }

        override fun audioDeviceChanged(
            mediaPlayer: MediaPlayer?,
            audioDevice: String?
        ) {

        }

        override fun chapterChanged(mediaPlayer: MediaPlayer?, newChapter: Int) {

        }

        override fun error(mediaPlayer: MediaPlayer?) {
            logger.error("VLC MediaPlayer error occurred")
            SwingUtilities.invokeLater {
                innerErrorFlow.update {
                    IllegalStateException("VLC MediaPlayer error occurred")
                }
            }

        }

        override fun mediaPlayerReady(mediaPlayer: MediaPlayer?) {
            SwingUtilities.invokeLater {
                innerPlayStateFlow.update { C.State.READY }
            }
        }
    }


    init {
        mediaPlayer.events().addMediaPlayerEventListener(mediaPlayerEventListener)
    }

}