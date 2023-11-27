package com.heyanle.easybangumi4.ui.cartoon_play_old

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.WifiProtectedSetup
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.DOWNLOAD
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.play.CartoonPlayingControllerOld
import com.heyanle.easybangumi4.cartoon_download.CartoonDownloadController
import com.heyanle.easybangumi4.cartoon_download.CartoonDownloadDispatcher
import com.heyanle.easybangumi4.exo.EasyExoPlayer
import com.heyanle.easybangumi4.navigationCartoonTag
import com.heyanle.easybangumi4.navigationDlna
import com.heyanle.easybangumi4.navigationSearch
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.easybangumi4.source_api.entity.CartoonSummary
import com.heyanle.easybangumi4.source_api.entity.Episode
import com.heyanle.easybangumi4.source_api.entity.PlayLine
import com.heyanle.easybangumi4.ui.common.Action
import com.heyanle.easybangumi4.ui.common.ActionRow
import com.heyanle.easybangumi4.ui.common.DetailedContainer
import com.heyanle.easybangumi4.ui.common.EasyMutiSelectionDialog
import com.heyanle.easybangumi4.ui.common.EmptyPage
import com.heyanle.easybangumi4.ui.common.ErrorPage
import com.heyanle.easybangumi4.ui.common.FastScrollToTopFab
import com.heyanle.easybangumi4.ui.common.LoadingPage
import com.heyanle.easybangumi4.ui.common.OkImage
import com.heyanle.easybangumi4.ui.common.TabIndicator
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.utils.isCurPadeMode
import com.heyanle.easybangumi4.utils.openUrl
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.easybangumi4.utils.toast
import com.heyanle.injekt.api.get
import com.heyanle.injekt.core.Injekt
import loli.ball.easyplayer2.BackBtn
import loli.ball.easyplayer2.ControlViewModel
import loli.ball.easyplayer2.ControlViewModelFactory
import loli.ball.easyplayer2.EasyPlayerScaffoldBase
import loli.ball.easyplayer2.LockBtn
import loli.ball.easyplayer2.ProgressBox
import loli.ball.easyplayer2.SimpleBottomBar
import loli.ball.easyplayer2.SimpleGestureController
import loli.ball.easyplayer2.TopControl
import loli.ball.easyplayer2.utils.rememberBatteryReceiver

/**
 * Created by heyanlin on 2023/10/31.
 */
@Composable
fun CartoonPlay(
    id: String,
    source: String,
    url: String,
    enterData: CartoonPlayViewModel.EnterData? = null
) {
    val summary = remember(key1 = id, key2 = source, key3 = url) {
        CartoonSummary(id, source, url)
    }
    val nav = LocalNavController.current
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        DetailedContainer(sourceKey = source) { _, sou, det ->
            val detailedVM =
                viewModel<DetailedViewModelOld>(factory = DetailedViewModelFactory(summary))
            val cartoonPlayViewModel =
                viewModel<CartoonPlayViewModel>()
            CartoonPlay(
                detailedVM = detailedVM,
                cartoonPlayVM = cartoonPlayViewModel,
                cartoonSummary = summary,
                enterData = enterData
            )

            val starDialog = detailedVM.starDialogState
            if (starDialog != null) {
                EasyMutiSelectionDialog(
                    show = true,
                    title = {
                        Text(text = stringResource(id = R.string.change_tag))
                    },
                    items = starDialog.tagList,
                    initSelection = emptyList(),
                    confirmText = stringRes(R.string.star),
                    onConfirm = {
                        detailedVM.starCartoon(starDialog.cartoon, starDialog.playLines, it)
                    },
                    onManage = {
                        nav.navigationCartoonTag()
                    }) {
                    detailedVM.starDialogState = null
                }
            }
        }
    }
}

