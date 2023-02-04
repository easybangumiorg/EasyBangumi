@file:OptIn(ExperimentalMaterialApi::class)

package com.heyanle.easybangumi.ui.home.history

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.heyanle.easybangumi.LocalNavController
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.db.entity.BangumiHistory
import com.heyanle.easybangumi.navigationPlay
import com.heyanle.easybangumi.source.AnimSourceFactory
import com.heyanle.easybangumi.ui.common.*
import com.heyanle.easybangumi.ui.common.easy_player.utils.TimeUtils
import com.heyanle.easybangumi.ui.home.star.AnimStarViewModel
import com.heyanle.easybangumi.ui.home.star.BangumiStarCard
import com.heyanle.easybangumi.utils.stringRes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/1/9 21:51.
 * https://github.com/heyanLE
 */
@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
@Composable
fun AnimHistory() {
    val lazyListState = rememberLazyListState()
    val vm = viewModel<AnimHistoryViewModel>()


    val nav = LocalNavController.current
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
    ) {
        AnimatedContent(
            targetState = pi,
            transitionSpec = {
                fadeIn(animationSpec = tween(300, delayMillis = 300)) with
                        fadeOut(animationSpec = tween(300, delayMillis = 0))
            },
        ) { pagingItems ->
            if (pagingItems.itemCount == 0) {
                EmptyPage(
                    modifier = Modifier.fillMaxSize(),
                    emptyMsg = stringResource(id = R.string.no_history)
                )
            } else {

                Box(modifier = Modifier) {
                    LazyColumn(
                        state = lazyListState
                    ) {
                        items(pagingItems) {
                            it?.let {
                                BangumiHistoryCard(bangumiHistory = it, onClick = {
                                    nav.navigationPlay(
                                        it.source,
                                        it.detailUrl,
                                        it.lastLinesIndex,
                                        it.lastEpisodeIndex,
                                        it.lastProcessTime
                                    )
                                })
                            }

                        }
                        when (pagingItems.loadState.append) {
                            is LoadState.Loading -> {
                                item() {
                                    LoadingPage(
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            }

                            is LoadState.Error -> {
                                item() {
                                    val errorMsg =
                                        (pagingItems.loadState.append as? LoadState.Error)?.error?.message
                                            ?: stringRes(
                                                R.string.net_error
                                            )
                                    ErrorPage(
                                        modifier = Modifier.fillMaxWidth(),
                                        errorMsg = errorMsg,
                                        clickEnable = true,
                                        other = {
                                            Text(text = stringResource(id = R.string.click_to_retry))
                                        },
                                        onClick = {
                                            pagingItems.retry()
                                        }
                                    )
                                }
                            }

                            else -> {
                                item() {
                                    Text(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(0.dp, 2.dp),
                                        textAlign = TextAlign.Center,
                                        text = stringResource(id = R.string.list_most_bottom)
                                    )
                                }
                            }
                        }
                    }

                }
            }
        }


        PullRefreshIndicator(
            refreshing,
            state,
            Modifier.align(Alignment.TopCenter),
            backgroundColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.secondary
        )
        FastScrollToTopFab(listState = lazyListState)
    }


}

@Composable
fun BangumiHistoryCard(
    modifier: Modifier = Modifier,
    bangumiHistory: BangumiHistory,
    onClick: (BangumiHistory) -> Unit,
) {
    val sourceText = AnimSourceFactory.label(bangumiHistory.source) ?: bangumiHistory.source
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick(bangumiHistory)
            }
            .padding(16.dp, 8.dp)
            .then(modifier),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .height(135.dp)
                .width(95.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {

            OkImage(
                image = bangumiHistory.cover,
                contentDescription = bangumiHistory.name,
                modifier = Modifier
                    .height(135.dp)
                    .width(95.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Text(
                fontSize = 13.sp,
                text = sourceText,
                color = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.secondary,
                        RoundedCornerShape(0.dp, 0.dp, 8.dp, 0.dp)
                    )
                    .padding(8.dp, 0.dp)
            )

        }
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = bangumiHistory.name,
                maxLines = 2,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(
                    id = R.string.last_episode_title,
                    bangumiHistory.lastEpisodeTitle
                ),
                maxLines = 1,
                color = MaterialTheme.colorScheme.onBackground.copy(0.6f)
            )
            Text(
                text = TimeUtils.toString(bangumiHistory.lastProcessTime).toString(),
                maxLines = 1,
                color = MaterialTheme.colorScheme.onBackground.copy(0.6f)
            )
        }
    }
}