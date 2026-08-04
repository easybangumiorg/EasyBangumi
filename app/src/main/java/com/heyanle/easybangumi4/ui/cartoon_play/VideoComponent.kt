package com.heyanle.easybangumi4.ui.cartoon_play

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Battery0Bar
import androidx.compose.material.icons.filled.Battery2Bar
import androidx.compose.material.icons.filled.Battery3Bar
import androidx.compose.material.icons.filled.Battery4Bar
import androidx.compose.material.icons.filled.Battery5Bar
import androidx.compose.material.icons.filled.Battery6Bar
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.media3.common.util.UnstableApi
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.cartoon.story.local.source.LocalSource
import com.heyanle.easybangumi4.navigationDlna
import com.heyanle.easybangumi4.plugin.api.ParserException
import com.heyanle.easybangumi4.plugin.api.component.BusinessActionType
import com.heyanle.easybangumi4.plugin.api.component.PlayInfoNeedVerificationBusinessException
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.CartoonPlayViewModel
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.CartoonPlayingViewModel
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.DetailedViewModel
import com.heyanle.easybangumi4.ui.common.CombineClickIconButton
import com.heyanle.easybangumi4.ui.common.ErrorPage
import com.heyanle.easybangumi4.ui.common.LoadingPage
import com.heyanle.easybangumi4.ui.common.ToggleButton
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.utils.bufferImageCache
import com.heyanle.easybangumi4.utils.downloadImage
import com.heyanle.easybangumi4.utils.logi
import com.heyanle.easybangumi4.utils.shareImageText
import com.heyanle.easybangumi4.utils.shareText
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.okkv2.core.okkv
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import loli.ball.easyplayer2.BackBtn
import loli.ball.easyplayer2.BottomControl
import loli.ball.easyplayer2.BrightVolumeUI
import loli.ball.easyplayer2.ControlViewModel
import loli.ball.easyplayer2.GestureController
import loli.ball.easyplayer2.LockBtn
import loli.ball.easyplayer2.LongTouchUI
import loli.ball.easyplayer2.PlayPauseBtn
import loli.ball.easyplayer2.ProgressBox
import loli.ball.easyplayer2.SimpleGestureController
import loli.ball.easyplayer2.SlideUI
import loli.ball.easyplayer2.TimeText
import loli.ball.easyplayer2.TopControl
import loli.ball.easyplayer2.ViewSeekBar
import loli.ball.easyplayer2.utils.rememberBatteryReceiver
import loli.ball.easyplayer2.utils.rememberCurrentTimeText

/**
 * Created by heyanle on 2023/12/17.
 * https://github.com/heyanLE
 */


