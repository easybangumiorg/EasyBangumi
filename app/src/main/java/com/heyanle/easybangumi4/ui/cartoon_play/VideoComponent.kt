package com.heyanle.easybangumi4.ui.cartoon_play

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.navigationDlna
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.CartoonPlayViewModel
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.CartoonPlayingViewModel
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.DetailedViewModel
import com.heyanle.easybangumi4.ui.common.CombineClickIconButton
import com.heyanle.easybangumi4.ui.common.ErrorPage
import com.heyanle.easybangumi4.ui.common.LoadingPage
import com.heyanle.easybangumi4.ui.common.ToggleButton
import com.heyanle.easybangumi4.utils.bufferImageCache
import com.heyanle.easybangumi4.utils.downloadImage
import com.heyanle.easybangumi4.utils.logi
import com.heyanle.easybangumi4.utils.shareImageText
import com.heyanle.easybangumi4.utils.shareText
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

/**
 * Created by heyanle on 2023/12/17.
 * https://github.com/heyanLE
 */
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
        if (defaultSpeed == -1f){
            controlVM.setSpeed(if (customSpeed > 0) customSpeed else 1f)
        }else{
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
                msgColor = Color.White.copy(0.6f)
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
            ErrorPage(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                errorMsg = playingState.errorMsg,
                errorMsgColor = Color.White.copy(0.6f),
                clickEnable = true,
                other = {
                    Text(text = stringResource(id = R.string.click_to_retry))
                },
                onClick = {
                    cartoonPlayingViewModel.tryRefresh()
                }
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
    AnimatedVisibility(
        showSpeedWin.value,
        enter = slideInHorizontally(tween()) { it },
        exit = slideOutHorizontally(tween()) { it },

        ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    onClick = { showSpeedWin.value = false },
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

                ToggleButton(
                    checked = cartoonPlayingViewModel.isCustomSpeed.value,
                    onClick = {
                        val custom = cartoonPlayingViewModel.customSpeed.value
                        if (custom > 0) {
                            cartoonPlayingViewModel.enableCustomSpeed()
                            controlVM.setSpeed(custom)
                        } else {
                            cartoonPlayingViewModel.setCustomSpeedDialog()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val custom = cartoonPlayingViewModel.customSpeed.collectAsState()
                        Text(
                            text = if (custom.value > 0f) custom.value.toString() + "X" else stringResource(
                                id = R.string.custom_speed
                            ),
                            color = if (cartoonPlayingViewModel.isCustomSpeed.value) MaterialTheme.colorScheme.primary else Color.White
                        )
                        if (custom.value > 0f) {
                            Icon(
                                Icons.Filled.Edit,
                                modifier = Modifier.clickable {
                                    cartoonPlayingViewModel.setCustomSpeedDialog()
                                },
                                contentDescription = stringResource(id = R.string.custom_speed)
                            )
                        }
                    }
                }


                speedConfig.forEach { (name, speed) ->
                    val checked =
                        !cartoonPlayingViewModel.isCustomSpeed.value && controlVM.curSpeed == speed
                    ToggleButton(
                        checked = checked,
                        onClick = {
                            controlVM.setSpeed(speed)
                            cartoonPlayingViewModel.disableCustomSpeed()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    ) {
                        Text(name)
                    }
                }
            }
        }
    }


    val playLine = playState.playLine
    // 选集
    AnimatedVisibility(
        showEpisodeWin.value && controlVM.isFullScreen,
        enter = slideInHorizontally(tween()) { it },
        exit = slideOutHorizontally(tween()) { it },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    onClick = { showEpisodeWin.value = false },
                    indication = null,
                    interactionSource = remember {
                        MutableInteractionSource()
                    }
                ),
            contentAlignment = Alignment.CenterEnd,
        ) {
            // 滚动到当前播放的视频位置
            val initItem = playLine.sortedEpisodeList.indexOf(playState.episode)
            val state = rememberLazyListState(initItem)
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .defaultMinSize(180.dp, Dp.Unspecified)
                    .background(Color.Black.copy(0.8f)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                state = state
            ) {
                playLine.sortedEpisodeList.forEach {
                    item {
                        val checked = playState.episode == it
                        ToggleButton(
                            checked = checked,
                            onClick = {
                                cartoonPlayViewModel.changePlay(
                                    cartoonSummary = playState.cartoonSummary,
                                    playLineWrapper = playLine,
                                    episode = it
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth(0.25f)
                                .padding(horizontal = 8.dp)
                        ) {
                            Text(it.label)
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
) {
    val nav = LocalNavController.current
    val scope = rememberCoroutineScope()
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

            if(fastWeight <= 0){
                // 手势
                SimpleGestureController(
                    vm = controlVM,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp, 64.dp),
                    longTouchText = stringResource(id = R.string.long_press_fast_forward),
                    slideFullTime = playerSeekFullWidthTime,
                )
            }else{

                GestureController(
                    controlVM,
                    Modifier
                        .fillMaxSize()
                        .padding(6.dp, 64.dp),
                    playerSeekFullWidthTime,
                    supportFast = true,
                    horizontalDoubleTapWeight = 1f/fastWeight,
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
            ){
                showVideoScaleTypeWin.value = true
            }

            FullScreenRightToolBar(
                vm = controlVM,
                modifier = Modifier
                    .fillMaxHeight()
                    .defaultMinSize(64.dp, Dp.Unspecified)
                    .align(Alignment.CenterEnd)
            ) {
                cartoonPlayingVM.showRecord()
            }

            NormalVideoTopBar(controlVM,
                showTools = playingState.isPlaying,
                onBack = {
                    nav.popBackStack()
                },
                onSpeed = {
                    showSpeedWin.value = true
                },
                onDlna = {
                    // cartoonPlayingVM.playCurrentExternal()
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
                            detailState.cartoonInfo?.id?:"",
                            detailState.cartoonInfo?.source?:"",
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
                                    APP, "${APP.packageName}.fileProvider", imageFile
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
                }
            )

            // 锁定按钮
            LockBtn(vm = controlVM)

            // 加载按钮
            ProgressBox(vm = controlVM)
        }
    }


}

@Composable
fun FullScreenRightToolBar(
    vm: ControlViewModel,
    modifier: Modifier = Modifier,
    isShowOnNormalScreen: Boolean = false,
    onShowRecorded: ()->Unit,
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
                Box(modifier = Modifier
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable {
                        onShowRecorded()
                    }
                    .padding(8.dp)
                ) {
                    Icon(
                        Icons.Filled.Videocam,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                        contentDescription = null
                    )
                }
            }
        }
    }

}

@Composable
fun FullScreenVideoTopBar(
    vm: ControlViewModel,
    modifier: Modifier = Modifier,
    isShowOnNormalScreen: Boolean = false,
    onMoreClick: ()->Unit,
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

//            Spacer(modifier = Modifier.weight(1f))

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
            Icon(ic, "el", modifier = Modifier.rotate(90F), tint = Color.White)
            Text(text = "${br.electricity.value}%", color = Color.White)
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

                IconButton(onClick = onDlna) {
                    Icon(
                        Icons.Filled.CastConnected,
                        tint = Color.White,
                        contentDescription = null
                    )
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

    Row(modifier = Modifier.fillMaxSize()){
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
                if(vm.isFastRewindTopShow) {
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

                if(vm.isFastRewindWinShow) {
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
            Spacer(modifier = Modifier.weight(1f - 2*realHorizontalWeight))
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(realHorizontalWeight)
            ) {
                if(vm.isFastForwardTopShow) {
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
                if(vm.isFastForwardWinShow) {
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
