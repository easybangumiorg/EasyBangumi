package com.heyanle.easybangumi.ui.dlna

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heyanle.bangumi_source_api.api.entity.BangumiSummary
import com.heyanle.easybangumi.LocalNavController
import com.heyanle.easybangumi.ui.common.ErrorPage
import com.heyanle.easybangumi.ui.common.FastScrollToTopFab
import com.heyanle.easybangumi.ui.common.LoadingPage
import com.heyanle.easybangumi.ui.common.moeSnackBar
import com.heyanle.easybangumi.ui.player.Action
import com.heyanle.easybangumi.ui.player.AnimPlayState
import com.heyanle.easybangumi.ui.player.AnimPlayingController
import com.heyanle.easybangumi.ui.player.BangumiInfoState
import com.heyanle.easybangumi.ui.player.BangumiPlayManager
import com.heyanle.easybangumi.ui.player.Detailed
import com.heyanle.easybangumi.ui.player.playMsg
import com.heyanle.easybangumi.utils.stringRes
import com.zane.androidupnpdemo.entity.ClingDevice

/**
 * Created by HeYanLe on 2023/2/11 20:25.
 * https://github.com/heyanLE
 */

@Composable
fun Dlna(
    id: String,
    source: String,
    detail: String,
    enterData: DlnaPlayingManager.EnterData? = null
) {
    LaunchedEffect(key1 = Unit) {
        DlnaPlayingManager.newBangumiSummary(BangumiSummary(id, source, detail))
    }


    val controller by DlnaPlayingManager.playingController.collectAsState()
    val con = controller
    controller?.let {
        DlnaPage(playingController = it, enterData)
    }


}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DlnaPage(
    playingController: AnimPlayingController,
    enterData: DlnaPlayingManager.EnterData?
) {

    val nav = LocalNavController.current

    val selectLines = remember {
        mutableStateOf(0)
    }

    val lazyGridState = rememberLazyGridState()

    val playerState by playingController.playerState.collectAsState()
    val infoState by playingController.infoState.collectAsState()

    var isDlnaListOpen by remember {
        mutableStateOf(DlnaManager.curDevice.value == null)
    }

    LaunchedEffect(key1 = infoState) {
        if (infoState is BangumiInfoState.None) {
            playingController.loadInfo()
        } else if (infoState is BangumiInfoState.Info) {
            val en = if (enterData == null) enterData else BangumiPlayManager.EnterData(
                enterData.lineIndex,
                enterData.episode,
                0
            )
            playingController.onShow(en, false)
        }
    }

    LaunchedEffect(key1 = Unit) {
        if (DlnaManager.curDevice.value == null) {
            isDlnaListOpen = true
        }
    }

    LaunchedEffect(key1 = playerState) {
        selectLines.value = playerState.lineIndex
    }

    DisposableEffect(key1 = Unit) {
        onDispose {
            kotlin.runCatching {
                DlnaManager.release()
                DlnaPlayingManager.release()
            }.onFailure {
                it.printStackTrace()
            }

        }
    }

    if (isDlnaListOpen) {
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


    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                title = {
                    val curDevice = DlnaManager.curDevice.value
                    val title = if (curDevice == null) {
                        stringRes(com.heyanle.easy_i18n.R.string.unselected_device)
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
                            Icons.Filled.ArrowBack,
                            contentDescription = stringResource(id = com.heyanle.easy_i18n.R.string.back),
                        )
                    }
                },
                actions = {
                    TextButton(onClick = {
                        isDlnaListOpen = true
                    }) {
                        Text(
                            text = stringResource(id = com.heyanle.easy_i18n.R.string.change_device),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                }
            )
        }
    ) {
        Column(modifier = Modifier.padding(it)) {
            AnimatedContent(targetState = playerState) {
                when (it) {
                    is AnimPlayState.Error -> {
                        Log.d("Play", "onPlayerViewError ${it.throwable}")
                        ErrorPage(
                            modifier = Modifier.fillMaxSize(),
                            errorMsg = it.errorMsg,
                            clickEnable = true,
                            other = {
                                Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.click_to_retry))
                            },
                            onClick = {
                                playingController.loadPlay(
                                    playerState.lineIndex,
                                    playerState.episode
                                )
                            }
                        )
                    }

                    is AnimPlayState.Loading -> {
                        LoadingPage(
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    is AnimPlayState.Play -> {
                        DlnaDeviceAction()
                    }

                    else -> {}
                }
            }
            AnimatedContent(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                targetState = infoState,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300, delayMillis = 300)) with
                            fadeOut(animationSpec = tween(300, delayMillis = 0))
                },
            ) {
                Info(
                    controller = playingController,
                    infoState = it,
                    playerState = playerState,
                    selectLines = selectLines,
                    lazyGridState = lazyGridState
                )
            }
            FastScrollToTopFab(listState = lazyGridState)
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
                    Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.click_to_retry))
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
                    contentPadding = PaddingValues(4.dp, 4.dp, 4.dp, 60.dp),

                    ) {
                    // 番剧详情
                    item(
                        span = {
                            GridItemSpan(maxLineSpan)
                        }
                    ) {
                        Detailed(bangumiDetail = infoState.detail)
                    }

                    // 播放信息
                    playMsg(controller, infoState, playerState, selectLines)
                }
            }
        }

        else -> {}
    }
}


@Composable
fun DlnaDeviceAction() {
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
                    DlnaPlayingManager.refresh()
                }
            )
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