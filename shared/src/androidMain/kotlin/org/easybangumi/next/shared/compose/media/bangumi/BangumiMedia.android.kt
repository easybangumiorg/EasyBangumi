package org.easybangumi.next.shared.compose.media.bangumi

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.easybangumi.next.shared.LocalNavController
import org.easybangumi.next.shared.compose.media.MediaParam
import org.easybangumi.next.shared.compose.media.MediaPlayer
import org.easybangumi.next.shared.foundation.elements.ErrorElements
import org.easybangumi.next.shared.foundation.elements.LoadingElements
import org.easybangumi.next.shared.foundation.stringRes
import org.easybangumi.next.shared.foundation.view_model.vm
import org.easybangumi.next.shared.playcon.android.AndroidPlayerVM
import org.easybangumi.next.shared.playcon.android.MediaPlayerSync
import org.easybangumi.next.shared.resources.Res

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

val speedConfig = linkedMapOf(
    "3.0X" to 3.0f,
    "2.75X" to 2.75f,
    "2.5X" to 2.5f,
    "2.0X" to 2f,
    "1.5X" to 1.5f,
    "1.25X" to 1.25f,
    "1.0X" to 1f,
    "0.75X" to 0.75f,
    "0.5X" to 0.5f,
)

@Composable
actual fun BangumiMedia(mediaParam: MediaParam) {
    val vm = vm(::AndroidBangumiMediaVM, mediaParam)
    val pageParam = remember(vm) {
        BangumiMediaPageParam(vm)
    }

    val state = vm.commonVM.state.collectAsState()
    val sta = state.value
    val nav = LocalNavController.current

    var showSpeedPanel by remember { mutableStateOf(false) }
    var showEpisodePanel by remember { mutableStateOf(false) }

    BackHandler {
        when {
            showSpeedPanel || showEpisodePanel -> {
                showSpeedPanel = false
                showEpisodePanel = false
            }
            sta.isFullscreen -> {
                vm.playerVM.screenModeViewModel.fireUserFullScreenChange(false)
            }
            else -> {
                nav.popBackStack()
            }
        }
    }

    LaunchedEffect(Unit) {
        vm.onLaunch()
    }
    DisposableEffect(Unit) {
        onDispose {
            vm.onDispose()
        }
    }

    BangumiPopup(vm.commonVM)

    val playIndex = vm.commonVM.playIndexState.collectAsState().value
    val currentEpisodeLabel = playIndex.currentEpisodeOrNull?.label ?: ""
    val title = currentEpisodeLabel

    Column {
        if (!sta.isFullscreen) {
            Box(
                modifier = Modifier
                    .background(Color.Black)
                    .fillMaxWidth()
                    .height(
                        with(LocalDensity.current) {
                            WindowInsets.statusBars.getTop(LocalDensity.current).toDp()
                        }
                    )
            )
        }
        val playerModifier = remember(sta.isFullscreen) {
            if (sta.isFullscreen) {
                Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            } else {
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(AndroidPlayerVM.MEDIA_COMPONENT_ASPECT)
                    .background(Color.Black)
            }
        }
        MediaPlayerSync(vm.playerVM)
        MediaPlayer(
            modifier = playerModifier,
            playerVm = vm.playerVM,
            title = title,
            onBack = {
                if (sta.isFullscreen) {
                    vm.playerVM.screenModeViewModel.fireUserFullScreenChange(false)
                } else {
                    nav.popBackStack()
                }
            },
            onNextEpisode = {
                vm.tryNextEpisode()
            },
            onShowSpeedPanel = {
                showSpeedPanel = true
            },
            onShowEpisodePanel = {
                showEpisodePanel = true
            },
        ) {
            // float layer: loading / error / ended overlays + panels
            val playInfo = playIndex.playInfo
            val isEnded = vm.playerVM.playconVM.isEnded
            val playerError = vm.playerVM.exoBridge.errorStateFlow.collectAsState().value

            // loading overlay
            if (playInfo.isLoading()) {
                Box(Modifier.matchParentSize().background(Color.Black)) {
                    LoadingElements(
                        modifier = Modifier.matchParentSize(),
                        isRow = false,
                        loadingMsg = stringRes(Res.strings.parsing),
                        msgColor = Color.White,
                    )
                    IconButton(onClick = {
                        if (sta.isFullscreen) {
                            vm.playerVM.screenModeViewModel.fireUserFullScreenChange(false)
                        } else {
                            nav.popBackStack()
                        }
                    }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringRes(Res.strings.back),
                            tint = Color.White,
                        )
                    }
                }
            }

            // error overlay (play info parsing error)
            if (playInfo.isError()) {
                Box(Modifier.matchParentSize().background(Color.Black)) {
                    ErrorElements(
                        modifier = Modifier.matchParentSize(),
                        isRow = false,
                        errorMsg = (playInfo as? org.easybangumi.next.lib.utils.DataState.Error)?.errorMsg
                            ?: stringRes(Res.strings.play_error),
                        msgColor = Color.White,
                        onClick = {
                            vm.commonVM.playLineIndexVM.tryRefreshPlayInfo()
                        },
                    ) {
                        Text(
                            text = stringRes(Res.strings.click_to_retry),
                            color = Color.White
                        )
                    }
                    IconButton(onClick = {
                        if (sta.isFullscreen) {
                            vm.playerVM.screenModeViewModel.fireUserFullScreenChange(false)
                        } else {
                            nav.popBackStack()
                        }
                    }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringRes(Res.strings.back),
                            tint = Color.White,
                        )
                    }
                }
            }

            // player error overlay
            if (playerError != null && !playInfo.isLoading() && !playInfo.isError()) {
                Box(Modifier.matchParentSize().background(Color.Black)) {
                    ErrorElements(
                        modifier = Modifier.matchParentSize(),
                        isRow = false,
                        errorMsg = playerError.message ?: stringRes(Res.strings.play_error),
                        msgColor = Color.White,
                        onClick = {
                            vm.commonVM.playLineIndexVM.tryRefreshPlayInfo()
                        },
                    ) {
                        Text(
                            text = stringRes(Res.strings.click_to_retry),
                            color = Color.White
                        )
                    }
                    IconButton(onClick = {
                        if (sta.isFullscreen) {
                            vm.playerVM.screenModeViewModel.fireUserFullScreenChange(false)
                        } else {
                            nav.popBackStack()
                        }
                    }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringRes(Res.strings.back),
                            tint = Color.White,
                        )
                    }
                }
            }

            // ended overlay
            if (isEnded && !playInfo.isLoading() && !playInfo.isError() && playerError == null) {
                Box(Modifier.matchParentSize()) {
                    IconButton(
                        modifier = Modifier.align(Alignment.Center),
                        onClick = {
                            vm.commonVM.playLineIndexVM.tryRefreshPlayInfo()
                        }
                    ) {
                        Icon(
                            Icons.Filled.Replay,
                            contentDescription = stringRes(Res.strings.replay),
                            tint = Color.White,
                        )
                    }
                    if (sta.isFullscreen) {
                        IconButton(
                            modifier = Modifier.align(Alignment.TopStart),
                            onClick = {
                                vm.playerVM.screenModeViewModel.fireUserFullScreenChange(false)
                            }
                        ) {
                            Icon(
                                Icons.Filled.ArrowBack,
                                contentDescription = stringRes(Res.strings.back),
                                tint = Color.White,
                            )
                        }
                    }
                }
            }

            // speed selection panel
            SpeedSelectionPanel(
                visible = showSpeedPanel,
                currentSpeed = vm.playerVM.playconVM.curSpeed,
                onSpeedSelected = { speed ->
                    vm.playerVM.playconVM.setSpeed(speed)
                },
                onDismiss = { showSpeedPanel = false }
            )

            // episode selection panel (fullscreen only)
            EpisodeSelectionPanel(
                visible = showEpisodePanel && sta.isFullscreen,
                vm = vm,
                onDismiss = { showEpisodePanel = false }
            )
        }
        if (!sta.isFullscreen) {
            BangumiMediaPage(
                pageParam,
                Modifier.fillMaxWidth().weight(1f)
            )
        }
    }
}

