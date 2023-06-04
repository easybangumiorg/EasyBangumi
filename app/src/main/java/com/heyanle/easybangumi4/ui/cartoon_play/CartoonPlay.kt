package com.heyanle.easybangumi4.ui.cartoon_play

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.bangumi_source_api.api.Source
import com.heyanle.bangumi_source_api.api.entity.Cartoon
import com.heyanle.bangumi_source_api.api.entity.CartoonSummary
import com.heyanle.bangumi_source_api.api.entity.PlayLine
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.R
import com.heyanle.easybangumi4.navigationSearch
import com.heyanle.easybangumi4.ui.common.*
import com.heyanle.easybangumi4.utils.TODO
import com.heyanle.easybangumi4.utils.isCurPadeMode
import com.heyanle.easybangumi4.utils.loge
import com.heyanle.easybangumi4.utils.openUrl
import kotlinx.coroutines.launch
import loli.ball.easyplayer2.*
import java.util.*

/**
 * Created by HeYanLe on 2023/3/4 16:34.
 * https://github.com/heyanLE
 */

@Composable
fun CartoonPlay(
    id: String,
    source: String,
    url: String,
    enterData: CartoonPlayViewModel.EnterData? = null
) {
    val summary = CartoonSummary(id, source, url)
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        DetailedContainer(sourceKey = source) { _, sou, det ->
            val detailedVM =
                viewModel<DetailedViewModel>(factory = DetailedViewModelFactory(summary, det))
            val cartoonPlayViewModel = viewModel<CartoonPlayViewModel>()
            CartoonPlay(
                detailedVM = detailedVM,
                cartoonPlayVM = cartoonPlayViewModel,
                cartoonSummary = summary,
                source = sou,
                enterData = enterData
            )
        }
    }
}

