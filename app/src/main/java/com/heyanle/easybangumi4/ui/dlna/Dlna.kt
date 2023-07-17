package com.heyanle.easybangumi4.ui.dlna

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heyanle.bangumi_source_api.api.component.detailed.DetailedComponent
import com.heyanle.bangumi_source_api.api.component.play.PlayComponent
import com.heyanle.bangumi_source_api.api.entity.Cartoon
import com.heyanle.bangumi_source_api.api.entity.CartoonSummary
import com.heyanle.bangumi_source_api.api.entity.PlayLine
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.ui.cartoon_play.CartoonDescCard
import com.heyanle.easybangumi4.ui.common.Action
import com.heyanle.easybangumi4.ui.common.EmptyPage
import com.heyanle.easybangumi4.ui.common.ErrorPage
import com.heyanle.easybangumi4.ui.common.FastScrollToTopFab
import com.heyanle.easybangumi4.ui.common.LoadingPage
import com.heyanle.easybangumi4.ui.common.OkImage
import com.heyanle.easybangumi4.ui.common.SourceContainerBase
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.utils.loge
import com.heyanle.easybangumi4.utils.stringRes
import com.zane.androidupnpdemo.entity.ClingDevice
import java.util.Arrays

/**
 * Created by HeYanLe on 2023/6/18 13:50.
 * https://github.com/heyanLE
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dlna(
    id: String,
    source: String,
    url: String,
    enterData: DlnaViewModel.EnterData? = null
) {


    SourceContainerBase(hasSource = {
        val playComponent = it.play(source)
        val detailedComponent = it.detailed(source)
        (playComponent != null && detailedComponent != null).loge("Dlna")
        playComponent != null && detailedComponent != null
    }) { bundle ->
        bundle.play(source)?.let { play ->
            bundle.detailed(source)?.let { detailed ->
                DlnaPage(
                    id = id,
                    source = source,
                    url = url,
                    playComponent = play,
                    detailComponent = detailed,
                    enterData = enterData,
                )
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DlnaPage(
    id: String,
    source: String,
    url: String,
    playComponent: PlayComponent,
    detailComponent: DetailedComponent,
    enterData: DlnaViewModel.EnterData? = null,
) {

    val nav = LocalNavController.current

    var isDlnaListOpen by remember {
        mutableStateOf(DlnaManager.curDevice.value == null)
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val lazyGridState = rememberLazyGridState()

    val scope = rememberCoroutineScope()

    val vm = DlnaViewModelFactory.new(
        cartoonSummary = CartoonSummary(id, source, url),
        detailedComponent = detailComponent,
        playComponent = playComponent,
        enterData = enterData,
    )


    DisposableEffect(key1 = Unit) {
        onDispose {
            kotlin.runCatching {
                DlnaManager.release()
            }.onFailure {
                it.printStackTrace()
            }

        }
    }

    LaunchedEffect(key1 = Unit) {
        if (DlnaManager.curDevice.value == null) {
            isDlnaListOpen = true
        }
    }

    LaunchedEffect(key1 = Unit) {
        DlnaManager.initIfNeed()
        vm.loadDetailed()
    }


    if (isDlnaListOpen) {
        AlertDialog(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = stringResource(id = R.string.please_choose_device))
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .padding(16.dp, 0.dp)
                            .size(32.dp)

                    )
                }

            },
            text = {
                DlnaDeviceList(onClick = {
                    isDlnaListOpen = false
                    DlnaManager.select(it)
                })
            },
            onDismissRequest = {
                isDlnaListOpen = false
            },
            confirmButton = {}
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        Column {
            TopAppBar(
                title = {
                    val curDevice = DlnaManager.curDevice.value
                    val title = if (curDevice == null) {
                        stringRes(R.string.unselected_device)
                    } else {
                        curDevice.device.details.friendlyName
                    }
                    Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                navigationIcon = {
                    IconButton(onClick = {
                        nav.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            stringResource(id = R.string.back)
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                actions = {
                    TextButton(onClick = {
                        isDlnaListOpen = true
                    }) {
                        Text(
                            text = stringResource(id = R.string.change_device),
                        )
                    }

                }

            )
            Column(
                modifier = Modifier
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                when (val playState = vm.playingState) {
                    is DlnaViewModel.PlayingState.Loading -> {
                        LoadingPage(
                            modifier = Modifier.fillMaxWidth(), loadingMsg = stringResource(
                                id = R.string.parsing
                            )
                        )
                        Divider()
                    }

                    is DlnaViewModel.PlayingState.Playing -> {
                        DlnaDeviceAction {
                            vm.loadPlay(
                                playState.detailInfo,
                                playState.playLineIndex,
                                playState.curEpisode
                            )
                        }
                        Divider()
                    }

                    is DlnaViewModel.PlayingState.Error -> {
                        ErrorPage(
                            modifier = Modifier.fillMaxWidth(),
                            errorMsg = playState.errMsg + playState.throwable?.message,
                            other = { Text(text = stringResource(id = R.string.click_to_retry)) },
                            clickEnable = true,
                            onClick = {
                                vm.loadPlay(
                                    playState.detailInfo,
                                    playState.playLineIndex,
                                    playState.curEpisode
                                )
                            },
                        )
                        Divider()
                    }

                    is DlnaViewModel.PlayingState.None -> {

                    }
                }

                when (val detailedState = vm.detailedState) {
                    is DlnaViewModel.DetailedState.Loading -> {
                        LoadingPage(
                            modifier = Modifier.fillMaxWidth())
                    }

                    is DlnaViewModel.DetailedState.Error -> {
                        ErrorPage(
                            modifier = Modifier.fillMaxWidth(),
                            errorMsg = detailedState.errorMsg + detailedState.throwable?.message,
                            other = { Text(text = stringResource(id = R.string.click_to_retry)) },
                            clickEnable = true,
                            onClick = {
                                vm.loadDetailed()
                            },
                        )
                    }

                    is DlnaViewModel.DetailedState.Info -> {
                        CartoonPlayDetailed(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            cartoon = detailedState.detail,

                            playLines = detailedState.playLine,
                            selectLineIndex = vm.selectedLineIndex,
                            playingPlayLine = vm.playingState.playLine(),
                            playingEpisode = vm.playingState.episode(),
                            listState = lazyGridState,
                            behavior = scrollBehavior,
                            onLineSelect = {
                                vm.selectedLineIndex = it
                            },
                            onEpisodeClick = { playLineIndex, _, episode ->
                                vm.loadPlay(detailedState, playLineIndex, episode)
                            },

                            showPlayLine = detailedState.isShowPlayLine,
                        )
                    }

                    is DlnaViewModel.DetailedState.None -> {

                    }
                }


            }
        }

        FastScrollToTopFab(listState = lazyGridState)
    }
}

@Composable
fun DlnaDeviceAction(
    onRefresh: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
                    DlnaManager.play()
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
                    DlnaManager.pause()
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
                    DlnaManager.stop()
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
    behavior: TopAppBarScrollBehavior,
    onLineSelect: (Int) -> Unit,
    onEpisodeClick: (Int, PlayLine, Int) -> Unit,

    showPlayLine: Boolean = true,
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
            modifier = modifier.nestedScroll(behavior.nestedScrollConnection),
            columns = GridCells.Adaptive(128.dp),
            state = listState,
            contentPadding = PaddingValues(0.dp, 0.dp, 0.dp, 96.dp),
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

                        if (showPlayLine) {
                            ScrollableTabRow(
                                modifier = Modifier
                                    .weight(1f)
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
                        } else {
                            Text(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(16.dp, 0.dp),
                                text = stringResource(id = R.string.play_list)
                            )
                        }
                    }
                }

                if (selectLineIndex >= 0 && selectLineIndex < playLines.size && unEmptyLinesIndex.contains(
                        selectLineIndex
                    )
                ) {
                    items(playLines[selectLineIndex].episode.size) { index ->
                        val item = playLines[selectLineIndex].episode[index]

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

@Composable
fun DlnaDeviceList(
    onClick: (ClingDevice) -> Unit,
) {
    val list = DlnaManager.dmrDevices
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        items(list) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .clickable {
                        onClick(it)
                    }
                    .padding(16.dp, 8.dp),
                text = it.device.details.friendlyName,
                color = if (it.isSelected) MaterialTheme.colorScheme.secondary else Color.Unspecified
            )
        }
    }
}