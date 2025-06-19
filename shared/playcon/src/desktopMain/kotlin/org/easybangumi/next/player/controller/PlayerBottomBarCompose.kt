package org.easybangumi.next.player.controller

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.easybangumi.next.shared.foundation.seekbar.Seekbar
import org.easybangumi.next.shared.foundation.seekbar.SeekbarState

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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesktopPlayerScope.ControllerBottomBar() {
    val vm = vm
    val scope = this

    Column(
        modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
    ) {
        Timeline(vm)

    }
}

@Composable
fun Timeline(vm: DesktopPlayerViewModel) {

    val seekbarState = remember(vm.duration) {
        SeekbarState(
            0L,
            maxValue = vm.duration.coerceAtLeast(0),
            ticksIndex = null,
        ).apply {
            // only from scroll
            onValueChange = {
                // 滑动中
                vm.endHideDelayJob()
            }
            onValueChangeFinished = {
                // 进度条 -> 播放器
                vm.onSeekEnd(value)
            }
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow {
            seekbarState.isDragging
        }.collect {
            if (!it) {
                // 播放器 -> 进度条
                seekbarState.value = vm.position
            }
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow {
            seekbarState.isDragging
        }.collect {
            if (it) {
                vm.showController(false)
            } else {
                vm.restartHideDelayJob()
            }
        }
    }


    val mutableInteractionSource = remember { MutableInteractionSource() }

    Seekbar(
        state = seekbarState,
        modifier = Modifier.fillMaxWidth(),
        enabled = true,
        interactionSource = mutableInteractionSource,
        thumb = { state ->
            SliderDefaults.Thumb(
                interactionSource = mutableInteractionSource
            )
        },
        track = { state ->
            Box(modifier = Modifier.fillMaxWidth())
        },
    )

}