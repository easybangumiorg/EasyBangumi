package com.heyanle.easybangumi4.ui.search.searchpage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.google.accompanist.flowlayout.FlowRow
import com.heyanle.bangumi_source_api.api.component.search.SearchComponent
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.navigationDetailed
import com.heyanle.easybangumi4.ui.common.EmptyPage
import com.heyanle.easybangumi4.ui.common.FastScrollToTopFab
import com.heyanle.easybangumi4.ui.common.PagingCommon
import com.heyanle.easybangumi4.ui.common.pagingCommon
import com.heyanle.easybangumi4.ui.search.SearchViewModel
import com.heyanle.easybangumi4.ui.sourcehome.search.CartoonSearchItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/3/27 22:57.
 * https://github.com/heyanLE
 */
@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun SearchPage(
    isShow: Boolean,
    searchComponent: SearchComponent,
    searchViewModel: SearchViewModel,
) {

    val keyboard = LocalSoftwareKeyboardController.current

    val realSearchKey by searchViewModel.searchFlow.collectAsState()
    val vm = SearchPageViewModelFactory.newViewModel(searchComponent = searchComponent)

    val scope = rememberCoroutineScope()


    LaunchedEffect(key1 = realSearchKey, key2 = isShow) {
        if (realSearchKey != vm.curKeyWord && isShow) {
            vm.newSearchKey(realSearchKey)
        }
    }

    val pageFlow by vm.searchPagingState
    val page = pageFlow?.collectAsLazyPagingItems()

    val nav = LocalNavController.current

    var refreshing by remember { mutableStateOf(false) }
    val state = rememberPullRefreshState(refreshing, onRefresh = {
        scope.launch {
            refreshing = true
            vm.newSearchKey(vm.curKeyWord)
            delay(500)
            refreshing = false
        }
    })

    val lazyListState = rememberLazyListState()

    if (page == null) {
        SearchEmptyPage(
            searchViewModel.searchHistory.toList(),
            onHistoryClick = {
                searchViewModel.search(it)
            },
            onClearHistory = {
                searchViewModel.clearHistory()
            },
        )
    } else {


        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(state)
        ) {


            if (page.itemCount > 0) {
                LazyColumn(
                    modifier = Modifier.nestedScroll(object: NestedScrollConnection {
                        override fun onPostScroll(
                            consumed: Offset,
                            available: Offset,
                            source: NestedScrollSource
                        ): Offset {
                            keyboard?.hide()
                            return super.onPostScroll(consumed, available, source)
                        }
                    }),
                    state = lazyListState,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    contentPadding = PaddingValues(4.dp, 4.dp, 4.dp, 88.dp)
                ) {
                    items(page) {
                        it?.let {
                            CartoonSearchItem(cartoonCover = it) {
                                nav.navigationDetailed(it)
                            }
                        }
                    }
                    pagingCommon(page)

                }
            }
            PagingCommon(items = page)


            PullRefreshIndicator(
                refreshing,
                state,
                Modifier.align(Alignment.TopCenter),
                backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            FastScrollToTopFab(listState = lazyListState, after = 10)

        }
    }


}

@Composable
fun SearchEmptyPage(
    historyList: List<String>,
    onHistoryClick: (String) -> Unit,
    onClearHistory: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        EmptyPage(
            modifier = Modifier
                .fillMaxSize(),
            emptyMsg = stringResource(id = R.string.please_input_keyword_to_search)
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(id = R.string.history))
                IconButton(onClick = {
                    onClearHistory()
                }) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = stringResource(id = R.string.delete)
                    )
                }
            }

            FlowRow(
                mainAxisSpacing = 4.dp,
                crossAxisSpacing = 4.dp
            ) {
                historyList.forEach {
                    Surface(
                        shadowElevation = 4.dp,
                        shape = CircleShape,
                        modifier =
                        Modifier
                            .padding(2.dp, 8.dp),
                        color = MaterialTheme.colorScheme.secondary,
                    ) {
                        Text(
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    onHistoryClick(it)
                                }
                                .padding(8.dp, 4.dp),
                            color = MaterialTheme.colorScheme.onSecondary,
                            fontWeight = FontWeight.W900,
                            text = it,
                            fontSize = 12.sp,
                        )
                    }
                }
            }
        }

    }
}