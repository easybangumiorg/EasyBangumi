package com.heyanle.easybangumi4.ui.search_migrate.search.normal

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.navigationDetailed
import com.heyanle.easybangumi4.plugin.source.LocalSourceBundleController
import com.heyanle.easybangumi4.source_api.entity.CartoonCover
import com.heyanle.easybangumi4.source_api.entity.toIdentify
import com.heyanle.easybangumi4.ui.common.CartoonCard
import com.heyanle.easybangumi4.ui.common.FastScrollToTopFab
import com.heyanle.easybangumi4.ui.common.PagingCommon
import com.heyanle.easybangumi4.ui.common.TabIndicator
import com.heyanle.easybangumi4.ui.common.cover_star.CoverStarCommon
import com.heyanle.easybangumi4.ui.common.pagingCommon
import com.heyanle.easybangumi4.ui.common.cover_star.CoverStarViewModel
import com.heyanle.easybangumi4.ui.search_migrate.search.SearchViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Created by heyanlin on 2023/12/18.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ColumnScope.NormalSearch(
    defSourceKey: String,
    searchViewModel: SearchViewModel,
) {
    val scope = rememberCoroutineScope()
    val searchComponents = LocalSourceBundleController.current.searches()
    val pagerState =
        rememberPagerState(if (searchComponents.isNotEmpty()) searchComponents.indexOfFirst { it.source.key == defSourceKey }
            .coerceIn(0, searchComponents.size - 1) else 0, 0f) {
            searchComponents.size
        }

    ScrollableTabRow(
        edgePadding = 0.dp,
        selectedTabIndex = pagerState.currentPage,
        divider = {},
        indicator = {
            val index = 0.coerceAtLeast(
                pagerState.currentPage
            )
            if (index >= 0 && index < it.size) {
                TabIndicator(
                    currentTabPosition = it[index]
                )

            }

        },
        modifier = Modifier.fillMaxWidth()
    ) {
        searchComponents.forEachIndexed { index, searchComponent ->
            Tab(
                selected = index == pagerState.currentPage,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
                text = {
                    Text(text = searchComponent.source.label)

                },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onBackground
            )
        }
    }
    Divider()

    HorizontalPager(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        state = pagerState
    ) {

        searchComponents.getOrNull(it)?.let { searchComponent ->
            val normalSearchViewModel = viewModel<NormalSearchViewModel>(
                factory = NormalSearchViewModelFactory(searchComponent),
                viewModelStoreOwner = searchViewModel.viewModelOwnerMap.getViewModelStoreOwner(
                    searchComponent.source.key
                )
            )
            NormalSearchPage(
                isShow = it == pagerState.currentPage,
                searchViewModel = searchViewModel,
                normalSearchViewModel = normalSearchViewModel
            )
        }

    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NormalSearchPage(
    isShow: Boolean,
    searchViewModel: SearchViewModel,
    normalSearchViewModel: NormalSearchViewModel
) {

    val keyboard = LocalSoftwareKeyboardController.current

    val realSearchKey by searchViewModel.searchFlow.collectAsState()

    val starVm = viewModel<CoverStarViewModel>()
    val starSet = starVm.stateFlow.collectAsState().value.identifySet

    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = realSearchKey, key2 = isShow) {
        if (realSearchKey != normalSearchViewModel.curKeyWord && isShow) {
            normalSearchViewModel.newSearchKey(realSearchKey)
        }
    }

    val pageFlow by normalSearchViewModel.searchPagingState
    val page = pageFlow?.collectAsLazyPagingItems()

    val nav = LocalNavController.current

    val state = rememberPullRefreshState(normalSearchViewModel.isRefreshing.value, onRefresh = {
        scope.launch {
            normalSearchViewModel.isRefreshing.value = true
            normalSearchViewModel.newSearchKey(normalSearchViewModel.curKeyWord)
            // 自欺欺人刷新标记
            delay(500)
            normalSearchViewModel.isRefreshing.value = false
        }
    })

    val lazyListState = rememberLazyListState()
    val haptic = LocalHapticFeedback.current

    CoverStarCommon(starVm)
    if (page != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(state)
        ) {


            if (page.itemCount > 0) {
                LazyColumn(
                    modifier = Modifier.nestedScroll(object : NestedScrollConnection {
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
                    items(page.itemCount) {
                        page[it]?.let {
                            CartoonSearchItem(
                                cartoonCover = it,
                                isStar = starSet.contains(it.toIdentify()),
                                onClick = {
                                    nav.navigationDetailed(it)
                                },
                                onLongPress = {
                                    starVm.dispatchStar(it)
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                })
                        }

                    }
                    pagingCommon(page)

                }
            }
            PagingCommon(items = page)


            PullRefreshIndicator(
                normalSearchViewModel.isRefreshing.value,
                state,
                Modifier.align(Alignment.TopCenter),
                backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            FastScrollToTopFab(listState = lazyListState, after = 10)

        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CartoonSearchItem(
    modifier: Modifier = Modifier,
    cartoonCover: CartoonCover,
    isStar: Boolean = false,
    onClick: (CartoonCover) -> Unit,
    onLongPress: (CartoonCover) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(4.dp))
            .combinedClickable(
                onClick = {
                    onClick(cartoonCover)
                },
                onLongClick = {
                    onLongPress(cartoonCover)
                }
            )
            .padding(4.dp)
            .then(modifier),
        horizontalArrangement = Arrangement.Start
    ) {
        if (cartoonCover.coverUrl != null) {
            CartoonCard(
                cover = cartoonCover.coverUrl ?: "",
                name = cartoonCover.title,
                source = null
            )

            Spacer(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(8.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Text(
                    text = cartoonCover.title,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = cartoonCover.intro ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                if (isStar) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        fontSize = 13.sp,
                        text = stringResource(id = R.string.stared_min),
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(8.dp, 4.dp)
                            .align(Alignment.End),
                    )
                }
            }
        } else {
            Text(
                text = cartoonCover.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = cartoonCover.intro ?: "",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (isStar) {
                Text(
                    fontSize = 13.sp,
                    text = stringResource(id = R.string.stared_min),
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(4.dp)
                        )
                        .padding(8.dp, 4.dp)
                )
            }

        }
    }
}