package com.heyanle.easybangumi.ui.player

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.exoplayer2.C.ColorTransfer
import com.heyanle.bangumi_source_api.api.entity.BangumiSummary
import com.heyanle.easybangumi.LocalNavController
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.navigationSearch
import com.heyanle.easybangumi.player.PlayerController
import com.heyanle.easybangumi.player.TinyStatusController
import com.heyanle.easybangumi.source.AnimSourceFactory
import com.heyanle.easybangumi.ui.common.*
import com.heyanle.easybangumi.ui.common.easy_player.EasyPlayerView
import com.heyanle.easybangumi.utils.openUrl
import com.heyanle.eplayer_core.constant.EasyPlayStatus
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/1/11 15:27.
 * https://github.com/heyanLE
 */

@SuppressLint("UnsafeOptInUsageError")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Play(
    source: String,
    detail: String,
    enterData: BangumiPlayController.EnterData? = null,
) {

    val nav = LocalNavController.current
    // 多实例的时候，当前页面的动画如果不是当前播放的，需要改变当前播放的
    LaunchedEffect(key1 = BangumiPlayController.curAnimPlayViewModel.value) {
        val old = BangumiPlayController.curAnimPlayViewModel.value?.bangumiSummary
        Log.d("Play", "bangumi(source=${source}, detail=${detail})")
        if (old?.source != source || old.detailUrl != detail) {
            BangumiPlayController.newBangumi(BangumiSummary(source, detail), enterData)
        }
        TinyStatusController.onPlayScreenLaunch()

    }


    val vm: AnimPlayItemController =
        BangumiPlayController.getAnimPlayViewModel(BangumiSummary(source, detail))

    DisposableEffect(key1 = Unit) {
        onDispose {
            TinyStatusController.onPlayScreenDispose()
            vm.onDispose()
            //uiController.setStatusBarColor(oldColor)
        }
    }

    val playerStatus by vm.playerStatus.collectAsState(initial = null)
    val detailStatus by vm.detailController.detailFlow.collectAsState(initial = null)
    val playMsgStatus by vm.playMsgController.flow.collectAsState(initial = null)

    LaunchedEffect(key1 = Unit) {
        vm.load()
    }
    val ps = playerStatus
    val ms = playMsgStatus
    val ds = detailStatus
    LaunchedEffect(key1 = ps, key2 = ms, key3 = ds) {
        Log.d(
            "Play",
            "root launchedEffect ${ps?.javaClass?.simpleName}  ${ms?.javaClass?.simpleName} ${ds?.javaClass?.simpleName}"
        )
        if (ms != null && ms is PlayMsgController.PlayMsgStatus.Completely) {
            if (ds != null && ds is DetailController.DetailStatus.Completely) {
                if (ps != null && (ps is AnimPlayItemController.PlayerStatus.None)) {
                    val curLines = ps.sourceIndex
                    val curEpi = ps.episode
                    vm.onShow(curLines, curEpi)
                } else if (ps != null && (ps is AnimPlayItemController.PlayerStatus.Play)) {
                    BangumiPlayController.trySaveHistory(-1)
                }
            }
        }
    }

    Scaffold(
        containerColor = Color.Black,
        contentColor = MaterialTheme.colorScheme.onBackground,
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                playerStatus?.let {
                    Video(vm, playerStatus = it)
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (ms is PlayMsgController.PlayMsgStatus.Error || ds is DetailController.DetailStatus.Error) {
                        val sb = StringBuilder()
                        if (ms is PlayMsgController.PlayMsgStatus.Error) {
                            sb.append(ms.errorMsg).append("\n")
                        }
                        if (ds is DetailController.DetailStatus.Error) {
                            sb.append(ds.errorMsg).append("\n")
                        }
                        ErrorPage(
                            modifier = Modifier.fillMaxSize(),
                            errorMsg = sb.toString(),
                            clickEnable = true,
                            other = {
                                Text(text = stringResource(id = R.string.click_to_retry))
                            },
                            onClick = {
                                vm.load()
                            }
                        )
                    } else {
                        LazyVerticalGrid(columns = GridCells.Fixed(3)) {
                            detailStatus?.let {
                                detail(vm, detailStatus = it)
                            }
                            detailStatus?.let {
                                // action(vm, detailStatus = it)
                            }
                            playMsgStatus?.let { playMsg ->
                                playerStatus?.let {
                                    playerMsg(vm, playerMsgStatus = playMsg, it)
                                }
                            }
                        }
                    }

                }
            }
        }
    )
}

