package com.heyanle.easybangumi4.ui.cartoon_play

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.DOWNLOAD
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.cartoon_download.CartoonDownloadDispatcher
import com.heyanle.easybangumi4.navigationCartoonTag
import com.heyanle.easybangumi4.navigationDlna
import com.heyanle.easybangumi4.navigationSearch
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.easybangumi4.source_api.entity.CartoonSummary
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.CartoonPlayViewModel
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.CartoonPlayViewModelFactory
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.CartoonPlayingViewModel
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.DetailedViewModel
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.DetailedViewModelFactory
import com.heyanle.easybangumi4.ui.common.DetailedContainer
import com.heyanle.easybangumi4.ui.common.EasyMutiSelectionDialog
import com.heyanle.easybangumi4.ui.common.ErrorPage
import com.heyanle.easybangumi4.ui.common.LoadingPage
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.ui.main.home.HomeBottomSheet
import com.heyanle.easybangumi4.utils.isCurPadeMode
import com.heyanle.easybangumi4.utils.openUrl
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.injekt.core.Injekt
import loli.ball.easyplayer2.ControlViewModel
import loli.ball.easyplayer2.ControlViewModelFactory
import loli.ball.easyplayer2.EasyPlayerScaffoldBase

/**
 * Created by heyanle on 2023/12/17.
 * https://github.com/heyanLE
 */
@Composable
fun CartoonPlay(
    id: String,
    source: String,
    enterData: CartoonPlayViewModel.EnterData? = null
) {
    val summary = remember(key1 = id, key2 = source) {
        CartoonSummary(id, source)
    }
    val nav = LocalNavController.current

    val detailedVM = viewModel<DetailedViewModel>(factory = DetailedViewModelFactory(summary))
    val playVM = viewModel<CartoonPlayViewModel>(factory = CartoonPlayViewModelFactory(enterData))
    val playingVM = viewModel<CartoonPlayingViewModel>()
    val isPad = isCurPadeMode()
    val controlVM = ControlViewModelFactory.viewModel(playingVM.exoPlayer, isPad)

    val detailedState = detailedVM.stateFlow.collectAsState()
    val playState = playVM.curringPlayState.collectAsState()
    val playingState = playingVM.playingState.collectAsState()

    LaunchedEffect(key1 = detailedState.value) {
        detailedState.value.cartoonInfo?.let {
            playVM.onCartoonInfoChange(it)
        }
    }

    LaunchedEffect(key1 = playState.value) {
        playingVM.changePlay(playState.value, playVM.adviceProgress)
        playVM.adviceProgress = -1L
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        DetailedContainer(sourceKey = source) { _, sou, det ->
            CartoonPlay(
                isPad,
                controlVM,
                detailedVM,
                playVM,
                playingVM,
                detailedState.value,
                playState.value,
                playingState.value
            )
        }

        val starDialog = detailedState.value.starDialogState
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
                    detailedVM.dialogSetCartoonStar(starDialog.cartoon, it)
                },
                onManage = {
                    runCatching {
                        nav.navigationCartoonTag()
                    }.onFailure {
                        it.printStackTrace()
                    }
                }) {
                detailedVM.dialogExit()
            }
        }
    }

    if (playingVM.isCustomSpeedDialog.value) {
        val focusRequest = remember {
            FocusRequester()
        }
        val text = remember {
            mutableStateOf(playingVM.customSpeed.value.let { if (it > 0) it else 1 }.toString())
        }
        DisposableEffect(key1 = Unit ){
            runCatching {
                focusRequest.requestFocus()
            }.onFailure {
                it.printStackTrace()
            }
           onDispose {
               runCatching {
                   focusRequest.freeFocus()
               }.onFailure {
                   it.printStackTrace()
               }
           }
        }
        AlertDialog(
            onDismissRequest = {
                playingVM.isCustomSpeedDialog.value = false
            },
            title = {
                Text(text = stringRes(R.string.custom_speed))
            },
            text = {
                OutlinedTextField(
                    modifier = Modifier.focusRequester(focusRequest),
                    value = text.value,
                    onValueChange = { s ->
                        if (s.none {
                                (it < '0' || it > '9')
                            }) {

                        }
                        text.value = s
                    })
            },
            confirmButton = {
                TextButton(onClick = {
                    val tex = text.value
                    val f = tex.toFloatOrNull() ?: -1f
                    if (f <= 0) {
                        playingVM.setCustomSpeed(-1f)
                        if (playingVM.isCustomSpeed.value) {
                            controlVM.setSpeed(1f)
                            playingVM.isCustomSpeed.value = false
                        }
                        stringRes(R.string.please_input_right_speed).moeSnackBar()
                    } else {
                        playingVM.setCustomSpeed(f)
                        if (playingVM.isCustomSpeed.value) {
                            controlVM.setSpeed(f)
                        }
                    }
                    playingVM.isCustomSpeedDialog.value = false
                }) {
                    Text(text = stringRes(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    playingVM.isCustomSpeedDialog.value = false
                }) {
                    Text(text = stringRes(R.string.cancel))
                }
            }
        )
    }


}

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


