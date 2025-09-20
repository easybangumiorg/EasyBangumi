package org.easybangumi.next.shared.playcon.android

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.easybangumi.next.shared.foundation.seekbar.AndroidSeekBar
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

interface AndroidPlayconRowBarScope: AndroidPlayconScope, RowScope

class AndroidPlayconRowBarScopeImpl(
    playconScope: AndroidPlayconScope,
    rowScope: RowScope,
): AndroidPlayconRowBarScope, AndroidPlayconScope by playconScope, RowScope by rowScope

@Composable
fun AndroidPlayconContentScope.AndroidPlayconBottomBar(
    modifier: Modifier,
    content: @Composable AndroidPlayconRowBarScope.() -> Unit = {},
) {
    Row (
        modifier.fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    listOf(Color.Transparent, Color.Black)
                )
            ).padding(
                16.dp, 4.dp
            ).height(40.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally),
    ) {
        val scope = remember(this, this@AndroidPlayconBottomBar.vm) {
            AndroidPlayconRowBarScopeImpl(
                this@AndroidPlayconBottomBar,
                this
            )
        }
        scope.content()

    }
}

@Composable
fun AndroidPlayconContentScope.AndroidPlayconTopBar(
    modifier: Modifier,
    content: @Composable AndroidPlayconRowBarScope.() -> Unit = {},
) {
    Row (
        modifier.fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    listOf(Color.Black, Color.Transparent)
                )
            ).padding(
                16.dp, 4.dp
            ).height(40.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally),
    ) {
        val scope = remember(this, this@AndroidPlayconTopBar.vm) {
            AndroidPlayconRowBarScopeImpl(
                this@AndroidPlayconTopBar,
                this
            )
        }
        scope.content()

    }
}

@Composable
fun AndroidPlayconRowBarScope.PlayPauseBtn() {
    val playWhenReady = vm.playWhenReady
    val icon = if (playWhenReady) {
        Icons.Filled.Pause
    } else {
        Icons.Filled.PlayArrow
    }
    Icon(
        icon,
        modifier = Modifier
            .clip(CircleShape)
            .clickable {
                vm.setPlayWhenReady(!playWhenReady)
            }
            .padding(4.dp),
        tint = Color.White,
        contentDescription = null
    )
}

@Composable
fun AndroidPlayconRowBarScope.PositionText() {
    val positionText = remember(
        vm.position
    ) {
        TimeUtils.toString(vm.position.coerceAtLeast(0))
    }
    Text(
        modifier = Modifier.width(64.dp),
        text = positionText,
        color = Color.White,
        textAlign = TextAlign.Center
    )
}

@Composable
fun AndroidPlayconRowBarScope.TimeSeekBar() {
    val position = remember(vm.isPositionScrolling, vm.scrollingPosition, vm.position) {
        // 正在拖动进度条时，使用进度条的值
        if (vm.isPositionScrolling) {
            vm.scrollingPosition
        } else {
            vm.position
        }
    }
    AndroidSeekBar(
        modifier = Modifier.weight(1f),
        during = vm.duration.coerceAtLeast(vm.position).toInt(),
        position = position.toInt(),
        secondary = vm.bufferedPosition.coerceAtLeast(0).toInt(),
        onValueChange = {
            vm.onSeekBarPositionChange(it.toLong())
        },
        onValueChangeFinish = {
            vm.onActionUP()
        }

    )
}

@Composable
fun AndroidPlayconRowBarScope.DuringText() {
    val duringText = remember(
        vm.duration,
    ) {
        TimeUtils.toString(vm.duration.coerceAtLeast(0))
    }
    Text(
        modifier = Modifier.width(64.dp),
        text = duringText,
        color = Color.White,
        textAlign = TextAlign.Center
    )
}

@Composable
fun AndroidPlayconRowBarScope.FullScreenBtn(
    onFullScreenChange: ((Boolean) -> Unit)? = null
) {
    val isFullScreen = vm.screenMode == AndroidPlayconViewModel.ScreenMode.FULLSCREEN
    val fullScreenIcon = if (isFullScreen) {
        Icons.Filled.FullscreenExit
    } else {
        Icons.Filled.Fullscreen
    }

    Icon(
        fullScreenIcon,
        modifier = Modifier
            .clip(CircleShape)
            .clickable {
                onFullScreenChange?.invoke(!isFullScreen)
            }
            .padding(4.dp),
        tint = Color.White,
        contentDescription = null
    )
}