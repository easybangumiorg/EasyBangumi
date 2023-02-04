package com.heyanle.easybangumi.ui.search

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemsIndexed
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.pager.ExperimentalPagerApi
import com.heyanle.bangumi_source_api.api.ISearchParser
import com.heyanle.bangumi_source_api.api.entity.Bangumi
import com.heyanle.easybangumi.LocalNavController
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.navigationPlay
import com.heyanle.easybangumi.source.AnimSourceFactory
import com.heyanle.easybangumi.ui.common.*
import com.heyanle.easybangumi.utils.stringRes

/**
 * Created by HeYanLe on 2023/1/10 18:54.
 * https://github.com/heyanLE
 */
@OptIn(ExperimentalPagerApi::class, ExperimentalAnimationApi::class)
@Composable
fun SearchPage(
    isShowTabForever: MutableState<Boolean>,
    padding: PaddingValues = PaddingValues(0.dp),
    searchEventState: State<String>,
    historyKey: SnapshotStateList<String>,
    searchParser: ISearchParser,
    isEnable: Boolean, // 是否刷新
    lazyListState: LazyListState = rememberLazyListState(),
    onHistoryKeyClick: (String) -> Unit,
    onHistoryDelete: () -> Unit,
) {

    val vm = viewModel<SearchPageViewModel>(factory = SearchPageViewModelFactory(searchParser))

    if (isEnable) {
        val lastItem by remember() {
            derivedStateOf {
                val lastVisibleItem = lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()
                    ?: return@derivedStateOf -1
                lastVisibleItem.index
            }
        }

        val endReached by remember(lastItem) {
            derivedStateOf {
                lastItem == -1 || lastItem == lazyListState.layoutInfo.totalItemsCount - 1
            }
        }
        SideEffect {
            vm.isCurLast = endReached
        }
    }

    if (isEnable) {
        val keyword by searchEventState
        LaunchedEffect(key1 = keyword) {
            val cur = vm.getCurKeyword()
            if (cur != keyword) {
                vm.search(keyword)
            }

        }
    }
    val state by vm.pagerFlow.collectAsState(
        initial = vm.lastPagerState ?: SearchPageViewModel.EmptyBangumi
    )
    LaunchedEffect(key1 = state) {
        Log.d("SearchPage", state.toString())
    }
    val sta = state
    val nav = LocalNavController.current

    AnimatedContent(
        targetState = sta,
        transitionSpec = {
            fadeIn(animationSpec = tween(300, delayMillis = 300)) with
                    fadeOut(animationSpec = tween(300, delayMillis = 0))
        },
    ) { newState ->
        when (newState) {
            is SearchPageViewModel.SearchPageState.Empty -> {
                LaunchedEffect(key1 = Unit) {
                    isShowTabForever.value = true
                }
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
                                onHistoryDelete()
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
                            historyKey.forEach {
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
                                                onHistoryKeyClick(it)
                                            }
                                            .padding(8.dp, 4.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
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

            is SearchPageViewModel.SearchPageState.Page -> {
                val lazyPagingItems = newState.flow.collectAsLazyPagingItems()
                when (lazyPagingItems.loadState.refresh) {
                    is LoadState.Loading -> {
                        LoadingPage(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                        )
                    }

                    is LoadState.Error -> {
                        ErrorPage(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            errorMsg = stringResource(R.string.net_error),
                            clickEnable = true,
                            other = {
                                Text(text = stringResource(id = R.string.click_to_retry))
                            },
                            onClick = {
                                vm.search(vm.getCurKeyword())
                            }
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize(),
                            state = lazyListState,
                            contentPadding = PaddingValues(
                                0.dp,
                                padding.calculateTopPadding() + 6.dp,
                                0.dp,
                                6.dp
                            ),
                        ) {
                            itemsIndexed(lazyPagingItems) { _, v ->
                                v?.let { bangumi ->
                                    BangumiSearchItem(bangumi = bangumi) {
                                        nav.navigationPlay(it)
                                    }
                                }
                            }
                            when (lazyPagingItems.loadState.append) {
                                is LoadState.Loading -> {
                                    item {
                                        LoadingPage(
                                            modifier = Modifier.fillMaxWidth(),
                                        )
                                    }
                                }

                                is LoadState.Error -> {
                                    item {
                                        val errorMsg =
                                            (lazyPagingItems.loadState.append as? LoadState.Error)?.error?.message
                                                ?: stringRes(R.string.net_error)
                                        ErrorPage(
                                            modifier = Modifier.fillMaxWidth(),
                                            errorMsg = errorMsg,
                                            clickEnable = true,
                                            other = {
                                                Text(text = stringResource(id = R.string.click_to_retry))
                                            },
                                            onClick = {
                                                lazyPagingItems.retry()
                                            }
                                        )
                                    }
                                }

                                else -> {
                                    item {
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
        }
    }
}

@Composable
fun BangumiSearchItem(
    modifier: Modifier = Modifier,
    bangumi: Bangumi,
    onClick: (Bangumi) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick(bangumi)
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
            val sourceText = AnimSourceFactory.label(bangumi.source) ?: bangumi.source
            OkImage(
                image = bangumi.cover,
                contentDescription = bangumi.name,
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
            Text(text = bangumi.name, maxLines = 2, color = MaterialTheme.colorScheme.onBackground)
            Text(
                text = bangumi.intro,
                maxLines = 4,
                color = MaterialTheme.colorScheme.onBackground.copy(0.6f)
            )
        }
    }
}