private val speedConfig = linkedMapOf(
    "0.5X" to 0.5f,
    "0.75X" to 0.75f,
    "1.0X" to 1f,
    "1.25X" to 1.25f,
    "1.5X" to 1.5f,
    "2.0X" to 2f,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartoonPlay(
    detailedVM: DetailedViewModel,
    cartoonPlayVM: CartoonPlayViewModel,
    cartoonSummary: CartoonSummary,
    source: Source,
    enterData: CartoonPlayViewModel.EnterData? = null,
) {
    val isPad = isCurPadeMode()

    val controlVM = ControlViewModelFactory.viewModel(CartoonPlayingManager.exoPlayer, isPad)
    val nav = LocalNavController.current

    LaunchedEffect(key1 = detailedVM.detailedState) {
        val sta = detailedVM.detailedState
        if (sta is DetailedViewModel.DetailedState.None) {
            detailedVM.load()
        } else if (sta is DetailedViewModel.DetailedState.Info) {
            // 加载好之后进入 播放环节
            cartoonPlayVM.onDetailedLoaded(cartoonSummary, sta, enterData)
        }
    }

    LaunchedEffect(key1 = Unit) {
        detailedVM.checkUpdate()
    }

    DisposableEffect(key1 = Unit) {
        onDispose {
            CartoonPlayingManager.trySaveHistory()
        }
    }

    var showEpisodeWin by remember {
        mutableStateOf(false)
    }

    var showSpeedWin by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(key1 = controlVM.curSpeed){
        controlVM.curSpeed.loge("CartoonPlay")
    }
    val lazyGridState = rememberLazyGridState()
    EasyPlayerScaffoldBase(
        modifier = Modifier.fillMaxSize(),
        vm = controlVM,
        isPadMode = isPad,
        contentWeight = 0.5f,
        videoFloat = { model ->
            val ctx = LocalContext.current as Activity
            LaunchedEffect(key1 = CartoonPlayingManager.state) {
                when (CartoonPlayingManager.state) {
                    is CartoonPlayingManager.PlayingState.Playing -> {
                        model.onPrepare()
                        // CartoonPlayingManager.trySaveHistory()
                    }

                    is CartoonPlayingManager.PlayingState.Loading -> {}
                    is CartoonPlayingManager.PlayingState.Error -> {
                        model.onFullScreen(false, false, ctx)
                    }

                    else -> {}
                }
            }
            when (val state = CartoonPlayingManager.state) {
                is CartoonPlayingManager.PlayingState.Playing -> {}
                is CartoonPlayingManager.PlayingState.Loading -> {
                    LoadingPage(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(
                                onClick = {

                                },
                                indication = null,
                                interactionSource = remember {
                                    MutableInteractionSource()
                                }
                            )
                    )
                }

                is CartoonPlayingManager.PlayingState.Error -> {
                    ErrorPage(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black),
                        errorMsg = state.errMsg,
                        errorMsgColor = Color.White.copy(0.6f),
                        clickEnable = true,
                        other = {
                            Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.click_to_retry))
                        },
                        onClick = {
                            CartoonPlayingManager.defaultScope.launch {
                                CartoonPlayingManager.refresh()
                            }
                        }
                    )
                }

                else -> {}
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
                            .fillMaxHeight()
                            .background(Color.Black.copy(0.6f))
                            .verticalScroll(rememberScrollState())
                    ) {
                        speedConfig.forEach {

                            Text(
                                text = it.key,
                                modifier = Modifier
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


            if (!model.isFullScreen) {
                FilledIconButton(
                    modifier = Modifier.padding(8.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Black.copy(0.6f),
                        contentColor = Color.White
                    ),
                    onClick = {
                        nav.popBackStack()
                    }) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowLeft,
                        stringResource(id = com.heyanle.easy_i18n.R.string.back)
                    )
                }
            }
        },
        control = {
            Box(Modifier.fillMaxSize()) {

                // 手势
                SimpleGestureController(
                    vm = it,
                    modifier = Modifier.fillMaxSize(),
                    longTouchText = stringResource(id = com.heyanle.easy_i18n.R.string.long_press_fast_forward)
                )

                // 顶部工具栏
                SimpleTopBar(
                    vm = it,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                )

                // 底部工具栏
                SimpleBottomBar(
                    vm = it,
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    Text(
                        modifier = Modifier.clickable {
                            showSpeedWin = true
                        },
                        text = stringResource(id = com.heyanle.easy_i18n.R.string.speed))
                }

                // 锁定按钮
                LockBtn(vm = it)

                // 加载按钮
                ProgressBox(vm = it)
            }
        }
    ) {
        val contentColumn: @Composable () -> Unit = {
            Column {
                Surface(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onBackground
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .navigationBarsPadding()
                    ) {
                        CartoonPlayUI(
                            detailedVM = detailedVM,
                            cartoonPlayVM = cartoonPlayVM,
                            listState = lazyGridState
                        )
                        FastScrollToTopFab(listState = lazyGridState)
                    }
                }
            }
        }
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
                        contentColumn()
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
                contentColumn()
            }
        }
    }
}


@Composable
fun CartoonPlayUI(
    detailedVM: DetailedViewModel,
    cartoonPlayVM: CartoonPlayViewModel,
    listState: LazyGridState = rememberLazyGridState()
) {

    when (val detailedState = detailedVM.detailedState) {
        is DetailedViewModel.DetailedState.Info -> {
            CartoonPlayPage(detailedVM, cartoonPlayVM, detailedState, listState)
        }

        is DetailedViewModel.DetailedState.Error -> {
            ErrorPage(
                modifier = Modifier.fillMaxSize(),
                errorMsg = detailedState.errorMsg,
                clickEnable = true,
                onClick = {
                    detailedVM.load()
                },
                other = { Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.click_to_retry)) }
            )
        }

        is DetailedViewModel.DetailedState.Loading -> {
            LoadingPage(
                modifier = Modifier.fillMaxSize()
            )
        }

        else -> Unit
    }

}


