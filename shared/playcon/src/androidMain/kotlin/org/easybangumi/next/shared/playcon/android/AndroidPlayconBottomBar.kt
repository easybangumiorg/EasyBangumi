package org.easybangumi.next.shared.playcon.android

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.easybangumi.next.shared.foundation.seekbar.Seekbar
import org.easybangumi.next.shared.foundation.seekbar.SeekbarState
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
 *
 *  虽然跟 PointerPlayconBottomBar 差不多，但还是分开写把
 */
@Composable
fun AndroidPlayconScope.AndroidPlayconBottomBar(
    modifier: Modifier
) {
    val seekbarState = remember(
        vm.duration
    ) {
        val position = vm.position.coerceAtLeast(0)
        val duration = vm.duration.coerceAtLeast(position)
        SeekbarState(position, duration).apply {
            onValueChangeFinished = {
                logger.info("PointerPlayconBottomBar seekbar onValueChangeFinished: $value")
                // 进度条 -> 播放器
                vm.seekTo(value)
            }
        }
    }

    LaunchedEffect(vm) {
        snapshotFlow {
            seekbarState.isDragging to vm.position
        }.collect {
            if (!it.first) {
                if (it.second <= seekbarState.maxValue && it.second >= 0) {
                    // 播放器 -> 进度条
                    seekbarState.value = it.second
                }
            }
        }
    }

    Column (
        modifier.fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    listOf(Color.Transparent, Color.Black)
                )
            ).padding(
                16.dp, 4.dp
            ),
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
    ) {

        Seekbar(
            seekbarState,
            modifier = Modifier
        )

        Row (
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val playWhenReady = vm.playWhenReady
            val icon = if (playWhenReady) {
                Icons.Filled.Pause
            } else {
                Icons.Filled.PlayArrow
            }
            IconButton(
                modifier = Modifier,
                onClick = {
                    vm.setPlayWhenReady(!playWhenReady)
                }) {
                Icon(
                    imageVector = icon,
                    tint = Color.White,
                    contentDescription = if (playWhenReady) "Pause" else "Play",
                )
            }

            val duringText = remember(
                vm.duration,
            ) {
                TimeUtils.toString(vm.duration.coerceAtLeast(0))
            }

            val positionText = remember(
                vm.position
            ) {
                TimeUtils.toString(vm.position.coerceAtLeast(0))
            }

            Row (
                modifier = Modifier.width(132.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ){
                Text(
                    modifier = Modifier.width(64.dp),
                    text = positionText,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Text(
                    modifier = Modifier,
                    text = "/",
                    color = Color.White,
                    textAlign = TextAlign.Center
                )


                Text(
                    modifier = Modifier.width(64.dp),
                    text = duringText,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}