// 播放器

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnsafeOptInUsageError")
@Composable
fun Video(
    vm: AnimPlayItemController,
    playerStatus: AnimPlayItemController.PlayerStatus,
) {

    val density = LocalDensity.current

    val nav = LocalNavController.current

    var height by remember {
        mutableStateOf(200.dp)
    }
    Box(
        modifier = Modifier
            .height(height)
            .fillMaxWidth()
            .onSizeChanged {
                height =
                    with(density) { ((it.width / (PlayerController.ratioWidth)) * PlayerController.ratioHeight).toDp() }
            }
    ) {
        if (playerStatus is AnimPlayItemController.PlayerStatus.Error) {
            ErrorPage(
                modifier = Modifier.fillMaxSize(),
                errorMsg = playerStatus.errorMsg,
                clickEnable = true,
                other = {
                    Text(text = stringResource(id = R.string.click_to_retry))
                },
                onClick = {
                    vm.changePlayer(playerStatus.sourceIndex, playerStatus.episode)
                }
            )
        } else {
            val curVideo by PlayerController.videoSizeStatus.observeAsState()
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = {
                    EasyPlayerView(it).apply {
                        basePlayerView.attachToPlayer(com.heyanle.easybangumi.player.PlayerController.exoPlayer)
                        BangumiPlayController.onNewComposeView(this)
                    }
                }
            ) {
                when (playerStatus) {
                    is AnimPlayItemController.PlayerStatus.None -> {
                        it.visibility = View.GONE
                    }

                    is AnimPlayItemController.PlayerStatus.Play -> {
                        it.visibility = View.VISIBLE
                        BangumiPlayController.onNewComposeView(it)
                        // it.basePlayerView.dispatchPlayStateChange(curStatus)
                        curVideo?.apply {
                            it.basePlayerView.onVideoSizeChanged(this)
                        }
                        // it.basePlayerView.refreshStateOnce()
                    }

                    is AnimPlayItemController.PlayerStatus.Loading -> {
                        it.basePlayerView.dispatchPlayStateChange(EasyPlayStatus.STATE_PREPARING)
                    }

                    else -> {}
                }
                PlayerController.exoPlayer.setVideoSurfaceView(it.basePlayerView.surfaceView)
                // PlayerController.onContainer(it)
            }
        }


    }


}


