package com.heyanle.easybangumi4.ui.sourcehome.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.heyanle.bangumi_source_api.api.component.search.SearchComponent
import com.heyanle.bangumi_source_api.api.entity.CartoonCover
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.navigationDetailed
import com.heyanle.easybangumi4.ui.common.CartoonCard
import com.heyanle.easybangumi4.ui.common.FastScrollToTopFab
import com.heyanle.easybangumi4.ui.common.pagingCommon
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/3/1 16:07.
 * https://github.com/heyanLE
 */

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SourceSearchPage(
    modifier: Modifier = Modifier,
    keyword: String,
    searchSource: SearchComponent,
    header: (@Composable () -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    val vm = viewModel<SearchViewModel>(factory = SearchViewModelFactory(searchSource))

    LaunchedEffect(key1 = keyword) {
        vm.search(keyword)
    }

    val nav = LocalNavController.current
    var refreshing by remember { mutableStateOf(false) }
    val state = rememberPullRefreshState(refreshing, onRefresh = {
        scope.launch {
            refreshing = true
            vm.search(keyword)
            delay(500)
            refreshing = false
        }
    })

    val lazyListState = rememberLazyListState()
    val pi = vm.curPager.value.collectAsLazyPagingItems()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(state)
            .then(modifier)
    ) {
        if(!vm.isEmpty.value){
            LazyColumn(
                state = lazyListState,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(4.dp, 4.dp, 4.dp, 88.dp)
            ) {
                header?.let {
                    item {
                        it()
                    }
                }
                items(pi) {
                    it?.let {
                        CartoonSearchItem(cartoonCover = it){
                            nav.navigationDetailed(it)
                        }
                    }
                }
                pagingCommon(pi)

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


}


@Composable
fun CartoonSearchItem(
    modifier: Modifier = Modifier,
    cartoonCover: CartoonCover,
    onClick: (CartoonCover) -> Unit,
) {
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .clickable {
                onClick(cartoonCover)
            }
            .padding(4.dp)
            .then(modifier),
        horizontalArrangement = Arrangement.Start
    ) {
        if(cartoonCover.coverUrl != null){
            CartoonCard(cover = cartoonCover.coverUrl?:"", name = cartoonCover.title, source = null)

            Spacer(modifier = Modifier.size(8.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(text = cartoonCover.title, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.size(8.dp))
                Text(text = cartoonCover.intro?:"", style = MaterialTheme.typography.bodyLarge,)
            }
        }else{
            Text(text = cartoonCover.title, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            Text(text = cartoonCover.intro?:"", maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}