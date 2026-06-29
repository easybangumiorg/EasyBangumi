package org.easybangumi.next.shared.playcon.android

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.easybangumi.next.shared.playcon.TimeUtils

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
fun AndroidPlayconContentScope.BrightVolumeOverlay() {
    AnimatedVisibility(
        visible = vm.showBrightVolumeUi.value,
        modifier = Modifier.align(Alignment.Center),
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        val icon = when (vm.brightVolumeType.value) {
            DragType.BRIGHTNESS -> Icons.Filled.BrightnessHigh
            DragType.VOLUME -> Icons.Filled.VolumeUp
        }
        val desc = when (vm.brightVolumeType.value) {
            DragType.BRIGHTNESS -> "brightness"
            DragType.VOLUME -> "volume"
        }
        BrightVolumeUi(icon, desc, vm.brightVolumePercent.value)
    }
}

@Composable
fun AndroidPlayconContentScope.PositionSlideOverlay() {
    val vm = this.vm
    AnimatedVisibility(
        visible = vm.isPositionScrolling,
        modifier = Modifier.align(Alignment.Center),
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.7f))
                .padding(horizontal = 24.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${TimeUtils.toString(vm.scrollingPosition)} / ${TimeUtils.toString(vm.duration.coerceAtLeast(0))}",
                color = Color.White,
            )
        }
    }
}

@Composable
fun AndroidPlayconContentScope.LongPressSpeedOverlay() {
    AnimatedVisibility(
        visible = vm.isLongPressing,
        modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp),
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Speed,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = "${AndroidPlayconVM.LONG_PRESS_SPEED}x",
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun AndroidPlayconContentScope.FastForwardRewindOverlay(
    fastWeight: Float = 0.2f,
) {
    val realWeight = fastWeight.coerceAtLeast(0.2f)
    val vm = this.vm
    AnimatedVisibility(
        visible = vm.isFastRewindWinShow || vm.isFastForwardWinShow,
        modifier = Modifier.fillMaxSize(),
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // left: rewind
            AnimatedVisibility(
                visible = vm.isFastRewindWinShow,
                modifier = Modifier
                    .weight(realWeight)
                    .fillMaxHeight(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(0.dp, 16.dp, 16.dp, 0.dp))
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            Icons.Filled.FastRewind,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "${AndroidPlayconVM.FAST_SEEK_MS / 1000}s",
                            textAlign = TextAlign.Center,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f - 2 * realWeight))

            // right: forward
            AnimatedVisibility(
                visible = vm.isFastForwardWinShow,
                modifier = Modifier
                    .weight(realWeight)
                    .fillMaxHeight(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(16.dp, 0.dp, 0.dp, 16.dp))
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = "${AndroidPlayconVM.FAST_SEEK_MS / 1000}s",
                            textAlign = TextAlign.Center,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Icon(
                            Icons.Filled.FastForward,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