val speedConfig = linkedMapOf(
    "0.5X" to 0.5f,
    "0.75X" to 0.75f,
    "1.0X" to 1f,
    "1.25X" to 1.25f,
    "1.5X" to 1.5f,
    "2.0X" to 2f,
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun CartoonPlay(
    detailedVM: DetailedViewModelOld,
    cartoonPlayVM: CartoonPlayViewModel,
    cartoonSummary: CartoonSummary,
    enterData: CartoonPlayViewModel.EnterData? = null,
) {
    val isPad = isCurPadeMode()

    val cartoonPlayingControllerOld: CartoonPlayingControllerOld by Injekt.injectLazy()
    val controlVM = ControlViewModelFactory.viewModel(Injekt.get<EasyExoPlayer>(), isPad)
    val nav = LocalNavController.current

    val playingState = cartoonPlayingControllerOld.state.collectAsState()

    LaunchedEffect(key1 = detailedVM.detailedState) {
        val sta = detailedVM.detailedState
        if (sta is DetailedViewModelOld.DetailedState.None) {
            detailedVM.load()
        } else if (sta is DetailedViewModelOld.DetailedState.Info) {
            // 加载好之后进入 播放环节
            cartoonPlayVM.onDetailedLoaded(sta, enterData)
        }
    }

    LaunchedEffect(key1 = Unit) {
        detailedVM.checkUpdate()
    }

    DisposableEffect(key1 = Unit) {
        onDispose {
            cartoonPlayingControllerOld.trySaveHistory()
            cartoonPlayingControllerOld.release()
        }
    }

    LaunchedEffect(key1 = playingState) {
        val cartoon = playingState.value.cartoon()
        val episode = playingState.value.episode()

        if (cartoon != null && episode != null) {
            controlVM.title = "${cartoon.title} - ${episode.label}"
        } else {
            controlVM.title = ""
        }
    }

    val settingPreferences: SettingPreferences by Injekt.injectLazy()
    val orMode = settingPreferences.playerOrientationMode.flow()
        .collectAsState(initial = SettingPreferences.PlayerOrientationMode.Auto)
    LaunchedEffect(key1 = orMode) {
        controlVM.orientationEnableMode = when (orMode.value) {
            SettingPreferences.PlayerOrientationMode.Auto -> ControlViewModel.OrientationEnableMode.AUTO
            SettingPreferences.PlayerOrientationMode.Enable -> ControlViewModel.OrientationEnableMode.ENABLE
            SettingPreferences.PlayerOrientationMode.Disable -> ControlViewModel.OrientationEnableMode.DISABLE
        }
    }

    val lazyGridState = rememberLazyGridState()

    val showEpisodeWin = remember {
        mutableStateOf(false)
    }

    val showSpeedWin = remember {
        mutableStateOf(false)
    }

    EasyPlayerScaffoldBase(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        vm = controlVM,
        isPadMode = isPad,
        contentWeight = 0.5f,
        videoFloat = {
            VideoFloat(
                cartoonPlayingControllerOld = cartoonPlayingControllerOld,
                playingState = playingState.value,
                detailedVM = detailedVM,
                controlVM = controlVM,
                showSpeedWin, showEpisodeWin
            )
        },
        control = {
            VideoControl(
                controlVM = controlVM,
                playingState = playingState.value,
                showSpeedWin = showSpeedWin,
                showEpisodeWin = showEpisodeWin
            )
        }) {
        if (isPad) {
            Column {
                Spacer(
                    modifier = Modifier
                        .background(Color.Black)
                        .fillMaxWidth()
                        .windowInsetsTopHeight(WindowInsets.statusBars),
                )
                Row(
                    modifier = Modifier
                ) {

                    Spacer(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary)
                            .width(2.dp)
                            .fillMaxHeight(),
                    )
                    Box() {
                        PlayContent(
                            detailedVM,
                            cartoonPlayVM,
                            playingState.value,
                            lazyGridState
                        )
                    }
                }
            }
        } else {
            Column {
                Spacer(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary)
                        .height(2.dp)
                        .fillMaxWidth(),
                )
                PlayContent(
                    detailedVM,
                    cartoonPlayVM,
                    playingState.value,
                    lazyGridState
                )
            }
        }
    }

}

