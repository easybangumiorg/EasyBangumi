package com.heyanle.easybangumi.ui.player

import android.annotation.SuppressLint
import android.content.Context
import android.inputmethodservice.Keyboard.Row
import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.heyanle.easybangumi.BangumiApp
import com.heyanle.easybangumi.LocalNavController
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.player.PlayerController
import com.heyanle.easybangumi.player.TinyStatusController
import com.heyanle.easybangumi.theme.EasyThemeController
import com.heyanle.easybangumi.ui.common.ErrorPage
import com.heyanle.easybangumi.ui.common.HomeTabItem
import com.heyanle.easybangumi.ui.common.HomeTabRow
import com.heyanle.easybangumi.ui.common.LoadingPage
import com.heyanle.easybangumi.ui.common.OkImage
import com.heyanle.easybangumi.ui.common.easy_player.EasyPlayerView
import com.heyanle.easybangumi.ui.home.animSubPageItems
import com.heyanle.easybangumi.utils.dip2px
import com.heyanle.eplayer_core.constant.EasyPlayStatus
import com.heyanle.eplayer_core.player.IPlayerEngine
import com.heyanle.eplayer_core.player.IPlayerEngineFactory
import com.heyanle.eplayer_core.render.IRender
import com.heyanle.eplayer_core.render.IRenderFactory
import com.heyanle.lib_anim.entity.BangumiSummary
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
){

    val nav = LocalNavController.current
    // 多实例的时候，当前页面的动画如果不是当前播放的，需要改变当前播放的
    LaunchedEffect(key1 = BangumiPlayController.curAnimPlayViewModel.value){
        val old = BangumiPlayController.curAnimPlayViewModel.value?.bangumiSummary
        if(old?.source != source || old.detailUrl != detail){
            BangumiPlayController.newBangumi(BangumiSummary(source, detail), nav)
        }
        TinyStatusController.onPlayScreenLaunch()

    }

    val uiController = rememberSystemUiController()
    val oldColor = MaterialTheme.colorScheme.primary
    DisposableEffect(key1 = Unit){
        onDispose {
            TinyStatusController.onPlayScreenDispose()
            uiController.setStatusBarColor(oldColor)
        }
    }

    val vm: AnimPlayViewModel = BangumiPlayController.getAnimPlayViewModel(BangumiSummary(source, detail))

    val playerStatus by vm.playerStatus.collectAsState(initial = null)
    val detailStatus by vm.detailController.detailFlow.collectAsState(initial = null)
    val playMsgStatus by vm.playMsgController.flow.collectAsState(initial = null)
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            uiController.setStatusBarColor(Color.Black)
        }
    }
    LaunchedEffect(key1 = Unit){
        vm.load()
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
                    LazyVerticalGrid(columns = GridCells.Fixed(2)){
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
    )
}

// 播放器

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun  Video(
    vm: AnimPlayViewModel,
    playerStatus: AnimPlayViewModel.PlayerStatus,
){

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
    ){
        if(playerStatus is AnimPlayViewModel.PlayerStatus.Error){
            ErrorPage(
                errorMsg = playerStatus.errorMsg,
                clickEnable = true,
                other = {
                    Text(text = stringResource(id = R.string.click_to_retry))
                },
                onClick = {
                    vm.changePlayer(playerStatus.sourceIndex, playerStatus.episode)
                }
            )
        }else{
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = {
                    EasyPlayerView(it).apply {
                        basePlayerView.attachToPlayer(com.heyanle.easybangumi.player.PlayerController.exoPlayer)
                        BangumiPlayController.onNewComposeView(this)
                    }
                }
            ){
                BangumiPlayController.onNewComposeView(it)
                when(playerStatus){
                    is AnimPlayViewModel.PlayerStatus.None -> {}
                    is AnimPlayViewModel.PlayerStatus.Play -> {

                        it.basePlayerView.refreshStateOnce()
                    }
                    is AnimPlayViewModel.PlayerStatus.Loading -> {
                        it.basePlayerView.dispatchPlayStateChange(EasyPlayStatus.STATE_PREPARING)
                    }
                    else ->{}
                }
                PlayerController.exoPlayer.setVideoSurfaceView(it.basePlayerView.surfaceView)
                // PlayerController.onContainer(it)
            }
        }


    }



}


// 播放线路 & 集
fun LazyGridScope.playerMsg(
    vm: AnimPlayViewModel,
    playerMsgStatus: PlayMsgController.PlayMsgStatus,
    playerStatus: AnimPlayViewModel.PlayerStatus,
){



    when(playerMsgStatus){
        is PlayMsgController.PlayMsgStatus.None -> {}
        is PlayMsgController.PlayMsgStatus.Error -> {}
        is PlayMsgController.PlayMsgStatus.Loading -> {
            item(span = {
                // LazyGridItemSpanScope:
                // maxLineSpan
                GridItemSpan(maxLineSpan)
            }){
                LoadingPage()
            }

        }
        is PlayMsgController.PlayMsgStatus.Completely -> {

            val lines = playerMsgStatus.playMsg.keys.toList()

            val curLines = playerStatus.sourceIndex
            val curEpi = playerStatus.episode
            val epi = playerMsgStatus.playMsg[lines[curLines]]?: emptyList()

            item(span = {
                // LazyGridItemSpanScope:
                // maxLineSpan
                GridItemSpan(maxLineSpan)
            }){
                LaunchedEffect(key1 = Unit){
                    vm.changePlayer(curLines, curEpi)
                }
                HomeTabRow(
                    containerColor = Color.Transparent,
                    selectedTabIndex = curLines,
                    indicatorColor = {MaterialTheme.colorScheme.secondary}
                ) {
                    for(i in lines.indices){
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
                    }
                }
            }

            itemsIndexed(epi){index, item ->
                val selected = index== curEpi
                Surface(
                    shadowElevation = 4.dp,
                    shape = CircleShape,
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(4.dp, 4.dp)
                    ,
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

fun LazyGridScope.action(
    vm: AnimPlayViewModel,
    detailStatus: DetailController.DetailStatus
){
    item(span = {
        // LazyGridItemSpanScope:
        // maxLineSpan
        GridItemSpan(maxLineSpan)
    }){
        Row(
            horizontalArrangement = Arrangement.Center
        ){
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(Icons.Filled.Star, contentDescription = "追番")
                Text(text = "追番")
            }
            
            Spacer(modifier = Modifier.size(16.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(Icons.Filled.OpenInBrowser, contentDescription = "追番")
                Text(text = "打开网站")
            }
        }
    }
}

// 番剧详情
fun LazyGridScope.detail(
    vm: AnimPlayViewModel,
    detailStatus: DetailController.DetailStatus
){
    item(span = {
        // LazyGridItemSpanScope:
        // maxLineSpan
        GridItemSpan(maxLineSpan)
    }) {
        Box(modifier = Modifier
            .padding(8.dp)
            .height(135.dp)
            .fillMaxWidth()){
            when(detailStatus){
                is DetailController.DetailStatus.None -> {}
                is DetailController.DetailStatus.Error -> {}
                is DetailController.DetailStatus.Loading -> {
                    LoadingPage(
                        modifier = Modifier.fillMaxSize()
                    )
                }
                is DetailController.DetailStatus.Completely -> {
                    Log.d("Play", detailStatus.bangumiDetail.cover)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OkImage(
                            modifier = Modifier
                                .height(135.dp)
                                .width(95.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            image = detailStatus.bangumiDetail.cover,
                            contentDescription = detailStatus.bangumiDetail.name)
                        Column (
                            modifier = Modifier.weight(1.0f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ){
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

                }
                else -> {}
            }
        }
    }


}