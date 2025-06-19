package org.easybangumi.next.shared.playcon.pointer

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.lifecycle.compose.LocalLifecycleOwner
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
            vm.stopLoop()
        }
    }



}

@Composable
fun PointerPlayconScope.ControllerContent(
    modifier: Modifier
) {
    val isLock = vm.isLocked


}

