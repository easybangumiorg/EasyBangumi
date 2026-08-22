package com.heyanle.easybangumi4.v2.ui.dlna

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.entity.PlayLineWrapper
import com.heyanle.easybangumi4.plugin.api.entity.CartoonSummary
import com.heyanle.easybangumi4.plugin.api.entity.Episode
import com.heyanle.easybangumi4.ui.cartoon_play.cartoonEpisodeList
import com.heyanle.easybangumi4.ui.cartoon_play.cartoonMessage
import com.heyanle.easybangumi4.ui.cartoon_play.cartoonPlayLines
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.CartoonPlayViewModel
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.CartoonPlayViewModelFactory
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.DetailedViewModel
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.DetailedViewModelFactory
import com.heyanle.easybangumi4.ui.common.DetailedContainer
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.ui.common.proc.SortState
import com.heyanle.easybangumi4.ui.dlna.DlnaPlayingViewModel
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.easybangumi4.v2.theme.V2Tokens
import com.heyanle.easybangumi4.v2.theme.V2Theme
import com.heyanle.easybangumi4.v2.ui.component.V2SecondaryHeader
import com.heyanle.easybangumi4.v2.ui.story.StoryLoadingV2
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/** V2 DLNA route. UPnP discovery and playback commands remain in [DlnaPlayingViewModel]. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DlnaV2(
    id: String,
    source: String,
    enterData: CartoonPlayViewModel.EnterData? = null,
) {
    val summary = remember(id, source) { CartoonSummary(id, source) }
    val navController = LocalNavController.current
    val detailedViewModel = viewModel<DetailedViewModel>(factory = DetailedViewModelFactory(summary))
    val playViewModel = viewModel<CartoonPlayViewModel>(factory = CartoonPlayViewModelFactory(enterData))
    val dlnaViewModel = viewModel<DlnaPlayingViewModel>()
    val detailedState by detailedViewModel.stateFlow.collectAsState()
    val playState by playViewModel.curringPlayState.collectAsState()
    val playingState by dlnaViewModel.playingState.collectAsState()

    DisposableEffect(dlnaViewModel) {
        dlnaViewModel.onEnter()
        onDispose { dlnaViewModel.onDispose() }
    }
    LaunchedEffect(detailedState.cartoonInfo) {
        detailedState.cartoonInfo?.let(playViewModel::onCartoonInfoChange)
    }
    LaunchedEffect(playState) {
        playState?.let(dlnaViewModel::changePlay)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(V2Tokens.WarmBackground),
    ) {
        V2SecondaryHeader(
            title = playingState.device?.friendlyName ?: stringResource(R.string.unselected_device),
            onBack = navController::popBackStack,
            actions = {
                TextButton(onClick = { dlnaViewModel.showDeviceDialog.value = true }) {
                    Text(stringResource(R.string.change_device), color = V2Theme.colors.accent)
                }
            },
        )
        DetailedContainer(sourceKey = source, errorContainerColor = V2Tokens.WarmBackground) { _, _, _ ->
            when {
                detailedState.isLoading -> StoryLoadingV2("正在读取番剧信息")
                detailedState.isError || detailedState.cartoonInfo == null -> DlnaErrorV2(
                    message = detailedState.errorMsg.ifBlank {
                        detailedState.throwable?.message.orEmpty()
                    },
                    onRetry = detailedViewModel::load,
                )
                else -> {
                    val cartoonInfo = detailedState.cartoonInfo ?: return@DetailedContainer
                    val sortState by detailedViewModel.sortStateFlow.collectAsState()
                    val gridCount by detailedViewModel.gridCount.collectAsState()
                    DlnaDetailedV2(
                        cartoon = cartoonInfo,
                        playLines = cartoonInfo.playLineWrapper,
                        selectLineIndex = playViewModel.selectedLineIndex,
                        playingPlayLine = playState?.playLine,
                        playingEpisode = playState?.episode,
                        showPlayLine = cartoonInfo.playLine.size > 1 || cartoonInfo.isShowLine,
                        sortState = sortState,
                        gridCount = gridCount,
                        playingState = playingState,
                        onLineSelect = { playViewModel.selectedLineIndex = it },
                        onEpisodeClick = { line, episode ->
                            playViewModel.changePlay(cartoonInfo, line, episode)
                        },
                        onSortChange = { sortKey, reverse ->
                            detailedViewModel.setCartoonSort(sortKey, reverse, cartoonInfo)
                        },
                        onGridChange = detailedViewModel::setGridCount,
                        onRetry = { playState?.let(dlnaViewModel::changePlay) },
                        onPlay = {
                            stringRes(R.string.dnla_try_play).moeSnackBar()
                            dlnaViewModel.tryPlay()
                        },
                        onPause = {
                            stringRes(R.string.dnla_try_pause).moeSnackBar()
                            dlnaViewModel.tryPause()
                        },
                        onStop = {
                            stringRes(R.string.dnla_try_stop).moeSnackBar()
                            dlnaViewModel.tryStop()
                        },
                        onRefresh = dlnaViewModel::tryRefresh,
                    )
                }
            }
        }
    }

    if (dlnaViewModel.showDeviceDialog.value) {
        LaunchedEffect(Unit) {
            while (isActive && dlnaViewModel.showDeviceDialog.value) {
                dlnaViewModel.search()
                delay(5_000)
            }
        }
        val devices by dlnaViewModel.deviceList.collectAsState()
        AlertDialog(
            onDismissRequest = { dlnaViewModel.showDeviceDialog.value = false },
            containerColor = V2Tokens.Surface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.please_choose_device), color = V2Tokens.TextPrimary)
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(22.dp),
                        color = V2Theme.colors.accent,
                        strokeWidth = 2.dp,
                    )
                }
            },
            text = {
                if (devices.isEmpty()) {
                    Text("正在搜索同一网络中的投屏设备", color = V2Tokens.TextSecondary)
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(devices) { device ->
                            Text(
                                text = device.friendlyName,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        dlnaViewModel.changeDevice(device)
                                        dlnaViewModel.showDeviceDialog.value = false
                                    }
                                    .padding(vertical = 11.dp),
                                color = if (playingState.device == device) {
                                    V2Theme.colors.accent
                                } else {
                                    V2Tokens.TextPrimary
                                },
                                fontWeight = if (playingState.device == device) FontWeight.SemiBold else FontWeight.Normal,
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { dlnaViewModel.showDeviceDialog.value = false }) {
                    Text(stringResource(R.string.cancel), color = V2Tokens.TextSecondary)
                }
            },
        )
    }
}

@Composable
private fun DlnaErrorV2(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("无法读取投屏内容", color = V2Tokens.Error, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            if (message.isNotBlank()) {
                Text(
                    message,
                    modifier = Modifier.padding(top = 6.dp),
                    color = V2Tokens.TextSecondary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            TextButton(onClick = onRetry) { Text("重试", color = V2Theme.colors.accent) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DlnaDetailedV2(
    cartoon: CartoonInfo,
    playLines: List<PlayLineWrapper>,
    selectLineIndex: Int,
    playingPlayLine: PlayLineWrapper?,
    playingEpisode: Episode?,
    showPlayLine: Boolean,
    sortState: SortState<Episode>,
    gridCount: Int,
    playingState: DlnaPlayingViewModel.DlnaPlayingState,
    onLineSelect: (Int) -> Unit,
    onEpisodeClick: (PlayLineWrapper, Episode) -> Unit,
    onSortChange: (String, Boolean) -> Unit,
    onGridChange: (Int) -> Unit,
    onRetry: () -> Unit,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onRefresh: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val selectionMode = remember { mutableStateOf<Pair<Int, PlayLineWrapper>?>(null) }
    val selection = remember { mutableStateOf<Set<Int>>(emptySet()) }
    LazyVerticalGrid(
        columns = GridCells.Fixed(gridCount),
        state = rememberLazyGridState(),
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        contentPadding = PaddingValues(0.dp, 0.dp, 0.dp, 28.dp),
    ) {
        dlnaControlsV2(
            playingState = playingState,
            onRetry = onRetry,
            onPlay = onPlay,
            onPause = onPause,
            onStop = onStop,
            onRefresh = onRefresh,
        )
        cartoonMessage(cartoon)
        cartoonPlayLines(
            playLines = playLines,
            selectionMode = selectionMode,
            selection = selection,
            showPlayLine = showPlayLine,
            selectLineIndex = selectLineIndex,
            sortState = sortState,
            gridCount = gridCount,
            onGridChange = onGridChange,
            playingPlayLine = playingPlayLine,
            onLineSelect = onLineSelect,
            onSortChange = onSortChange,
        )
        cartoonEpisodeList(
            playLines = playLines,
            selectLineIndex = selectLineIndex,
            playingPlayLine = playingPlayLine,
            playingEpisode = playingEpisode,
            selection = selection,
            selectionMode = selectionMode,
            onEpisodeClick = onEpisodeClick,
        )
    }
}

private fun LazyGridScope.dlnaControlsV2(
    playingState: DlnaPlayingViewModel.DlnaPlayingState,
    onRetry: () -> Unit,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onRefresh: () -> Unit,
) {
    item(span = { GridItemSpan(maxLineSpan) }) {
        Surface(
            modifier = Modifier.padding(horizontal = V2Tokens.ScreenHorizontalPadding, vertical = 14.dp),
            color = V2Tokens.Surface,
            shape = RoundedCornerShape(14.dp),
            tonalElevation = 0.dp,
        ) {
            when {
                playingState.isLoading -> Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator(color = V2Theme.colors.accent, strokeWidth = 2.dp)
                    Text("正在解析播放地址", modifier = Modifier.padding(top = 8.dp), color = V2Tokens.TextSecondary)
                }
                playingState.isError -> Column(modifier = Modifier.padding(18.dp)) {
                    Text("播放地址解析失败", color = V2Tokens.Error, fontWeight = FontWeight.SemiBold)
                    Text(
                        playingState.errorMsg,
                        modifier = Modifier.padding(top = 4.dp),
                        color = V2Tokens.TextSecondary,
                    )
                    TextButton(onClick = onRetry) { Text("重新解析", color = V2Theme.colors.accent) }
                }
                else -> Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp)) {
                    Text(
                        if (playingState.device == null) "选择设备后开始投屏" else "投屏控制",
                        modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
                        color = V2Tokens.TextSecondary,
                        fontSize = 12.sp,
                    )
                    Row(modifier = Modifier.fillMaxWidth()) {
                        DlnaControlActionV2(Icons.Filled.PlayArrow, stringResource(R.string.play), onPlay)
                        DlnaControlActionV2(Icons.Filled.Pause, stringResource(R.string.pause), onPause)
                        DlnaControlActionV2(Icons.Filled.Stop, stringResource(R.string.stop), onStop, V2Tokens.Error)
                        DlnaControlActionV2(Icons.Filled.Refresh, stringResource(R.string.refresh), onRefresh)
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.DlnaControlActionV2(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    color: Color = V2Theme.colors.accent,
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick)
            .padding(vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(23.dp))
        Spacer(Modifier.size(4.dp))
        Text(label, color = V2Tokens.TextPrimary, fontSize = 11.sp)
    }
}