// 播放线路 & 集
fun LazyGridScope.playerMsg(
    vm: AnimPlayItemController,
    playerMsgStatus: PlayMsgController.PlayMsgStatus,
    playerStatus: AnimPlayItemController.PlayerStatus,
) {


    when (playerMsgStatus) {
        is PlayMsgController.PlayMsgStatus.None -> {}
        is PlayMsgController.PlayMsgStatus.Error -> {
        }

        is PlayMsgController.PlayMsgStatus.Loading -> {
            item(span = {
                // LazyGridItemSpanScope:
                // maxLineSpan
                GridItemSpan(maxLineSpan)
            }) {
                LoadingPage()
            }

        }

        is PlayMsgController.PlayMsgStatus.Completely -> {

            val lines = playerMsgStatus.playMsg.keys.toList()

            val curLines = playerStatus.sourceIndex
            val curEpi = playerStatus.episode
            if (curLines >= 0 && curLines < lines.size) {
                val epi = kotlin.runCatching {
                    playerMsgStatus.playMsg[lines[curLines]]
                }.getOrNull() ?: emptyList()
                item(span = {
                    // LazyGridItemSpanScope:
                    // maxLineSpan
                    GridItemSpan(maxLineSpan)
                }) {

                    HomeTabRow(
                        modifier = Modifier.padding(4.dp, 0.dp, 4.dp, 8.dp),
                        containerColor = Color.Transparent,
                        selectedTabIndex = curLines,
                        indicatorColor = { MaterialTheme.colorScheme.secondary }
                    ) {
                        for (i in lines.indices) {
                            if (playerMsgStatus.playMsg[lines[i]]?.isNotEmpty() == true) {
                                HomeTabItem(
                                    selected = i == curLines,
                                    text = {
                                        Text(lines[i])
                                    },
                                    onClick = {
                                        vm.changeLines(i)
                                    },
                                    selectedContentColor = MaterialTheme.colorScheme.secondary,
                                    unselectedContentColor = MaterialTheme.colorScheme.onBackground
                                )
                            } else {
                                // 为空的话避免异常加空占位
                                HomeTabItem(
                                    selected = false,
                                    text = { },
                                    onClick = { },
                                    selectedContentColor = Color.Transparent,
                                    unselectedContentColor = Color.Transparent,
                                    enable = false
                                )
                            }
                        }
                    }
                }
                itemsIndexed(epi) { index, item ->
                    val selected = index == curEpi
                    Surface(
                        shadowElevation = 4.dp,
                        shape = RoundedCornerShape(4.dp),
                        modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(4.dp, 4.dp),
                        color = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.secondaryContainer,
                    ) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(CircleShape)
                                .clickable {
                                    vm.changePlayer(curLines, index)
                                }
                                .padding(16.dp, 4.dp),
                            color = if (selected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSecondaryContainer,
                            text = item,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

            }
        }
    }

}


// 番剧详情
fun LazyGridScope.detail(
    vm: AnimPlayItemController,
    detailStatus: DetailController.DetailStatus
) {


    item(span = {
        // LazyGridItemSpanScope:
        // maxLineSpan
        GridItemSpan(maxLineSpan)
    }) {
        val nav = LocalNavController.current
        Box(
            modifier = Modifier
                .padding(8.dp)
                .height(210.dp)
                .fillMaxWidth()
        ) {
            when (detailStatus) {
                is DetailController.DetailStatus.None -> {}
                is DetailController.DetailStatus.Error -> {}
                is DetailController.DetailStatus.Loading -> {
                    LoadingPage(
                        modifier = Modifier.fillMaxSize()
                    )
                }

                is DetailController.DetailStatus.Completely -> {
                    Log.d("Play", detailStatus.bangumiDetail.cover)
                    Column(
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Row(
                            modifier = Modifier
                                .height(135.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .height(135.dp)
                                    .width(95.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            ) {

                                OkImage(
                                    image = detailStatus.bangumiDetail.cover,
                                    contentDescription = detailStatus.bangumiDetail.name,
                                    modifier = Modifier
                                        .height(135.dp)
                                        .width(95.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )

                                val sourceText =
                                    AnimSourceFactory.label(detailStatus.bangumiDetail.source)
                                        ?: detailStatus.bangumiDetail.source
                                Text(
                                    fontSize = 13.sp,
                                    text = sourceText,
                                    color = MaterialTheme.colorScheme.onSecondary,
                                    modifier = Modifier
                                        .background(
                                            MaterialTheme.colorScheme.secondary,
                                            RoundedCornerShape(0.dp, 0.dp, 8.dp, 0.dp)
                                        )
                                        .padding(8.dp, 0.dp)
                                )

                            }
                            Column(
                                modifier = Modifier.weight(1.0f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = detailStatus.bangumiDetail.name,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 16.sp
                                )
                                Text(
                                    modifier = Modifier
                                        .weight(1.0f)
                                        .alpha(0.8f),
                                    text = detailStatus.bangumiDetail.description,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 12.sp,
                                    lineHeight = 14.sp
                                )
                            }

                        }
                        val scope = rememberCoroutineScope()
                        val isStar by vm.detailController.isBangumiStar
                        Row(
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(1f)
                                    .clip(CircleShape)
                                    .clickable {
                                        scope.launch {
                                            vm.detailController.setBangumiStar(
                                                !isStar,
                                                detailStatus.bangumiDetail
                                            )
                                        }

                                    },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                val icon =
                                    if (isStar) Icons.Filled.Star else Icons.Filled.StarOutline
                                val textId = if (isStar) R.string.stared else R.string.click_star
                                Icon(
                                    icon,
                                    stringResource(id = textId),
                                    tint = if (isStar) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = stringResource(id = textId),
                                    color = if (isStar) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onBackground,
                                    fontSize = 12.sp
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(CircleShape)
                                    .fillMaxHeight()
                                    .clickable {
                                        // "还在支持".moeSnackBar()
                                        nav.navigationSearch(
                                            detailStatus.bangumiDetail.name,
                                            detailStatus.bangumiDetail.source
                                        )
                                    },
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Filled.Search,
                                    stringResource(id = R.string.search_same_bangumi)
                                )
                                Text(
                                    text = stringResource(id = R.string.search_same_bangumi),
                                    fontSize = 12.sp
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(CircleShape)
                                    .fillMaxHeight()
                                    .clickable {
                                        // "还在支持".moeSnackBar()
                                        vm.bangumiSummary.detailUrl.openUrl()
                                    },
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Filled.Web,
                                    stringResource(id = R.string.open_source_url)
                                )
                                Text(
                                    text = stringResource(id = R.string.open_source_url),
                                    fontSize = 12.sp
                                )
                            }

                        }

                    }


                }

                else -> {}
            }
        }
    }


}