@Composable
fun CartoonPlayPage(
    detailedVM: DetailedViewModel,
    cartoonPlayVM: CartoonPlayViewModel,
    detailedState: DetailedViewModel.DetailedState.Info,
    listState: LazyGridState = rememberLazyGridState()
) {
    val nav = LocalNavController.current
    CartoonPlayDetailed(
        modifier = Modifier.fillMaxSize(),
        cartoon = detailedState.detail,
        playLines = detailedState.playLine,
        selectLineIndex = cartoonPlayVM.selectedLineIndex,
        playingPlayLine = CartoonPlayingManager.state.playLine(),
        playingEpisode = CartoonPlayingManager.state.episode(),
        listState = listState,
        showPlayLine = detailedState.isShowPlayLine,
        onLineSelect = {
            cartoonPlayVM.selectedLineIndex = it
        },
        onEpisodeClick = { playLineIndex, playLine, episode ->
            if (CartoonPlayingManager.state.playLine() == playLine) {
                CartoonPlayingManager.defaultScope.launch {
                    CartoonPlayingManager.changeEpisode(episode, 0L)
                }
            } else {
                CartoonPlayingManager.defaultScope.launch {
                    CartoonPlayingManager.changeLine(
                        detailedState.detail.source,
                        detailedState.detail,
                        playLineIndex,
                        playLine,
                        defaultEpisode = episode,
                        defaultProgress = 0L
                    )
                }
            }
        },
        isStar = detailedVM.isStar,
        onStar = {
            detailedState.playLine
            detailedVM.setCartoonStar(it, detailedState.detail, detailedState.playLine)
        },
        onSearch = {
            nav.navigationSearch(detailedState.detail.title, detailedState.detail.source)
            //TODO("搜索同名番")
        },
        onWeb = {
            runCatching {
                detailedState.detail.url.openUrl()
            }.onFailure {
                it.printStackTrace()
            }
        },
        onDlna = {
            TODO("投屏")
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun CartoonPlayDetailed(
    modifier: Modifier,
    cartoon: Cartoon,

    playLines: List<PlayLine>,
    selectLineIndex: Int,
    playingPlayLine: PlayLine?,
    playingEpisode: Int,
    listState: LazyGridState = rememberLazyGridState(),
    onLineSelect: (Int) -> Unit,
    onEpisodeClick: (Int, PlayLine, Int) -> Unit,

    showPlayLine: Boolean = true,

    isStar: Boolean,
    onStar: (Boolean) -> Unit,
    onSearch: () -> Unit,
    onWeb: () -> Unit,
    onDlna: () -> Unit,
) {

    // 将非空的 播放线路 下标存成离散序列
    val unEmptyLinesIndex = remember(playLines) {
        arrayListOf<Int>().apply {
            playLines.forEachIndexed { index, playLine ->
                if (playLine.episode.isNotEmpty()) {
                    add(index)
                }
            }
        }
    }

    LaunchedEffect(key1 = playLines, key2 = selectLineIndex) {
        if (!unEmptyLinesIndex.contains(selectLineIndex) && unEmptyLinesIndex.isNotEmpty()) {
            Arrays.toString(unEmptyLinesIndex.toArray()).loge("CartoonPlay")
            onLineSelect(unEmptyLinesIndex[0])
        }
    }



    Box(modifier = modifier) {

        var isExpended by remember {
            mutableStateOf(false)
        }

        LazyVerticalGrid(
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
                            fadeIn(animationSpec = tween(300, delayMillis = 300)) with
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
                                    image = cartoon.coverUrl,
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
                        onStar = onStar,
                        onSearch = onSearch,
                        onWeb = onWeb,
                        onDlna = onDlna
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Divider()
                }
            }

            if (unEmptyLinesIndex.isEmpty()) {
                item(
                    span = {
                        // LazyGridItemSpanScope:
                        // maxLineSpan
                        GridItemSpan(maxLineSpan)
                    }
                ) {
                    EmptyPage(
                        modifier = Modifier.fillMaxWidth(),
                        emptyMsg = stringResource(id = com.heyanle.easy_i18n.R.string.no_play_line)
                    )
                }
            } else {

                if (showPlayLine) {
                    item(
                        span = {
                            // LazyGridItemSpanScope:
                            // maxLineSpan
                            GridItemSpan(maxLineSpan)
                        }
                    ) {

                        ScrollableTabRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(0.dp, 8.dp),
                            selectedTabIndex = 0.coerceAtLeast(
                                unEmptyLinesIndex.indexOf(
                                    selectLineIndex
                                )
                            ),
                            edgePadding = 0.dp,
                            divider = {
                            }

                        ) {
                            unEmptyLinesIndex.forEach { index ->
                                val playLine = playLines[index]
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

                                            if (playLines[index] == playingPlayLine) {
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
                    }
                }


                if (selectLineIndex >= 0 && selectLineIndex < playLines.size && unEmptyLinesIndex.contains(
                        selectLineIndex
                    )
                ) {
                    itemsIndexed(playLines[selectLineIndex].episode) { index, item ->

                        val select =
                            playLines[selectLineIndex] == playingPlayLine && index == playingEpisode
                        Column(
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
                                        selectLineIndex,
                                        playLines[selectLineIndex],
                                        index
                                    )
                                }
                                .padding(8.dp),
                        ) {

                            Text(
                                color = if (select) MaterialTheme.colorScheme.onSecondary else Color.Unspecified,
                                text = item,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
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
    cartoon: Cartoon
) {


    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Top,
        ) {
            OkImage(
                modifier = Modifier
                    .width(95.dp)
                    .aspectRatio(19 / 27F)
                    .clip(RoundedCornerShape(4.dp)),
                image = cartoon.coverUrl,
                contentDescription = cartoon.title
            )

            Spacer(modifier = Modifier.size(8.dp))

            Column {
                Text(
                    modifier = Modifier,
                    text = cartoon.title,
                    style = MaterialTheme.typography.titleLarge,
                    overflow = TextOverflow.Ellipsis,
                )


                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    cartoon.getGenres()?.forEach {
                        Surface(
                            shape = CircleShape,
                            modifier =
                            Modifier
                                .padding(2.dp, 8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                        ) {
                            Text(
                                modifier = Modifier
                                    .clip(CircleShape)
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

        Text(modifier = Modifier.padding(8.dp), text = cartoon.description ?: cartoon.intro ?: "")
    }

}

@Composable
fun CartoonActions(
    isStar: Boolean,
    onStar: (Boolean) -> Unit,
    onSearch: () -> Unit,
    onWeb: () -> Unit,
    onDlna: () -> Unit,
) {
    ActionRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        val starIcon =
            if (isStar) Icons.Filled.Star else Icons.Filled.StarOutline
        val starTextId =
            if (isStar) com.heyanle.easy_i18n.R.string.stared else com.heyanle.easy_i18n.R.string.click_star
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
                    stringResource(id = com.heyanle.easy_i18n.R.string.search_same_bangumi)
                )
            },
            msg = {
                Text(
                    text = stringResource(id = com.heyanle.easy_i18n.R.string.search_same_bangumi),
                    fontSize = 12.sp
                )
            },
            onClick = onSearch
        )

        // 打开原网站
        Action(
            icon = {
                Icon(
                    painterResource(id = R.drawable.ic_webview_24dp),
                    stringResource(id = com.heyanle.easy_i18n.R.string.open_source_url)
                )
            },
            msg = {
                Text(
                    text = stringResource(id = com.heyanle.easy_i18n.R.string.open_source_url),
                    fontSize = 12.sp
                )
            },
            onClick = onWeb
        )

        // 投屏
        Action(
            icon = {
                Icon(
                    Icons.Filled.CastConnected,
                    stringResource(id = com.heyanle.easy_i18n.R.string.screen_cast)
                )
            },
            msg = {
                Text(
                    text = stringResource(id = com.heyanle.easy_i18n.R.string.screen_cast),
                    fontSize = 12.sp
                )
            },
            onClick = onDlna
        )
    }


}






