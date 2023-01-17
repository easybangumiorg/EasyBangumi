package com.heyanle.easybangumi.ui.home.home

import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.heyanle.easybangumi.LocalNavController
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.navigationPlay
import com.heyanle.easybangumi.ui.common.*

/**
 * Created by HeYanLe on 2023/1/9 21:29.
 * https://github.com/heyanLE
 */


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimHome(){
    HomeSourceContainer {
        val vm = viewModel<AnimHomeViewModel>(factory = AnimHomeViewModelFactory(it))
        AnimHomePage(vm = vm)
    }
}
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimHomePage(
    vm: AnimHomeViewModel
){
    val labels = vm.homes.map {
        it.getLabel()
    }

    val status by vm.homeResultFlow.collectAsState(initial = AnimHomeViewModel.HomeAnimState.None)
    if(status == AnimHomeViewModel.HomeAnimState.None){
        // 这里要成闭环
        Box(modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .clickable {
                vm.refresh()
            })
        return
    }
    val sta = status

    val lazyListState = rememberLazyListState()

    var isHeaderShowForever by remember {
        mutableStateOf(false)
    }
    val endReached by remember {
        derivedStateOf {
            val lastVisibleItem = lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()
                ?: return@derivedStateOf true

            lastVisibleItem.index == lazyListState.layoutInfo.totalItemsCount - 1
        }
    }
    Log.d("AnimHome", "label not empty")
    ScrollHeaderBox(
        canScroll = {
            if(isHeaderShowForever){
                false
            }else{
                !(it.y < 0 && endReached)
            }
        },
        modifier = Modifier.fillMaxSize(),
        header = { dp ->
            KeyTabRow(
                modifier = Modifier.offset(0.dp, dp),
                selectedTabIndex = sta.curIndex,
                textList = labels,
                onItemClick = {
                    vm.changeHomeSource(it)
                })
        },
        content = {
            AnimatedContent(
                targetState = sta,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300, delayMillis = 300)) with
                            fadeOut(animationSpec = tween(300, delayMillis = 0))
                },
            ){ stat ->
                when (stat) {
                    is AnimHomeViewModel.HomeAnimState.Loading -> {
                        LaunchedEffect(key1 = Unit){
                            isHeaderShowForever = true
                        }

                        LoadingPage(
                            modifier = Modifier
                                .fillMaxSize()
                        )
                    }

                    is AnimHomeViewModel.HomeAnimState.Completely -> {
                        LaunchedEffect(key1 = Unit){
                            isHeaderShowForever = false
                        }
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize(),
                            contentPadding = it,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            state = lazyListState
                        ){
                            animHomePage(state = stat)
                        }
                    }

                    is AnimHomeViewModel.HomeAnimState.Error -> {
                        LaunchedEffect(key1 = Unit){
                            isHeaderShowForever = true
                        }
                        ErrorPage(
                            modifier = Modifier
                                .fillMaxSize(),
                            errorMsg = if(stat.error.isParserError) stat.error.throwable.message?:"" else stringResource(id = R.string.net_error),
                            clickEnable = true,
                            onClick = {
                                vm.refresh()
                            },
                            other = {
                                Text(text = stringResource(id = R.string.click_to_retry))
                            }
                        )
                    }
                    else -> {}
                }
            }
        }
    )
    Box(modifier = Modifier){
        FastScrollToTopFab(listState = lazyListState)
    }
}

fun LazyListScope.animHomePage(state: AnimHomeViewModel.HomeAnimState.Completely){

    items(state.keyList){ key ->
        val nav = LocalNavController.current
        Text(
            modifier = Modifier.padding(8.dp, 4.dp),
            text = key,
            color = MaterialTheme.colorScheme.secondary,
            fontSize = MaterialTheme.typography.titleMedium.fontSize,
            fontWeight = FontWeight.W900,
        )
        val da = state.data[key]?: emptyList()
        LazyRow(){
            items(da.size?:0){ i ->
                val item = da[i]
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            nav.navigationPlay(item)
                        }
                        .padding(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    OkImage(
                        image = item.cover,
                        contentDescription = item.name,
                        modifier = Modifier
                            .height(135.dp)
                            .width(95.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    var needEnter by remember() {
                        mutableStateOf(false)
                    }
                    Text(
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(95.dp),
                        text = "${item.name}${if(needEnter) "\n " else ""}",
                        maxLines = 2,
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                        overflow = TextOverflow.Ellipsis,
                        onTextLayout = {
                            if(it.lineCount < 2){
                                needEnter = true
                            }
                        }
                    )
                }
            }
        }
    }
}