package org.easybangumi.next.shared.compose.media

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.easybangumi.next.shared.LocalNavController
import org.easybangumi.next.shared.compose.media.bangumi.DesktopBangumiMediaVM
import org.easybangumi.next.shared.playcon.BasePlayconViewModel
import org.easybangumi.next.shared.playcon.desktop.DesktopPlayerVM
import org.easybangumi.next.shared.playcon.pointer.BackBtn
import org.easybangumi.next.shared.playcon.pointer.DuringText
import org.easybangumi.next.shared.playcon.pointer.FullScreenBtn
import org.easybangumi.next.shared.playcon.pointer.PlayPauseBtn
import org.easybangumi.next.shared.playcon.pointer.PlayconBottomBar
import org.easybangumi.next.shared.playcon.pointer.PointerPlaycon
import org.easybangumi.next.shared.playcon.pointer.PointerPlayconContentScope
import org.easybangumi.next.shared.playcon.pointer.PositionText
import org.easybangumi.next.shared.playcon.pointer.TimeSeekBar


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
    playerVm: DesktopPlayerVM,
    float: (@Composable BoxScope.()->Unit)? = null,
){
    val nav = LocalNavController.current
    Box(
        modifier = modifier
    ) {
        playerVm.vlcPlayerFrameState.FrameCanvas(Modifier.matchParentSize())
        PointerPlaycon(
            modifier = Modifier.matchParentSize(),
            vm = playerVm.playconVM,
        ) {
            // 控制器层
            ControllerContent(modifier = Modifier.fillMaxSize(), onFullScreenChange = {
//                playerVm.screenModeViewModel.fireUserFullScreenChange(it)
                if (it) {
                    playerVm.enterFullscreen()
                } else {
                    playerVm.exitFullscreen()
                }
            }, onBack = {
                nav.popBackStack()
            })
            if (playerVm.playconVM.isLoading) {
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

            }

        }
        float?.invoke(this)
    }
}

@Composable
fun PointerPlayconContentScope.ControllerContent(
    modifier: Modifier,
    onFullScreenChange: (Boolean) -> Unit,
    onBack: () -> Unit,
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
                    TimeSeekBar(Modifier.weight(1f))
                    DuringText()
                    FullScreenBtn(onFullScreenChange = {
                        onFullScreenChange(it)
                    })
                }
                this@ControllerContent.BackBtn {
                    onBack()
                }


            } else if (isLock) {

            } else {

            }


        }

    }
}