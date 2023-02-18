@file:OptIn(ExperimentalMaterialApi::class)

package com.heyanle.easybangumi.ui.home.history

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemsIndexed
import com.heyanle.easybangumi.LocalNavController
import com.heyanle.easybangumi.db.entity.BangumiHistory
import com.heyanle.easybangumi.navigationPlay
import com.heyanle.easybangumi.ui.common.*
import com.heyanle.easybangumi.ui.common.easy_player.utils.TimeUtils
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

    var deleteState by remember {
        mutableStateOf<BangumiHistory?>(null)
    }

    EasyDeleteDialog(
        show = deleteState != null,
        onDelete = {
            deleteState?.let {
                vm.delete(it)
            }
        },
        onDismissRequest = {
            deleteState = null
        }
    )

    var clearState by remember {
        mutableStateOf(false)
    }

    EasyClearDialog(
        show = clearState,
        onDelete = {
            vm.clear()
        },
        onDismissRequest = {
            clearState = false
        }
    )



    LaunchedEffect(key1 = Unit) {
        vm.onPageLaunch()
    }

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
                    emptyMsg = stringResource(id = com.heyanle.easy_i18n.R.string.no_history)
                )
            } else {

                Box(modifier = Modifier) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = lazyListState,
                        contentPadding = PaddingValues(0.dp, 0.dp, 0.dp, 56.dp),
                    ) {
                        itemsIndexed(pagingItems) { index, it ->
                            it?.let {
                                BangumiHistoryCard(bangumiHistory = it, onClick = {
                                    nav.navigationPlay(
                                        it.bangumiId,
                                        it.source,
                                        it.detailUrl,
                                        it.lastLinesIndex,
                                        it.lastEpisodeIndex,
                                        it.lastProcessTime
                                    )
                                }, onDelete = {
                                    deleteState = it
                                    //vm.delete(hi)
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
                                                com.heyanle.easy_i18n.R.string.net_error
                                            )
                                    ErrorPage(
                                        modifier = Modifier.fillMaxWidth(),
                                        errorMsg = errorMsg,
                                        clickEnable = true,
                                        other = {
                                            Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.click_to_retry))
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
                                        text = stringResource(id = com.heyanle.easy_i18n.R.string.list_most_bottom)
                                    )
                                }
                            }
                        }
                    }
                    val clearFabState =
                        remember { derivedStateOf { lazyListState.firstVisibleItemIndex < 2 } }
                    EasyFab(
                        state = clearFabState,
                        icon = {
                            androidx.compose.material3.Icon(
                                Icons.Filled.ClearAll,
                                contentDescription = stringResource(id = com.heyanle.easy_i18n.R.string.clear),
                                tint = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                    ) {
                        clearState = true
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
    onDelete: (BangumiHistory) -> Unit,
) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .clickable {
            onClick(bangumiHistory)
        }
        .padding(16.dp, 8.dp)
        .then(modifier)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BangumiCard(bangumiHistory.cover, bangumiHistory.name, bangumiHistory.source)
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
                        id = com.heyanle.easy_i18n.R.string.last_episode_title,
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
        IconButton(
            modifier = Modifier.align(Alignment.BottomEnd),
            onClick = {
                onDelete(bangumiHistory)
            }) {
            Icon(
                Icons.Filled.Delete,
                contentDescription = stringResource(id = com.heyanle.easy_i18n.R.string.delete)
            )
        }
    }
}

