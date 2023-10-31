package com.heyanle.easybangumi4.ui.local_play

import android.app.Activity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.More
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WifiProtectedSetup
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.heyanle.easybangumi4.download.entity.LocalCartoon
import com.heyanle.easybangumi4.download.entity.LocalEpisode
import com.heyanle.easybangumi4.download.entity.LocalPlayLine
import com.heyanle.easybangumi4.navigationSearch
import com.heyanle.easybangumi4.ui.cartoon_play.FullScreenVideoTopBar
import com.heyanle.easybangumi4.ui.cartoon_play.speedConfig
import com.heyanle.easybangumi4.ui.common.Action
import com.heyanle.easybangumi4.ui.common.ActionRow
import com.heyanle.easybangumi4.ui.common.EmptyPage
import com.heyanle.easybangumi4.ui.common.FastScrollToTopFab
import com.heyanle.easybangumi4.ui.common.LoadingPage
import com.heyanle.easybangumi4.ui.common.OkImage
import com.heyanle.easybangumi4.ui.common.TabIndicator
import com.heyanle.easybangumi4.utils.isCurPadeMode
import com.heyanle.easybangumi4.utils.loge
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

/**
 * Created by heyanlin on 2023/9/25.
 */
@Composable
fun LocalPlay(
    uuid: String
) {
    val isPad = isCurPadeMode()
    val vm = viewModel<LocalPlayViewModel>(factory = LocalPlayViewModelFactory(uuid = uuid))
    val controlVM = ControlViewModelFactory.viewModel(Injekt.get<ExoPlayer>().let {
        it.toString().loge("ExoPlayer-----")
        it
    }, isPad)
    val nav = LocalNavController.current

    val state by vm.flow.collectAsState()

    var showEpisodeWin by remember {
        mutableStateOf(false)
    }

    var showSpeedWin by remember {
        mutableStateOf(false)
    }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        val lazyGridState = rememberLazyGridState()
        EasyPlayerScaffoldBase(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            vm = controlVM,
            isPadMode = isPad,
            contentWeight = 0.5f,
            videoFloat = { model ->
                val ctx = LocalContext.current as Activity
                LaunchedEffect(key1 = controlVM.controlState) {
                    if (controlVM.controlState == ControlViewModel.ControlState.Ended) {
                        vm.tryNext()
                        stringRes(com.heyanle.easy_i18n.R.string.try_play_next).toast()
                    }
                }


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
                                onClick = {
                                    showSpeedWin = false
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
                                .clickable(
                                    onClick = {
                                        showEpisodeWin = false
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

                                for (i in 0 until localPlayLine.list.size) {
                                    val index =
                                        if (isReverse) localPlayLine.list.size - 1 - i else i
                                    val s = localPlayLine.list[index]
                                    Text(
                                        textAlign = TextAlign.Center,
                                        text = s.label,
                                        modifier = Modifier
                                            .defaultMinSize(180.dp, Dp.Unspecified)
                                            .clickable {
                                                vm.onEpisodeClick(localPlayLine, s)
                                            }
                                            .padding(16.dp, 8.dp),
                                        color = if (state.curPlayingEpisode == s) MaterialTheme.colorScheme.primary else Color.White
                                    )
                                }
                            }
                        }

                    }
                }

//            if (!model.isFullScreen) {
//                FilledIconButton(
//                    modifier = Modifier.padding(8.dp),
//                    colors = IconButtonDefaults.iconButtonColors(
//                        containerColor = Color.Black.copy(0.6f),
//                        contentColor = Color.White
//                    ),
//                    onClick = {
//                        nav.popBackStack()
//                    }) {
//                    Icon(
//                        imageVector = Icons.Filled.KeyboardArrowLeft,
//                        stringResource(id = com.heyanle.easy_i18n.R.string.back)
//                    )
//                }
//            }
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

                    LaunchedEffect(key1 = vm.playingTitle.value){
                        it.title = vm.playingTitle.value
                    }

                    // 全屏顶部工具栏
                    FullScreenVideoTopBar(
                        vm = it,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                    )


                    LocalVideoTopBar(
                        vm = it,
                        showTools = state.curPlayingEpisode != null,
                        onBack = { nav.popBackStack() },
                        onSpeed = {
                            showSpeedWin = true
                        },
                        onPlayExt = {
                            vm.flow.value.curPlayingEpisode?.let {
                                vm.externalPlay(it)
                            }
                        }
                    )


                    // 底部工具栏
                    SimpleBottomBar(
                        vm = it,
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
                                        showSpeedWin = true
                                    }
                                    .padding(8.dp),
                                text = stringResource(id = com.heyanle.easy_i18n.R.string.speed),
                                color = Color.White
                            )
                            Text(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable {
                                        showEpisodeWin = true
                                    }
                                    .padding(8.dp),
                                text = stringResource(id = com.heyanle.easy_i18n.R.string.episode),
                                color = Color.White
                            )
                        }
                    }

                    // 锁定按钮
                    LockBtn(vm = it)

                    // 加载按钮
                    ProgressBox(vm = it)
                }
            }
        ) {
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
                                vm,
                                state,
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
                        vm,
                        state,
                        lazyGridState
                    )
                }
            }
        }
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
                modifier = Modifier
                    .fillMaxSize()
            ) {
                LocalPlayUI(vm = vm, state)
                FastScrollToTopFab(listState = lazyGridState)
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
        CartoonPlayDetailed(
            modifier = Modifier.fillMaxSize(),
            state = state,
            cartoon = cartoon,
            onLineSelect = {
                vm.onPlayLineSelect(it)
            },
            onEpisodeClick = { line, episode ->
                vm.onEpisodeClick(line, episode)
            },
            isReversal = vm.isReversal.value,
            onReversal = {
                vm.isReversal.value = it
            },
            onSearch = {
                nav.navigationSearch(cartoon.cartoonTitle, cartoon.cartoonSource)
            }) {

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun CartoonPlayDetailed(
    modifier: Modifier,
    state: LocalPlayViewModel.LocalPlayState,
    cartoon: LocalCartoon,
    listState: LazyGridState = rememberLazyGridState(),
    onLineSelect: (Int) -> Unit,
    onEpisodeClick: (LocalPlayLine, LocalEpisode) -> Unit,

    isReversal: Boolean = false,
    onReversal: (Boolean) -> Unit,// 外界不需要处理 playLines 的翻转，内部处理

    onSearch: () -> Unit,
    onDetailed: () -> Unit,
) {

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
            item(
                span = {
                    // LazyGridItemSpanScope:
                    // maxLineSpan
                    GridItemSpan(maxLineSpan)
                }
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
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
                        modifier = Modifier
                            .fillMaxWidth(), contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isExpended) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            contentDescription = cartoon.cartoonTitle
                        )
                    }
                }
            }

            item(
                span = {
                    // LazyGridItemSpanScope:
                    // maxLineSpan
                    GridItemSpan(maxLineSpan)
                }
            ) {
                Column {
                    LocalCartoonActions(
                        onSearch = onSearch,
                        onNet = onDetailed,
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Divider()
                }
            }

            if (cartoon.playLines.isEmpty()) {
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
                        ScrollableTabRow(
                            modifier = Modifier
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
                            divider = { }

                        ) {
                            cartoon.playLines.forEachIndexed { index, localPlayLine ->
                                Tab(
                                    selected = state.selectPlayLine == index,
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

                                            if (state.curPlayingLine == localPlayLine) {
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



                        IconButton(onClick = {
                            onReversal(!isReversal)
                        }) {
                            Icon(
                                Icons.Filled.WifiProtectedSetup,
                                stringResource(id = R.string.reverse),
                                tint = if (isReversal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }

                cartoon.playLines.getOrNull(state.selectPlayLine)?.let { localPlayLine ->
                    val episode = localPlayLine.list
                    items(episode.size) {
                        val index =
                            if (isReversal) episode.size - 1 - it else it
                        val item = episode[index]
                        val select = item == state.curPlayingEpisode
                        Row(
                            modifier = Modifier
                                .padding(4.dp)
                                .fillMaxWidth()
                                //.then(modifier)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (select) MaterialTheme.colorScheme.secondary else Color.Transparent)
                                .run {
                                    if (select) {
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
                                    onEpisodeClick(
                                        localPlayLine, item
                                    )

                                }
                                .padding(8.dp),
                        ) {

                            Text(
                                color = if (select) MaterialTheme.colorScheme.onSecondary else Color.Unspecified,
                                text = item.label,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
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
        Text(modifier = Modifier.padding(8.dp), text = cartoon.cartoonDescription)
    }
}

@Composable
fun LocalCartoonActions(
    onSearch: () -> Unit,
    onNet: () -> Unit,
) {
    ActionRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 搜索同名番
        Action(
            icon = {
                Icon(
                    Icons.Filled.Search,
                    stringResource(id = com.heyanle.easy_i18n.R.string.search)
                )
            },
            msg = {
                Text(
                    text = stringResource(id = com.heyanle.easy_i18n.R.string.search),
                    fontSize = 12.sp
                )
            },
            onClick = onSearch
        )

        // 打开原网站
        Action(
            icon = {
                Icon(
                    Icons.Filled.More,
                    stringResource(id = com.heyanle.easy_i18n.R.string.detailed)
                )
            },
            msg = {
                Text(
                    text = stringResource(id = com.heyanle.easy_i18n.R.string.detailed),
                    fontSize = 12.sp
                )
            },
            onClick = onNet
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
                        Icons.Filled.Airplay,
                        tint = Color.White,
                        contentDescription = null
                    )
                }
            }


        }
    }
}