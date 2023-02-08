package com.heyanle.easybangumi.ui.player

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.heyanle.bangumi_source_api.api.entity.BangumiDetail
import com.heyanle.bangumi_source_api.api.entity.BangumiSummary
import com.heyanle.easybangumi.LocalNavController
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.navigationSearch
import com.heyanle.easybangumi.player.PlayerController
import com.heyanle.easybangumi.player.TinyStatusController
import com.heyanle.easybangumi.ui.common.BangumiCard
import com.heyanle.easybangumi.ui.common.ErrorPage
import com.heyanle.easybangumi.ui.common.FastScrollToTopFab
import com.heyanle.easybangumi.ui.common.HomeTabItem
import com.heyanle.easybangumi.ui.common.HomeTabRow
import com.heyanle.easybangumi.ui.common.LoadingPage
import com.heyanle.easybangumi.ui.common.easy_player.EasyPlayerView
import com.heyanle.easybangumi.utils.openUrl
import com.heyanle.eplayer_core.constant.EasyPlayStatus

/**
 * Created by HeYanLe on 2023/2/5 12:58.
 * https://github.com/heyanLE
 */
@SuppressLint("UnsafeOptInUsageError")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun Play(
    id: String,
    source: String,
    detail: String,
    enterData: BangumiPlayManager.EnterData? = null,
) {

    val controller = PlayingControllerFactory.getItemController(BangumiSummary(id, source, detail))

    LaunchedEffect(key1 = controller) {
        BangumiPlayManager.newAnimPlayItemController(controller, enterData)
    }

    LaunchedEffect(key1 = Unit) {
        AnimPlayingManager.newBangumi(source, detail)
    }

    DisposableEffect(key1 = Unit) {
        TinyStatusController.onPlayScreenLaunch()
        onDispose {
            TinyStatusController.onPlayScreenDispose()
            BangumiPlayManager.trySaveHistory(-1)
        }
    }

    val infoState by controller.infoState.collectAsState(BangumiInfoState.None)
    val playerState by controller.playerState.collectAsState(AnimPlayState.None)

    LaunchedEffect(key1 = infoState) {
        if (infoState is BangumiInfoState.None) {
            controller.loadInfo()
        } else if (infoState is BangumiInfoState.Info) {
            controller.onShow(enterData)
        }
    }

    val density = LocalDensity.current

    val nav = LocalNavController.current

    var videoPlayer by remember {
        mutableStateOf(200.dp)
    }

    val selectLines = remember {
        mutableStateOf(0)
    }

    val lazyGridState = rememberLazyGridState()

    LaunchedEffect(key1 = playerState) {
        selectLines.value = playerState.lineIndex
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .onSizeChanged {
                val h = with(density) {
                    ((it.width / (PlayerController.ratioWidth)) * PlayerController.ratioHeight)
                        .coerceAtMost(
                            it.height / 2F
                        )
                        .toDp()
                }
                videoPlayer = h
            },
        color = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {

        Column {
            PlayerView(
                modifier = Modifier
                    .background(Color.Black)
                    .statusBarsPadding()
                    .height(videoPlayer),
                controller = controller,
                playerState = playerState
            )
            AnimatedContent(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                targetState = infoState,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300, delayMillis = 300)) with
                            fadeOut(animationSpec = tween(300, delayMillis = 0))
                },
            ) {
                Info(
                    controller = controller,
                    infoState = it,
                    playerState = playerState,
                    selectLines = selectLines,
                    lazyGridState = lazyGridState
                )
            }

        }
        FastScrollToTopFab(listState = lazyGridState)

    }


}

