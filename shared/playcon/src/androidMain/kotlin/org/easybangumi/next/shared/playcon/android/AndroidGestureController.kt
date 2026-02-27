package org.easybangumi.next.shared.playcon.android

import android.app.Activity
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import org.easybangumi.next.shared.playcon.BasePlayconViewModel


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
class AndroidGestureControllerScope(
    boxScope: BoxScope,
    val vm: AndroidPlayconVM,
) : BoxScope by boxScope

@Composable
fun AndroidGestureController(
    vm: AndroidPlayconVM,
    modifier: Modifier = Modifier,
    slideFullTime: Long = 300000,
    supportFast: Boolean = false,
    fastWeight: Float = 0.2f,
    content: @Composable AndroidGestureControllerScope.(AndroidPlayconVM) -> Unit,
) {
    val ctx = LocalContext.current as Activity
    var viewSize by remember { mutableStateOf(IntSize.Zero) }


    val enableGuest by remember {
        derivedStateOf {
            vm.screenMode == BasePlayconViewModel.ScreenMode.FULLSCREEN && !vm.isLocked
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier)
            .onSizeChanged { viewSize = it }
            .pointerInput("单击双击", true) {
                // 双击
                detectTapGestures(
                    onTap = {
                        "onTap".loge("GestureController")
                        vm.onSingleClick()
                    },
                    onDoubleTap = {
                        "onDoubleTap".loge("GestureController")
                        if (!supportFast) {
                            vm.onPlayPause(!vm.playWhenReady)
                        } else if (enableGuest && it.x < viewSize.width * fastWeight) {
                            vm.fastRewind()
                        } else if (enableGuest && it.x > viewSize.width * (1 - fastWeight)) {
                            vm.fastForward()
                        } else {
                            vm.onPlayPause(!vm.playWhenReady)
                        }

                    }
                )
            }
            .pointerInput("长按倍速", enableGuest) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { vm.onLongPress() },
                    onDragCancel = { vm.onLongPressRelease() },
                    onDragEnd = { vm.onLongPressRelease() },
                    onDrag = { _, _ -> }
                )
            }
            .pointerInput("横向滑动", enableGuest) {
                var horizontalOffset = 0F
                var oldPosition = 0L
                // 横向滑动
                detectHorizontalDragGestures(
                    onDragStart = {
                        "onDragStart".loge("GestureController")
                        oldPosition = vm.position
                        horizontalOffset = 0F
                    },
                    onDragCancel = { vm.onActionUP() },
                    onDragEnd = { vm.onActionUP() },
                    onHorizontalDrag = { _: PointerInputChange, dragAmount: Float ->
                        horizontalOffset += dragAmount
                        val percent = horizontalOffset / viewSize.width
                        vm.onGesturePositionChange((oldPosition + (slideFullTime * percent)).toLong())
                    },
                )
            }
            .brightVolume(enableGuest, vm.showBrightVolumeUi, vm.brightVolumeType) { type -> // 音量、亮度

                vm.brightVolumePercent.value = (when (type) {
                    DragType.BRIGHTNESS -> ctx.windowBrightness
                    DragType.VOLUME -> with(ctx) { systemVolume }
                } * 100).toInt()
            }
    ) {
        val scope = remember(this, vm) {
            AndroidGestureControllerScope(
                this,
                vm,
            )
        }
        scope.content(vm)
    }
}