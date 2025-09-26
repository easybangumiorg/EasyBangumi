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
import org.easybangumi.next.shared.debug.DebugScope
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.libplayer.api.MediaItem
import org.easybangumi.next.libplayer.vlcj.VlcjBridgeManager
import org.easybangumi.next.libplayer.vlcj.VlcjPlayerFrame
import org.easybangumi.next.libplayer.vlcj.rememberVlcjPlayerFrameState
import org.easybangumi.next.shared.playcon.pointer.PointerPlaycon
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

    val manager = koinInject<VlcjBridgeManager>()
    val tag = remember {
        "debug-player"
    }
    val frameState = rememberVlcjPlayerFrameState()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val lifecycleState = lifecycle.currentStateAsState()
    val bridge = remember {
        manager.getOrCreateBridge(tag, null)
    }

    DisposableEffect(Unit) {
        onDispose {
            logger.info("PlayerDebug disposing bridge: $tag")
            // 销毁时释放资源
            frameState.unbindBridge()
            manager.release(tag)
        }
    }
    LaunchedEffect(Unit) {
        val mediaItem = MediaItem(uri = url)
        bridge.prepare(mediaItem)
        snapshotFlow {
            lifecycleState.value
        }.collect {
            logger.info("PlayerDebug lifecycle state: $it")
            when (it) {
                // 最小化
                Lifecycle.State.CREATED -> {
//                    bridge.setPlayWhenReady(false)
                }
                // 没有焦点
                Lifecycle.State.STARTED -> {
                    // 创建时绑定桥接
                    frameState.bindBridge(bridge)
                }
                // 有焦点
                Lifecycle.State.RESUMED -> {
//                    bridge.setPlayWhenReady(true)
                }

                Lifecycle.State.INITIALIZED -> {
                    // 接收不到，因为此时 Compose 还没有创建
                }
                Lifecycle.State.DESTROYED -> {
                    // 接收不到，因为这时 Compose 已经销毁了
                }
            }
        }
    }



    VlcjPlayerFrame(
        modifier = Modifier.fillMaxSize(),
        state = frameState,
    )

//    PointerPlaycon(
//        modifier = Modifier.fillMaxSize(),
//        bridge = bridge,
//    )


}