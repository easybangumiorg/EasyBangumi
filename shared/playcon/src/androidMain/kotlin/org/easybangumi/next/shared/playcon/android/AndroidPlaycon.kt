package org.easybangumi.next.shared.playcon.android

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.shared.playcon.pointer.PointerPlayconBottomBar
import org.easybangumi.next.shared.playcon.pointer.PointerPlayconScope

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
internal val logger = logger("AndroidPlaycon")

class AndroidPlayconScope(
    val vm: AndroidPlayconViewModel,
    val interactionSource: MutableInteractionSource
)

@Composable
fun AndroidPlaycon(
    modifier: Modifier,
    viewModel: AndroidPlayconViewModel
) {

    val scope = remember(viewModel) {
        AndroidPlayconScope(
            viewModel,
            MutableInteractionSource()
        )
    }

    DisposableEffect(Unit) {
        viewModel.needLoop()
        onDispose {
            viewModel.noNeedLoop()
        }
    }
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {

        AndroidGestureController(
            viewModel,
            modifier = Modifier.fillMaxSize()
        ) {

        }
        scope.ControllerContent(Modifier.fillMaxSize())
        if (viewModel.isLoadingShow) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
fun AndroidPlayconScope.ControllerContent(
    modifier: Modifier
) {
    val isFullScreen = vm.screenMode == AndroidPlayconViewModel.ScreenMode.FULLSCREEN
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
                AndroidPlayconBottomBar(
                    modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
                )
            } else if (isLock) {

            } else {

            }


        }

    }

}
