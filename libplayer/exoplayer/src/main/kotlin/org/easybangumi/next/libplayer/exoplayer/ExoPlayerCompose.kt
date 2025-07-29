package org.easybangumi.next.libplayer.exoplayer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.easybangumi.next.lib.logger.logger

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

private val logger = logger("ExoPlayerCompose")
@Composable
fun ExoPlayerCompose(
    modifier: Modifier,
    state: ExoPlayerFrameState,
) {
    state.FrameCanvas(modifier)
}

@Composable
fun rememberExoPlayerFrameState(
): ExoPlayerFrameState {
    return remember() {
        ExoPlayerFrameState()
    }
}


class ExoPlayerFrameState() {
    private var bridge by mutableStateOf<ExoPlayerBridge?>(null)
    private var textureView: EasyTextureView? by mutableStateOf(null)


    fun bindBridge(bridge: ExoPlayerBridge) {
//        if (this.bridge == bridge) return
        this.bridge = bridge
//        textureView?.let {
//            bridge.attachTextureView(it)
//        }
    }

    fun unbindBridge() {
        textureView?.let {
            bridge?.detachTextureView(it)
        }
        bridge = null
    }

    @Composable
    fun FrameCanvas(
        modifier: Modifier
    ) {

        val bridge = bridge
        val textureView = textureView
        LaunchedEffect(bridge, textureView) {
            if (bridge == null || textureView == null) return@LaunchedEffect
            bridge.attachTextureView(textureView)
        }
        if (bridge != null) {
            val scaleType by bridge.scaleTypeFlow.collectAsState()
            val videoSize by bridge.videoSizeFlow.collectAsState()
            LaunchedEffect(scaleType, videoSize, textureView) {
                logger.info("ExoPlayerCompose: scaleType=$scaleType, videoSize=$videoSize, textureView=$textureView")
                textureView?.setScaleType(scaleType)
                textureView?.setVideoSize(videoSize)
            }
        }

        AndroidView(
            modifier = modifier,
            factory = {
                EasyTextureView(it)
            },
            update = {
                this.textureView = it
            }
        )
    }


}