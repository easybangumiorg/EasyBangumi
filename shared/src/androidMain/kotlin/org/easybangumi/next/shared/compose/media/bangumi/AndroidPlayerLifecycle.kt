package org.easybangumi.next.shared.compose.media.bangumi

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import org.easybangumi.next.shared.LocalActivity
import org.easybangumi.next.shared.compose.media.AndroidPlayerViewModel
import org.easybangumi.next.shared.foundation.utils.OnLifecycleEvent
import org.easybangumi.next.shared.foundation.utils.OnOrientationEvent
import org.easybangumi.next.shared.utils.MediaUtils

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
    vm: AndroidBangumiMediaViewModel
) {
    val ctx = LocalActivity.current as Activity

    val state = vm.state.collectAsState()
    DisposableEffect(Unit) {
        val old = ctx.requestedOrientation
        onDispose {
            logger.info("Player Dispose")
            ctx.requestedOrientation = old
        }
    }

    LaunchedEffect(state.value.fullscreen) {
        if (vm.state.value.fullscreen) {
            MediaUtils.setSystemBarsBehavior(ctx, WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE)
//            MediaUtils.setIsStatusBarsShow(ctx, false)
            MediaUtils.setIsNavBarsShow(ctx, false)
        } else {
            MediaUtils.setIsStatusBarsShow(ctx, true)
            MediaUtils.setIsNavBarsShow(ctx, true)
        }
    }

    // 根据传感器来横竖屏
    OnOrientationEvent { _, orientation ->

    }



    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                MediaUtils.setIsNavBarsShow(ctx, !state.value.fullscreen)
                MediaUtils.setIsStatusBarsShow(ctx, !state.value.fullscreen)
            }
            Lifecycle.Event.ON_PAUSE -> vm.playerViewModel.exoBridge.setPlayWhenReady(false)
            else -> Unit
        }
    }

}