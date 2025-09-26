package org.easybangumi.next.shared.compose.media

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.easybangumi.next.shared.playcon.BasePlayconViewModel
import org.easybangumi.next.shared.playcon.android.AndroidGestureController
import org.easybangumi.next.shared.playcon.android.AndroidPlaycon
import org.easybangumi.next.shared.playcon.android.AndroidPlayconContentScope
import org.easybangumi.next.shared.playcon.android.PlayconBottomBar
import org.easybangumi.next.shared.playcon.android.AndroidPlayerVM
import org.easybangumi.next.shared.playcon.android.DuringText
import org.easybangumi.next.shared.playcon.android.FullScreenBtn
import org.easybangumi.next.shared.playcon.android.PlayPauseBtn
import org.easybangumi.next.shared.playcon.android.PositionText
import org.easybangumi.next.shared.playcon.android.TimeSeekBar

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
fun MediaPlayer(
    modifier: Modifier,
    playerVm: AndroidPlayerVM,
){


    Box(
        modifier = modifier
    ) {
        playerVm.exoPlayerFrameState.FrameCanvas(Modifier.matchParentSize())
        AndroidPlaycon(
            modifier = Modifier.matchParentSize(),
            vm = playerVm.playconVM,
        ) {
            // 手势层
           AndroidGestureController(
                vm,
                modifier = Modifier.fillMaxSize()
            ) {

            }

            // 控制器层
            ControllerContent(modifier = Modifier.fillMaxSize(), onFullScreenChange = {
                playerVm.screenModeViewModel.fireUserFullScreenChange(it)
            })

            if (vm.isLoadingShow) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }


        }
    }
}

@Composable
fun AndroidPlayconContentScope.ControllerContent(
    modifier: Modifier,
    onFullScreenChange: (isFullScreen: Boolean) -> Unit = { _ -> }
) {
    val isFullScreen = vm.screenMode == BasePlayconViewModel.ScreenMode.FULLSCREEN
    val isLock = vm.isLocked
    val isShowController = vm.isShowController

    AnimatedContent(
        isLock to isShowController,
        transitionSpec = {
            (fadeIn(animationSpec = tween(90)))
                .togetherWith(fadeOut(animationSpec = tween(90)))
        },
    ) {
        val (isLock, isShowController) = it

        Box(
            modifier
        ) {

            if (!isLock && isShowController) {
                this@ControllerContent.PlayconBottomBar(
                    modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
                ) {
                    PlayPauseBtn()
                    PositionText()
                    TimeSeekBar()
                    DuringText()
                    FullScreenBtn(onFullScreenChange = {
                        onFullScreenChange(it)
                    })
                }


            } else if (isLock) {

            } else {

            }


        }

    }

}
