package org.easybangumi.next.shared.playcon.pointer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.shared.foundation.seekbar.Seekbar
import org.easybangumi.next.shared.foundation.seekbar.SeekbarState
import org.easybangumi.next.shared.playcon.BasePlayconViewModel
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
interface PointerPlayconRowBarScope: PointerPlayconScope, RowScope

class PointerPlayconRowBarScopeImpl(
    playconScope: PointerPlayconScope,
    rowScope: RowScope,
): PointerPlayconRowBarScope, PointerPlayconScope by playconScope, RowScope by rowScope

@Composable
fun PointerPlayconContentScope.PlayconBottomBar(
    modifier: Modifier,
    content: @Composable PointerPlayconRowBarScope.() -> Unit = {},
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
        val scope = remember(this, this@PlayconBottomBar.vm) {
            PointerPlayconRowBarScopeImpl(
                this@PlayconBottomBar,
                this
            )
        }
        scope.content()

    }
}

@Composable
fun PointerPlayconContentScope.PlayconBottomBarDoubleLine(
    modifier: Modifier,
    firstLine: @Composable PointerPlayconRowBarScope.() -> Unit = {},
    secondLine: @Composable PointerPlayconRowBarScope.() -> Unit = {},
) {
    Row (
        modifier.fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    listOf(Color.Transparent, Color.Black)
                )
            ).padding(
                16.dp, 4.dp
            ).height(80.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally),
    ) {

        Column(
            modifier
        ) {
            Row(
                modifier.fillMaxWidth().weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val scope = remember(this, this@PlayconBottomBarDoubleLine.vm) {
                    PointerPlayconRowBarScopeImpl(
                        this@PlayconBottomBarDoubleLine,
                        this
                    )
                }
                scope.firstLine()
            }
            Row (
                modifier.fillMaxWidth().weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ){
                val scope = remember(this, this@PlayconBottomBarDoubleLine.vm) {
                    PointerPlayconRowBarScopeImpl(
                        this@PlayconBottomBarDoubleLine,
                        this
                    )
                }
                scope.secondLine()
            }
        }




    }
}

@Composable
fun PointerPlayconContentScope.PlayconTopBar(
    modifier: Modifier,
    content: @Composable PointerPlayconRowBarScope.() -> Unit = {},
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
        val scope = remember(this, this@PlayconTopBar.vm) {
            PointerPlayconRowBarScopeImpl(
                this@PlayconTopBar,
                this
            )
        }
        scope.content()

    }
}

@Composable
fun PointerPlayconRowBarScope.PlayPauseBtn() {
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
fun PointerPlayconRowBarScope.PositionText() {
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
fun PointerPlayconRowBarScope.TimeSeekBar(
    modifier: Modifier,
) {
    val seekbarState = remember(
        vm.duration
    ) {
        val position = vm.position.coerceAtLeast(0)
        val duration = vm.duration.coerceAtLeast(position)
        SeekbarState(position, duration).apply {
            onValueChangeFinished = {
                // 进度条 -> 播放器
                vm.seekTo(value)
            }
        }
    }
    Box(modifier) {
        Seekbar(seekbarState, Modifier.fillMaxWidth())
    }

}

@Composable
fun PointerPlayconRowBarScope.DuringText() {
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
fun PointerPlayconRowBarScope.FullScreenBtn(
    onFullScreenChange: ((Boolean) -> Unit)? = null
) {
    val isFullScreen = vm.screenMode == BasePlayconViewModel.ScreenMode.FULLSCREEN
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

@Composable
fun PointerPlayconRowBarScope.SpeedBtn(
    diySpeed: Float,
    onDiySpeedChange: (Float) -> Unit,
    onSpeedChange: (Float) -> Unit,
) {


}