package com.heyanle.easybangumi4.ui.cartoon_play

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.heyanle.bangumi_source_api.api.Source
import com.heyanle.bangumi_source_api.api.entity.Cartoon
import com.heyanle.bangumi_source_api.api.entity.CartoonSummary
import com.heyanle.bangumi_source_api.api.entity.PlayLine
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.R
import com.heyanle.easybangumi4.ui.common.Action
import com.heyanle.easybangumi4.ui.common.ActionRow
import com.heyanle.easybangumi4.ui.common.DetailedContainer
import com.heyanle.easybangumi4.ui.common.EmptyPage
import com.heyanle.easybangumi4.ui.common.ErrorPage
import com.heyanle.easybangumi4.ui.common.FastScrollToTopFab
import com.heyanle.easybangumi4.ui.common.LoadingPage
import com.heyanle.easybangumi4.ui.common.OkImage
import com.heyanle.easybangumi4.ui.common.player.ControlViewModel
import com.heyanle.easybangumi4.ui.common.player.ControlViewModelFactory
import com.heyanle.easybangumi4.ui.common.player.EasyPlayerScaffold
import com.heyanle.easybangumi4.ui.common.player.GestureController
import com.heyanle.easybangumi4.ui.common.player.LockBtn
import com.heyanle.easybangumi4.ui.common.player.ProgressBox
import com.heyanle.easybangumi4.ui.common.player.SimpleBottomBar
import com.heyanle.easybangumi4.ui.common.player.SimpleTopBar
import com.heyanle.easybangumi4.utils.TODO
import com.heyanle.easybangumi4.utils.loge
import com.heyanle.easybangumi4.utils.openUrl
import kotlinx.coroutines.launch
import java.util.Arrays

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
    val owner = DetailedViewModel.getViewModelStoreOwner(summary)
    CompositionLocalProvider(
        LocalViewModelStoreOwner provides owner
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartoonPlay(
    detailedVM: DetailedViewModel,
    cartoonPlayVM: CartoonPlayViewModel,
    cartoonSummary: CartoonSummary,
    source: Source,
    enterData: CartoonPlayViewModel.EnterData? = null,
) {

    val controlVM =
        viewModel<ControlViewModel>(factory = ControlViewModelFactory(CartoonPlayingManager.exoPlayer))

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

    DisposableEffect(key1 = Unit) {
        onDispose {
            CartoonPlayingManager.trySaveHistory()
        }
    }


    val lazyGridState = rememberLazyGridState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {

        EasyPlayerScaffold(
            modifier = Modifier.fillMaxSize(),
            vm = controlVM,
            videoFloat = {
                LaunchedEffect(key1 = CartoonPlayingManager.state) {
                    when (CartoonPlayingManager.state) {
                        is CartoonPlayingManager.PlayingState.Playing -> {
                            it.onPrepare()
                            // CartoonPlayingManager.trySaveHistory()
                        }

                        is CartoonPlayingManager.PlayingState.Loading -> {}
                        is CartoonPlayingManager.PlayingState.Error -> {}
                        else -> {}
                    }
                }
                when (val state = CartoonPlayingManager.state) {
                    is CartoonPlayingManager.PlayingState.Playing -> {}
                    is CartoonPlayingManager.PlayingState.Loading -> {
                        LoadingPage(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { }
                        )
                    }

                    is CartoonPlayingManager.PlayingState.Error -> {
                        ErrorPage(
                            modifier = Modifier
                                .fillMaxSize(),
                            errorMsg = state.errMsg,
                            clickEnable = true,
                            onClick = {
                                CartoonPlayingManager.defaultScope.launch {
                                    CartoonPlayingManager.refresh()
                                }
                            }
                        )
                    }

                    else -> {}
                }
                if (!it.isFullScreen) {
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {

                    // 手势
                    GestureController(vm = it, modifier = Modifier.fillMaxSize())

                    // 顶部工具栏
                    SimpleTopBar(
                        vm = it,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                    )

                    // 底部工具栏
                    SimpleBottomBar(
                        vm = it,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                    ) {

                    }

                    // 锁定按钮
                    LockBtn(vm = it)

                    // 加载按钮
                    ProgressBox(vm = it)
                }
            }
        ) {
            Spacer(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary)
                    .height(2.dp)
                    .fillMaxWidth(),
            )
            Surface(
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground
            ) {
                Box(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
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

}


@OptIn(ExperimentalPagerApi::class)
@Composable
fun CartoonPlayUI(
    detailedVM: DetailedViewModel,
    cartoonPlayVM: CartoonPlayViewModel,
    listState: LazyGridState = rememberLazyGridState()
) {

    val scope = rememberCoroutineScope()



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

        else -> {}
    }

}


@Composable
fun CartoonPlayPage(
    detailedVM: DetailedViewModel,
    cartoonPlayVM: CartoonPlayViewModel,
    detailedState: DetailedViewModel.DetailedState.Info,
    listState: LazyGridState = rememberLazyGridState()
) {
    CartoonPlayDetailed(
        modifier = Modifier.fillMaxSize(),
        cartoon = detailedState.detail,
        playLines = detailedState.playLine,
        selectLineIndex = cartoonPlayVM.selectedLineIndex,
        playingPlayLine = CartoonPlayingManager.state.playLine(),
        playingEpisode = CartoonPlayingManager.state.episode(),
        listState = listState,
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
            TODO("搜索同名番")
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
                        }
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
                                Column {
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






