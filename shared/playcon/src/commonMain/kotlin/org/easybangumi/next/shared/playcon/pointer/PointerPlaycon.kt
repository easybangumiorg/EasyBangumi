package org.easybangumi.next.shared.playcon.pointer

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
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
    val vm: PointerPlayerViewModel,
    val interactionSource: MutableInteractionSource
)

@Composable
fun PointerPlaycon(
    modifier: Modifier,
    bridge: PlayerBridge,
) {

    val vm = vm(::PointerPlayerViewModel, bridge)
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



