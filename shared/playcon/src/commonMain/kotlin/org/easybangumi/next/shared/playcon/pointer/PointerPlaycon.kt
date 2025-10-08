package org.easybangumi.next.shared.playcon.pointer

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import org.easybangumi.next.libplayer.api.C

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
interface PointerPlayconScope {
    val vm: PointerPlayconVM
    val interactionSource: MutableInteractionSource
}

class PointerPlayconScopeImpl(
    override val vm: PointerPlayconVM,
    override val interactionSource: MutableInteractionSource
): PointerPlayconScope

interface PointerPlayconContentScope: PointerPlayconScope, BoxScope

class PointerPlayconContentScopeImpl(
    playconScope: PointerPlayconScope,
    boxScope: BoxScope,
): PointerPlayconContentScope, PointerPlayconScope by playconScope, BoxScope by boxScope


@Composable
fun PointerPlaycon(
    modifier: Modifier,
    vm: PointerPlayconVM,
    content: @Composable PointerPlayconContentScope.() -> Unit = { }
) {

    val scope = remember(vm) {
        PointerPlayconScopeImpl(
            vm,
            MutableInteractionSource()
        )
    }
    DisposableEffect(Unit) {
        vm.needLoop()
        onDispose {
            vm.noNeedLoop()
        }
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
        val scope = remember(scope, this) {
            PointerPlayconContentScopeImpl(
                scope,
                this
            )
        }
        scope.content()
    }


}


@Composable
fun PointerPlayconContentScope.BackBtn(
    onBack: ()->Unit,
) {
    IconButton(onClick = onBack, modifier = Modifier.align(Alignment.TopStart)) {
        Icon(Icons.Filled.ArrowBack, "back", tint = Color.White)
    }
}


@Composable
fun BoxScope.Buffering(
    modifier: Modifier,
    vm: PointerPlayconVM,
) {

    if (vm.playerState == C.State.BUFFERING) {
        CircularProgressIndicator(
            modifier = modifier
        )
    }
}



