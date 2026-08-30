package com.heyanle.easybangumi4.v2.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.navigationDetailed
import com.heyanle.easybangumi4.plugin.api.entity.toIdentify
import com.heyanle.easybangumi4.ui.common.CartoonCardWithCover
import com.heyanle.easybangumi4.ui.common.FastScrollToTopFab
import com.heyanle.easybangumi4.ui.common.PagingCommonSourceSearch
import com.heyanle.easybangumi4.ui.common.cover_star.CoverStarViewModel
import com.heyanle.easybangumi4.ui.common.pagingCommonSourceSearch
import com.heyanle.easybangumi4.ui.search_migrate.search.SearchViewModel
import com.heyanle.easybangumi4.ui.search_migrate.search.normal.NormalSearchViewModel
import com.heyanle.easybangumi4.v2.theme.V2Theme
import com.heyanle.easybangumi4.v2.theme.V2Tokens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** V2-owned copy of the V1 single-source paging result page. */
@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun NormalSearchPageV1CopyV2(
    visible: Boolean,
    searchViewModel: SearchViewModel,
    pageViewModel: NormalSearchViewModel,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val request by searchViewModel.searchRequestFlow.collectAsState()
    val starViewModel = viewModel<CoverStarViewModel>()
    val starred = starViewModel.stateFlow.collectAsState().value.identifySet
    val scope = rememberCoroutineScope()
    LaunchedEffect(request, visible) { if (visible) pageViewModel.submitSearch(request) }
    val pageFlow by pageViewModel.searchPagingState
    val page = pageFlow?.collectAsLazyPagingItems() ?: return
    val nav = LocalNavController.current
    val listState = rememberLazyListState()
    val haptic = LocalHapticFeedback.current
    val refreshState = rememberPullRefreshState(refreshing = pageViewModel.isRefreshing.value, onRefresh = {
        scope.launch {
            pageViewModel.isRefreshing.value = true
            pageViewModel.newSearchKey(pageViewModel.curKeyWord, force = true)
            delay(500)
            pageViewModel.isRefreshing.value = false
        }
    })
    Box(Modifier.fillMaxSize().pullRefresh(refreshState)) {
        if (page.itemCount > 0) {
            LazyColumn(
                state = listState,
                modifier = Modifier.nestedScroll(object : NestedScrollConnection {
                    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                        keyboard?.hide()
                        return super.onPostScroll(consumed, available, source)
                    }
                }),
                contentPadding = PaddingValues(4.dp, 4.dp, 4.dp, 88.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                items(page.itemCount) { index ->
                    page[index]?.let { cover ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                                .padding(horizontal = 4.dp),
                        ) {
                            CartoonCardWithCover(
                                modifier = Modifier.width(100.dp),
                                star = cover.toIdentify() in starred,
                                cartoonCover = cover,
                                onClick = { nav.navigationDetailed(it) },
                                onLongPress = {
                                    starViewModel.dispatchStar(it)
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                },
                                v2Presentation = true,
                            )
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f).fillMaxHeight().padding(vertical = 4.dp)) {
                                Text(
                                    text = cover.title,
                                    color = V2Tokens.TextPrimary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = cover.intro.orEmpty(),
                                    modifier = Modifier.padding(top = 8.dp),
                                    color = V2Tokens.TextSecondary,
                                    maxLines = 4,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
                pagingCommonSourceSearch(page) { exception ->
                    pageViewModel.onSearchNeedWebCheck(exception, onRetry = page::retry)
                }
            }
        }
        PagingCommonSourceSearch(page) { exception ->
            pageViewModel.onSearchNeedWebCheck(exception, onRetry = page::retry)
        }
        PullRefreshIndicator(
            pageViewModel.isRefreshing.value,
            refreshState,
            Modifier.align(Alignment.TopCenter),
            backgroundColor = V2Tokens.SurfaceMuted,
            contentColor = V2Theme.colors.accent,
        )
        FastScrollToTopFab(listState = listState, after = 10)
    }
}
