package com.heyanle.easybangumi.ui.home.star

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.heyanle.easybangumi.LocalNavController
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.db.entity.BangumiStar
import com.heyanle.easybangumi.navigationPlay
import com.heyanle.easybangumi.source.AnimSourceFactory
import com.heyanle.easybangumi.ui.common.*
import com.heyanle.easybangumi.utils.stringRes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/1/9 21:27.
 * https://github.com/heyanLE
 */

@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
@Composable
fun AnimMy() {

    val lazyGridState = rememberLazyGridState()
    val vm = viewModel<AnimStarViewModel>()


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

    Box(modifier = Modifier
        .fillMaxSize()
        .pullRefresh(state)) {
        AnimatedContent(
            targetState = pi,
            transitionSpec = {
                fadeIn(animationSpec = tween(300, delayMillis = 300)) with
                        fadeOut(animationSpec = tween(300, delayMillis = 0))
            },
        ) { pagingItems ->
            if (pagingItems.itemCount == 0) {
                EmptyPage(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    emptyMsg = stringResource(id = R.string.no_star_bangumi)
                )
            } else {
                LazyVerticalGrid(
                    contentPadding = PaddingValues(4.dp, 4.dp),
                    columns = GridCells.Adaptive(95.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(
                        count = pagingItems.itemCount,
                    ) { index ->
                        pagingItems[index]?.let { star ->
                            BangumiStarCard(item = star) {
                                nav.navigationPlay(it.source, it.detailUrl)
                            }
                        }
                    }
                    when (pagingItems.loadState.append) {
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
                            item(
                                span = {
                                    // LazyGridItemSpanScope:
                                    // maxLineSpan
                                    GridItemSpan(maxLineSpan)
                                }
                            ) {
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

        PullRefreshIndicator(
            refreshing,
            state,
            Modifier.align(Alignment.TopCenter),
            backgroundColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.secondary
        )
        FastScrollToTopFab(listState = lazyGridState)
    }
}

@Composable
fun BangumiStarCard(
    item: BangumiStar,
    onClick: (BangumiStar) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable {
                onClick(item)
            }
            .padding(0.dp, 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .height(135.dp)
                .width(95.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {

            OkImage(
                image = item.cover,
                contentDescription = item.name,
                modifier = Modifier
                    .height(135.dp)
                    .width(95.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            val sourceText = AnimSourceFactory.label(item.source) ?: item.source
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

        var needEnter by remember() {
            mutableStateOf(false)
        }
        Text(
            textAlign = TextAlign.Center,
            modifier = Modifier.width(95.dp),
            text = "${item.name}${if (needEnter) "\n " else ""}",
            maxLines = 2,
            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = {
                if (it.lineCount < 2) {
                    needEnter = true
                }
            }
        )
    }
}