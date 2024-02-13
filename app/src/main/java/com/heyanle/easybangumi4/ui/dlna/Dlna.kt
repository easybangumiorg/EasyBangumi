package com.heyanle.easybangumi4.ui.dlna

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.entity.PlayLineWrapper
import com.heyanle.easybangumi4.source_api.entity.CartoonSummary
import com.heyanle.easybangumi4.source_api.entity.Episode
import com.heyanle.easybangumi4.ui.cartoon_play.cartoonEpisodeList
import com.heyanle.easybangumi4.ui.cartoon_play.cartoonMessage
import com.heyanle.easybangumi4.ui.cartoon_play.cartoonPlayLines
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.CartoonPlayViewModel
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.CartoonPlayViewModelFactory
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.DetailedViewModel
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.DetailedViewModelFactory
import com.heyanle.easybangumi4.ui.common.Action
import com.heyanle.easybangumi4.ui.common.DetailedContainer
import com.heyanle.easybangumi4.ui.common.ErrorPage
import com.heyanle.easybangumi4.ui.common.LoadingPage
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.ui.common.proc.SortState
import com.heyanle.easybangumi4.utils.stringRes
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Created by heyanlin on 2024/2/6 15:29.
 */
@Composable
fun Dlna(
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
    val dlnaVM = viewModel<DlnaPlayingViewModel>()

    DisposableEffect(key1 = Unit) {
        dlnaVM.onEnter()
        onDispose {
            dlnaVM.onDispose()
        }
    }

    val detailedState = detailedVM.stateFlow.collectAsState()
    val playState = playVM.curringPlayState.collectAsState()
    val playingState = dlnaVM.playingState.collectAsState()

    LaunchedEffect(key1 = Unit) {
        launch {
            snapshotFlow {
                detailedState.value.cartoonInfo
            }.collectLatest {
                it?.let {
                    playVM.onCartoonInfoChange(it)
                }
            }
        }

        launch {
            snapshotFlow {
                playState.value
            }.collectLatest {
                it?.let {
                    dlnaVM.changePlay(it)
                }
            }
        }


    }



    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        DetailedContainer(sourceKey = source) { _, sou, det ->
            DlnaPage(
                detailedVM = detailedVM,
                playVM = playVM,
                dlnaVM = dlnaVM,
                detailState = detailedState.value,
                playState = playState.value,
                playingState = playingState.value
            )
        }
    }

    if (dlnaVM.showDeviceDialog.value) {
        val scope = rememberCoroutineScope()
        DisposableEffect(key1 = Unit) {
            scope.launch {
                while (isActive && dlnaVM.showDeviceDialog.value) {
                    dlnaVM.search()
                    delay(5000)
                }
            }
            onDispose {
                scope.cancel()
            }
        }
        AlertDialog(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.please_choose_device))
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .padding(16.dp, 0.dp)
                            .size(32.dp)

                    )
                }

            },
            text = {
//                DlnaDeviceList(onClick = {
//                    isDlnaListOpen = false
//                    DlnaManager.select(it)
//                })
                val list = dlnaVM.deviceList.collectAsState()
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    items(list.value) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(CircleShape)
                                .clickable {
                                    dlnaVM.changeDevice(it)
                                    dlnaVM.showDeviceDialog.value = false
                                }
                                .padding(16.dp, 8.dp),
                            text = it.friendlyName,
                            color = if (playingState.value.device == it) MaterialTheme.colorScheme.secondary else Color.Unspecified
                        )
                    }
                }
            },
            onDismissRequest = {
                dlnaVM.showDeviceDialog.value = false
            },
            confirmButton = {}
        )
    }


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DlnaPage(
    detailedVM: DetailedViewModel,
    playVM: CartoonPlayViewModel,
    dlnaVM: DlnaPlayingViewModel,

    detailState: DetailedViewModel.DetailState,
    playState: CartoonPlayViewModel.CartoonPlayState?,
    playingState: DlnaPlayingViewModel.DlnaPlayingState,
) {

    val nav = LocalNavController.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Column {
        TopAppBar(
            title = {
                val curDevice = playingState.device
                val title = if (curDevice == null) {
                    stringRes(com.heyanle.easy_i18n.R.string.unselected_device)
                } else {
                    curDevice.friendlyName
                }
                Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis)
            },
            navigationIcon = {
                IconButton(onClick = {
                    nav.popBackStack()
                }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        stringResource(id = com.heyanle.easy_i18n.R.string.back)
                    )
                }
            },
            scrollBehavior = scrollBehavior,
            actions = {
                TextButton(onClick = {
                    dlnaVM.showDeviceDialog.value = true
                }) {
                    Text(
                        text = stringResource(id = com.heyanle.easy_i18n.R.string.change_device),
                    )
                }

            }

        )

        if (detailState.isLoading) {
            LoadingPage(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        } else if (detailState.isError || detailState.cartoonInfo == null) {
            ErrorPage(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                errorMsg = detailState.errorMsg.ifEmpty { detailState.throwable?.message ?: "" },
                clickEnable = true,
                onClick = {
                    detailedVM.load()
                },
                other = { Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.click_to_retry)) }
            )
        } else {
            DlnaPlayDetailed(
                cartoon = detailState.cartoonInfo,
                playLines = detailState.cartoonInfo.playLineWrapper,
                selectLineIndex = playVM.selectedLineIndex,
                playingPlayLine = playState?.playLine,
                playingEpisode = playState?.episode,
                showPlayLine = if (detailState.cartoonInfo.playLine.size > 1) true else detailState.cartoonInfo.isShowLine,
                listState = rememberLazyGridState(),
                scrollBehavior = scrollBehavior,
                onLineSelect = {
                    playVM.selectedLineIndex = it
                },
                onEpisodeClick = { line, epi ->
                    playVM.changePlay(detailState.cartoonInfo, line, epi)
                },
                sortState = detailedVM.sortState,
                onSortChange = { sortKey, isReverse ->
                    detailedVM.setCartoonSort(sortKey, isReverse, detailState.cartoonInfo)
                },
                dlnaPlayingState = playingState,
                onDlnaRetry = {
                    playState?.let {
                        dlnaVM.changePlay(it)
                    }
                },
                onPause = {
                    dlnaVM.tryPause()
                },
                onPlay = {
                    dlnaVM.tryPlay()
                },
                onRefresh = {
                    dlnaVM.tryRefresh()
                },
                onStop = {
                    dlnaVM.tryStop()
                }
            )
        }


    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DlnaPlayDetailed(
    cartoon: CartoonInfo,

    playLines: List<PlayLineWrapper>,
    selectLineIndex: Int,

    playingPlayLine: PlayLineWrapper?,
    playingEpisode: Episode?,

    listState: LazyGridState = rememberLazyGridState(),
    scrollBehavior: TopAppBarScrollBehavior,

    onLineSelect: (Int) -> Unit,
    onEpisodeClick: (PlayLineWrapper, Episode) -> Unit,

    showPlayLine: Boolean = true,

    sortState: SortState<Episode>,
    onSortChange: (String, Boolean) -> Unit,

    dlnaPlayingState: DlnaPlayingViewModel.DlnaPlayingState,
    onDlnaRetry: () -> Unit,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onRefresh: () -> Unit,
) {
    LazyVerticalGrid(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        columns = GridCells.Adaptive(128.dp),
        state = listState,
        contentPadding = PaddingValues(0.dp, 0.dp, 0.dp, 96.dp)
    ) {

        // 投屏控制
        dlnaAction(
            dlnaPlayingState, onDlnaRetry, onPlay, onPause, onStop, onRefresh
        )

        // 番剧信息
        cartoonMessage(cartoon)

        // 播放线路
        cartoonPlayLines(
            playLines = playLines,
            currentDownloadPlayLine = mutableStateOf(null),
            showPlayLine = showPlayLine,
            selectLineIndex = selectLineIndex,
            sortState = sortState,
            playingPlayLine = playingPlayLine,
            currentDownloadSelect = mutableStateOf(emptySet()),
            onLineSelect = onLineSelect,
            onSortChange = onSortChange,
        )

        // 集数
        cartoonEpisodeList(
            playLines = playLines,
            selectLineIndex = selectLineIndex,
            playingPlayLine = playingPlayLine,
            playingEpisode = playingEpisode,
            currentDownloadSelect = mutableStateOf(emptySet()),
            currentDownloadPlayLine = mutableStateOf(null),
            onEpisodeClick = onEpisodeClick
        )
    }
}

