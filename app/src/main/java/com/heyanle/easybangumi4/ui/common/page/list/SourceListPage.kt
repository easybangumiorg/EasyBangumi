package com.heyanle.easybangumi4.ui.common.page.list

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.heyanle.bangumi_source_api.api.page.SourcePage
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.ui.common.CartoonCard
import com.heyanle.easybangumi4.ui.common.ErrorPage
import com.heyanle.easybangumi4.ui.common.FastScrollToTopFab
import com.heyanle.easybangumi4.ui.common.LoadingPage
import com.heyanle.easybangumi4.utils.stringRes
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
    lazyGridState: LazyGridState = rememberLazyGridState(),
    header: (@Composable ()->Unit)? = null
){

    val vm = viewModel<SourceListViewModel>(factory = SourceListViewModelFactory(listPage))
    val scope = rememberCoroutineScope()
    var refreshing by remember { mutableStateOf(false) }
    val state = rememberPullRefreshState(refreshing, onRefresh = {
        scope.launch {
            refreshing = true
            vm.refresh()
            delay(500)
            refreshing = false
        }
    })

    val pi = vm.curPager.value.collectAsLazyPagingItems()


    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(state)
            .then(modifier)
    ){
        pi.let{ items ->
            LazyVerticalGrid(
                modifier = Modifier
                    .fillMaxSize(),
                state = lazyGridState,
                columns = GridCells.Adaptive(95.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement =Arrangement.spacedBy(4.dp) ,
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
                        CartoonCard(

                            cartoonCover = it
                        ){

                        }
                    }

                }

                when(items.loadState.refresh){
                    is LoadState.Loading -> {
                        item(
                            span = {
                                // LazyGridItemSpanScope:
                                // maxLineSpan
                                GridItemSpan(maxLineSpan)
                            }
                        ) {
                            LoadingPage(
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }

                    is LoadState.Error -> {
                        item(
                            span = {
                                // LazyGridItemSpanScope:
                                // maxLineSpan
                                GridItemSpan(maxLineSpan)
                            }
                        ) {
                            val errorMsg =
                                (items.loadState.refresh as? LoadState.Error)?.error?.message
                                    ?: stringRes(
                                        com.heyanle.easy_i18n.R.string.net_error
                                    )
                            ErrorPage(
                                modifier = Modifier.fillMaxWidth(),
                                errorMsg = errorMsg,
                                clickEnable = true,
                                other = {
                                    Text(text = stringResource(id = R.string.click_to_retry))
                                },
                                onClick = {
                                    items.refresh()
                                }
                            )
                        }
                    }

                    else -> {

                    }
                }

                when (items.loadState.append) {
                    is LoadState.Loading -> {
                        item(
                            span = {
                                // LazyGridItemSpanScope:
                                // maxLineSpan
                                GridItemSpan(maxLineSpan)
                            }
                        ) {
                            LoadingPage(
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }

                    is LoadState.Error -> {
                        item(
                            span = {
                                // LazyGridItemSpanScope:
                                // maxLineSpan
                                GridItemSpan(maxLineSpan)
                            }
                        ) {
                            val errorMsg =
                                (items.loadState.append as? LoadState.Error)?.error?.message
                                    ?: stringRes(
                                        com.heyanle.easy_i18n.R.string.net_error
                                    )
                            ErrorPage(
                                modifier = Modifier.fillMaxWidth(),
                                errorMsg = errorMsg,
                                clickEnable = true,
                                other = {
                                    Text(text = stringResource(id = R.string.click_to_retry))
                                },
                                onClick = {
                                    items.retry()
                                }
                            )
                        }
                    }

                    else -> {

                    }
                }
            }
        }

        PullRefreshIndicator(
            refreshing,
            state,
            Modifier.align(Alignment.TopCenter),
            backgroundColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
        FastScrollToTopFab(listState = lazyGridState, after = 30)
    }

}