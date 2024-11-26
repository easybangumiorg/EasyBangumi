package com.heyanle.easybangumi4.ui.cartoon_play

import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.entity.PlayLineWrapper
import com.heyanle.easybangumi4.cartoon.story.download.runtime.CartoonDownloadDispatcher
import com.heyanle.easybangumi4.navigationCartoonTag
import com.heyanle.easybangumi4.navigationSearch
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.easybangumi4.source_api.entity.CartoonSummary
import com.heyanle.easybangumi4.source_api.entity.Episode
import com.heyanle.easybangumi4.ui.cartoon_play.cartoon_recorded.CartoonRecorded
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.CartoonPlayViewModel
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.CartoonPlayViewModelFactory
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.CartoonPlayingViewModel
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.CartoonStoryViewModel
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.DetailedViewModel
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.DetailedViewModelFactory
import com.heyanle.easybangumi4.ui.common.DetailedContainer
import com.heyanle.easybangumi4.ui.common.EasyDeleteDialog
import com.heyanle.easybangumi4.ui.common.EasyMutiSelectionDialog
import com.heyanle.easybangumi4.ui.common.ErrorPage
import com.heyanle.easybangumi4.ui.common.LoadingPage
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.utils.isCurPadeMode
import com.heyanle.easybangumi4.utils.logi
import com.heyanle.easybangumi4.utils.openUrl
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.inject.core.Inject
import kotlinx.coroutines.launch
import loli.ball.easyplayer2.ControlViewModel
import loli.ball.easyplayer2.ControlViewModelFactory
import loli.ball.easyplayer2.EasyPlayerScaffoldBase
import loli.ball.easyplayer2.EasyPlayerStateSync

/**
 * Created by heyanle on 2023/12/17.
 * https://github.com/heyanLE
 */
@OptIn(UnstableApi::class)
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
    val storyViewModel = viewModel<CartoonStoryViewModel>()
    val controlVM = ControlViewModelFactory.viewModel(
        playingVM.exoPlayer,
        isPad,
        render = playingVM.easyTextRenderer
    )

    val detailedState = detailedVM.stateFlow.collectAsState()
    val playState = playVM.curringPlayState.collectAsState()
    val playingState = playingVM.playingState.collectAsState()

    var downloadModel by remember {
        mutableStateOf<Triple<CartoonInfo, PlayLineWrapper, List<Episode>>?>(null)
    }
    var deleteDialogState by remember {
        mutableStateOf<Triple<CartoonInfo, PlayLineWrapper, List<Episode>>?>(null)
    }
    var saveDialogState by remember {
        mutableStateOf<Triple<CartoonInfo, PlayLineWrapper, List<Episode>>?>(null)
    }

    // 将同步范围拓展到整个界面，包括 recorded dialog
    EasyPlayerStateSync(controlVM)

    LaunchedEffect(key1 = detailedState.value) {
        detailedState.value.cartoonInfo?.let {
            playVM.onCartoonInfoChange(it)
        }
    }

    LaunchedEffect(Unit) {
        launch {
            snapshotFlow {
                playingVM.showRecording.value
            }.collect() {
                if (it == null) {
                    try {
                        "bind".logi("CartoonPlay")
                        controlVM.bind()
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }

                } else {
                    try {
                        "unbind".logi("CartoonPlay")
                        controlVM.unbind()
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }
                }
            }
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
                playingState.value,
                onDownload = {
                    controlVM.onPlayPause(false)
                    downloadModel = it
                },
                onDelete = {
                    controlVM.onPlayPause(false)
                    deleteDialogState = it
                },
                onSave = {
                    controlVM.onPlayPause(false)
                    saveDialogState = it
                }
            )

            downloadModel?.let {
                CartoonDownloadDialog(
                    it.first,
                    it.second,
                    it.third,
                    onDismissRequest = {
                        downloadModel = null
                    }
                )
            }

            deleteDialogState?.let {
                EasyDeleteDialog(show = true, onDelete = {
                    storyViewModel.delete(it) {
                        detailedVM.load()
                    }
                }) {
                    deleteDialogState = null
                }
            }

            saveDialogState?.let {
                AlertDialog(
                    text = {
                        Text(
                            text = stringResource(
                                id = R.string.save_to_media_ready,
                                it.third.size.toString()
                            )
                        )
                    },
                    onDismissRequest = {
                        saveDialogState = null
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            storyViewModel.save(it) {
                                detailedVM.load()
                            }
                            saveDialogState = null
                        }) {
                            Text(text = stringResource(id = R.string.confirm))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            saveDialogState = null
                        }) {
                            Text(text = stringResource(id = R.string.cancel))
                        }
                    }
                )
            }


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
                action = {
                    TextButton(
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.onBackground
                        ),
                        onClick = {
                            runCatching {
                                nav.navigationCartoonTag()
                            }.onFailure {
                                it.printStackTrace()
                            }
                            detailedVM.dialogExit()
                        }
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = stringResource(id = com.heyanle.easy_i18n.R.string.edit))
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.edit))
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
        DisposableEffect(key1 = Unit) {
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

    val recordState = playingVM.showRecording.value

    if (recordState != null) {

        CartoonRecorded(
            controlViewModel = controlVM,
            cartoonRecordedModel = recordState,
            show = true,
        ) {
            playingVM.showRecording.value = null
        }
        BackHandler(
            playingVM.showRecording.value != null
        ) {
            playingVM.showRecording.value = null
        }
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

    onDownload: (Triple<CartoonInfo, PlayLineWrapper, List<Episode>>) -> Unit,
    onDelete: (Triple<CartoonInfo, PlayLineWrapper, List<Episode>>) -> Unit,
    onSave: (Triple<CartoonInfo, PlayLineWrapper, List<Episode>>) -> Unit
) {
    val nav = LocalNavController.current


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
    val settingPreferences: SettingPreferences by Inject.injectLazy()
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
        needSync = false,
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



        val compose: @Composable ()->Unit = {
            Surface(
                modifier = Modifier
                    .fillMaxSize(),
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
                        gridCount = gridCount.value,
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

                            onDownload(
                                Triple(
                                    detailState.cartoonInfo,
                                    playLine,
                                    episodes
                                )
                            )
                        },
                        onDelete = { playLine, episodes ->
                            onDelete(
                                Triple(
                                    detailState.cartoonInfo,
                                    playLine,
                                    episodes
                                )
                            )
                        },
                        onSortChange = { sortKey, isReverse ->
                            detailedVM.setCartoonSort(sortKey, isReverse, detailState.cartoonInfo)
                        },
                        onSave = { playLine, episodes ->
                            onSave(
                                Triple(
                                    detailState.cartoonInfo,
                                    playLine,
                                    episodes
                                )
                            )
                        },
                    )
                }
            }
        }

        if (isPad) {
            Box(modifier = Modifier.background(Color.Black).statusBarsPadding()){
                compose()
            }
        } else {
            compose()
        }


    }
}