fun LazyGridScope.dlnaAction(
    dlnaPlayingState: DlnaPlayingViewModel.DlnaPlayingState,
    onRetry: () -> Unit,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onRefresh: () -> Unit,
) {

    item(
        span = {
            // LazyGridItemSpanScope:
            // maxLineSpan
            GridItemSpan(maxLineSpan)
        }
    ) {


        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (dlnaPlayingState.isLoading) {
                LoadingPage(
                    modifier = Modifier.fillMaxWidth(), loadingMsg = stringResource(
                        id = com.heyanle.easy_i18n.R.string.parsing
                    )
                )
            } else if (dlnaPlayingState.isError) {
                ErrorPage(
                    modifier = Modifier.fillMaxWidth(),
                    errorMsg = dlnaPlayingState.errorMsg + dlnaPlayingState.errorThrowable?.message,
                    other = { Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.click_to_retry)) },
                    clickEnable = true,
                    onClick = {
                        onRetry()
                    },
                )
            } else {
                Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.dlna_alert))
                Row(
                    modifier = Modifier
                        .padding(4.dp)
                        .height(72.dp)
                ) {
                    Action(
                        icon = {
                            Icon(
                                Icons.Filled.PlayArrow,
                                stringResource(id = com.heyanle.easy_i18n.R.string.play),
                            )
                        },
                        msg = {
                            Text(
                                text = stringResource(id = com.heyanle.easy_i18n.R.string.play),
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 12.sp
                            )
                        },
                        onClick = {
                            stringRes(com.heyanle.easy_i18n.R.string.dnla_try_play).moeSnackBar()
                            onPlay()
                        }
                    )
                    Action(
                        icon = {
                            Icon(
                                Icons.Filled.Pause,
                                stringResource(id = com.heyanle.easy_i18n.R.string.pause),
                            )
                        },
                        msg = {
                            Text(
                                text = stringResource(id = com.heyanle.easy_i18n.R.string.pause),
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 12.sp
                            )
                        },
                        onClick = {
                            stringRes(com.heyanle.easy_i18n.R.string.dnla_try_pause).moeSnackBar()
                            onPause()
                        }
                    )

                    Action(
                        icon = {
                            Icon(
                                Icons.Filled.Stop,
                                stringResource(id = com.heyanle.easy_i18n.R.string.stop),
                            )
                        },
                        msg = {
                            Text(
                                text = stringResource(id = com.heyanle.easy_i18n.R.string.stop),
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 12.sp
                            )
                        },
                        onClick = {
                            stringRes(com.heyanle.easy_i18n.R.string.dnla_try_stop).moeSnackBar()
                            onStop()
                        }
                    )

                    Action(
                        icon = {
                            Icon(
                                Icons.Filled.Refresh,
                                stringResource(id = com.heyanle.easy_i18n.R.string.refresh),
                            )
                        },
                        msg = {
                            Text(
                                text = stringResource(id = com.heyanle.easy_i18n.R.string.refresh),
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 12.sp
                            )
                        },
                        onClick = {
                            onRefresh()
                        }
                    )
                }
            }
        }
    }
}