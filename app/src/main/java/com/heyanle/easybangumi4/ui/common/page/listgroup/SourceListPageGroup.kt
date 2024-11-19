package com.heyanle.easybangumi4.ui.common.page.listgroup

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easybangumi4.source_api.component.page.SourcePage
import com.heyanle.easybangumi4.ui.common.ErrorPage
import com.heyanle.easybangumi4.ui.common.FastScrollToTopFab
import com.heyanle.easybangumi4.ui.common.LoadingPage
import com.heyanle.easybangumi4.ui.common.cover_star.CoverStarCommon
import com.heyanle.easybangumi4.ui.common.page.list.SourceListPage
import com.heyanle.easybangumi4.ui.common.cover_star.CoverStarViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/2/25 21:18.
 * https://github.com/heyanLE
 */
@Composable
fun SourceListPageGroup(
    listPageGroup: SourcePage.Group
) {
    val vm =
        viewModel<SourceListGroupViewModel>(factory = SourceListGroupViewModelFactory(listPageGroup))

    LaunchedEffect(Unit) {
        if (vm.groupState is SourceListGroupViewModel.GroupState.None) {
            vm.refresh()
        }
    }

    vm.groupState.let{
        when (it) {
            SourceListGroupViewModel.GroupState.None -> {}
            SourceListGroupViewModel.GroupState.Loading -> {
                LoadingPage(
                    modifier = Modifier.fillMaxSize()
                )
            }
            is SourceListGroupViewModel.GroupState.Group -> {
                SourceListWithGroup(vm, it)
            }
            is SourceListGroupViewModel.GroupState.Error -> {
                ErrorPage(
                    modifier = Modifier.fillMaxSize(),
                    errorMsg = it.errorMsg,
                    clickEnable = true,
                    onClick = {
                        vm.refresh()
                    },
                    other = {
                        Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.click_to_retry))
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SourceListWithGroup(
    groupVM: SourceListGroupViewModel,
    groupState: SourceListGroupViewModel.GroupState.Group,
){

    val coverStarViewModel = viewModel<CoverStarViewModel>()
    val scope = rememberCoroutineScope()
    var refreshing by remember { mutableStateOf(false) }
    val lazyGridState = rememberLazyGridState()
    val lazyStaggeredGridState = rememberLazyStaggeredGridState()
    val state = rememberPullRefreshState(refreshing, onRefresh = {
        scope.launch {
            refreshing = true
            groupVM.refresh()
            delay(500)
            refreshing = false
        }
    })
    CoverStarCommon(coverStarViewModel)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(state)
    ) {
        SourceListPage(
            coverStarViewModel, groupState.list, lazyGridState, lazyStaggeredGridState
        )
        PullRefreshIndicator(
            refreshing,
            state,
            Modifier.align(Alignment.TopCenter),
            backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        FastScrollToTopFab(listState = lazyGridState, after = 20)
    }
}

//@Composable
//fun SourceListGroupTab(
//    list: List<SourcePage.SingleCartoonPage>,
//    curPage: SourcePage.SingleCartoonPage,
//    onClick: (SourcePage.SingleCartoonPage)->Unit,
//){
//    LazyRow(
//        modifier = Modifier.fillMaxWidth(),
//        horizontalArrangement = Arrangement.Center
//    ){
//        items(list) {
//            val selected = it == curPage
//            Surface(
//                shape = CircleShape,
//                modifier =
//                Modifier
//                    .padding(2.dp, 8.dp),
//                color = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
//            ) {
//                Text(
//                    modifier = Modifier
//                        .clip(CircleShape)
//                        .clickable {
//                            onClick(it)
//                        }
//                        .padding(8.dp, 4.dp),
//                    color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onBackground,
//                    fontWeight = FontWeight.W900,
//                    text = it.label,
//                    fontSize = 12.sp,
//                )
//            }
//        }
//    }
//}