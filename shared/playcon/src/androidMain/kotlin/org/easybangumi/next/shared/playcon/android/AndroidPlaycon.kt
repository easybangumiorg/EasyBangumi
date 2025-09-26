package org.easybangumi.next.shared.playcon.android

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.easybangumi.next.lib.logger.logger

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

interface AndroidPlayconScope {
    val vm: AndroidPlayconVM
    val interactionSource: MutableInteractionSource
}

class AndroidPlayconScopeImpl(
    override val vm: AndroidPlayconVM,
    override val interactionSource: MutableInteractionSource
): AndroidPlayconScope

interface AndroidPlayconContentScope: AndroidPlayconScope, BoxScope

class AndroidPlayconContentScopeImpl(
    playconScope: AndroidPlayconScope,
    boxScope: BoxScope,
): AndroidPlayconContentScope, AndroidPlayconScope by playconScope, BoxScope by boxScope

@Composable
fun AndroidPlaycon(
    modifier: Modifier,
    vm: AndroidPlayconVM,
    content: @Composable AndroidPlayconContentScope.() -> Unit = {},
) {

    val scope = remember(vm) {
        AndroidPlayconScopeImpl(
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
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val scope = remember(scope, this) {
            AndroidPlayconContentScopeImpl(
                scope,
                this
            )
        }
        scope.content()
    }
}