@UnstableApi
@Composable
fun VideoFloat(
    cartoonPlayingViewModel: CartoonPlayingViewModel,
    cartoonPlayViewModel: CartoonPlayViewModel,
    playingState: CartoonPlayingViewModel.PlayingState,
    playState: CartoonPlayViewModel.CartoonPlayState,
    controlVM: ControlViewModel,
    showSpeedWin: MutableState<Boolean>,
    showEpisodeWin: MutableState<Boolean>,
    showScaleTypeWin: MutableState<Boolean>,
) {
    val nav = LocalNavController.current
    val ctx = LocalContext.current as Activity
    val scaleType by cartoonPlayingViewModel.videoScaleType.collectAsState()

    LaunchedEffect(Unit) {
        launch {
            snapshotFlow {
                playingState
            }.collectLatest {
                if (it.isError) {
                    controlVM.onFullScreen(fullScreen = false, reverse = false, ctx)
                }
            }
        }

        launch {
            snapshotFlow {
                controlVM.controlState
            }.collectLatest {
                if (it == ControlViewModel.ControlState.Ended) {
                    cartoonPlayViewModel.tryNext()
                }
            }
        }

        launch {
            snapshotFlow {
                scaleType
            }.collectLatest {
                it.logi("VideoComponent")
                controlVM.render.setScaleType(it)
                controlVM.render.getViewOrNull()?.requestLayout()
            }
        }

        val defaultSpeed = cartoonPlayingViewModel.defaultSpeed.value
        val customSpeed = cartoonPlayingViewModel.customSpeed.value
        if (defaultSpeed == -1f) {
            controlVM.setSpeed(if (customSpeed > 0) customSpeed else 1f)
        } else {
            controlVM.setSpeed(if (defaultSpeed > 0) defaultSpeed else 1f)
        }
    }

    BackHandler(
        showSpeedWin.value || showEpisodeWin.value || showScaleTypeWin.value
    ) {
        showSpeedWin.value = false
        showEpisodeWin.value = false
        showScaleTypeWin.value = false
    }

    if (playingState.isLoading) {
        Box {
            LoadingPage(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable(
                        onClick = {
                        },
                        indication = null,
                        interactionSource = remember {
                            MutableInteractionSource()
                        }
                    ),
                loadingMsg = stringResource(id = R.string.parsing),
                msgColor = Color.White
            )
            IconButton(
                modifier = Modifier.align(Alignment.TopStart),
                onClick = {
                    if (controlVM.isFullScreen)
                        controlVM.onFullScreen(fullScreen = false, reverse = false, ctx)
                    else
                        nav.popBackStack()
                }) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = stringResource(id = R.string.back),
                    tint = Color.White
                )
            }
        }
    } else if (playingState.isError) {
        Box {
            val inner = (playingState.errorThrowable as? ParserException)?.exception
            if (playingState.errorThrowable is ParserException &&
                inner is PlayInfoNeedVerificationBusinessException) {
                val isCaptcha = inner.actionType == BusinessActionType.DIALOG_CAPTCHA
                ErrorPage(modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                    image = com.heyanle.easybangumi4.R.drawable.empty_bocchi,
                    errorMsgColor = Color.White,
                    errorMsg = if (isCaptcha) "需要输入验证码" else "需要人机效验",
                    other = {
                        Text(text = if (isCaptcha) "点击输入验证码" else "点击跳转效验")
                    },
                    clickEnable = true,
                    onClick = {
                        cartoonPlayingViewModel.onSearchNeedWebCheck(inner)
                    })

            } else {
                ErrorPage(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    errorMsg = playingState.errorMsg.ifBlank { playingState.errorThrowable?.message?:"解析错误" },
                    errorMsgColor = Color.White,
                    clickEnable = true,
                    other = {
                        Text(text = stringResource(id = R.string.click_to_retry), color = Color.White)
                    },
                    onClick = {
                        cartoonPlayingViewModel.tryRefresh()
                    }
                )
            }

            IconButton(
                modifier = Modifier.align(Alignment.TopStart),
                onClick = {
                    if (controlVM.isFullScreen)
                        controlVM.onFullScreen(fullScreen = false, reverse = false, ctx)
                    else
                        nav.popBackStack()
                }) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = stringResource(id = R.string.back),
                    tint = Color.White
                )
            }
        }

    } else if (playingState.isPlaying) {
        if (controlVM.controlState == ControlViewModel.ControlState.Ended) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                IconButton(
                    modifier = Modifier.align(Alignment.Center),
                    onClick = {
                        cartoonPlayingViewModel.tryRefresh()
                    }) {
                    Icon(
                        Icons.Filled.Replay,
                        contentDescription = stringResource(id = R.string.replay)
                    )
                }


                if (controlVM.isFullScreen) {
                    IconButton(
                        modifier = Modifier.align(Alignment.TopStart),
                        onClick = {
                            controlVM.onFullScreen(
                                fullScreen = false,
                                reverse = false,
                                ctx = ctx
                            )
                        }) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                }
            }
        }
    }


    // 倍速窗口
    val customSpeed by cartoonPlayingViewModel.customSpeed.collectAsState()
    FullscreenPlayerSidePanel(
        visible = showSpeedWin.value,
        onDismiss = { showSpeedWin.value = false },
        panelModifier = Modifier.semantics {
            contentDescription = "播放速度选择"
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, top = 12.dp, end = 8.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = R.string.speed),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = "当前 ${formatPlaybackSpeed(controlVM.curSpeed)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = { showSpeedWin.value = false }) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(id = R.string.close),
                )
            }
        }
        HorizontalDivider()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            val customSelected = cartoonPlayingViewModel.isCustomSpeed.value
            PlaybackSpeedOption(
                label = if (customSpeed > 0f) {
                    "${stringResource(id = R.string.custom_speed)} · ${formatPlaybackSpeed(customSpeed)}"
                } else {
                    stringResource(id = R.string.custom_speed)
                },
                selected = customSelected,
                supportingText = if (customSpeed > 0f) "使用自定义播放速度" else "点击设置播放速度",
                onClick = {
                    if (customSpeed > 0f) {
                        cartoonPlayingViewModel.enableCustomSpeed()
                        controlVM.setSpeed(customSpeed)
                    } else {
                        cartoonPlayingViewModel.setCustomSpeedDialog()
                    }
                },
                action = if (customSpeed > 0f) {
                    {
                        IconButton(onClick = cartoonPlayingViewModel::setCustomSpeedDialog) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "编辑自定义倍速",
                            )
                        }
                    }
                } else {
                    null
                },
            )

            speedConfig.forEach { (name, speed) ->
                val selected = !customSelected && controlVM.curSpeed == speed
                PlaybackSpeedOption(
                    label = name,
                    selected = selected,
                    onClick = {
                        controlVM.setSpeed(speed)
                        cartoonPlayingViewModel.disableCustomSpeed()
                    },
                )
            }
        }
    }


    val playLine = playState.playLine
    // 选集
    val currentEpisodeIndex = playLine.sortedEpisodeList.indexOf(playState.episode)
        .coerceAtLeast(0)
    val episodeListState = rememberLazyListState(currentEpisodeIndex)
    LaunchedEffect(currentEpisodeIndex, showEpisodeWin.value) {
        if (showEpisodeWin.value && playLine.sortedEpisodeList.isNotEmpty()) {
            episodeListState.animateScrollToItem(currentEpisodeIndex)
        }
    }
    FullscreenPlayerSidePanel(
        visible = showEpisodeWin.value && controlVM.isFullScreen,
        onDismiss = { showEpisodeWin.value = false },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, top = 12.dp, end = 8.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = R.string.episode),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = "正在播放 ${playState.episode.label} · 共 ${playLine.sortedEpisodeList.size} 集",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = { showEpisodeWin.value = false }) {
                Icon(Icons.Filled.Close, contentDescription = stringResource(id = R.string.close))
            }
        }
        HorizontalDivider()
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            state = episodeListState,
        ) {
            itemsIndexed(
                items = playLine.sortedEpisodeList,
                key = { index, episode -> "${episode.id}-$index" },
            ) {
                    _, episode ->
                val selected = playState.episode == episode
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            cartoonPlayViewModel.changePlay(
                                cartoonSummary = playState.cartoonSummary,
                                playLineWrapper = playLine,
                                episode = episode,
                            )
                        },
                    color = if (selected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerLow
                    },
                    contentColor = if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = episode.label,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        if (selected) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "正在播放",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        }
    }

    // 填充模式
    AnimatedVisibility(
        showScaleTypeWin.value && controlVM.isFullScreen,
        enter = slideInHorizontally(tween()) { it },
        exit = slideOutHorizontally(tween()) { it },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    onClick = { showScaleTypeWin.value = false },
                    indication = null,
                    interactionSource = remember {
                        MutableInteractionSource()
                    }
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
                    .verticalScroll(rememberScrollState())
                    .align(Alignment.CenterEnd),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                cartoonPlayingViewModel.videoScaleTypeSelection.forEach {
                    val checked = scaleType == it.first
                    ToggleButton(
                        checked = checked,
                        onClick = {
                            cartoonPlayingViewModel.setVideoScaleType(it.first)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    ) {
                        Text(stringResource(id = it.second))
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaybackSpeedOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    supportingText: String? = null,
    action: (@Composable () -> Unit)? = null,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        contentColor = if (selected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        shape = RoundedCornerShape(14.dp),
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 11.dp, end = 8.dp, bottom = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                )
                supportingText?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "当前播放速度",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            action?.invoke()
        }
    }
}

private fun formatPlaybackSpeed(speed: Float): String {
    val value = if (speed % 1f == 0f) {
        speed.toInt().toString()
    } else {
        speed.toString().trimEnd('0').trimEnd('.')
    }
    return "$value×"
}

@OptIn(UnstableApi::class)
@Composable
fun VideoControl(
    controlVM: ControlViewModel,
    cartoonPlayingVM: CartoonPlayingViewModel,
    cartoonPlayVM: CartoonPlayViewModel,
    playingState: CartoonPlayingViewModel.PlayingState,
    sourcePlayState: CartoonPlayViewModel.CartoonPlayState?,
    detailState: DetailedViewModel.DetailState,
    showSpeedWin: MutableState<Boolean>,
    showEpisodeWin: MutableState<Boolean>,
    showVideoScaleTypeWin: MutableState<Boolean>,
    danmakuControlState: PlayerDanmakuControlState? = null,
    onShowPlayerSettings: (() -> Unit)? = null,
    danmakuRenderer: DfmDanmakuRenderer? = null,
    includeDanmakuInScreenshot: Boolean = false,
) {
    val nav = LocalNavController.current
    val scope = rememberCoroutineScope()
    val screenshotController = rememberPlayerScreenshotController(
        playingViewModel = cartoonPlayingVM,
        danmakuRenderer = danmakuRenderer,
        includeDanmaku = includeDanmakuInScreenshot,
    )
    if (sourcePlayState == null) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            IconButton(onClick = {
                nav.popBackStack()
            }) {
                Icon(
                    Icons.Filled.ArrowBack,
                    tint = Color.White,
                    contentDescription = null
                )
            }
        }
    } else {
        Box(Modifier.fillMaxSize()) {

            val fastWeight by cartoonPlayingVM.fastWeight.collectAsState()
            val fastSecond by cartoonPlayingVM.fastSecond.collectAsState()
            val fastTopSecond by cartoonPlayingVM.fastTopSecond.collectAsState()
            val fastWeightTopDenominator = cartoonPlayingVM.fastWeightTopDenominator
            val fastTopWeightMolecule by cartoonPlayingVM.fastTopWeightMolecule.collectAsState()
            val playerSeekFullWidthTime by cartoonPlayingVM.playerSeekFullWidthTimeMS.collectAsState()

            if (fastWeight <= 0) {
                // 手势
                SimpleGestureController(
                    vm = controlVM,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp, 64.dp),
                    longTouchText = stringResource(id = R.string.long_press_fast_forward),
                    slideFullTime = playerSeekFullWidthTime,
                )
            } else {

                GestureController(
                    controlVM,
                    Modifier
                        .fillMaxSize()
                        .padding(6.dp, 64.dp),
                    playerSeekFullWidthTime,
                    supportFast = true,
                    horizontalDoubleTapWeight = 1f / fastWeight,
                    verticalDoubleTapWeight = fastTopWeightMolecule.toFloat() / fastWeightTopDenominator.toFloat(),
                    topFastTime = fastTopSecond * 1000L,
                ) {
                    BrightVolumeUI()
                    SlideUI()
                    LongTouchUI(stringResource(id = R.string.long_press_fast_forward))
                }


                FastUI(
                    vm = controlVM,
                    fastForwardText = "${fastSecond}s",
                    fastRewindText = "${fastSecond}s",
                    fastForwardTopText = "${fastTopSecond}s",
                    fastRewindTopText = "${fastTopSecond}s",
                    horizontalDoubleTapWeight = 1f / fastWeight,
                    verticalDoubleTapWeight = fastTopWeightMolecule / fastWeightTopDenominator.toFloat(),
                    delayTime = 500
                )


            }


            // 全屏顶部工具栏
            FullScreenVideoTopBar(
                vm = controlVM,
                modifier = Modifier
                    .align(Alignment.TopCenter)
            ) {
                if (onShowPlayerSettings == null) {
                    showVideoScaleTypeWin.value = true
                } else {
                    onShowPlayerSettings()
                }
            }


            FullScreenRightToolBar(
                vm = controlVM,
                modifier = Modifier
                    .fillMaxHeight()
                    .defaultMinSize(64.dp, Dp.Unspecified)
                    .align(Alignment.CenterEnd),
                screenshotState = screenshotController.state,
                onImage = screenshotController::capture,
                onShowRecorded = {
                    cartoonPlayingVM.showRecord()
                }
            )
            CaptureResultCard(
                state = screenshotController.state,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 64.dp),
            )
            NormalVideoTopBar(controlVM,
                showTools = playingState.isPlaying,
                showDlna = detailState.cartoonInfo?.source != LocalSource.LOCAL_SOURCE_KEY,
                onBack = {
                    nav.popBackStack()
                },
                onSpeed = {
                    showSpeedWin.value = true
                },
                onDlna = {
                    // cartoonPlayingVM.playCurrentExternal()
                    if (detailState.cartoonInfo?.source == LocalSource.LOCAL_SOURCE_KEY) {
                        stringRes(R.string.local_cant_dlna).moeSnackBar()
                        return@NormalVideoTopBar
                    }
                    sourcePlayState?.let { playState ->
                        val playLine = playState?.playLine
                        val episode = playState?.episode
                        val enterData = CartoonPlayViewModel.EnterData(
                            playLineId = playLine?.playLine?.id ?: "",
                            playLineLabel = playLine?.playLine?.label ?: "",
                            playLineIndex = detailState.cartoonInfo?.playLineWrapper?.indexOf(
                                playState?.playLine
                            ) ?: -1,
                            episodeId = episode?.id ?: "",
                            episodeIndex = playState?.playLine?.playLine?.episode?.indexOf(playState.episode)
                                ?: -1,
                            episodeLabel = episode?.label ?: "",
                            episodeOrder = episode?.order ?: -1,
                            adviceProgress = 0,
                        )
                        nav.navigationDlna(
                            detailState.cartoonInfo?.id ?: "",
                            detailState.cartoonInfo?.source ?: "",
                            enterData
                        )
                    }
                    // cartoonPlayingController.playCurrentExternal()
                },
                onShare = { withCover ->
                    if (detailState.cartoonInfo == null) return@NormalVideoTopBar
                    if (withCover) {
                        scope.launch(Dispatchers.IO) {
                            val image = downloadImage(detailState.cartoonInfo.coverUrl)
                            if (image != null) {
                                val imageFile = bufferImageCache(image)
                                val imageUri = FileProvider.getUriForFile(
                                    APP, "${APP.packageName}.provider", imageFile
                                )
                                shareImageText(imageUri, detailState.cartoonInfo.url)
                            }
                        }
                    } else {
                        shareText(detailState.cartoonInfo.url)
                    }
                }
            )

            EasyVideoBottomControl(
                vm = controlVM,
                modifier = Modifier.align(Alignment.BottomCenter),
                paddingValues = if (controlVM.isFullScreen) PaddingValues(
                    16.dp,
                    0.dp,
                    16.dp,
                    8.dp
                ) else PaddingValues(8.dp, 0.dp),
                onShowEpisodeWin = {
                    showEpisodeWin.value = true
                },
                onSHowSpeedWin = {
                    showSpeedWin.value = true
                },
                onNext = {
                    cartoonPlayVM.tryNext()
                },
                danmakuControlState = danmakuControlState,
            )

            // 锁定按钮
            LockBtn(vm = controlVM)

            // 加载按钮
            ProgressBox(vm = controlVM)
        }
    }


}