@Composable
fun VideoControl(
    controlVM: ControlViewModel,
    playingState: CartoonPlayingControllerOld.PlayingState,
    showSpeedWin: MutableState<Boolean>,
    showEpisodeWin: MutableState<Boolean>,
) {
    val cartoonPlayingControllerOld: CartoonPlayingControllerOld by Injekt.injectLazy()
    val nav = LocalNavController.current
    Box(Modifier.fillMaxSize()) {

        // 手势
        SimpleGestureController(
            vm = controlVM,
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp, 64.dp),
            longTouchText = stringResource(id = com.heyanle.easy_i18n.R.string.long_press_fast_forward)
        )


        // 全屏顶部工具栏
        FullScreenVideoTopBar(
            vm = controlVM,
            modifier = Modifier
                .align(Alignment.TopCenter)
        )


        NormalVideoTopBar(controlVM,
            showTools = playingState is CartoonPlayingControllerOld.PlayingState.Playing,
            onBack = {
                nav.popBackStack()
            },
            onSpeed = {
                showSpeedWin.value = true
            },
            onPlayExt = {
                cartoonPlayingControllerOld.playCurrentExternal()
            }
        )

        // 底部工具栏
        SimpleBottomBar(
            vm = controlVM,
            modifier = Modifier.align(Alignment.BottomCenter),
            paddingValues = if (controlVM.isFullScreen) PaddingValues(
                16.dp,
                0.dp,
                16.dp,
                8.dp
            ) else PaddingValues(8.dp, 0.dp)
        ) {

            if (it.isFullScreen) {
                Text(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable {
                            showSpeedWin.value = true
                        }
                        .padding(8.dp),
                    text = stringResource(id = com.heyanle.easy_i18n.R.string.speed),
                    color = Color.White
                )
                Text(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable {
                            showEpisodeWin.value = true
                        }
                        .padding(8.dp),
                    text = stringResource(id = com.heyanle.easy_i18n.R.string.episode),
                    color = Color.White
                )
            }
        }

        // 锁定按钮
        LockBtn(vm = controlVM)

        // 加载按钮
        ProgressBox(vm = controlVM)
    }
}

