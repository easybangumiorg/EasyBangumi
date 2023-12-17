package com.heyanle.easybangumi4.ui.cartoon_play

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.DOWNLOAD
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.cartoon_download.CartoonDownloadDispatcher
import com.heyanle.easybangumi4.navigationCartoonTag
import com.heyanle.easybangumi4.navigationDlna
import com.heyanle.easybangumi4.navigationSearch
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.easybangumi4.source_api.entity.CartoonSummary
import com.heyanle.easybangumi4.ui.common.DetailedContainer
import com.heyanle.easybangumi4.ui.common.EasyMutiSelectionDialog
import com.heyanle.easybangumi4.ui.common.ErrorPage
import com.heyanle.easybangumi4.ui.common.LoadingPage
import com.heyanle.easybangumi4.ui.common.moeSnackBar
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
    url: String,
    enterData: CartoonPlayViewModel.EnterData? = null
) {
    val summary = remember(key1 = id, key2 = source, key3 = url) {
        CartoonSummary(id, source, url)
    }
    val nav = LocalNavController.current

    val detailedVM = viewModel<DetailedViewModel>(factory = DetailedViewModelFactory(summary))
    val playVM = viewModel<CartoonPlayViewModel>(factory = CartoonPlayViewModelFactory(enterData))
    val playingVM = viewModel<CartoonPlayingViewModel>()

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
                    nav.navigationCartoonTag()
                }) {
                detailedVM.dialogExit()
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
    detailedVM: DetailedViewModel,
    playVM: CartoonPlayViewModel,
    playingVM: CartoonPlayingViewModel,

    detailState: DetailedViewModel.DetailState,
    playState: CartoonPlayViewModel.CartoonPlayState?,
    playingState: CartoonPlayingViewModel.PlayingState,
) {
    val isPad = isCurPadeMode()

    val controlVM = ControlViewModelFactory.viewModel(playingVM.exoPlayer, isPad)
    val nav = LocalNavController.current
    val cartoonDownloadDispatcher: CartoonDownloadDispatcher by Injekt.injectLazy()


    DisposableEffect(key1 = Unit) {
        onDispose {
            playingVM.trySaveHistory()
        }
    }

    LaunchedEffect(key1 = playState) {
        if (playState == null) {
            controlVM.title = ""
        } else {
            val cartoon = playState.cartoonInfo
            val episode = playState.episode
            controlVM.title = "${cartoon.name} - ${episode.label}"
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
                    showEpisodeWin = showEpisodeWin
                )
            }
        },
        control = {
            VideoControl(
                controlVM = controlVM,
                cartoonPlayingVM = playingVM,
                playingState = playingState,
                sourcePlayState = playState,
                showSpeedWin = showSpeedWin,
                showEpisodeWin = showEpisodeWin,
            )
        }) {

        if (detailState.isLoading) {
            LoadingPage(
                modifier = Modifier.fillMaxSize()
            )
        } else if (detailState.isError || detailState.cartoonInfo == null) {
            ErrorPage(
                modifier = Modifier.fillMaxSize(),
                errorMsg = detailState.errorMsg.ifEmpty { detailState.throwable?.message ?: "" },
                clickEnable = true,
                onClick = {
                    detailedVM.load()
                },
                other = { Text(text = stringResource(id = R.string.click_to_retry)) }
            )
        } else {
            CartoonPlayDetailed(
                cartoon = detailState.cartoonInfo,
                playLines = detailState.cartoonInfo.playLineWrapper,
                selectLineIndex = playVM.selectedLineIndex,
                playingPlayLine = playState?.playLine,
                playingEpisode = playState?.episode,
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
                sortState = detailedVM.sortState,
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
                onDlna = {
                    nav.navigationDlna(
                        CartoonSummary(
                            detailState.cartoonInfo.id,
                            detailState.cartoonInfo.source,
                            detailState.cartoonInfo.url
                        ),
                        detailState.cartoonInfo.playLineWrapper.indexOf(playState?.playLine) ?: -1,
                        playState?.playLine?.playLine?.episode?.indexOf(playState.episode) ?: -1
                    )
                },
                onDownload = { playLine, episodes ->
                    stringRes(R.string.add_download_completely).moeSnackBar(
                        confirmLabel = stringRes(R.string.click_to_view),
                        onConfirm = {
                            nav.navigate(DOWNLOAD)
                        }
                    )
                    cartoonDownloadDispatcher.newDownload(detailState.cartoonInfo, episodes.map {
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