@kotlin.OptIn(ExperimentalFoundationApi::class)
@Composable
fun FullScreenRightToolBar(
    vm: ControlViewModel,
    modifier: Modifier = Modifier,
    isShowOnNormalScreen: Boolean = false,
    screenshotState: PlayerScreenshotState,
    onShowRecorded: () -> Unit,
    onImage: () -> Unit,
) {

    (vm.isShowOverlay() && (vm.isFullScreen || isShowOnNormalScreen)).logi("VideoComponent")
    Box(
        modifier = modifier
    ) {
        AnimatedVisibility(
            visible = vm.isShowOverlay() && (vm.isFullScreen || isShowOnNormalScreen),
            exit = fadeOut(),
            enter = fadeIn(),
        ) {
            Column(
                modifier = Modifier
            ) {
                Spacer(Modifier.height(64.dp))
                val isCapturing = screenshotState is PlayerScreenshotState.Capturing
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .combinedClickable(
                            enabled = !isCapturing,
                            onClickLabel = "截图",
                            onClick = onImage,
                            onLongClickLabel = "录制片段",
                            onLongClick = onShowRecorded,
                        )
                        .semantics {
                            contentDescription = if (isCapturing) {
                                "正在截图"
                            } else {
                                "截图，长按录制"
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    if (isCapturing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Box {
                            Icon(
                                Icons.Filled.CameraAlt,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp),
                                contentDescription = null,
                            )
                            Icon(
                                Icons.Filled.FiberManualRecord,
                                tint = Color(0xFFFF796F),
                                modifier = Modifier
                                    .size(8.dp)
                                    .align(Alignment.BottomEnd),
                                contentDescription = null,
                            )
                        }
                    }
                }
            }
        }
    }

}

