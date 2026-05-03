package org.easybangumi.next.shared.compose.media

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalSlider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import org.easybangumi.next.shared.playcon.BasePlayconViewModel
import org.easybangumi.next.shared.playcon.desktop.DesktopPlayerVM
import org.easybangumi.next.shared.playcon.pointer.DuringText
import org.easybangumi.next.shared.playcon.pointer.FullScreenBtn
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
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = modifier
            .focusRequester(focusRequester)
            .focusable()
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) {
                    return@onPreviewKeyEvent false
                }
                when {
                    event.key == Key.Escape && playerVm.playconVM.screenMode == BasePlayconViewModel.ScreenMode.FULLSCREEN -> {
                        playerVm.exitFullscreen()
                        true
                    }

                    event.key == Key.F11 -> {
                        playerVm.toggleFullscreen()
                        true
                    }

                    event.key == Key.F && event.isMetaPressed && event.isCtrlPressed -> {
                        playerVm.toggleFullscreen()
                        true
                    }

                    event.key == Key.DirectionUp -> {
                        playerVm.stepVolume(DesktopPlayerVM.VOLUME_STEP)
                        playerVm.playconVM.showController()
                        true
                    }

                    event.key == Key.DirectionDown -> {
                        playerVm.stepVolume(-DesktopPlayerVM.VOLUME_STEP)
                        playerVm.playconVM.showController()
                        true
                    }

                    event.key == Key.M -> {
                        playerVm.toggleMute()
                        playerVm.playconVM.showController()
                        true
                    }

                    else -> false
                }
            }
            .pointerInput(playerVm) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.type != PointerEventType.Scroll) {
                            continue
                        }
                        val deltaY = event.changes.firstOrNull()?.scrollDelta?.y ?: 0f
                        if (deltaY == 0f) {
                            continue
                        }
                        val step = if (deltaY < 0f) DesktopPlayerVM.VOLUME_STEP else -DesktopPlayerVM.VOLUME_STEP
                        playerVm.stepVolume(step)
                        playerVm.playconVM.showController()
                    }
                }
            }
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
                playerVm = playerVm,
                onFullScreenChange = {
                    if (it) {
                        playerVm.enterFullscreen()
                    } else {
                        playerVm.exitFullscreen()
                    }
                },
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
    playerVm: DesktopPlayerVM,
    onFullScreenChange: ((Boolean) -> Unit)? = null,
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
                        VolumeController(playerVm)
                        Spacer(Modifier.weight(1f))
                        secondLineControllerOther()
                        if (onFullScreenChange != null) {
                            FullScreenBtn(onFullScreenChange = {
                                onFullScreenChange(it)
                            })
                        }
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
@Suppress("INVISIBLE_REFERENCE", "SYNTHETIC_ACCESSOR", "INVISIBLE_MEMBER",)
@OptIn(ExperimentalComposeUiApi::class)
fun PointerPlayconRowBarScope.VolumeController(playerVm: DesktopPlayerVM) {
    val playconVm = vm
    val volume = playerVm.volume
    val isMuted = playerVm.mute
    val isAvailable = playerVm.isVolumeControlAvailable
    val displayVolume = if (isMuted) 0 else volume
    val scope = rememberCoroutineScope()
    var isVolumePopupVisible by remember { mutableStateOf(false) }
    var hidePopupJob by remember { mutableStateOf<Job?>(null) }
    var isButtonHovered by remember { mutableStateOf(false) }
    var isPopupHovered by remember { mutableStateOf(false) }
    var isSliderDragging by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val popupGapPx = remember(density) { with(density) { 8.dp.roundToPx() } }

    val popupPositionProvider = remember(popupGapPx) {
        object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize
            ): IntOffset {
                val x = anchorBounds.left + (anchorBounds.width - popupContentSize.width) / 2
                val y = anchorBounds.top - popupContentSize.height - popupGapPx
                return IntOffset(
                    x = x.coerceIn(0, windowSize.width - popupContentSize.width),
                    y = y.coerceAtLeast(0),
                )
            }
        }
    }

    var sliderValue by remember(playerVm) { mutableFloatStateOf(displayVolume.toFloat()) }

    LaunchedEffect(displayVolume, isSliderDragging) {
        if (!isSliderDragging && sliderValue != displayVolume.toFloat()) {
            sliderValue = displayVolume.toFloat()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            hidePopupJob?.cancel()
        }
    }

    fun showVolumePopup() {
        hidePopupJob?.cancel()
        isVolumePopupVisible = true
        playconVm.restartHideDelayJob()
    }

    fun requestHideVolumePopupWithDelay() {
        if (isButtonHovered || isPopupHovered || isSliderDragging) {
            return
        }
        hidePopupJob?.cancel()
        hidePopupJob = scope.launch {
            delay(120.milliseconds)
            if (!isButtonHovered && !isPopupHovered && !isSliderDragging) {
                isVolumePopupVisible = false
            }
        }
    }

    LaunchedEffect(isSliderDragging) {
        if (isSliderDragging) {
            showVolumePopup()
        } else {
            requestHideVolumePopupWithDelay()
        }
    }

    val icon = if (isMuted || displayVolume == 0 || !isAvailable) {
        Icons.AutoMirrored.Filled.VolumeOff
    } else {
        Icons.AutoMirrored.Filled.VolumeUp
    }

    Box(
        modifier = Modifier
            .onPointerEvent(PointerEventType.Enter) {
                isButtonHovered = true
                showVolumePopup()
            }
            .onPointerEvent(PointerEventType.Exit) {
                isButtonHovered = false
                requestHideVolumePopupWithDelay()
            },
        contentAlignment = Alignment.BottomCenter
    ) {
        IconButton(
            enabled = isAvailable,
            onClick = {
                playerVm.toggleMute()
                playconVm.restartHideDelayJob()
            }
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isAvailable) Color.White else Color.Gray,
            )
        }

        if (isVolumePopupVisible) {
            Popup(
                popupPositionProvider = popupPositionProvider,
                onDismissRequest = { isVolumePopupVisible = false },
                properties = PopupProperties(
                    focusable = false,
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false,
                    clippingEnabled = false,
                )
            ) {
                Box(
                    modifier = Modifier
                        .width(64.dp)
                        .background(Color(0xCC101010), shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                        .onPointerEvent(PointerEventType.Enter) {
                            isPopupHovered = true
                            showVolumePopup()
                        }
                        .onPointerEvent(PointerEventType.Exit) {
                            isPopupHovered = false
                            requestHideVolumePopupWithDelay()
                        }
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (isAvailable) "$displayVolume%" else "N/A",
                            color = if (isAvailable) Color.White else Color.Gray,
                            textAlign = TextAlign.Center,
                        )

                        // Rotate Material3 horizontal slider to become a vertical popup slider.
                        VerticalSlider(
                            value = sliderValue,
                            onValueChange = { newValue ->
                                isSliderDragging = true
                                sliderValue = newValue
                                val targetVolume = newValue.toInt().coerceIn(0, DesktopPlayerVM.VOLUME_MAX)
                                if (playerVm.mute && targetVolume > 0) {
                                    playerVm.toggleMute()
                                }
                                playerVm.previewVolume(targetVolume)
                                showVolumePopup()
                            },
                            onValueChangeFinished = {
                                isSliderDragging = false
                                playerVm.commitPreviewVolume()
                                playconVm.restartHideDelayJob()
                                requestHideVolumePopupWithDelay()
                            },
                            enabled = isAvailable,
                            valueRange = 0f..DesktopPlayerVM.VOLUME_MAX.toFloat(),
                            modifier = Modifier
                                .height(140.dp)
                                .width(24.dp),
                        )
                    }
                }
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
