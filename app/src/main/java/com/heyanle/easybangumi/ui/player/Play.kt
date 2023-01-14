package com.heyanle.easybangumi.ui.player

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.TextButton
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import com.heyanle.easybangumi.BangumiApp
import com.heyanle.easybangumi.theme.EasyThemeController
import com.heyanle.easybangumi.ui.common.HomeTabItem
import com.heyanle.easybangumi.ui.common.HomeTabRow
import com.heyanle.easybangumi.ui.common.LoadingPage
import com.heyanle.easybangumi.ui.common.OkImage
import com.heyanle.easybangumi.ui.home.animSubPageItems
import com.heyanle.easybangumi.utils.dip2px
import com.heyanle.eplayer_core.player.IPlayerEngine
import com.heyanle.eplayer_core.player.IPlayerEngineFactory
import com.heyanle.eplayer_core.render.IRender
import com.heyanle.eplayer_core.render.IRenderFactory
import com.heyanle.lib_anim.entity.BangumiSummary
import com.heyanle.player_controller.EasyPlayerView
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

    LaunchedEffect(key1 = Unit){
        PlayerController.onScreenLaunch()
    }
    DisposableEffect(key1 = Unit){
        onDispose {
            PlayerController.onScreenDispose()
        }
    }

    val vm: AnimPlayViewModel = PlayerController.getAnimPlayViewModel(BangumiSummary(source, detail))

    val playerStatus by vm.playerStatus.collectAsState(initial = null)
    val detailStatus by vm.detailController.detailFlow.collectAsState(initial = null)
    val playMsgStatus by vm.playMsgController.flow.collectAsState(initial = null)

    LaunchedEffect(key1 = Unit){
        vm.load()
    }

    Scaffold(
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(it)
            ) {
                playerStatus?.let {
                    Video(vm, playerStatus = it)
                }

                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    playMsgStatus?.let { playMsg ->
                        playerStatus?.let {
                            PlayerMsg(vm, playerMsgStatus = playMsg, it)
                        }

                    }
                    detailStatus?.let {
                        Detail(vm, detailStatus = it)
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

    var height by remember {
        mutableStateOf(200.dp)
    }
    Box(
        modifier = Modifier
            .height(height)
            .fillMaxWidth()
            .onSizeChanged {
                height = with(density){((it.width/(PlayerController.ratioWidth))*PlayerController.ratioHeight).toDp()}
            }
    ){
        when(playerStatus){
            is AnimPlayViewModel.PlayerStatus.None -> {}
            is AnimPlayViewModel.PlayerStatus.Play -> {

//            LaunchedEffect(key1 = Unit){
//                PlayerController.newPlayer(playerStatus)
//            }
                if(PlayerController.canAddToCompose.value){
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = {
                            EasyPlayerView(it).apply {
                                basePlayerView.attachToPlayer(PlayerController.exoPlayer)
                            }
                        }
                    ){
                        PlayerController.newPlayer(playerStatus)
                        PlayerController.exoPlayer.setVideoSurfaceView(it.basePlayerView.surfaceView)
                        // PlayerController.onContainer(it)
                    }
                }

            }
            is AnimPlayViewModel.PlayerStatus.Error -> {}
            is AnimPlayViewModel.PlayerStatus.Loading -> {}
        }
    }



}


// 播放线路 & 集
@Composable
fun PlayerMsg(
    vm: AnimPlayViewModel,
    playerMsgStatus: PlayMsgController.PlayMsgStatus,
    playerStatus: AnimPlayViewModel.PlayerStatus,
){

    when(playerMsgStatus){
        is PlayMsgController.PlayMsgStatus.None -> {}
        is PlayMsgController.PlayMsgStatus.Error -> {}
        is PlayMsgController.PlayMsgStatus.Loading -> {
            LoadingPage()
        }
        is PlayMsgController.PlayMsgStatus.Completely -> {

            val lines = playerMsgStatus.playMsg.keys.toList()

            val curLines = playerStatus.sourceIndex
            val curEpi = playerStatus.episode

            LaunchedEffect(key1 = Unit){
                vm.changePlayer(curLines, curEpi)
            }

            val epi = playerMsgStatus.playMsg[lines[curLines]]?: emptyList()

            HomeTabRow(
                containerColor = Color.Transparent,
                selectedTabIndex = curLines
            ) {
                for(i in lines.indices){
                    HomeTabItem(
                        selected = i == curLines,
                        text = {
                            Text(lines[i])
                        },
                        onClick = {
                            vm.changeLines(i)
                        }
                    )
                }
            }

            LazyRow(){
                itemsIndexed(epi){index, item ->
                    val selected = index== curEpi
                    Surface(
                        shadowElevation = 4.dp,
                        shape = CircleShape,
                        modifier =
                        Modifier
                            .padding(2.dp, 8.dp)
                        ,
                        color = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.background,
                    ) {
                        Text(
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    vm.changePlayer(curLines, index)
                                }
                                .padding(8.dp, 4.dp),
                            color = if (selected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.W900,
                            text = item,
                            fontSize = 18.sp,
                        )
                    }
                }
            }
        }
    }

}

// 番剧详情
@Composable
fun Detail(
    vm: AnimPlayViewModel,
    detailStatus: DetailController.DetailStatus
){
    when(detailStatus){
        is DetailController.DetailStatus.None -> {}
        is DetailController.DetailStatus.Error -> {}
        is DetailController.DetailStatus.Loading -> {
            LoadingPage()
        }
        is DetailController.DetailStatus.Completely -> {
            Log.d("Play", detailStatus.bangumiDetail.cover)
            Column() {
                OkImage(
                    modifier = Modifier
                        .height(135.dp)
                        .width(95.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    image = detailStatus.bangumiDetail.cover,
                    contentDescription = detailStatus.bangumiDetail.name)
                Text(text = detailStatus.bangumiDetail.description)
            }

        }
    }

}