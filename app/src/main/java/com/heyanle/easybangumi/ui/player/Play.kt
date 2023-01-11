package com.heyanle.easybangumi.ui.player

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easybangumi.ui.common.LoadingPage
import com.heyanle.easybangumi.ui.common.OkImage
import com.heyanle.easybangumi.ui.common.moeSnackBar

/**
 * Created by HeYanLe on 2023/1/11 15:27.
 * https://github.com/heyanLE
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Play(
    source: String,
    detail: String,
){

    val vm: AnimPlayViewModel = viewModel(factory = AnimPlayerViewModelFactory(source, detail))

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
@Composable
fun Video(
    vm: AnimPlayViewModel,
    playerStatus: AnimPlayViewModel.PlayerStatus,
){
    when(playerStatus){
        is AnimPlayViewModel.PlayerStatus.None -> {}
        is AnimPlayViewModel.PlayerStatus.Play -> {
            Text(text = "play ${playerStatus.url}")
        }
        is AnimPlayViewModel.PlayerStatus.Error -> {}
        is AnimPlayViewModel.PlayerStatus.Loading -> {}
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

            LazyRow(){
                itemsIndexed(lines){index, item ->
                    TextButton(onClick = {
                        vm.changeLines(index)
                    }) {
                        Text(text = "${item}${if (index == curLines) "√" else ""}")
                    }
                }
            }
            LazyRow(){
                itemsIndexed(epi){index, item ->
                    TextButton(onClick = {
                        vm.changePlayer(curLines, index)
                    }) {
                        Text(text = "${item}${if (index == curEpi) "√" else ""}")
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