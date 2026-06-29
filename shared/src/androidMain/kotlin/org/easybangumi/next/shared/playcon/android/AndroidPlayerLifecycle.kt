package org.easybangumi.next.shared.playcon.android

import android.app.Activity
import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import org.easybangumi.next.shared.foundation.utils.MediaUtils
import org.easybangumi.next.shared.foundation.utils.OnLifecycleEvent
import org.easybangumi.next.shared.foundation.utils.OnOrientationEvent

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
@Composable
fun MediaPlayerSync(
    playerViewModel: AndroidPlayerVM,
) {
    val ctx = LocalContext.current as Activity

    val state = playerViewModel.screenModeViewModel.logic.collectAsState()
    // 退出时恢复 activity 的 requestedOrientation
    DisposableEffect(Unit) {
        val old = ctx.requestedOrientation
        onDispose {
            logger.info("Player Dispose")
            ctx.requestedOrientation = old
            // 先直接恢复显示，如果有其他页面需要自定义该状态
            MediaUtils.setIsStatusBarsShow(ctx, true)
            MediaUtils.setIsNavBarsShow(ctx, true)
        }
    }

    LaunchedEffect(state.value.isFullScreen) {
        if (state.value.isFullScreen) {
            MediaUtils.setSystemBarsBehavior(ctx, WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE)
            MediaUtils.setIsStatusBarsShow(ctx, false)
            MediaUtils.setStatusBarColor(ctx, Color.TRANSPARENT)
            MediaUtils.setIsNavBarsShow(ctx, false)
        } else {
            MediaUtils.setIsStatusBarsShow(ctx, true)
            MediaUtils.setIsNavBarsShow(ctx, true)
        }
    }

    // 根据传感器来横竖屏
    OnOrientationEvent { _, orientation ->
        playerViewModel.screenModeViewModel.onOrientationEvent(orientation)
    }


    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                MediaUtils.setIsNavBarsShow(ctx, !state.value.isFullScreen)
                MediaUtils.setIsStatusBarsShow(ctx, !state.value.isFullScreen)
            }
            Lifecycle.Event.ON_PAUSE -> playerViewModel.exoBridge.setPlayWhenReady(false)
            else -> Unit
        }
    }

}