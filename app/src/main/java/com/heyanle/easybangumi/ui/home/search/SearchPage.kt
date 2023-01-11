package com.heyanle.easybangumi.ui.home.search

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemsIndexed
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.ui.common.EmptyPage
import com.heyanle.easybangumi.ui.common.ErrorPage
import com.heyanle.easybangumi.ui.common.LoadingPage
import com.heyanle.easybangumi.ui.common.OkImage
import com.heyanle.easybangumi.ui.common.moeSnackBar
import com.heyanle.easybangumi.utils.stringRes
import com.heyanle.lib_anim.entity.Bangumi

/**
 * Created by HeYanLe on 2023/1/10 18:54.
 * https://github.com/heyanLE
 */
@OptIn(ExperimentalPagerApi::class, ExperimentalAnimationApi::class)
@Composable
fun SearchPage(
    isShowTabForever: MutableState<Boolean>,
    padding: PaddingValues,
    vm: SearchViewModel,
    controller: SearchPageController,
    isEnable: Boolean, // 是否刷新
    lazyListState: LazyListState = rememberLazyListState(),
){
    val keyword by vm.keywordState
    if(isEnable){
        val lastItem by remember() {
            derivedStateOf {
                val lastVisibleItem = lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()
                    ?: return@derivedStateOf -1
                lastVisibleItem.index
            }
        }

        val endReached by remember(lastItem) {
            derivedStateOf {
                lastItem == -1 || lastItem == lazyListState.layoutInfo.totalItemsCount-1
            }
        }
        SideEffect {
            controller.isCurLast = endReached
        }
    }

    if(isEnable){
        LaunchedEffect(key1 = keyword){
            if(controller.keywordFlow.value != keyword){
                Log.d("SearchPage", "${controller.keywordFlow.value} $keyword")
                controller.refreshKeyword(keyword)
            }

        }
    }
    val state by controller.pagerFlow.collectAsState(initial = SearchPageController.EmptyBangumi)
    val sta = state

    AnimatedContent(
        targetState = sta,
        transitionSpec = {
            fadeIn(animationSpec = tween(300, delayMillis = 300)) with
                    fadeOut(animationSpec = tween(300, delayMillis = 0))
        },
    ){ newState ->
        when(newState){
            is SearchPageController.SearchPageState.Empty -> {
                LaunchedEffect(key1 = Unit){
                    isShowTabForever.value = true
                }
                EmptyPage(
                    modifier = Modifier
                        .fillMaxSize(),
                    emptyMsg = stringResource(id = R.string.please_input_keyword_to_search)
                )
            }
            is SearchPageController.SearchPageState.Page -> {
                val lazyPagingItems = newState.flow.collectAsLazyPagingItems()
                if(lazyPagingItems.loadState.refresh == LoadState.Loading){
                    LaunchedEffect(key1 = Unit){
                        isShowTabForever.value = true
                    }
                    LoadingPage(
                        modifier = Modifier
                            .fillMaxSize(),
                    )
                }else{
                    LaunchedEffect(key1 = Unit){
                        isShowTabForever.value = false
                    }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize(),
                        state = lazyListState,
                        contentPadding = PaddingValues(0.dp, padding.calculateTopPadding() + 6.dp, 0.dp, 6.dp),
                    ) {
                        itemsIndexed(lazyPagingItems){_, v ->
                            v?.let {
                                BangumiSearchItem(bangumi = it){

                                }
                            }
                        }
                        if (lazyPagingItems.loadState.append == LoadState.Loading) {
                            item {
                                LoadingPage(
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }else if(lazyPagingItems.loadState.append is LoadState.Error){
                            item {
                                val errorMsg = (lazyPagingItems.loadState.append as? LoadState.Error)?.error?.message?: stringRes(R.string.net_error)
                                ErrorPage(
                                    modifier = Modifier.fillMaxWidth(),
                                    errorMsg = errorMsg,
                                    clickEnable = true,
                                    onClick = {
                                        lazyPagingItems.retry()
                                    }
                                )
                            }
                        }else {
                            item {  
                                Text(modifier = Modifier.fillMaxWidth().padding(0.dp, 2.dp), textAlign = TextAlign.Center,text = stringResource(id = R.string.list_most_bottom))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BangumiSearchItem(
    modifier: Modifier = Modifier,
    bangumi: Bangumi,
    onClick: (Bangumi)->Unit,
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick(bangumi)
            }
            .padding(16.dp, 8.dp)
            .then(modifier),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OkImage(
            image = bangumi.cover,
            contentDescription = bangumi.name,
            modifier = Modifier
                .height(120.dp)
                .width(90.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = bangumi.name, maxLines = 2, color = MaterialTheme.colorScheme.onBackground)
            Text(text = bangumi.intro, maxLines = 4, color = MaterialTheme.colorScheme.onBackground.copy(0.6f))
        }
    }
}