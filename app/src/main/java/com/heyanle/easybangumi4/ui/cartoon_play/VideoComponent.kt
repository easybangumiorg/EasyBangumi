package com.heyanle.easybangumi4.ui.cartoon_play

import android.app.Activity
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
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Airplay
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Battery0Bar
import androidx.compose.material.icons.filled.Battery2Bar
import androidx.compose.material.icons.filled.Battery3Bar
import androidx.compose.material.icons.filled.Battery4Bar
import androidx.compose.material.icons.filled.Battery5Bar
import androidx.compose.material.icons.filled.Battery6Bar
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.CartoonPlayViewModel
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.CartoonPlayingViewModel
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.DetailedViewModel
import com.heyanle.easybangumi4.ui.common.CombineClickIconButton
import com.heyanle.easybangumi4.ui.common.ErrorPage
import com.heyanle.easybangumi4.ui.common.LoadingPage
import com.heyanle.easybangumi4.ui.common.ToggleButton
import com.heyanle.easybangumi4.utils.bufferImageCache
import com.heyanle.easybangumi4.utils.downloadImage
import com.heyanle.easybangumi4.utils.shareImageText
import com.heyanle.easybangumi4.utils.shareText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import loli.ball.easyplayer2.BackBtn
import loli.ball.easyplayer2.BottomControl
import loli.ball.easyplayer2.ControlViewModel
import loli.ball.easyplayer2.FullScreenBtn
import loli.ball.easyplayer2.LockBtn
import loli.ball.easyplayer2.PlayPauseBtn
import loli.ball.easyplayer2.ProgressBox
import loli.ball.easyplayer2.SimpleGestureController
import loli.ball.easyplayer2.TimeSlider
import loli.ball.easyplayer2.TimeText
import loli.ball.easyplayer2.TopControl
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
) {
    val ctx = LocalContext.current as Activity

    LaunchedEffect(key1 = playingState) {
        if (playingState.isError) {
            controlVM.onFullScreen(false, false, ctx)
        }
    }
    LaunchedEffect(key1 = controlVM.controlState) {
        if (controlVM.controlState == ControlViewModel.ControlState.Ended) {
            cartoonPlayViewModel.tryNext()
        }
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
                    controlVM.onFullScreen(false, false, ctx)
                }) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = stringResource(id = R.string.back),
                    tint = Color.White
                )
            }
        }
    } else if (playingState.isError) {
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
                    .fillMaxHeight()
                    .background(Color.Black.copy(0.6f))
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                speedConfig.forEach { (name, speed) ->
                    val checked = controlVM.curSpeed == speed
                    Text(
                        textAlign = TextAlign.Center,
                        text = name,
                        modifier = Modifier
                            .defaultMinSize(180.dp, Dp.Unspecified)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { controlVM.setSpeed(speed) }
                            .padding(16.dp, 14.dp),
                        color = if (checked) MaterialTheme.colorScheme.primary else Color.White
                    )
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

            // 手势
            SimpleGestureController(
                vm = controlVM,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp, 64.dp),
                longTouchText = stringResource(id = R.string.long_press_fast_forward)
            )


            // 全屏顶部工具栏
            FullScreenVideoTopBar(
                vm = controlVM,
                modifier = Modifier
                    .align(Alignment.TopCenter)
            )


            NormalVideoTopBar(controlVM,
                showTools = playingState.isPlaying,
                onBack = {
                    nav.popBackStack()
                },
                onSpeed = {
                    showSpeedWin.value = true
                },
                onPlayExt = {
                    cartoonPlayingVM.playCurrentExternal()
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
fun FullScreenVideoTopBar(
    vm: ControlViewModel,
    modifier: Modifier = Modifier,
    isShowOnNormalScreen: Boolean = false,
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
    onPlayExt: () -> Unit,
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

                IconButton(onClick = onPlayExt) {
                    Icon(
                        Icons.Filled.Airplay,
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
                    Icons.Filled.NavigateNext,
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

            TimeSlider(
                during = vm.during,
                position = position,
                onValueChange = {
                    vm.onPositionChange(it)
                },
                onValueChangeFinish = {
                    vm.onActionUP()
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
            FullScreenBtn(isFullScreen = vm.isFullScreen, onClick = {
                vm.onFullScreen(it, ctx = ctx)
            })
        }
    }
}