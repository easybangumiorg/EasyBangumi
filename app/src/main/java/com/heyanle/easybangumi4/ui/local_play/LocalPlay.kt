package com.heyanle.easybangumi4.ui.local_play

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Airplay
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.More
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.exoplayer.ExoPlayer
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.cartoon.entity.PlayLineWrapper
import com.heyanle.easybangumi4.cartoon_download.entity.LocalCartoon
import com.heyanle.easybangumi4.cartoon_download.entity.LocalEpisode
import com.heyanle.easybangumi4.navigationDetailed
import com.heyanle.easybangumi4.navigationSearch
import com.heyanle.easybangumi4.ui.cartoon_play.FullScreenVideoTopBar
import com.heyanle.easybangumi4.ui.cartoon_play.speedConfig
import com.heyanle.easybangumi4.ui.common.Action
import com.heyanle.easybangumi4.ui.common.ActionRow
import com.heyanle.easybangumi4.ui.common.EasyDeleteDialog
import com.heyanle.easybangumi4.ui.common.EmptyPage
import com.heyanle.easybangumi4.ui.common.FastScrollToTopFab
import com.heyanle.easybangumi4.ui.common.LoadingPage
import com.heyanle.easybangumi4.ui.common.OkImage
import com.heyanle.easybangumi4.ui.common.TabIndicator
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.ui.common.proc.SortDropDownMenu
import com.heyanle.easybangumi4.ui.common.proc.SortState
import com.heyanle.easybangumi4.utils.isCurPadeMode
import com.heyanle.easybangumi4.utils.loge
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.injekt.api.get
import com.heyanle.injekt.core.Injekt
import loli.ball.easyplayer2.BackBtn
import loli.ball.easyplayer2.BottomControl
import loli.ball.easyplayer2.ControlViewModel
import loli.ball.easyplayer2.ControlViewModelFactory
import loli.ball.easyplayer2.EasyPlayerScaffoldBase
import loli.ball.easyplayer2.FullScreenBtn
import loli.ball.easyplayer2.LockBtn
import loli.ball.easyplayer2.PlayPauseBtn
import loli.ball.easyplayer2.ProgressBox
import loli.ball.easyplayer2.SimpleBottomBar
import loli.ball.easyplayer2.SimpleBottomBarWithSeekBar
import loli.ball.easyplayer2.SimpleGestureController
import loli.ball.easyplayer2.TimeSlider
import loli.ball.easyplayer2.TimeText
import loli.ball.easyplayer2.TopControl
import loli.ball.easyplayer2.ViewSeekBar
import loli.ball.easyplayer2.utils.loge

/**
 * Created by heyanlin on 2023/9/25.
 */
