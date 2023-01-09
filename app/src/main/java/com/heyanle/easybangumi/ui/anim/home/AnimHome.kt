package com.heyanle.easybangumi.ui.anim.home

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.ui.common.ErrorPage
import com.heyanle.easybangumi.ui.common.KeyTabRow
import com.heyanle.easybangumi.ui.common.LoadingPage
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/1/9 21:29.
 * https://github.com/heyanLE
 */
@Composable
fun AnimHome(){


    val vm = viewModel<AnimHomeViewModel>()
    val status by vm.homeResultFlow.collectAsState(initial = null)
    if(status == null){
        // 这里要成闭环
        Box(modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .clickable {
                vm.refresh()
            })
    }
    val sta = status?:return

    when (sta) {
        is AnimHomeViewModel.HomeAnimState.Loading -> {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                KeyTabRow(selectedTabIndex = sta.curIndex, textList = vm.homeTitle, onItemClick = {
                    vm.changeHomeSource(it)
                })
                LoadingPage(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                )
            }
        }

        is AnimHomeViewModel.HomeAnimState.Completely -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
            ){
                item {
                    KeyTabRow(selectedTabIndex = sta.curIndex, textList = vm.homeTitle, onItemClick = {
                        vm.changeHomeSource(it)
                    })
                }
                animHomePage(state = sta)
            }

        }

        is AnimHomeViewModel.HomeAnimState.Error -> {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                KeyTabRow(selectedTabIndex = sta.curIndex, textList = vm.homeTitle, onItemClick = {
                    vm.changeHomeSource(it)
                })
                ErrorPage(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    errorMsg = if(sta.error.isParserError) sta.error.throwable.message?:"" else stringResource(id = R.string.net_error),
                    clickEnable = true,
                    onClick = {
                        vm.refresh()
                    },
                    other = {
                        Text(text = stringResource(id = R.string.click_to_retry))
                    }
                )
            }
        }
    }
}

fun LazyListScope.animHomePage(state: AnimHomeViewModel.HomeAnimState.Completely){
    items(state.keyList){ key ->
        Text(text = key, color = MaterialTheme.colorScheme.secondary)
        
        val da = state.data[key]?: emptyList()
        LazyRow(){
            items(da.size?:0){
                val item = da[it]
                Column(
                    
                ) {
                    AsyncImage(
                        model = ImageRequest
                            .Builder(LocalContext.current)
                            .decoderFactory(
                                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                                    ImageDecoderDecoder.Factory()
                                else GifDecoder.Factory()
                            )
                            .data(item.cover).build(),
                        contentDescription = item.name,
                    )
                    Text(text = item.name)
                }
            }
        }
        
    }
}