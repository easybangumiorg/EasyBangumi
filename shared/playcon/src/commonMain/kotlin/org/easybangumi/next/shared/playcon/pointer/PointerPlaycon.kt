package org.easybangumi.next.shared.playcon.pointer

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import org.easybangumi.next.libplayer.api.C
import org.easybangumi.next.libplayer.api.PlayerBridge
import org.easybangumi.next.shared.foundation.view_model.vm

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

class PointerPlayconScope(
    val vm: PointerPlayconViewModel,
    val interactionSource: MutableInteractionSource
)

@Composable
fun PointerPlaycon(
    modifier: Modifier,
    bridge: PlayerBridge,
) {

    val vm = vm(::PointerPlayconViewModel, bridge)
    val mutableInteractionSource = remember {
        MutableInteractionSource()
    }

    DisposableEffect(Unit) {
        vm.needLoop()
        onDispose {
            vm.noNeedLoop()
        }
    }


    val scope = remember(vm, mutableInteractionSource) {
        PointerPlayconScope(vm, mutableInteractionSource)
    }
    Box(modifier.pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent()
                // 监听所有类型指针移动（包含鼠标）
                if (event.type == PointerEventType.Move) {
                    vm.showController(true)
                }
            }
        }
    }) {
        scope.ControllerContent(Modifier.fillMaxSize())
        scope.Buffering(Modifier.align(Alignment.Center))
    }


}

@Composable
fun PointerPlayconScope.ControllerContent(
    modifier: Modifier
) {
    val isLock = vm.isLocked
    val isShowController = vm.isShowController

    AnimatedContent(
        isLock to isShowController,
        transitionSpec = {
        (fadeIn(animationSpec = tween(220, delayMillis = 90)))
            .togetherWith(fadeOut(animationSpec = tween(90)))
    },
    ) {
        val (isLock, isShowController) = it

        Box(
            modifier
        ) {

            if (!isLock && isShowController) {
                PointerPlayconBottomBar(
                    modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
                )
            } else if (isLock) {

            } else {

            }


        }

    }

}


@Composable
fun PointerPlayconScope.Buffering(
    modifier: Modifier
) {

    if (vm.playerState == C.State.BUFFERING) {
        CircularProgressIndicator(
            modifier = modifier
        )
    }
}