@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun CartoonPlay(
    isPad: Boolean,
    controlVM: ControlViewModel,

    detailedVM: DetailedViewModel,
    playVM: CartoonPlayViewModel,
    playingVM: CartoonPlayingViewModel,

    detailState: DetailedViewModel.DetailState,
    playState: CartoonPlayViewModel.CartoonPlayState?,
    playingState: CartoonPlayingViewModel.PlayingState,
) {
    val nav = LocalNavController.current
    val cartoonDownloadDispatcher: CartoonDownloadDispatcher by Injekt.injectLazy()


    DisposableEffect(key1 = Unit) {
        onDispose {
            playingVM.onExit()
        }
    }

    LaunchedEffect(key1 = playState, key2 = detailState) {
        if (playState == null || detailState.cartoonInfo == null) {
            controlVM.title = ""
        } else {
            val cartoon = detailState.cartoonInfo
            val episode = playState.episode
            controlVM.title = "${cartoon.name} - ${episode.label}"
        }
    }

    val gridCount = detailedVM.gridCount.collectAsState()
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

    val showScaleTypeWin = remember {
        mutableStateOf(false)
    }

    EasyPlayerScaffoldBase(
        modifier = Modifier
            .fillMaxSize()
            .let {
                if (settingPreferences.playerBottomNavigationBarPadding.get()) {
                    it.navigationBarsPadding()
                } else it
            },
        vm = controlVM,
        isPadMode = isPad,
        contentWeight = 0.5f,
        videoFloat = {
            if (playState != null) {
                VideoFloat(
                    cartoonPlayingViewModel = playingVM,
                    cartoonPlayViewModel = playVM,
                    playingState = playingState,
                    playState = playState,
                    controlVM = controlVM,
                    showSpeedWin = showSpeedWin,
                    showEpisodeWin = showEpisodeWin,
                    showScaleTypeWin = showScaleTypeWin
                )
            }
        },
        control = {
            VideoControl(
                controlVM = controlVM,
                cartoonPlayingVM = playingVM,
                cartoonPlayVM = playVM,
                playingState = playingState,
                detailState = detailState,
                sourcePlayState = playState,
                showSpeedWin = showSpeedWin,
                showEpisodeWin = showEpisodeWin,
                showVideoScaleTypeWin = showScaleTypeWin
            )
        }) {

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ) {
            if (detailState.isLoading) {
                LoadingPage(
                    modifier = Modifier.fillMaxSize()
                )
            } else if (detailState.isError || detailState.cartoonInfo == null) {
                ErrorPage(
                    modifier = Modifier.fillMaxSize(),
                    errorMsg = detailState.errorMsg.ifEmpty {
                        detailState.throwable?.message ?: ""
                    },
                    clickEnable = true,
                    onClick = {
                        detailedVM.load()
                    },
                    other = { Text(text = stringResource(id = R.string.click_to_retry)) }
                )
            } else {
                val sortState = detailedVM.sortStateFlow.collectAsState()
                CartoonPlayDetailed(
                    cartoon = detailState.cartoonInfo,
                    gridCount = gridCount.value ,
                    onGridChange = {
                        detailedVM.setGridCount(it)
                    },
                    playLines = detailState.cartoonInfo.playLineWrapper,
                    selectLineIndex = playVM.selectedLineIndex,
                    playingPlayLine = playState?.playLine,
                    playingEpisode = playState?.episode,
                    showPlayLine = if (detailState.cartoonInfo.playLine.size > 1) true else detailState.cartoonInfo.isShowLine,
                    listState = lazyGridState,
                    onLineSelect = {
                        playVM.selectedLineIndex = it
                    },
                    onEpisodeClick = { line, epi ->
                        playVM.changePlay(detailState.cartoonInfo, line, epi)
                    },
                    isStar = detailState.cartoonInfo.starTime > 0,
                    onStar = {
                        detailedVM.setCartoonStar(it, detailState.cartoonInfo)
                    },
                    sortState = sortState.value,
                    onSearch = {
                        nav.navigationSearch(
                            detailState.cartoonInfo.name,
                            detailState.cartoonInfo.source
                        )
                    },
                    onWeb = {
                        runCatching {
                            detailState.cartoonInfo.url.openUrl()
                        }.onFailure {
                            it.printStackTrace()
                        }
                    },
                    onExtPlayer = {
                        playingVM.playCurrentExternal()
                    },
                    onDownload = { playLine, episodes ->
                        stringRes(R.string.add_download_completely).moeSnackBar(
                            confirmLabel = stringRes(R.string.click_to_view),
                            onConfirm = {
                                runCatching {
                                    nav.navigate(DOWNLOAD)
                                }.onFailure {
                                    it.printStackTrace()
                                }
                            }
                        )
                        cartoonDownloadDispatcher.newDownload(
                            detailState.cartoonInfo,
                            episodes.map {
                                playLine.playLine to it
                            })
                    },
                    onSortChange = { sortKey, isReverse ->
                        detailedVM.setCartoonSort(sortKey, isReverse, detailState.cartoonInfo)
                    }
                )
            }
        }
    }
}