@Composable
fun LocalPlay(
    uuid: String
) {
    val isPad = isCurPadeMode()
    val vm = viewModel<LocalPlayViewModel>(factory = LocalPlayViewModelFactory(uuid = uuid))
    val controlVM = ControlViewModelFactory.viewModel(vm.exoPlayer, isPad, LocalPlayViewModel.TAG)
    val nav = LocalNavController.current

    val state by vm.flow.collectAsState()

    val dialog by vm.dialogFlow.collectAsState()

    var showEpisodeWin by remember {
        mutableStateOf(false)
    }

    var showSpeedWin by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(key1 = state.curPlayingEpisode) {
        state.curPlayingEpisode?.let {
            vm.play(it)
        }
    }

    DisposableEffect(key1 = Unit) {
        onDispose {
            vm.clearPlay()
        }
    }

    BackHandler(
        state.deleteModePlayLine != null
    ) {
        vm.exitDeleteMode()
    }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        val lazyGridState = rememberLazyGridState()
        EasyPlayerScaffoldBase(modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
            vm = controlVM,
            isPadMode = isPad,
            contentWeight = 0.5f,
            videoFloat = { model ->


                // 倍速窗口
                AnimatedVisibility(
                    showSpeedWin,
                    enter = slideInHorizontally(tween()) { it },
                    exit = slideOutHorizontally(tween()) { it },

                    ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(
                                onClick = { showSpeedWin = false },
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

                            Row(
                                modifier = Modifier
                                    .defaultMinSize(180.dp, Dp.Unspecified)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        val custom = vm.customSpeed.value
                                        if (custom > 0) {
                                            vm.enableCustomSpeed()
                                            controlVM.setSpeed(custom)
                                        } else {
                                            vm.setCustomSpeedDialog()
                                        }
                                    }
                                    .padding(16.dp, 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                val custom = vm.customSpeed.collectAsState()
                                Text(
                                    text = if (custom.value > 0f) custom.value.toString() + "X" else stringResource(
                                        id = R.string.custom_speed
                                    ),
                                    color = if (vm.isCustomSpeed.value) MaterialTheme.colorScheme.primary else Color.White
                                )
                                if (custom.value > 0f) {
                                    IconButton(onClick = {
                                        vm.setCustomSpeedDialog()
                                    }) {
                                        Icon(
                                            Icons.Filled.Edit,
                                            contentDescription = stringResource(id = R.string.custom_speed)
                                        )
                                    }
                                }
                            }

                            speedConfig.forEach { (name, speed) ->
                                val checked =
                                    !vm.isCustomSpeed.value && controlVM.curSpeed == speed
                                Text(
                                    textAlign = TextAlign.Center,
                                    text = name,
                                    modifier = Modifier
                                        .defaultMinSize(180.dp, Dp.Unspecified)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            controlVM.setSpeed(speed)
                                            vm.disableCustomSpeed()
                                        }
                                        .padding(16.dp, 14.dp),
                                    color = if (checked) MaterialTheme.colorScheme.primary else Color.White
                                )
                            }
                        }
                    }
                }

                state.curPlayingLine?.let { localPlayLine ->
                    // 选集
                    AnimatedVisibility(
                        showEpisodeWin && controlVM.isFullScreen,
                        enter = slideInHorizontally(tween()) { it },
                        exit = slideOutHorizontally(tween()) { it },

                        ) {

                        val isReverse = vm.isReversal.value

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable(onClick = {
                                    showEpisodeWin = false
                                }, indication = null, interactionSource = remember {
                                    MutableInteractionSource()
                                }),
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

                                for (i in 0 until localPlayLine.sortedEpisodeList.size) {
                                    val s = localPlayLine.sortedEpisodeList[i]
                                    Text(textAlign = TextAlign.Center,
                                        text = s.label,
                                        modifier = Modifier
                                            .defaultMinSize(180.dp, Dp.Unspecified)
                                            .clickable {
                                                vm.onEpisodeClick(localPlayLine, s)
                                            }
                                            .padding(16.dp, 8.dp),
                                        color = if (state.curPlayingEpisode == s) MaterialTheme.colorScheme.primary else Color.White)
                                }
                            }
                        }

                    }
                }
            },
            control = {
                Box(Modifier.fillMaxSize()) {

                    // 手势
                    SimpleGestureController(
                        vm = it,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp, 64.dp),
                        longTouchText = stringResource(id = com.heyanle.easy_i18n.R.string.long_press_fast_forward)
                    )

                    LaunchedEffect(key1 = vm.playingTitle.value) {
                        it.title = vm.playingTitle.value
                    }

                    // 全屏顶部工具栏
                    FullScreenVideoTopBar(
                        vm = it, modifier = Modifier.align(Alignment.TopCenter)
                    )


                    LocalVideoTopBar(vm = it,
                        showTools = state.curPlayingEpisode != null,
                        onBack = { nav.popBackStack() },
                        onSpeed = {
                            showSpeedWin = true
                        },
                        onPlayExt = {
                            vm.flow.value.curPlayingEpisode?.let {
                                vm.externalPlay(it)
                            }
                        })


                    // 底部工具栏
                    SimpleBottomBarWithSeekBar(
                        vm = it,
                        modifier = Modifier.align(Alignment.BottomCenter),
                        paddingValues = if (controlVM.isFullScreen) PaddingValues(
                            16.dp, 0.dp, 16.dp, 8.dp
                        ) else PaddingValues(8.dp, 0.dp)
                    ) {

                        if (it.isFullScreen) {
                            Text(modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable {
                                    showSpeedWin = true
                                }
                                .padding(8.dp),
                                text = stringResource(id = com.heyanle.easy_i18n.R.string.speed),
                                color = Color.White)
                            Text(modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable {
                                    showEpisodeWin = true
                                }
                                .padding(8.dp),
                                text = stringResource(id = com.heyanle.easy_i18n.R.string.episode),
                                color = Color.White)
                        }
                    }

                    // 锁定按钮
                    LockBtn(vm = it)

                    // 加载按钮
                    ProgressBox(vm = it)
                }
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
                                vm, state, lazyGridState
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
                        vm, state, lazyGridState
                    )
                }
            }
        }
    }

    when (val dia = dialog) {
        is LocalPlayViewModel.DialogState.Delete -> {
            EasyDeleteDialog(show = true, onDelete = { vm.onFinalDelete(dia.episodes) }, message = {
                Text(
                    text = stringResource(
                        id = R.string.delete_confirmation_num, dia.episodes.size
                    )
                )
            }, onDismissRequest = {
                vm.onDismissRequest()
            })
        }

        else -> {}
    }

    if (vm.isCustomSpeedDialog.value) {
        val focusRequest = remember {
            FocusRequester()
        }
        val text = remember {
            mutableStateOf(vm.customSpeed.value.let { if (it > 0) it else 1 }.toString())
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
                vm.isCustomSpeedDialog.value = false
            },
            title = {
                Text(text = stringRes(R.string.custom_speed))
            },
            text = {
                OutlinedTextField(
                    modifier = Modifier.focusRequester(focusRequest),
                    value = text.value,
                    onValueChange = {
                        text.value = it
                    })
            },
            confirmButton = {
                TextButton(onClick = {
                    val tex = text.value
                    val f = tex.toFloatOrNull() ?: -1f
                    if (f <= 0) {
                        vm.setCustomSpeed(-1f)
                        if (vm.isCustomSpeed.value) {
                            controlVM.setSpeed(1f)
                            vm.isCustomSpeed.value = false
                        }
                        stringRes(R.string.please_input_right_speed).moeSnackBar()
                    } else {
                        vm.setCustomSpeed(f)
                        if (vm.isCustomSpeed.value) {
                            controlVM.setSpeed(f)
                        }
                    }
                    vm.isCustomSpeedDialog.value = false
                }) {
                    Text(text = stringRes(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    vm.isCustomSpeedDialog.value = false
                }) {
                    Text(text = stringRes(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun PlayContent(
    vm: LocalPlayViewModel,
    state: LocalPlayViewModel.LocalPlayState,
    lazyGridState: LazyGridState,
) {
    Column {
        Surface(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                LocalPlayUI(vm = vm, state)
                FastScrollToTopFab(listState = lazyGridState)

                if (state.deleteModePlayLine != null) {
                    Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.fillMaxSize()) {
                        val up =
                            remember { derivedStateOf { lazyGridState.firstVisibleItemIndex > 10 } }
                        val downPadding by animateDpAsState(
                            if (up.value) 80.dp else 40.dp,
                            label = ""
                        )
                        ExtendedFloatingActionButton(modifier = Modifier.padding(
                            16.dp,
                            downPadding
                        ),
                            text = {
                                Text(text = stringResource(id = R.string.delete_selection))
                            },
                            icon = {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = stringResource(id = R.string.delete_selection)
                                )
                            },
                            onClick = {
                                vm.onDeleteDialog()
                            })
                    }
                }
            }
        }
    }
}

@Composable
fun LocalPlayUI(
    vm: LocalPlayViewModel,
    state: LocalPlayViewModel.LocalPlayState,
) {

    val cartoon = state.localCartoon
    val nav = LocalNavController.current
    if (state.isLoading || cartoon == null) {
        LoadingPage(
            modifier = Modifier.fillMaxSize()
        )
    } else {
        CartoonPlayDetailed(modifier = Modifier.fillMaxSize(),
            state = state,
            cartoon = cartoon,
            onLineSelect = {
                vm.onPlayLineSelect(it)
            },
            onEpisodeClick = { line, episode ->
                vm.onEpisodeClick(line, episode)
            },
            onEpisodeDeleteChange = { item, select ->
                vm.onDeleteClick(item)
            },
            sortState = vm.sortState,
            onSortChange = { key, isReverse ->
                vm.onSortChange(key, isReverse)
            },
            onDeleteSelectionRevert = {
                vm.onDeleteSelectionRevert()
            },
            onSearch = {
                nav.navigationSearch(cartoon.cartoonTitle, cartoon.cartoonSource)
            },
            onDetailed = {
                nav.navigationDetailed(cartoon.cartoonId, cartoon.cartoonUrl, cartoon.cartoonSource)
            },
            onDelete = {
                state.curPlayingLine?.let {
                    vm.deleteMode(it)
                }
            }

        )
    }
}

@Composable
fun CartoonPlayDetailed(
    modifier: Modifier,
    state: LocalPlayViewModel.LocalPlayState,
    sortState: SortState<LocalEpisode>,
    cartoon: LocalCartoon,
    listState: LazyGridState = rememberLazyGridState(),
    onLineSelect: (Int) -> Unit,
    onEpisodeClick: (LocalPlayLineWrapper, LocalEpisode) -> Unit,
    onEpisodeDeleteChange: (LocalEpisode, Boolean) -> Unit,
    onSortChange: (String, Boolean) -> Unit,
    onDeleteSelectionRevert: () -> Unit,
    onSearch: () -> Unit,
    onDetailed: () -> Unit,
    onDelete: () -> Unit,
) {
    var isSortShow by remember {
        mutableStateOf(false)
    }
    Column(modifier = modifier) {

        var isExpended by remember {
            mutableStateOf(false)
        }

        LazyVerticalGrid(
            modifier = Modifier.weight(1f),
            columns = GridCells.Adaptive(128.dp),
            state = listState,
            contentPadding = PaddingValues(0.dp, 0.dp, 0.dp, 96.dp)
        ) {
            item(span = {
                // LazyGridItemSpanScope:
                // maxLineSpan
                GridItemSpan(maxLineSpan)
            }) {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        isExpended = !isExpended
                    }
                    .padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    AnimatedContent(
                        modifier = Modifier, targetState = isExpended, transitionSpec = {
                            fadeIn(
                                animationSpec = tween(
                                    300,
                                    delayMillis = 300
                                )
                            ) togetherWith fadeOut(animationSpec = tween(300, delayMillis = 0))
                        }, label = ""
                    ) {
                        if (it) {
                            CartoonDescCard(cartoon)
                        } else {
                            Row(
                                modifier = Modifier
                            ) {
                                OkImage(
                                    modifier = Modifier
                                        .width(95.dp)
                                        .aspectRatio(19 / 13.5F)
                                        .clip(RoundedCornerShape(4.dp)),
                                    image = cartoon.cartoonCover,
                                    contentDescription = cartoon.cartoonTitle
                                )
                                Spacer(modifier = Modifier.size(8.dp))
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        modifier = Modifier,
                                        text = (cartoon.cartoonTitle),
                                        style = MaterialTheme.typography.titleMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Spacer(modifier = Modifier.size(4.dp))
                                    Text(
                                        modifier = Modifier,
                                        text = (cartoon.cartoonDescription),
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
                        modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isExpended) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            contentDescription = cartoon.cartoonTitle
                        )
                    }
                }
            }
            item(span = {
                // LazyGridItemSpanScope:
                // maxLineSpan
                GridItemSpan(maxLineSpan)
            }) {
                Column {
                    LocalCartoonActions(
                        isDeleteMode = state.deleteModePlayLine != null,
                        onSearch = onSearch,
                        onNet = onDetailed,
                        onDelete = onDelete,
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Divider()
                }
            }

            if (cartoon.playLines.isEmpty()) {
                item(span = {
                    // LazyGridItemSpanScope:
                    // maxLineSpan
                    GridItemSpan(maxLineSpan)
                }) {
                    EmptyPage(
                        modifier = Modifier.fillMaxWidth(),
                        emptyMsg = stringResource(id = R.string.no_play_line)
                    )
                }
            } else {
                item(span = {
                    // LazyGridItemSpanScope:
                    // maxLineSpan
                    GridItemSpan(maxLineSpan)
                }) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        if (state.deleteModePlayLine == null) {
                            if (state.sorted.size == 1) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = state.sorted.getOrNull(0)?.playLine?.label?.ifEmpty {
                                        null
                                    } ?: stringResource(
                                        id = R.string.play_list
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                ScrollableTabRow(modifier = Modifier
                                    .weight(1f)
                                    .padding(0.dp, 8.dp),
                                    selectedTabIndex = state.selectPlayLine,
                                    edgePadding = 0.dp,
                                    indicator = {
                                        if (state.selectPlayLine >= 0 && state.selectPlayLine < it.size) {
                                            TabIndicator(
                                                currentTabPosition = it[state.selectPlayLine]
                                            )
                                        }
                                    },
                                    divider = { }) {
                                    state.sorted.forEachIndexed { index, localPlayLineWrapper ->
                                        val localPlayLine = localPlayLineWrapper.playLine
                                        Tab(selected = state.selectPlayLine == index,
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
                                                    Text(text = localPlayLine.label)

                                                    if (state.curPlayingLine == localPlayLineWrapper) {
                                                        Box(
                                                            modifier = Modifier
                                                                .padding(
                                                                    2.dp, 0.dp, 0.dp, 0.dp
                                                                )
                                                                .size(8.dp)
                                                                .background(
                                                                    MaterialTheme.colorScheme.primary,
                                                                    CircleShape
                                                                )
                                                        )
                                                    }
                                                }
                                            })
                                    }
                                }
                            }

                        } else {
                            // 删除模式的播放线路
                            Text(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(16.dp, 0.dp),
                                text = stringResource(id = com.heyanle.easy_i18n.R.string.select_to_delete)
                            )
                            IconButton(onClick = {
                                onDeleteSelectionRevert()
                            }) {
                                Icon(
                                    Icons.Filled.SelectAll,
                                    stringResource(id = com.heyanle.easy_i18n.R.string.select_all),
                                )
                            }
                        }

                        IconButton(onClick = {
                            // TODO
                            isSortShow = true

                        }) {
                            val curKey = sortState.current.collectAsState()
                            Icon(
                                Icons.Filled.Sort,
                                stringResource(id = R.string.sort),
                                tint = if (curKey.value != PlayLineWrapper.SORT_DEFAULT_KEY) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                            )

                            SortDropDownMenu(isShow = isSortShow,
                                sortState = sortState,
                                onClick = { sort, state ->
                                    when (state) {
                                        SortState.STATUS_OFF -> {
                                            onSortChange(sort.id, false)
                                        }

                                        SortState.STATUS_ON -> {
                                            onSortChange(sort.id, true)
                                        }

                                        else -> {
                                            onSortChange(sort.id, false)
                                        }
                                    }
                                }) {
                                isSortShow = false
                            }
                        }
                    }
                }

                state.sorted.getOrNull(state.selectPlayLine)?.let { localPlayLine ->
                    val episode = localPlayLine.sortedEpisodeList
                    items(episode.size) {
                        val index = it
                        val item = episode[index]
                        val select = item == state.curPlayingEpisode
                        Row(
                            modifier = Modifier
                                .padding(4.dp)
                                .fillMaxWidth()
                                //.then(modifier)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (select && state.deleteModePlayLine == null) MaterialTheme.colorScheme.secondary else Color.Transparent)
                                .run {
                                    if (select && state.deleteModePlayLine == null) {
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
                                    if (state.deleteModePlayLine == null) {
                                        onEpisodeClick(
                                            localPlayLine, item
                                        )
                                    } else {
                                        onEpisodeDeleteChange(
                                            item, !state.deleteSelection.contains(item)
                                        )
                                    }

                                }
                                .padding(8.dp),
                        ) {
                            Text(
                                color = if (select && state.deleteModePlayLine == null) MaterialTheme.colorScheme.onSecondary else Color.Unspecified,
                                text = item.label,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(Modifier.weight(1f))
                            if (state.deleteModePlayLine != null) {
                                Spacer(Modifier.size(4.dp))
                                Checkbox(checked = state.deleteSelection.contains(item),
                                    onCheckedChange = {
                                        onEpisodeDeleteChange(item, it)
                                    })
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CartoonDescCard(
    cartoon: LocalCartoon
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Top,
        ) {
            OkImage(
                modifier = Modifier
                    .width(95.dp)
                    .aspectRatio(19 / 27F)
                    .clip(RoundedCornerShape(4.dp)),
                image = cartoon.cartoonCover,
                contentDescription = cartoon.cartoonTitle
            )

            Spacer(modifier = Modifier.size(8.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    modifier = Modifier,
                    text = cartoon.cartoonTitle,
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
                            Text(modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .clickable {}
                                .padding(8.dp, 4.dp),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.W900,
                                text = it,
                                fontSize = 12.sp)
                        }
                    }
                }
            }
        }
        Text(modifier = Modifier.padding(8.dp), text = cartoon.cartoonDescription)
    }
}

@Composable
fun LocalCartoonActions(
    isDeleteMode: Boolean,
    onSearch: () -> Unit,
    onNet: () -> Unit,
    onDelete: () -> Unit,
) {
    ActionRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 搜索同名番
        Action(icon = {
            Icon(
                Icons.Filled.Search, stringResource(id = com.heyanle.easy_i18n.R.string.search)
            )
        }, msg = {
            Text(
                text = stringResource(id = com.heyanle.easy_i18n.R.string.search),
                fontSize = 12.sp
            )
        }, onClick = onSearch
        )

        // 打开原网站
        Action(icon = {
            Icon(
                Icons.Filled.More, stringResource(id = com.heyanle.easy_i18n.R.string.detailed)
            )
        }, msg = {
            Text(
                text = stringResource(id = com.heyanle.easy_i18n.R.string.detailed),
                fontSize = 12.sp
            )
        }, onClick = onNet
        )

        // 删除视频
        Action(icon = {
            Icon(
                Icons.Filled.Delete, stringResource(id = com.heyanle.easy_i18n.R.string.delete),
                tint = if (isDeleteMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
            )
        }, msg = {
            Text(
                text = stringResource(id = com.heyanle.easy_i18n.R.string.delete),
                fontSize = 12.sp,
                color = if (isDeleteMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
            )
        }, onClick = onDelete
        )
    }
}

@Composable
fun LocalVideoTopBar(
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
                        Icons.Filled.Airplay, tint = Color.White, contentDescription = null
                    )
                }
            }


        }
    }
}