@Composable
fun VideoFloat(
    cartoonPlayingControllerOld: CartoonPlayingControllerOld,
    playingState: CartoonPlayingControllerOld.PlayingState,
    detailedVM: DetailedViewModelOld,
    controlVM: ControlViewModel,
    showSpeedWin: MutableState<Boolean>,
    showEpisodeWin: MutableState<Boolean>,
) {
    val ctx = LocalContext.current as Activity

    LaunchedEffect(key1 = playingState) {
        when (playingState) {
            is CartoonPlayingControllerOld.PlayingState.Playing -> {
                controlVM.onPrepare()
                // CartoonPlayingManager.trySaveHistory()
            }

            is CartoonPlayingControllerOld.PlayingState.Loading -> {
            }

            is CartoonPlayingControllerOld.PlayingState.Error -> {
                controlVM.onFullScreen(false, false, ctx)
            }

            else -> {}
        }
    }
    LaunchedEffect(key1 = controlVM.controlState) {
        if (controlVM.controlState == ControlViewModel.ControlState.Ended) {
            cartoonPlayingControllerOld.tryNext(isReverse = detailedVM.isReverse)
            stringRes(com.heyanle.easy_i18n.R.string.try_play_next).toast()
        }
    }
    when (playingState) {
        is CartoonPlayingControllerOld.PlayingState.Playing -> {
            if (controlVM.controlState == ControlViewModel.ControlState.Ended) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    IconButton(
                        modifier = Modifier.align(Alignment.Center),
                        onClick = {
                            cartoonPlayingControllerOld.refresh()
                        }) {
                        Icon(
                            Icons.Filled.Replay,
                            contentDescription = stringResource(id = com.heyanle.easy_i18n.R.string.replay_error)
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
                                contentDescription = stringResource(id = com.heyanle.easy_i18n.R.string.back)
                            )
                        }
                    }
                }
            }
        }

        is CartoonPlayingControllerOld.PlayingState.Loading -> {
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
                    loadingMsg = stringResource(id = com.heyanle.easy_i18n.R.string.parsing),
                    msgColor = Color.White.copy(0.6f)
                )
                IconButton(
                    modifier = Modifier.align(Alignment.TopStart),
                    onClick = {
                        controlVM.onFullScreen(false, false, ctx)
                    }) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = stringResource(id = com.heyanle.easy_i18n.R.string.back),
                        tint = Color.White
                    )
                }
            }

        }

        is CartoonPlayingControllerOld.PlayingState.Error -> {
            ErrorPage(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                errorMsg = playingState.errMsg,
                errorMsgColor = Color.White.copy(0.6f),
                clickEnable = true,
                other = {
                    Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.click_to_retry))
                },
                onClick = {
                    cartoonPlayingControllerOld.refresh()
                }
            )
        }

        else -> {}
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
                    onClick = {
                        showSpeedWin.value = false
                    },
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
                speedConfig.forEach {

                    Text(
                        textAlign = TextAlign.Center,
                        text = it.key,
                        modifier = Modifier
                            .defaultMinSize(180.dp, Dp.Unspecified)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                controlVM.setSpeed(it.value)
                            }
                            .padding(16.dp, 8.dp),
                        color = if (controlVM.curSpeed == it.value) MaterialTheme.colorScheme.primary else Color.White
                    )
                }
            }
        }
    }

    playingState.playLine()?.let { playLine ->
        playingState.cartoon()?.let { cartoonInfo ->
            playingState.episode()?.let { episode ->
                // 选集
                AnimatedVisibility(
                    showEpisodeWin.value && controlVM.isFullScreen,
                    enter = slideInHorizontally(tween()) { it },
                    exit = slideOutHorizontally(tween()) { it },

                    ) {

                    val isReverse = detailedVM.isReverse
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(
                                onClick = {
                                    showEpisodeWin.value = false
                                },
                                indication = null,
                                interactionSource = remember {
                                    MutableInteractionSource()
                                }
                            ),
                        contentAlignment = Alignment.CenterEnd,
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .defaultMinSize(180.dp, Dp.Unspecified)
                                .background(Color.Black.copy(0.6f))
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            for (i in 0 until playLine.episode.size) {
                                val index = if (isReverse) playLine.episode.size - 1 - i else i
                                val s = playLine.episode[index]
                                Text(
                                    textAlign = TextAlign.Center,
                                    text = s.label,
                                    modifier = Modifier
                                        .defaultMinSize(180.dp, Dp.Unspecified)
                                        .clickable {
                                            cartoonPlayingControllerOld.changePlay(
                                                cartoonInfo,
                                                playLine,
                                                s
                                            )
                                        }
                                        .padding(16.dp, 8.dp),
                                    color = if (episode == s) MaterialTheme.colorScheme.primary else Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlayContent(
    detailedVM: DetailedViewModelOld,
    cartoonPlayVM: CartoonPlayViewModel,
    playingState: CartoonPlayingControllerOld.PlayingState,
    lazyGridState: LazyGridState,
) {
    Column {
        Surface(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                CartoonPlayUI(
                    detailedVM = detailedVM,
                    cartoonPlayVM = cartoonPlayVM,
                    playingState = playingState,
                    lazyGridState
                )
                FastScrollToTopFab(listState = lazyGridState)
            }
        }
    }
}

@Composable
fun CartoonPlayUI(
    detailedVM: DetailedViewModelOld,
    cartoonPlayVM: CartoonPlayViewModel,
    playingState: CartoonPlayingControllerOld.PlayingState,
    listState: LazyGridState = rememberLazyGridState(),
    //onTitle: (String) -> Unit,
) {

    when (val detailedState = detailedVM.detailedState) {
        is DetailedViewModelOld.DetailedState.Info -> {
            CartoonPlayPage(
                detailedVM,
                cartoonPlayVM,
                detailedState,
                playingState,
                listState
            )
        }

        is DetailedViewModelOld.DetailedState.Error -> {
            ErrorPage(
                modifier = Modifier.fillMaxSize(),
                errorMsg = detailedState.errorMsg,
                clickEnable = true,
                onClick = {
                    detailedVM.load()
                },
                other = { Text(text = stringResource(id = R.string.click_to_retry)) }
            )
        }

        is DetailedViewModelOld.DetailedState.Loading -> {
            LoadingPage(
                modifier = Modifier.fillMaxSize()
            )
        }

        else -> Unit
    }

}

@Composable
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
fun CartoonPlayPage(
    detailedVM: DetailedViewModelOld,
    cartoonPlayVM: CartoonPlayViewModel,
    detailedState: DetailedViewModelOld.DetailedState.Info,
    playingState: CartoonPlayingControllerOld.PlayingState,
    listState: LazyGridState = rememberLazyGridState(),
    //onTitle: (String) -> Unit,
) {
    val playingController: CartoonPlayingControllerOld by Injekt.injectLazy()
    val cartoonDownloadDispatcher: CartoonDownloadDispatcher by Injekt.injectLazy()
    val nav = LocalNavController.current
    CartoonPlayDetailed(
        cartoon = detailedState.detail,
        playLines = detailedState.playLine,
        selectLineIndex = cartoonPlayVM.selectedLineIndex,
        playingPlayLine = playingState.playLine(),
        playingEpisode = playingState.episode(),
        showPlayLine = detailedState.isShowPlayLine,
        listState = listState,
        isReversal = detailedVM.isReverse,
        onLineSelect = {
            cartoonPlayVM.selectedLineIndex = it
        },
        onEpisodeClick = { playLine, episode ->
            playingController.changePlay(detailedState.detail, playLine, episode, 0)
        },
        isStar = detailedVM.isStar,
        onStar = {
            detailedVM.setCartoonStar(it, detailedState.detail, detailedState.playLine)
        },
        onReversal = {
            detailedVM.setCartoonReverse(it, detailedState.detail)
        },
        onSearch = {
            nav.navigationSearch(detailedState.detail.title, detailedState.detail.source)
        },
        onWeb = {
            runCatching {
                detailedState.detail.url.openUrl()
            }.onFailure {
                it.printStackTrace()
            }
        },
        onDlna = {
            nav.navigationDlna(
                CartoonSummary(
                    detailedState.detail.id,
                    detailedState.detail.source,
                    detailedState.detail.url
                ),
                detailedState.playLine.indexOf(playingState.playLine()) ?: -1,
                playingState.playLine()?.episode?.indexOf(playingState.episode()) ?: -1
            )
        },
        onDownload = { playLine, episodes ->
            stringRes(R.string.add_download_completely).moeSnackBar(
                confirmLabel = stringRes(R.string.click_to_view),
                onConfirm = {
                    nav.navigate(DOWNLOAD)
                }
            )
            cartoonDownloadDispatcher.newDownload(detailedState.detail, episodes.map {
                playLine to it
            })
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun CartoonPlayDetailed(
    cartoon: CartoonInfo,

    playLines: List<PlayLine>,
    selectLineIndex: Int,

    playingPlayLine: PlayLine?,
    playingEpisode: Episode?,

    listState: LazyGridState = rememberLazyGridState(),

    onLineSelect: (Int) -> Unit,
    onEpisodeClick: (PlayLine, Episode) -> Unit,

    showPlayLine: Boolean = true,

    isStar: Boolean,
    onStar: (Boolean) -> Unit,

    isReversal: Boolean = false,
    onReversal: (Boolean) -> Unit,// 外界不需要处理 playLines 的翻转，内部处理

    onSearch: () -> Unit,
    onWeb: () -> Unit,
    onDlna: () -> Unit,
    onDownload: (PlayLine, List<Episode>) -> Unit,
) {
    val currentDownloadPlayLine = remember {
        mutableStateOf<PlayLine?>(null)
    }
    val currentDownloadSelect = remember {
        mutableStateOf(setOf<Int>())
    }

    // 下载模式处理返回事件
    if (currentDownloadPlayLine.value != null) {
        BackHandler {
            currentDownloadPlayLine.value = null
        }
    }


    LazyVerticalGrid(
        modifier = Modifier.fillMaxSize(),
        columns = GridCells.Adaptive(128.dp),
        state = listState,
        contentPadding = PaddingValues(0.dp, 0.dp, 0.dp, 96.dp)
    ) {
        // 番剧信息
        cartoonMessage(cartoon)

        // action
        item(
            span = {
                // LazyGridItemSpanScope:
                // maxLineSpan
                GridItemSpan(maxLineSpan)
            }
        ) {
            Column {
                CartoonActions(
                    isStar = isStar,
                    isDownloading = currentDownloadPlayLine.value != null,
                    onStar = onStar,
                    onSearch = onSearch,
                    onWeb = onWeb,
                    onDlna = onDlna,
                    onDownload = {
                        if (currentDownloadPlayLine.value == null && selectLineIndex in playLines.indices) {
                            currentDownloadPlayLine.value = playLines[selectLineIndex]
                            currentDownloadSelect.value = setOf()
                        } else {
                            currentDownloadPlayLine.value = null
                        }
                        val cartoonDownloadController: CartoonDownloadController by Injekt.injectLazy()
                        cartoonDownloadController.tryShowFirstDownloadDialog()
                    },
                )
                Spacer(modifier = Modifier.size(8.dp))
                Divider()
            }
        }

        // 播放线路
        cartoonPlayLines(
            playLines,
            currentDownloadPlayLine,
            showPlayLine,
            selectLineIndex,
            playingPlayLine,
            currentDownloadSelect,
            isReversal,
            onLineSelect,
            onReversal
        )

        // 集数
        cartoonEpisodeList(
            playLines,
            selectLineIndex,
            isReversal,
            playingPlayLine,
            playingEpisode,
            currentDownloadSelect,
            currentDownloadPlayLine,
            onEpisodeClick
        )

    }

    currentDownloadPlayLine.value?.let { dowPlayLine ->
        Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.fillMaxSize()) {
            val up = remember { derivedStateOf { listState.firstVisibleItemIndex > 10 } }
            val downPadding by animateDpAsState(if (up.value) 80.dp else 40.dp, label = "")
            ExtendedFloatingActionButton(
                modifier = Modifier
                    .padding(16.dp, downPadding),
                text = {
                    Text(text = stringResource(id = R.string.start_download))
                },
                icon = {
                    Icon(
                        Icons.Filled.Download,
                        contentDescription = stringResource(id = R.string.start_download)
                    )
                },
                onClick = {
                    currentDownloadPlayLine.value = null
                    onDownload(dowPlayLine, currentDownloadSelect.value.flatMap {
                        val epi = dowPlayLine.episode.getOrNull(it)
                        if (epi == null) {
                            listOf()
                        } else {
                            listOf(epi)
                        }
                    })
                }
            )
        }
    }
}


fun LazyGridScope.cartoonMessage(
    cartoon: CartoonInfo
) {
    item(
        span = {
            // LazyGridItemSpanScope:
            // maxLineSpan
            GridItemSpan(maxLineSpan)
        }
    ) {
        var isExpended by remember {
            mutableStateOf(false)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    indication = null,
                    interactionSource = remember {
                        MutableInteractionSource()
                    }
                ) {
                    isExpended = !isExpended
                }
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedContent(
                modifier = Modifier,
                targetState = isExpended,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300, delayMillis = 300)) togetherWith
                            fadeOut(animationSpec = tween(300, delayMillis = 0))
                }, label = ""
            ) {
                if (it) {
                    CartoonTitleCard(cartoon)
                } else {
                    Row(
                        modifier = Modifier
                    ) {
                        OkImage(
                            modifier = Modifier
                                .width(95.dp)
                                .aspectRatio(19 / 13.5F)
                                .clip(RoundedCornerShape(4.dp)),
                            image = cartoon.coverUrl,
                            crossFade = false,
                            errorRes = com.heyanle.easybangumi4.R.drawable.placeholder,
                            contentDescription = cartoon.title
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                modifier = Modifier,
                                text = (cartoon.title),
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(modifier = Modifier.size(4.dp))
                            Text(
                                modifier = Modifier,
                                text = (cartoon.description ?: cartoon.intro ?: ""),
                                maxLines = 2,
                                style = MaterialTheme.typography.bodySmall,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }

                    }
                }
            }


            // 箭头
            Box(
                modifier = Modifier
                    .fillMaxWidth(), contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isExpended) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = cartoon.title
                )
            }
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CartoonTitleCard(
    cartoon: CartoonInfo
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Top,
        ) {
            OkImage(
                modifier = Modifier
                    .width(95.dp)
                    .aspectRatio(19 / 27F)
                    .clip(RoundedCornerShape(4.dp)),
                image = cartoon.coverUrl,
                crossFade = false,
                errorRes = com.heyanle.easybangumi4.R.drawable.placeholder,
                contentDescription = cartoon.title
            )

            Spacer(modifier = Modifier.size(8.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    modifier = Modifier,
                    text = cartoon.title,
                    style = MaterialTheme.typography.titleLarge,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(Modifier.size(16.dp))
                val list = cartoon.getGenres()
                if (list?.isNotEmpty() == true) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        list.forEach {
                            Text(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                                    .clickable {
                                    }
                                    .padding(8.dp, 4.dp),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.W900,
                                text = it,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
        Text(modifier = Modifier.padding(8.dp), text = cartoon.description)
    }
}


@Composable
fun CartoonActions(
    isStar: Boolean,
    isDownloading: Boolean,
    onStar: (Boolean) -> Unit,
    onSearch: () -> Unit,
    onWeb: () -> Unit,
    onDlna: () -> Unit,
    onDownload: () -> Unit,
) {
    ActionRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        val starIcon =
            if (isStar) Icons.Filled.Star else Icons.Filled.StarOutline
        val starTextId =
            if (isStar) R.string.started_miro else R.string.star
        // 点击追番
        Action(
            icon = {
                Icon(
                    starIcon,
                    stringResource(id = starTextId),
                    tint = if (isStar) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                )
            },
            msg = {
                Text(
                    text = stringResource(id = starTextId),
                    color = if (isStar) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                    fontSize = 12.sp
                )
            },
            onClick = {
                onStar(!isStar)
            }
        )

        // 搜索同名番
        Action(
            icon = {
                Icon(
                    Icons.Filled.Search,
                    stringResource(id = R.string.search)
                )
            },
            msg = {
                Text(
                    text = stringResource(id = R.string.search),
                    fontSize = 12.sp
                )
            },
            onClick = onSearch
        )

        // 打开原网站
        Action(
            icon = {
                Icon(
                    painterResource(id = com.heyanle.easybangumi4.R.drawable.ic_webview_24dp),
                    stringResource(id = R.string.open_source_url)
                )
            },
            msg = {
                Text(
                    text = stringResource(id = R.string.open_source_url),
                    fontSize = 12.sp
                )
            },
            onClick = onWeb
        )

        // 下载
        Action(
            icon = {
                Icon(
                    Icons.Filled.Download,
                    stringResource(id = R.string.download),
                    tint = if (isDownloading) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                )
            },
            msg = {
                Text(
                    text = stringResource(id = R.string.download),
                    color = if (isDownloading) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                    fontSize = 12.sp
                )
            },
            onClick = onDownload
        )

        // 投屏
        Action(
            icon = {
                Icon(
                    Icons.Filled.CastConnected,
                    stringResource(id = R.string.screen_cast)
                )
            },
            msg = {
                Text(
                    text = stringResource(id = R.string.screen_cast),
                    fontSize = 12.sp
                )
            },
            onClick = onDlna
        )
    }


}

fun LazyGridScope.cartoonPlayLines(
    playLines: List<PlayLine>,
    currentDownloadPlayLine: MutableState<PlayLine?>,
    showPlayLine: Boolean,
    selectLineIndex: Int,
    playingPlayLine: PlayLine?,
    currentDownloadSelect: MutableState<Set<Int>>,
    isReversal: Boolean,
    onLineSelect: (Int) -> Unit,
    onReversal: (Boolean) -> Unit,

    ) {
    // 播放线路
    if (playLines.isEmpty()) {
        item(
            span = {
                // LazyGridItemSpanScope:
                // maxLineSpan
                GridItemSpan(maxLineSpan)
            }
        ) {
            EmptyPage(
                modifier = Modifier.fillMaxWidth(),
                emptyMsg = stringResource(id = R.string.no_play_line)
            )
        }
    } else {
        item(
            span = {
                // LazyGridItemSpanScope:
                // maxLineSpan
                GridItemSpan(maxLineSpan)
            }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentDownloadPlayLine.value == null) {
                    if (showPlayLine) {
                        ScrollableTabRow(
                            modifier = Modifier
                                .weight(1f)
                                .padding(0.dp, 8.dp),
                            selectedTabIndex = 0.coerceAtLeast(selectLineIndex),
                            edgePadding = 0.dp,
                            indicator = {
                                TabIndicator(
                                    currentTabPosition = it[0.coerceAtLeast(selectLineIndex)]
                                )
                            },
                            divider = {}
                        ) {
                            playLines.forEachIndexed { index, playLine ->
                                Tab(
                                    selected = index == selectLineIndex,
                                    onClick = {
                                        onLineSelect(index)
                                    },
                                    unselectedContentColor = MaterialTheme.colorScheme.primary.copy(
                                        0.4f
                                    ),
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = playLine.label)

                                            if (playLine == playingPlayLine) {
                                                Box(
                                                    modifier = Modifier
                                                        .padding(2.dp, 0.dp, 0.dp, 0.dp)
                                                        .size(8.dp)
                                                        .background(
                                                            MaterialTheme.colorScheme.primary,
                                                            CircleShape
                                                        )
                                                )
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    } else {
                        Text(
                            modifier = Modifier
                                .weight(1f)
                                .padding(16.dp, 0.dp),
                            text = stringResource(id = com.heyanle.easy_i18n.R.string.play_list)
                        )
                    }
                } else {
                    // 下载模式的播放线路
                    Text(
                        modifier = Modifier
                            .weight(1f)
                            .padding(16.dp, 0.dp),
                        text = stringResource(id = com.heyanle.easy_i18n.R.string.select_to_download)
                    )
                    IconButton(onClick = {
                        currentDownloadPlayLine.value?.let {
                            val set = hashSetOf<Int>()
                            it.episode.forEachIndexed { index, _ ->
                                set.add(index)
                            }
                            currentDownloadSelect.value = set
                        }

                    }) {
                        Icon(
                            Icons.Filled.SelectAll,
                            stringResource(id = com.heyanle.easy_i18n.R.string.select_all),
                        )
                    }
                }
                IconButton(onClick = {
                    onReversal(!isReversal)
                }) {
                    Icon(
                        Icons.Filled.WifiProtectedSetup,
                        stringResource(id = com.heyanle.easy_i18n.R.string.reverse),
                        tint = if (isReversal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}

fun LazyGridScope.cartoonEpisodeList(
    playLines: List<PlayLine>,
    selectLineIndex: Int,
    isReversal: Boolean,
    playingPlayLine: PlayLine?,
    playingEpisode: Episode?,
    currentDownloadSelect: MutableState<Set<Int>>,
    currentDownloadPlayLine: MutableState<PlayLine?>,
    onEpisodeClick: (PlayLine, Episode) -> Unit,
) {
    playLines.getOrNull(selectLineIndex)?.let { playLine ->
        val episode = playLine.episode
        items(episode.size) {
            val index = if (isReversal) episode.size - 1 - it else it
            episode.getOrNull(index)?.let { item ->
                val select =
                    playLine == playingPlayLine && item == playingEpisode
                Row(
                    modifier = Modifier
                        .padding(4.dp)
                        .fillMaxWidth()
                        //.then(modifier)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (select && currentDownloadPlayLine.value == null) MaterialTheme.colorScheme.secondary else Color.Transparent)
                        .run {
                            if (select && currentDownloadPlayLine.value == null) {
                                this
                            } else {
                                border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline.copy(0.6f),
                                    RoundedCornerShape(4.dp)
                                )
                            }
                        }
                        .clickable {
                            if (currentDownloadPlayLine.value == null) {
                                onEpisodeClick(
                                    playLines[selectLineIndex],
                                    item
                                )
                            } else {
                                val check = currentDownloadSelect.value.contains(index)
                                if (!check) {
                                    currentDownloadSelect.value += index
                                } else {
                                    currentDownloadSelect.value -= index
                                    if (currentDownloadSelect.value.isEmpty()) {
                                        currentDownloadPlayLine.value = null
                                    }
                                }
                            }
                        }
                        .padding(8.dp),
                ) {
                    Text(
                        color = if (select && currentDownloadPlayLine.value == null) MaterialTheme.colorScheme.onSecondary else Color.Unspecified,
                        text = item.label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.weight(1f))
                    if (currentDownloadPlayLine.value != null) {
                        Spacer(Modifier.size(4.dp))
                        Checkbox(
                            checked = currentDownloadSelect.value.contains(index),
                            onCheckedChange = {
                                if (it) {
                                    currentDownloadSelect.value += index
                                } else {
                                    currentDownloadSelect.value -= index
                                    if (currentDownloadSelect.value.isEmpty()) {
                                        currentDownloadPlayLine.value = null
                                    }
                                }
                            }
                        )
                    }
                }
            }
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
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = vm.isShowOverlay() && !vm.isFullScreen,
        exit = fadeOut(),
        enter = fadeIn(),
    ) {
        TopControl {
            val ctx = LocalContext.current as Activity
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
            }
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