@Composable
private fun SpeedSelectionPanel(
    visible: Boolean,
    currentSpeed: Float,
    onSpeedSelected: (Float) -> Unit,
    onDismiss: () -> Unit,
) {
    AnimatedVisibility(
        visible,
        enter = slideInHorizontally(tween()) { it },
        exit = slideOutHorizontally(tween()) { it },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    onClick = onDismiss,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Column(
                modifier = Modifier
                    .defaultMinSize(180.dp, Dp.Unspecified)
                    .fillMaxWidth(0.25f)
                    .fillMaxHeight()
                    .background(Color.Black.copy(0.6f))
                    .padding(4.dp, 0.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                speedConfig.forEach { (name, speed) ->
                    val checked = currentSpeed == speed
                    val textColor = if (checked) MaterialTheme.colorScheme.primary else Color.White
                    Text(
                        text = name,
                        color = textColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .clickable {
                                onSpeedSelected(speed)
                                onDismiss()
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun EpisodeSelectionPanel(
    visible: Boolean,
    vm: AndroidBangumiMediaVM,
    onDismiss: () -> Unit,
) {
    AnimatedVisibility(
        visible,
        enter = slideInHorizontally(tween()) { it },
        exit = slideOutHorizontally(tween()) { it },
    ) {
        val playIndex = vm.commonVM.playIndexState.collectAsState().value
        val playLine = playIndex.playLineOrNull
        val episodes = playLine?.episodeList ?: emptyList()
        val currentEpisodeIndex = playIndex.currentEpisode

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    onClick = onDismiss,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ),
            contentAlignment = Alignment.CenterEnd,
        ) {
            val listState = rememberLazyListState(
                initialFirstVisibleItemIndex = currentEpisodeIndex.coerceAtLeast(0)
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .defaultMinSize(180.dp, Dp.Unspecified)
                    .fillMaxWidth(0.25f)
                    .background(Color.Black.copy(0.8f)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                state = listState,
            ) {
                items(episodes.size) { index ->
                    val ep = episodes[index]
                    val checked = index == currentEpisodeIndex
                    val textColor = if (checked) MaterialTheme.colorScheme.primary else Color.White
                    Text(
                        text = ep.label,
                        color = textColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .clickable {
                                vm.commonVM.playLineIndexVM.onEpisodeSelected(
                                    playIndex.currentPlayerLine,
                                    index
                                )
                                onDismiss()
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    )
                }
            }
        }
    }
}
