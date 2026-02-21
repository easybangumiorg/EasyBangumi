package org.easybangumi.next.shared.compose.media

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.easybangumi.next.shared.LocalNavController
import org.easybangumi.next.shared.playcon.BasePlayconViewModel
import org.easybangumi.next.shared.playcon.desktop.DesktopPlayerVM
import org.easybangumi.next.shared.playcon.pointer.BackBtn
import org.easybangumi.next.shared.playcon.pointer.DuringText
import org.easybangumi.next.shared.playcon.pointer.PlayPauseBtn
import org.easybangumi.next.shared.playcon.pointer.PlayconBottomBarDoubleLine
import org.easybangumi.next.shared.playcon.pointer.PointerPlaycon
import org.easybangumi.next.shared.playcon.pointer.PointerPlayconContentScope
import org.easybangumi.next.shared.playcon.pointer.PointerPlayconRowBarScope
import org.easybangumi.next.shared.playcon.pointer.PointerPlayconRowBarScopeImpl
import org.easybangumi.next.shared.playcon.pointer.PositionText
import org.easybangumi.next.shared.playcon.pointer.TimeSeekBar
import java.util.Locale


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
fun MediaPlayer(
    modifier: Modifier,
    playerVm: DesktopPlayerVM,
    float: (@Composable BoxScope.()->Unit)? = null,
    secondLineControllerOther: @Composable PointerPlayconRowBarScope.() -> Unit = { },
    topLineController: @Composable PointerPlayconRowBarScope.() -> Unit,
){
    val nav = LocalNavController.current
    Box(
        modifier = modifier
    ) {
//        playerVm.vlcjPlayerNativeFrameState.FrameCanvas(Modifier.matchParentSize())
        playerVm.vlcPlayerBitmapFrameState.FrameCanvas(Modifier.matchParentSize())
        PointerPlaycon(
            modifier = Modifier.matchParentSize(),
            vm = playerVm.playconVM,
        ) {
            // 控制器层
            ControllerContent(
                modifier = Modifier.fillMaxSize(),
//                onFullScreenChange = {
////                playerVm.screenModeViewModel.fireUserFullScreenChange(it)
//                    if (it) {
//                        playerVm.enterFullscreen()
//                    } else {
//                        playerVm.exitFullscreen()
//                    }
//                },
//                onBack = {
//                    nav.popBackStack()
//                },
                secondLineControllerOther = secondLineControllerOther,
                topLineController = topLineController
            )
            if (playerVm.playconVM.isLoading) {
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

            }

        }
        float?.invoke(this)
    }
}

@Composable
fun PointerPlayconContentScope.ControllerContent(
    modifier: Modifier,
    secondLineControllerOther: @Composable PointerPlayconRowBarScope.() -> Unit = { },
    topLineController: @Composable PointerPlayconRowBarScope.() -> Unit = { },
) {
    val isFullScreen = vm.screenMode == BasePlayconViewModel.ScreenMode.FULLSCREEN
    val isLock = vm.isLocked
    val isShowController = vm.isShowController
    AnimatedContent(
        isLock to isShowController,
        transitionSpec = {
            (fadeIn(animationSpec = tween(90)))
                .togetherWith(fadeOut(animationSpec = tween(90)))
        },
    ) {
        val (isLock, isShowController) = it

        Box(
            modifier
        ) {

            if (!isLock && isShowController) {
                this@ControllerContent.PlayconBottomBarDoubleLine(
                    modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
                    firstLine = {
                        TimeSeekBar(Modifier.fillMaxWidth().padding(8.dp, 8.dp))
                    },
                    secondLine = {
                        PlayPauseBtn()
                        PositionText()
                        Text("/",
                            color = Color.White,
                            textAlign = TextAlign.Center)
                        DuringText()
                        Spacer(Modifier.weight(1f))
                        secondLineControllerOther()

                        // TODO desktop 全屏
//                        FullScreenBtn(onFullScreenChange = {
//                            onFullScreenChange(it)
//                        })
                    },
                )
                Row(
                    modifier = Modifier.background(
                        brush = Brush.verticalGradient(
                            listOf(Color.Black, Color.Transparent)
                        )
                    ).padding(
                        16.dp, 4.dp
                    ).height(80.dp)
                ) {
                    val scope = remember(this, this@ControllerContent.vm) {
                        PointerPlayconRowBarScopeImpl(
                            this@ControllerContent,
                            this
                        )
                    }
                    scope.topLineController()
                }


//                this@ControllerContent.BackBtn {
//                    onBack()
//                }


            } else if (isLock) {

            } else {

            }


        }

    }
}

@Composable
fun PointerPlayconRowBarScope.SpeedTextBtn(
    diySpeed: Float,
    speedSet: List<Float>,
    currentSpeed: Float,
    onSpeedChange: (Float) -> Unit,
    onEditDiySpeed: () -> Unit,
    onMenuShowDismiss: (Boolean) -> Unit = { },
) {
    var expanded by remember { mutableStateOf(false) }
    val currentSpeedText = remember(currentSpeed) {
        "X${String.format(Locale.getDefault(), "%.1f", currentSpeed)}"
    }
    LaunchedEffect(expanded) {
        onMenuShowDismiss(expanded)
    }
    Box {
        Text(
            text = currentSpeedText,
            color = Color.White,
            modifier = Modifier.clickable { expanded = true }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                leadingIcon = {
                    if (currentSpeed == diySpeed) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                },
                text = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("X${remember(diySpeed) {
                            String.format(Locale.getDefault(), "%.1f", diySpeed)
                        }}")
                        Spacer(Modifier.weight(1f))
                        IconButton(
                            onClick = {
                                onEditDiySpeed()
                                expanded = false
                            }
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                        }
                    }
                },
                onClick = {
                    onSpeedChange(diySpeed)
                    expanded = false
                }
            )
            speedSet.forEach { speed ->
                val isSelected = currentSpeed == speed
                DropdownMenuItem(
                    leadingIcon = {
                        if (isSelected) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    },
                    text = {
                        Text("X${String.format(Locale.getDefault(), "%.1f", speed)}")
                    },
                    onClick = {
                        onSpeedChange(speed)
                        expanded = false
                    }
                )
            }
        }
    }
}