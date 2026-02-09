package org.easybangumi.next.shared.playcon.desktop

import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState

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
 *  先简单写，后续再优化
 */
class FullscreenStrategy(
    private val windowState: () -> WindowState
) {

    private var lastPlacement: WindowPlacement? = null

    fun enterFullscreen() {
        val windowState = windowState()
        lastPlacement = windowState.placement
        windowState.placement = WindowPlacement.Fullscreen
    }

    fun exitFullscreen() {
        val windowState = windowState()
        windowState.placement = WindowPlacement.Floating
    }

    fun isFullscreen(): Boolean {
        val windowState = windowState()
        return windowState.placement == WindowPlacement.Fullscreen
    }




}