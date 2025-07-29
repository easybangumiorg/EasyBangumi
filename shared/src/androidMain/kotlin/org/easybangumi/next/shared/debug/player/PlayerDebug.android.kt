package org.easybangumi.next.shared.debug.player

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.collect
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.utils.global
import org.easybangumi.next.libplayer.api.MediaItem
import org.easybangumi.next.libplayer.exoplayer.ExoPlayerBridge
import org.easybangumi.next.libplayer.exoplayer.ExoPlayerCompose
import org.easybangumi.next.libplayer.exoplayer.rememberExoPlayerFrameState
import org.easybangumi.next.shared.debug.DebugScope
import org.koin.compose.koinInject

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
val url = "https://media.w3.org/2010/05/sintel/trailer.mp4"
private val logger = logger("PlayerDebug")

@Composable
actual fun DebugScope.PlayerDebug() {
    val builder = koinInject<ExoPlayer.Builder>()
    val bridge = remember {
        ExoPlayerBridge(global.appContext, builder)
    }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val lifecycleState = lifecycle.currentStateAsState()

    val state = rememberExoPlayerFrameState()
    DisposableEffect(Unit) {
        onDispose {
            logger.info("PlayerDebug disposing bridge")
            // 销毁时释放资源
            state.unbindBridge()
            bridge.close()
        }
    }
    LaunchedEffect(Unit) {
        val mediaItem = MediaItem(uri = url)
        bridge.prepare(mediaItem)
        state.bindBridge(bridge)
        snapshotFlow {
            lifecycleState.value
        }.collect {
            when(it) {
                Lifecycle.State.STARTED -> {
                    logger.info("PlayerDebug lifecycle state: $it, binding bridge")
//                    state.bindBridge(bridge)
                }
                Lifecycle.State.RESUMED -> {
                    logger.info("PlayerDebug lifecycle state: $it, resuming player")
                    bridge.setPlayWhenReady(true)
                }
                Lifecycle.State.DESTROYED -> {
                    logger.info("PlayerDebug lifecycle state: $it, unbinding bridge")
//                    state.unbindBridge()
                }
                else -> {
                    logger.info("PlayerDebug lifecycle state: $it")
                }
            }
        }
    }
    ExoPlayerCompose(
        modifier = Modifier.fillMaxSize(),
        state = state
    )
}