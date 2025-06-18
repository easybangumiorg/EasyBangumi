package org.easybangumi.next.player.controller

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
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
// 因为不同平台 UI 有巨大差异，这里先不考虑复用了
class DesktopPlayerScope(
    val bridge: PlayerBridge,
    val vm: DesktopPlayerViewModel,
    val boxScope: BoxScope,
): BoxScope by boxScope

@Composable
fun DesktopPlayer(
    modifier: Modifier,
    bridge: PlayerBridge,
) {
    val vm = vm(::DesktopPlayerViewModel, bridge)

    LaunchedEffect(Unit) {
        vm.needLoop()
    }

    DisposableEffect(Unit) {
        onDispose {
            vm.stopLoop()
        }
    }

    Box(modifier) {
        val scope = remember(bridge, vm, this) {
            DesktopPlayerScope(bridge, vm, this)
        }
        if (vm.isShowController) {
            scope.ControllerContent()
        }

        scope.Loading()
    }
}

@Composable
fun DesktopPlayerScope.ControllerContent() {
    if (! vm.isLocked) {
        ControllerBottomBar()
    }
}



@Composable
fun DesktopPlayerScope.Loading() {
    if (vm.isLoading) {
        CircularProgressIndicator(
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun DesktopPlayerScope.rightTopToolColumn() {
    val vm = vm
    Column(
        modifier = Modifier.align(Alignment.TopEnd),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        ToolIcon(
            icon = Icons.Default.Camera,
            contentDescription = "Snapshot",
            onClick = {

            })

        ToolIcon(
            icon = Icons.Default.Videocam,
            contentDescription = "Record",
            onClick = {

            })

        val lockIcon = if (vm.isLocked) {
            Icons.Default.Lock
        } else {
            Icons.Default.LockOpen
        }
        val border = if (vm.isLocked) MaterialTheme.colorScheme.secondary else null
        ToolIcon(
            icon = lockIcon,
            contentDescription = "Lock/Unlock",
            border = border,
            onClick = {
                vm.toggleLock()
            })

    }
}

@Composable
private fun ToolIcon(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    border: Color? = null,
    onClick: () -> Unit,
){
    IconButton(
        modifier = modifier.clip(RoundedCornerShape(8.dp)).run {
            if (border != null) {
                this.border(1.dp, border)
            } else {
                this
            }
        },
        onClick = onClick,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
        )
    }
}

