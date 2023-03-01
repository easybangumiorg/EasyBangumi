package com.heyanle.easybangumi4.ui.common.page.list

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.heyanle.bangumi_source_api.api.component.page.SourcePage
import com.heyanle.bangumi_source_api.api.entity.CartoonCover
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.navigationDetailed
import com.heyanle.easybangumi4.ui.common.CartoonCardWithCover
import com.heyanle.easybangumi4.ui.common.CartoonCardWithoutCover
import com.heyanle.easybangumi4.ui.common.FastScrollToTopFab
import com.heyanle.easybangumi4.ui.common.pagingCommon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/2/25 20:44.
 * https://github.com/heyanLE
 */

@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun SourceListPage(
    modifier: Modifier = Modifier,
    listPage: SourcePage.SingleCartoonPage,
    header: (@Composable ()->Unit)? = null
){

    val vm = viewModel<SourceListViewModel>(factory = SourceListViewModelFactory(listPage))
    val scope = rememberCoroutineScope()



    val pi = vm.curPager.value.collectAsLazyPagingItems()

    when(listPage){
        is SourcePage.SingleCartoonPage.WithCover -> {
            SourceListPageContentWithCover(vm = vm, pagingItems = pi, scope = scope, header = header)
        }
        is SourcePage.SingleCartoonPage.WithoutCover ->{
            SourceListPageContentWithoutCover(vm = vm, pagingItems = pi, scope = scope, header = header)
        }
    }



}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SourceListPageContentWithCover(
    modifier: Modifier = Modifier,
    vm: SourceListViewModel,
    pagingItems: LazyPagingItems<CartoonCover>,
    scope: CoroutineScope,
    header: (@Composable ()->Unit)? = null,

){
    val nav = LocalNavController.current
    var refreshing by remember { mutableStateOf(false) }
    val state = rememberPullRefreshState(refreshing, onRefresh = {
        scope.launch {
            refreshing = true
            vm.refresh()
            delay(500)
            refreshing = false
        }
    })

    val lazyGridState = rememberLazyGridState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(state)
            .then(modifier)
    ){
        pagingItems.let{ items ->
            LazyVerticalGrid(
                modifier = Modifier
                    .fillMaxSize(),
                state = lazyGridState,
                columns = GridCells.Adaptive(95.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp) ,
                contentPadding = PaddingValues(4.dp, 4.dp, 4.dp, 88.dp)
            ){
                header?.let {
                    item (
                        span = {
                            // LazyGridItemSpanScope:
                            // maxLineSpan
                            GridItemSpan(maxLineSpan)
                        }
                    ) {
                        it()
                    }
                }
                items(items.itemCount){
                    items[it]?.let {
                        CartoonCardWithCover(

                            cartoonCover = it
                        ){
                            nav.navigationDetailed(it)
                        }
                    }

                }

                pagingCommon(items)
            }
        }

        PullRefreshIndicator(
            refreshing,
            state,
            Modifier.align(Alignment.TopCenter),
            backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        FastScrollToTopFab(listState = lazyGridState, after = 30)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SourceListPageContentWithoutCover(
    modifier: Modifier = Modifier,
    vm: SourceListViewModel,
    pagingItems: LazyPagingItems<CartoonCover>,
    scope: CoroutineScope,
    header: (@Composable ()->Unit)? = null,

    ){
    val nav = LocalNavController.current
    var refreshing by remember { mutableStateOf(false) }
    val state = rememberPullRefreshState(refreshing, onRefresh = {
        scope.launch {
            refreshing = true
            vm.refresh()
            delay(500)
            refreshing = false
        }
    })

    val lazyListState = rememberLazyListState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(state)
            .then(modifier)
    ){
        pagingItems.let{ items ->
            LazyVerticalGrid(
                columns = GridCells.Adaptive(260.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(4.dp, 4.dp, 4.dp, 88.dp)
            ){
                header?.let {
                    item(
                        span = {
                            // LazyGridItemSpanScope:
                            // maxLineSpan
                            GridItemSpan(maxLineSpan)
                        }
                    ) {
                        it()
                    }
                }
                items(items.itemCount){
                    items[it]?.let {
                        CartoonCardWithoutCover(
                            cartoonCover = it
                        ){
                            nav.navigationDetailed(it)
                        }
                    }

                }
                pagingCommon(items)
            }
//            LazyColumn(
//                state = lazyListState,
//                verticalArrangement = Arrangement.spacedBy(8.dp),
//                horizontalAlignment = Alignment.CenterHorizontally,
//                contentPadding = PaddingValues(8.dp, 4.dp, 8.dp, 88.dp)
//            ){
//                header?.let {
//                    item {
//                        it()
//                    }
//                }
//                items(items.itemCount){
//                    items[it]?.let {
//                        CartoonCardWithoutCover(
//                            cartoonCover = it
//                        ){
//                            nav.navigationDetailed(it)
//                        }
//                    }
//
//                }
//                pagingCommon(items)
//            }

        }

        PullRefreshIndicator(
            refreshing,
            state,
            Modifier.align(Alignment.TopCenter),
            backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        FastScrollToTopFab(listState = lazyListState, after = 30)
    }
}