@Composable
private fun CaptureResultCard(
    state: PlayerScreenshotState,
    modifier: Modifier = Modifier,
) {
    val visible = state is PlayerScreenshotState.Saved || state is PlayerScreenshotState.Failed
    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = fadeIn(tween(160)) + slideInHorizontally(tween(220)) { it / 2 },
        exit = fadeOut(tween(160)) + slideOutHorizontally(tween(180)) { it / 2 },
    ) {
        Surface(
            color = Color.Black.copy(alpha = 0.82f),
            contentColor = Color.White,
            shape = RoundedCornerShape(14.dp),
            tonalElevation = 6.dp,
        ) {
            when (state) {
                is PlayerScreenshotState.Saved -> {
                    Row(
                        modifier = Modifier.padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Image(
                            bitmap = state.preview.asImageBitmap(),
                            contentDescription = "截图预览",
                            modifier = Modifier
                                .size(width = 96.dp, height = 54.dp)
                                .clip(RoundedCornerShape(9.dp)),
                            contentScale = ContentScale.Crop,
                        )
                        Column(modifier = Modifier.padding(end = 10.dp)) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF92E6B3),
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = "已保存",
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                }

                is PlayerScreenshotState.Failed -> {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            Icons.Filled.ErrorOutline,
                            contentDescription = null,
                            tint = Color(0xFFFFAAA1),
                        )
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 2,
                        )
                    }
                }

                PlayerScreenshotState.Capturing,
                PlayerScreenshotState.Idle,
                -> Unit
            }
        }
    }
}

