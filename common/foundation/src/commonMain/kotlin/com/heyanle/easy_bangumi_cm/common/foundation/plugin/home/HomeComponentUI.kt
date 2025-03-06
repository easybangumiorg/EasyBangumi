package com.heyanle.easy_bangumi_cm.common.foundation.plugin.home

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.cash.paging.compose.collectAsLazyPagingItems
import com.heyanle.easy_bangumi_cm.common.foundation.lazy.PagingCommon
import com.heyanle.easy_bangumi_cm.common.foundation.lazy.pagingCommon
import com.heyanle.easy_bangumi_cm.plugin.api.component.media.home.HomeContent
import com.heyanle.easy_bangumi_cm.plugin.api.component.media.home.HomePage

/**
 * Created by heyanlin on 2025/3/3.
 */
@Composable
fun HomeComponentContent(
    content: HomeContent
) {



}

@Composable
fun HomeComponentGroupPage(
    group: HomePage.Group,
    needRefresh: Boolean,
) {



}

//@Composable
//fun HomeComponentSinglePage(
//    singleContent: HomeContent.SinglePage,
//) {
//
//    val vm = viewModel<SingleHomePageViewModel>(factory = SingleHomePageViewModelFactory(singleContent.singlePage))
//    val pager = vm.pager
//    val pagingState = pager.value.collectAsLazyPagingItems()
//
//    val lazyState = rememberLazyGridState()
//
//
//    LazyVerticalGrid(
//        state = lazyState,
//        columns = GridCells.Adaptive(100.dp),
//    ) {
//        items(pagingState.itemCount) {
//
//        }
//        pagingCommon(pagingState)
//    }
//    PagingCommon(pagingState)
//
//}

