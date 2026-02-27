package org.easybangumi.next.shared.compose.media

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.easybangumi.next.shared.playcon.BasePlayconViewModel
import org.easybangumi.next.shared.playcon.android.AndroidGestureController
import org.easybangumi.next.shared.playcon.android.AndroidPlaycon
import org.easybangumi.next.shared.playcon.android.AndroidPlayconContentScope
import org.easybangumi.next.shared.playcon.android.AndroidPlayerVM
import org.easybangumi.next.shared.playcon.android.BackBtn
import org.easybangumi.next.shared.playcon.android.BatteryIndicator
import org.easybangumi.next.shared.playcon.android.BrightVolumeOverlay
import org.easybangumi.next.shared.playcon.android.DuringText
import org.easybangumi.next.shared.playcon.android.EpisodeTextBtn
import org.easybangumi.next.shared.playcon.android.FastForwardRewindOverlay
import org.easybangumi.next.shared.playcon.android.FullScreenBtn
import org.easybangumi.next.shared.playcon.android.LockBtn
import org.easybangumi.next.shared.playcon.android.LongPressSpeedOverlay
import org.easybangumi.next.shared.playcon.android.NextEpisodeBtn
import org.easybangumi.next.shared.playcon.android.PlayPauseBtn
import org.easybangumi.next.shared.playcon.android.PlayconBottomBar
import org.easybangumi.next.shared.playcon.android.PlayconTopBar
import org.easybangumi.next.shared.playcon.android.PositionSlideOverlay
import org.easybangumi.next.shared.playcon.android.PositionText
import org.easybangumi.next.shared.playcon.android.SpeedTextBtn
import org.easybangumi.next.shared.playcon.android.TimeSeekBar
import org.easybangumi.next.shared.playcon.android.TitleText

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
    playerVm: AndroidPlayerVM,
    title: String = "",
    onBack: (() -> Unit)? = null,
    onNextEpisode: (() -> Unit)? = null,
    onShowSpeedPanel: (() -> Unit)? = null,
    onShowEpisodePanel: (() -> Unit)? = null,
    float: (@Composable BoxScope.() -> Unit)? = null,
) {
    playerVm.playconVM.title = title

    Box(modifier = modifier) {
        playerVm.exoPlayerFrameState.FrameCanvas(Modifier.matchParentSize())
        AndroidPlaycon(
            modifier = Modifier.matchParentSize(),
            vm = playerVm.playconVM,
        ) {
            AndroidGestureController(
                vm,
                modifier = Modifier.fillMaxSize(),
                supportFast = true,
            ) { _ -> }

            ControllerContent(
                modifier = Modifier.fillMaxSize(),
                onFullScreenChange = { playerVm.screenModeViewModel.fireUserFullScreenChange(it) },
                onBack = onBack,
                onNextEpisode = onNextEpisode,
                onShowSpeedPanel = onShowSpeedPanel,
                onShowEpisodePanel = onShowEpisodePanel,
            )

            // gesture overlays
            BrightVolumeOverlay()
            PositionSlideOverlay()
            LongPressSpeedOverlay()
            FastForwardRewindOverlay()

            LockBtn()

            if (vm.isLoadingShow) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
        float?.invoke(this)
    }
}

@Composable
fun AndroidPlayconContentScope.ControllerContent(
    modifier: Modifier,
    onFullScreenChange: (isFullScreen: Boolean) -> Unit = {},
    onBack: (() -> Unit)? = null,
    onNextEpisode: (() -> Unit)? = null,
    onShowSpeedPanel: (() -> Unit)? = null,
    onShowEpisodePanel: (() -> Unit)? = null,
) {
    val isFullScreen = vm.screenMode == BasePlayconViewModel.ScreenMode.FULLSCREEN
    val isLock = vm.isLocked
    val isShowController = vm.isShowController

    Box(modifier) {
        // top bar: normal mode
        AnimatedVisibility(
            visible = isShowController && !isLock && !isFullScreen,
            modifier = Modifier.align(Alignment.TopCenter),
            enter = fadeIn(animationSpec = tween(150)),
            exit = fadeOut(animationSpec = tween(150)),
        ) {
            this@ControllerContent.PlayconTopBar(
                modifier = Modifier.fillMaxWidth()
            ) {
                if (onBack != null) {
                    BackBtn(onClick = onBack)
                }
                Spacer(modifier = Modifier.weight(1f))
                if (onShowSpeedPanel != null) {
                    SpeedTextBtn(onClick = onShowSpeedPanel)
                }
            }
        }

        // top bar: fullscreen mode
        AnimatedVisibility(
            visible = isShowController && !isLock && isFullScreen,
            modifier = Modifier.align(Alignment.TopCenter),
            enter = fadeIn(animationSpec = tween(150)),
            exit = fadeOut(animationSpec = tween(150)),
        ) {
            this@ControllerContent.PlayconTopBar(
                modifier = Modifier.fillMaxWidth()
            ) {
                BackBtn(onClick = { onFullScreenChange(false) })
                TitleText(vm.title)
                BatteryIndicator()
            }
        }

        // bottom bar
        AnimatedVisibility(
            visible = isShowController && !isLock,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = fadeIn(animationSpec = tween(150)),
            exit = fadeOut(animationSpec = tween(150)),
        ) {
            this@ControllerContent.PlayconBottomBar(
                modifier = Modifier.fillMaxWidth()
            ) {
                PlayPauseBtn()
                if (isFullScreen && onNextEpisode != null) {
                    NextEpisodeBtn(onClick = onNextEpisode)
                }
                PositionText()
                TimeSeekBar()
                DuringText()
                if (isFullScreen) {
                    if (onShowEpisodePanel != null) {
                        EpisodeTextBtn(onClick = onShowEpisodePanel)
                    }
                    if (onShowSpeedPanel != null) {
                        SpeedTextBtn(onClick = onShowSpeedPanel)
                    }
                }
                FullScreenBtn(onFullScreenChange = { onFullScreenChange(it) })
            }
        }
    }
}