@Composable
fun FullScreenVideoTopBar(
    vm: ControlViewModel,
    modifier: Modifier = Modifier,
    isShowOnNormalScreen: Boolean = false,
    onMoreClick: () -> Unit,
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = vm.isShowOverlay() && (vm.isFullScreen || isShowOnNormalScreen),
        exit = fadeOut(),
        enter = fadeIn(),
    ) {
        TopControl {
            val ctx = LocalContext.current as Activity
            BackBtn {
                vm.onFullScreen(false, ctx = ctx)
            }
            Text(
                modifier = Modifier.weight(1f),
                text = vm.title,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = rememberCurrentTimeText(),
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
            )
            Spacer(modifier = Modifier.size(12.dp))
            val br = rememberBatteryReceiver()

            val ic = if (br.isCharge.value) {
                Icons.Filled.BatteryChargingFull
            } else {
                if (br.electricity.value <= 10) {
                    Icons.Filled.Battery0Bar
                } else if (br.electricity.value <= 20) {
                    Icons.Filled.Battery2Bar
                } else if (br.electricity.value <= 40) {
                    Icons.Filled.Battery3Bar
                } else if (br.electricity.value <= 60) {
                    Icons.Filled.Battery4Bar
                } else if (br.electricity.value <= 70) {
                    Icons.Filled.Battery5Bar
                } else if (br.electricity.value <= 90) {
                    Icons.Filled.Battery6Bar
                } else {
                    Icons.Filled.BatteryFull
                }
            }
            Icon(ic, null, modifier = Modifier.rotate(90F), tint = Color.White)
            Text(
                text = "${br.electricity.value}%",
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
            )
            Spacer(modifier = Modifier.size(16.dp))

            IconButton(onClick = {
                onMoreClick()
            }) {
                Icon(
                    Icons.Filled.MoreVert,
                    tint = Color.White,
                    contentDescription = null
                )
            }
            Spacer(modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun NormalVideoTopBar(
    vm: ControlViewModel,
    modifier: Modifier = Modifier,
    showTools: Boolean,
    showDlna: Boolean,
    onBack: () -> Unit,
    onSpeed: () -> Unit,
    onDlna: () -> Unit,
    onShare: (withCover: Boolean) -> Unit
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = vm.isShowOverlay() && !vm.isFullScreen,
        exit = fadeOut(),
        enter = fadeIn(),
    ) {
        TopControl {
            BackBtn(onBack)

            Spacer(modifier = Modifier.weight(1f))

            if (showTools) {
                IconButton(onClick = onSpeed) {
                    Icon(
                        Icons.Filled.Speed,
                        tint = Color.White,
                        contentDescription = stringResource(R.string.speed)
                    )
                }

                if (showDlna) {
                    IconButton(onClick = onDlna) {
                        Icon(
                            Icons.Filled.CastConnected,
                            tint = Color.White,
                            contentDescription = null
                        )
                    }
                }

                CombineClickIconButton(
                    onClick = { onShare(true) },
                    onLongClick = { onShare(false) }
                ) {
                    Icon(
                        Icons.Filled.Share,
                        tint = Color.White,
                        contentDescription = stringResource(id = R.string.share)
                    )
                }
            }
        }
    }
}

@Composable
fun EasyVideoBottomControl(
    vm: ControlViewModel,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(0.dp),
    onShowEpisodeWin: () -> Unit,
    onSHowSpeedWin: () -> Unit,
    onNext: () -> Unit,
    danmakuControlState: PlayerDanmakuControlState? = null,
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = vm.isShowOverlay(),
        exit = fadeOut(),
        enter = fadeIn(),
    ) {
        BottomControl(
            paddingValues
        ) {
            PlayPauseBtn(isPlaying = vm.playWhenReady, onClick = {
                vm.onPlayPause(it)
            })

            if (vm.isFullScreen) {
                Icon(
                    Icons.Filled.SkipNext,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable {
                            onNext()
                        }
                        .padding(4.dp),
                    tint = Color.White,
                    contentDescription = stringResource(id = R.string.try_play_next)
                )
            }

            OptionalPlayerDanmakuToggle(state = danmakuControlState)

            TimeText(time = vm.position, Color.White)

            val position =
                when (vm.controlState) {
                    ControlViewModel.ControlState.Normal -> vm.position
                    ControlViewModel.ControlState.HorizontalScroll -> vm.horizontalScrollPosition
                    else -> 0
                }
            vm.controlState.logi("ViewComponent")

            ViewSeekBar(
                during = vm.during.toInt(),
                position = position.toInt(),
                secondary = vm.bufferPosition.toInt(),
                onValueChange = {
                    vm.onPositionChange(it.toFloat())
                },
                onValueChangeFinish = {
                    "onValueChangeFinish".logi("ViewComponent")
                    vm.onActionUPScope()
                }
            )

            TimeText(time = vm.during, Color.White)

            if (vm.isFullScreen) {
                Text(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable {
                            onShowEpisodeWin()
                        }
                        .padding(8.dp),
                    text = stringResource(id = R.string.episode),
                    color = Color.White
                )
                Text(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable {
                            onSHowSpeedWin()
                        }
                        .padding(8.dp),
                    text = stringResource(id = R.string.speed),
                    color = Color.White
                )
            }

            val ctx = LocalContext.current as Activity
            Icon(
                if (vm.isFullScreen) Icons.Filled.CloseFullscreen
                else Icons.Filled.OpenInFull,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable {
                        vm.onFullScreen(!vm.isFullScreen, ctx = ctx)
                    }
                    .padding(4.dp),
                tint = Color.White,
                contentDescription = null
            )
        }
    }
}

@Composable
fun FastUI(
    vm: ControlViewModel,
    fastForwardText: String = "快进",
    fastRewindText: String = "快退",
    fastWeight: Float = 0.2f,
    delayTime: Long = 2000
) {
    LaunchedEffect(key1 = Unit) {
        launch {
            snapshotFlow {
                vm.isFastRewindWinShow
            }.collectLatest {
                if (it) {
                    delay(delayTime)
                    vm.isFastRewindWinShow = false
                }
            }
        }
        launch {
            snapshotFlow {
                vm.isFastForwardWinShow
            }.collectLatest {
                if (it) {
                    delay(delayTime)
                    vm.isFastForwardWinShow = false
                }
            }
        }
    }

    Row(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = vm.isFastRewindWinShow,
            modifier = Modifier
                .weight(maxOf(fastWeight, 0.2f))
                .fillMaxHeight(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                Modifier
                    .clip(
                        RoundedCornerShape(
                            0.dp,
                            16.dp,
                            16.dp,
                            0.dp
                        )
                    )
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Spacer(modifier = Modifier.size(8.dp))
                    Icon(
                        Icons.Filled.FastRewind,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        modifier = Modifier,
                        textAlign = TextAlign.Center,
                        text = fastRewindText,
                        color = Color.White
                    )


                }
            }
        }
        Spacer(modifier = Modifier.weight(1f - fastWeight))
        AnimatedVisibility(
            visible = vm.isFastForwardWinShow,
            modifier = Modifier
                .weight(maxOf(fastWeight, 0.2f))
                .fillMaxHeight(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                Modifier
                    .clip(
                        RoundedCornerShape(
                            16.dp,
                            0.dp,
                            0.dp,
                            16.dp
                        )
                    )
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        modifier = Modifier,
                        textAlign = TextAlign.Center,
                        text = fastForwardText,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Icon(
                        Icons.Filled.FastForward,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                }
            }
        }
    }

}