// 播放器
@Composable
fun PlayerView(
    modifier: Modifier,
    controller: AnimPlayingController,
    playerState: AnimPlayState,
) {
    Box(modifier = modifier) {
        when (playerState) {
            is AnimPlayState.Error -> {
                Log.d("Play", "onPlayerViewError ${playerState.throwable}")
                ErrorPage(
                    modifier = Modifier.fillMaxSize(),
                    errorMsg = playerState.errorMsg,
                    clickEnable = true,
                    other = {
                        Text(text = stringResource(id = R.string.click_to_retry))
                    },
                    onClick = {
                        controller.loadPlay(playerState.lineIndex, playerState.episode)
                    }
                )
            }

            else -> {
                val curVideo by PlayerController.videoSizeStatus.observeAsState()
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = {
                        EasyPlayerView(it).apply {
                            basePlayerView.attachToPlayer(PlayerController.exoPlayer)
                            BangumiPlayManager.onNewComposeView(this)
                        }
                    }
                ) {
                    Log.d("Play", "onPlayView $playerState")
                    when (playerState) {
                        is AnimPlayState.None -> {
                            it.visibility = View.GONE
                            it.basePlayerView.dispatchPlayStateChange(EasyPlayStatus.STATE_IDLE)
                            //it.basePlayerView.dispatchPlayStateChange(EasyPlayStatus.STATE_PREPARING)
                        }

                        is AnimPlayState.Play -> {
                            it.visibility = View.VISIBLE
                            BangumiPlayManager.onNewComposeView(it)
                            // it.basePlayerView.dispatchPlayStateChange(curStatus)
                            curVideo?.apply {
                                it.basePlayerView.onVideoSizeChanged(this)
                            }
                            // it.basePlayerView.refreshStateOnce()
                        }

                        is AnimPlayState.Loading -> {
                            it.basePlayerView.dispatchPlayStateChange(EasyPlayStatus.STATE_PREPARING)
                        }

                        else -> {}
                    }
                    PlayerController.exoPlayer.setVideoSurfaceView(it.basePlayerView.surfaceView)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Info(
    controller: AnimPlayingController,
    infoState: BangumiInfoState,
    playerState: AnimPlayState,
    selectLines: MutableState<Int>,
    lazyGridState: LazyGridState,
) {
    when (infoState) {
        is BangumiInfoState.Loading -> {
            LoadingPage(
                modifier = Modifier.fillMaxSize()
            )
        }

        is BangumiInfoState.Error -> {
            ErrorPage(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(0.dp, 32.dp),
                errorMsg = infoState.errorMsg,
                clickEnable = true,
                other = {
                    Text(text = stringResource(id = R.string.click_to_retry))
                },
                onClick = {
                    controller.loadInfo()
                }
            )
        }

        is BangumiInfoState.Info -> {
            CompositionLocalProvider(
                LocalOverscrollConfiguration provides null
            ) {
                LazyVerticalGrid(
                    state = lazyGridState,
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    // 番剧详情
                    item(
                        span = {
                            GridItemSpan(maxLineSpan)
                        }
                    ) {
                        Detailed(bangumiDetail = infoState.detail)
                    }

                    // 操作
                    item(
                        span = {
                            GridItemSpan(maxLineSpan)
                        }
                    ) {
                        ActionRow(controller = controller, infoState = infoState)
                    }

                    // 播放信息
                    playMsg(controller, infoState, playerState, selectLines)
                }
            }
        }

        else -> {}
    }
}
//

@Composable
fun Detailed(
    bangumiDetail: BangumiDetail
) {
    Row(
        modifier = Modifier
            .padding(4.dp)
            .height(135.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BangumiCard(bangumiDetail = bangumiDetail)
        Column(
            modifier = Modifier.weight(1.0f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = bangumiDetail.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 16.sp
            )
            Text(
                modifier = Modifier
                    .weight(1.0f)
                    .alpha(0.8f),
                text = bangumiDetail.description,
                overflow = TextOverflow.Ellipsis,
                fontSize = 12.sp,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
fun ActionRow(
    controller: AnimPlayingController,
    infoState: BangumiInfoState.Info,
) {
    val nav = LocalNavController.current
    val isStar by controller.isBangumiStar
    Row(
        modifier = Modifier.padding(4.dp)
    ) {
        val starIcon =
            if (isStar) Icons.Filled.Star else Icons.Filled.StarOutline
        val starTextId = if (isStar) R.string.stared else R.string.click_star
        // 点击追番
        Action(
            icon = {
                Icon(
                    starIcon,
                    stringResource(id = starTextId),
                    tint = if (isStar) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onBackground
                )
            },
            msg = {
                Text(
                    text = stringResource(id = starTextId),
                    color = if (isStar) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onBackground,
                    fontSize = 12.sp
                )
            },
            onClick = {
                controller.setBangumiStar(
                    !isStar,
                    infoState.detail
                )
            }
        )
        // 搜索同名番
        Action(
            icon = {
                Icon(
                    Icons.Filled.Search,
                    stringResource(id = R.string.search_same_bangumi)
                )
            },
            msg = {
                Text(
                    text = stringResource(id = R.string.search_same_bangumi),
                    fontSize = 12.sp
                )
            },
            onClick = {
                nav.navigationSearch(
                    infoState.detail.name,
                    infoState.detail.source
                )
            }
        )
        // 打开原网站
        Action(
            icon = {
                Icon(
                    Icons.Filled.Web,
                    stringResource(id = R.string.open_source_url)
                )
            },
            msg = {
                Text(
                    text = stringResource(id = R.string.open_source_url),
                    fontSize = 12.sp
                )
            },
            onClick = {
                infoState.detail.detailUrl.openUrl()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
fun LazyGridScope.playMsg(
    controller: AnimPlayingController,
    infoState: BangumiInfoState.Info,
    playerState: AnimPlayState,
    selectLines: MutableState<Int>,
) {
    val lines = infoState.playMsg.keys.toList()
    item(span = {
        // LazyGridItemSpanScope:
        // maxLineSpan
        GridItemSpan(maxLineSpan)
    }) {

        val lineIndex = remember(lines) {
            arrayListOf<Int>().apply {
                for (i in lines.indices) {
                    if (infoState.playMsg[lines[i]]?.isNotEmpty() == true) {
                        add(i)
                    }
                }
            }
        }

        HomeTabRow(
            modifier = Modifier.padding(4.dp, 0.dp, 4.dp, 8.dp),
            containerColor = Color.Transparent,
            selectedTabIndex = lineIndex.indexOf(selectLines.value),
            indicatorColor = { MaterialTheme.colorScheme.secondary }
        ) {

            for (i in lineIndex.indices) {
                if (infoState.playMsg[lines[lineIndex[i]]]?.isNotEmpty() == true) {
                    HomeTabItem(
                        selected = lineIndex[i] == selectLines.value,
                        text = {

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(lines[lineIndex[i]])
                                if (playerState.lineIndex == lineIndex[i]) {
                                    Box(
                                        modifier = Modifier
                                            .padding(2.dp, 0.dp, 0.dp, 0.dp)
                                            .size(8.dp)
                                            .background(
                                                MaterialTheme.colorScheme.secondary,
                                                CircleShape
                                            )
                                    )

                                }

                            }

                        },
                        onClick = {
                            selectLines.value = lineIndex[i]
                        },
                        selectedContentColor = MaterialTheme.colorScheme.secondary,
                        unselectedContentColor = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
    val epi = kotlin.runCatching {
        infoState.playMsg[lines[selectLines.value]]
    }.getOrNull() ?: emptyList()
    itemsIndexed(epi) { index, item ->
        val selected = selectLines.value == playerState.lineIndex && index == playerState.episode
        Surface(
            shadowElevation = 4.dp,
            shape = RoundedCornerShape(4.dp),
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(4.dp, 4.dp)
                .clip(RoundedCornerShape(4.dp))
                .clickable {
                    controller.loadPlay(selectLines.value, index)
                },
            color = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.secondaryContainer,
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 4.dp),
                color = if (selected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSecondaryContainer,
                text = item,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }
    }

}

@Composable
fun RowScope.Action(
    icon: @Composable () -> Unit,
    msg: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .weight(1f)
            .clip(CircleShape)
            .clickable {
                onClick()
            }
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        icon()
        msg()
    }
}