@Composable
fun FastUI(
    vm: ControlViewModel,
    fastForwardText: String = "快进",
    fastRewindText: String = "快退",

    fastForwardTopText: String = "快进",
    fastRewindTopText: String = "快退",

    horizontalDoubleTapWeight: Float = 0.2f,
    verticalDoubleTapWeight: Float = 0.5f,
    delayTime: Long = 200,
) {
    val realHorizontalWeight = horizontalDoubleTapWeight.coerceAtLeast(0.2f)
    LaunchedEffect(key1 = Unit) {
        launch {
            snapshotFlow {
                vm.isFastForwardTopShow || vm.isFastForwardWinShow || vm.isFastRewindWinShow || vm.isFastRewindTopShow
            }.collectLatest {
                if (it) {
                    delay(delayTime)
                    vm.isFastRewindWinShow = false
                    vm.isFastForwardWinShow = false

                    vm.isFastRewindTopShow = false
                    vm.isFastForwardTopShow = false
                }
            }
        }
    }

    AnimatedVisibility(
        modifier = Modifier.fillMaxSize(),
        visible = vm.isFastForwardTopShow || vm.isFastForwardWinShow || vm.isFastRewindWinShow || vm.isFastRewindTopShow,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(realHorizontalWeight)
            ) {
                if (vm.isFastRewindTopShow) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight(verticalDoubleTapWeight)
                            .fillMaxWidth()
                            .align(Alignment.TopStart)
                    ) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .clip(
                                    RoundedCornerShape(
                                        CornerSize(0),
                                        CornerSize(16.dp),
                                        CornerSize(16.dp),
                                        CornerSize(0)
                                    )
                                )
                                .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Filled.FastRewind,
                                    null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.size(8.dp))
                                Text(
                                    modifier = Modifier,
                                    textAlign = TextAlign.Center,
                                    text = fastRewindTopText,
                                    color = Color.White
                                )


                            }
                        }
                    }
                }

                if (vm.isFastRewindWinShow) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight(1 - verticalDoubleTapWeight)
                            .fillMaxWidth()
                            .align(Alignment.BottomStart)
                    ) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .clip(
                                    RoundedCornerShape(
                                        CornerSize(0),
                                        CornerSize(16.dp),
                                        CornerSize(16.dp),
                                        CornerSize(0)
                                    )
                                )
                                .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Filled.FastRewind,
                                    null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.size(8.dp))
                                Text(
                                    modifier = Modifier,
                                    textAlign = TextAlign.Center,
                                    text = fastRewindText,
                                    color = Color.White
                                )


                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f - 2 * realHorizontalWeight))
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(realHorizontalWeight)
            ) {
                if (vm.isFastForwardTopShow) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight(verticalDoubleTapWeight)
                            .fillMaxWidth()
                            .align(Alignment.TopEnd)
                    ) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .clip(
                                    RoundedCornerShape(
                                        CornerSize(16.dp),
                                        CornerSize(0),
                                        CornerSize(0),
                                        CornerSize(16.dp)
                                    )
                                )
                                .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    modifier = Modifier,
                                    textAlign = TextAlign.Center,
                                    text = fastForwardTopText,
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
                if (vm.isFastForwardWinShow) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight(1 - verticalDoubleTapWeight)
                            .fillMaxWidth()
                            .align(Alignment.BottomEnd)
                    ) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .clip(
                                    RoundedCornerShape(
                                        CornerSize(16.dp),
                                        CornerSize(0),
                                        CornerSize(0),
                                        CornerSize(16.dp)
                                    )
                                )
                                .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    modifier = Modifier,
                                    textAlign = TextAlign.Center,
                                    text = fastForwardText,
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
    }
}
