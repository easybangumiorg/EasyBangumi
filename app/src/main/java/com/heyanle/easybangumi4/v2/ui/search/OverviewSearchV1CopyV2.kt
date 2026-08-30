package com.heyanle.easybangumi4.v2.ui.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.R as AppR
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.navigationDetailed
import com.heyanle.easybangumi4.plugin.api.ParserException
import com.heyanle.easybangumi4.plugin.api.component.BusinessActionType
import com.heyanle.easybangumi4.plugin.api.component.SearchNeedVerificationBusinessException
import com.heyanle.easybangumi4.plugin.api.component.search.SearchComponent
import com.heyanle.easybangumi4.plugin.api.entity.CartoonCover
import com.heyanle.easybangumi4.plugin.api.entity.toIdentify
import com.heyanle.easybangumi4.plugin.source.LocalSourceBundleController
import com.heyanle.easybangumi4.ui.common.CartoonCardWithCover
import com.heyanle.easybangumi4.ui.common.EmptyPage
import com.heyanle.easybangumi4.ui.common.ErrorPage
import com.heyanle.easybangumi4.ui.common.LoadingPage
import com.heyanle.easybangumi4.ui.common.cover_star.CoverStarViewModel
import com.heyanle.easybangumi4.ui.search_migrate.search.SearchViewModel
import com.heyanle.easybangumi4.ui.search_migrate.search.gather.GatherSearchViewModel
import com.heyanle.easybangumi4.ui.search_migrate.search.gather.GatherSearchViewModelFactory
import com.heyanle.easybangumi4.ui.search_migrate.search.overview.OverviewContentState
import com.heyanle.easybangumi4.ui.search_migrate.search.overview.OverviewPageLoadState
import com.heyanle.easybangumi4.ui.search_migrate.search.overview.OverviewSourceLoadSnapshot
import com.heyanle.easybangumi4.ui.search_migrate.search.overview.isFirstPageLoading
import com.heyanle.easybangumi4.ui.search_migrate.search.overview.resolveOverviewContentState
import com.heyanle.easybangumi4.v2.theme.V2Theme
import com.heyanle.easybangumi4.v2.theme.V2Tokens

private data class V2OverviewResult(
    val sourceKey: String,
    val sourceIndex: Int,
    val resultIndex: Int,
    val cover: CartoonCover,
)

private data class V2OverviewVerification(
    val source: SearchComponent,
    val exception: SearchNeedVerificationBusinessException,
    val retry: () -> Unit,
)

private data class V2OverviewPage(
    val source: SearchComponent,
    val page: LazyPagingItems<CartoonCover>,
)

/** V2-owned copy of V1 overview search: All aggregation plus a persistent source rail. */
@Composable
internal fun ColumnScope.OverviewSearchV1CopyV2(searchViewModel: SearchViewModel) {
    val nav = LocalNavController.current
    val components = LocalSourceBundleController.current.searches()
    val overviewViewModel = viewModel<GatherSearchViewModel>(
        key = "v2-overview-search",
        factory = GatherSearchViewModelFactory(components),
    )
    val request by searchViewModel.searchRequestFlow.collectAsState()
    val sourceItems by overviewViewModel.searchItemList.collectAsState()
    val starViewModel = viewModel<CoverStarViewModel>()
    val starred = starViewModel.stateFlow.collectAsState().value.identifySet
    val selectedIndex = rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(request) { overviewViewModel.submitSearch(request) }
    LaunchedEffect(components) { overviewViewModel.updateSearchComponents(components) }
    LaunchedEffect(components.map { it.source.key }) {
        if (selectedIndex.intValue !in 0..components.size) selectedIndex.intValue = 0
    }

    val pages = sourceItems.orEmpty().map { sourceItem ->
        V2OverviewPage(sourceItem.searchComponent, sourceItem.flow.collectAsLazyPagingItems())
    }
    val loadSnapshots = pages.map { sourcePage ->
        sourcePage.page.toLoadSnapshotV2(sourcePage.source.source.key)
    }
    val selectedSourceKey = components.getOrNull(selectedIndex.intValue - 1)?.source?.key
        .takeIf { selectedIndex.intValue > 0 }
    val allResults = pages.flatMapIndexed { sourceIndex, sourcePage ->
        sourcePage.page.itemSnapshotList.items.take(10).mapIndexed { resultIndex, cover ->
            V2OverviewResult(sourcePage.source.source.key, sourceIndex, resultIndex, cover)
        }
    }.sortedWith(overviewResultComparator(request.keyword))
    val selectedPage = selectedSourceKey?.let { key ->
        pages.firstOrNull { it.source.source.key == key }
    }
    // A verification failure belongs to a source, rather than to a result. Keeping it as
    // a separate item lets the All tab expose every blocked source exactly once.
    val verificationItems = pages.mapNotNull { sourcePage ->
        sourcePage.page.refreshVerificationExceptionV2()?.let { exception ->
            V2OverviewVerification(
                source = sourcePage.source,
                exception = exception,
                retry = sourcePage.page::retry,
            )
        }
    }.let { items ->
        selectedSourceKey?.let { key -> items.filter { it.source.source.key == key } } ?: items
    }
    val contentState = if (loadSnapshots.isEmpty() && components.isNotEmpty()) {
        OverviewContentState.Loading
    } else {
        resolveOverviewContentState(
            sources = loadSnapshots,
            selectedSourceKey = selectedSourceKey,
            hasVerificationItem = verificationItems.isNotEmpty(),
        )
    }
    val pendingFirstPageCount = if (selectedSourceKey == null) {
        loadSnapshots.count { it.isFirstPageLoading }
    } else {
        0
    }

    Row(Modifier.fillMaxWidth().weight(1f)) {
        OverviewSourceRailV2(
            sources = components,
            loadSnapshots = loadSnapshots,
            selectedSourceKey = selectedSourceKey,
            onSourceSelected = { key ->
                selectedIndex.intValue = if (key == null) {
                    0
                } else {
                    components.indexOfFirst { it.source.key == key } + 1
                }
            },
        )
        LazyVerticalGrid(
            columns = GridCells.Adaptive(100.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(4.dp, 8.dp, 4.dp, 88.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            item(key = "overview-header", span = { GridItemSpan(maxLineSpan) }) {
                OverviewResultsHeaderV2(
                    selectedSourceKey = selectedSourceKey,
                    sources = components,
                )
            }
            // Verification is deliberately before covers so that All exposes every blocked
            // source at the top. Filtering makes a source tab show only its own item.
            items(
                verificationItems,
                key = { "verification:${it.source.source.key}" },
                span = { GridItemSpan(maxLineSpan) },
            ) { item ->
                OverviewVerificationCardV2(
                    item = item,
                    onVerify = {
                        overviewViewModel.onSearchNeedWebCheck(
                            sourceKey = item.source.source.key,
                            searchNeedWebViewCheckBusinessException = item.exception,
                            onRetry = item.retry,
                        )
                    },
                )
            }
            when (contentState) {
                OverviewContentState.Content -> {
                    if (selectedPage == null) {
                        // All deliberately reads snapshots only and never drives append.
                        items(
                            allResults,
                            key = {
                                "${request.sequence}:${it.sourceKey}:${it.resultIndex}"
                            },
                        ) { item ->
                            OverviewCoverV2(item.cover, item.cover.toIdentify() in starred, nav::navigationDetailed, starViewModel::dispatchStar)
                        }
                        if (pendingFirstPageCount > 0) {
                            item(
                                key = "overview-pending-sources",
                                span = { GridItemSpan(maxLineSpan) },
                            ) {
                                OverviewPendingSourcesV2(pendingFirstPageCount)
                            }
                        }
                    } else {
                        val sourceKey = selectedPage.source.source.key
                        // Index access is intentional: unlike itemSnapshotList, it notifies
                        // Paging of viewport demand and therefore triggers append.
                        items(
                            count = selectedPage.page.itemCount,
                            key = { index -> "${request.sequence}:$sourceKey:$index" },
                        ) { index ->
                            selectedPage.page[index]?.let { cover ->
                                OverviewCoverV2(cover, cover.toIdentify() in starred, nav::navigationDetailed, starViewModel::dispatchStar)
                            }
                        }
                        item(
                            key = "overview-append:$sourceKey",
                            span = { GridItemSpan(maxLineSpan) },
                        ) {
                            OverviewAppendStateV2(
                                sourcePage = selectedPage,
                                onVerificationRequired = { exception ->
                                    overviewViewModel.onSearchNeedWebCheck(
                                        sourceKey = sourceKey,
                                        searchNeedWebViewCheckBusinessException = exception,
                                        onRetry = selectedPage.page::retry,
                                    )
                                },
                            )
                        }
                    }
                }

                OverviewContentState.Loading,
                OverviewContentState.Empty,
                is OverviewContentState.Error,
                -> item(
                    key = "overview-pane-state",
                    span = { GridItemSpan(maxLineSpan) },
                ) {
                    OverviewFullPaneStateV2(
                        state = contentState,
                        onRetry = {
                            if (selectedPage != null) {
                                selectedPage.page.retry()
                            } else {
                                pages.forEach { it.page.retry() }
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun OverviewFullPaneStateV2(
    state: OverviewContentState,
    onRetry: () -> Unit,
) {
    val modifier = Modifier
        .fillMaxWidth()
        .height(320.dp)
    when (state) {
        OverviewContentState.Loading -> LoadingPage(modifier = modifier)
        OverviewContentState.Empty -> EmptyPage(modifier = modifier)
        is OverviewContentState.Error -> ErrorPage(
            modifier = modifier,
            errorMsg = state.throwable.message ?: stringResource(R.string.net_error),
            other = {
                TextButton(onClick = onRetry) {
                    Text(stringResource(R.string.click_to_retry))
                }
            },
        )

        OverviewContentState.Content -> Unit
    }
}

@Composable
private fun OverviewPendingSourcesV2(pendingCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            color = V2Theme.colors.accent,
            strokeWidth = 2.dp,
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = "$pendingCount 个来源仍在加载",
            style = MaterialTheme.typography.bodySmall,
            color = V2Tokens.TextSecondary,
        )
    }
}

@Composable
private fun OverviewAppendStateV2(
    sourcePage: V2OverviewPage,
    onVerificationRequired: (SearchNeedVerificationBusinessException) -> Unit,
) {
    when (val append = sourcePage.page.loadState.append) {
        LoadState.Loading -> LoadingPage(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            loadingMsg = "加载更多",
        )

        is LoadState.Error -> {
            val verificationException = append.error.searchVerificationExceptionV2()
            ErrorPage(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp),
                image = if (verificationException != null) {
                    AppR.drawable.search_verification
                } else {
                    AppR.drawable.error_ikuyo
                },
                errorMsg = verificationException?.let {
                    val isCaptcha = it.actionType == BusinessActionType.DIALOG_CAPTCHA
                    stringResource(
                        if (isCaptcha) {
                            R.string.search_verification_captcha_required
                        } else {
                            R.string.search_verification_human_required
                        },
                    )
                } ?: (append.error.message ?: stringResource(R.string.net_error)),
                other = {
                    TextButton(
                        onClick = {
                            if (verificationException != null) {
                                onVerificationRequired(verificationException)
                            } else {
                                sourcePage.page.retry()
                            }
                        },
                    ) {
                        Text(
                            if (verificationException != null) {
                                stringResource(R.string.search_verification_continue_action)
                            } else {
                                stringResource(R.string.click_to_retry)
                            },
                        )
                    }
                },
            )
        }

        is LoadState.NotLoading -> Unit
    }
}

@Composable
private fun OverviewResultsHeaderV2(
    selectedSourceKey: String?,
    sources: List<SearchComponent>,
) {
    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
        Text(
            text = selectedSourceKey?.let { key ->
                sources.firstOrNull { it.source.key == key }?.source?.label
            } ?: stringResource(R.string.search_overview_all_results),
            style = MaterialTheme.typography.titleMedium,
            color = V2Tokens.TextPrimary,
        )
        if (selectedSourceKey == null) {
            Text(
                text = stringResource(R.string.search_overview_match_order),
                style = MaterialTheme.typography.bodySmall,
                color = V2Tokens.TextSecondary,
            )
        }
    }
}

@Composable
private fun OverviewSourceRailV2(
    sources: List<SearchComponent>,
    loadSnapshots: List<OverviewSourceLoadSnapshot>,
    selectedSourceKey: String?,
    onSourceSelected: (String?) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxHeight()
            .width(108.dp)
            .padding(vertical = 4.dp)
            .selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        item {
            OverviewSourceTabV2(
                label = stringResource(R.string.all_word),
                selected = selectedSourceKey == null,
                loading = sources.isNotEmpty() && (
                    loadSnapshots.isEmpty() || loadSnapshots.any { it.isFirstPageLoading }
                    ),
                onClick = { onSourceSelected(null) },
            )
        }
        items(sources.size, key = { sources[it].source.key }) { index ->
            val source = sources[index].source
            val loadSnapshot = loadSnapshots.firstOrNull { it.sourceKey == source.key }
            OverviewSourceTabV2(
                label = source.label,
                selected = selectedSourceKey == source.key,
                loading = loadSnapshot?.isFirstPageLoading ?: true,
                onClick = { onSourceSelected(source.key) },
            )
        }
    }
}

@Composable
private fun OverviewSourceTabV2(
    label: String,
    selected: Boolean,
    loading: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) V2Theme.colors.accentContainer else V2Tokens.Surface)
            .selectable(
                selected = selected,
                role = Role.Tab,
                onClick = onClick,
            )
            .then(
                if (loading) {
                    Modifier.semantics { stateDescription = "正在加载首页" }
                } else {
                    Modifier
                },
            )
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = label,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = if (selected) {
                V2Theme.colors.accent
            } else {
                V2Tokens.TextPrimary
            },
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(15.dp),
                color = V2Theme.colors.accent,
                strokeWidth = 2.dp,
            )
        }
    }
}

/**
 * A source-level verification prompt. Each blocked source owns one full-width row and keeps
 * its own retry callback, so multiple verification requests remain independently actionable.
 */
@Composable
private fun OverviewVerificationCardV2(
    item: V2OverviewVerification,
    onVerify: () -> Unit,
) {
    val isCaptcha = item.exception.actionType == BusinessActionType.DIALOG_CAPTCHA
    val status = stringResource(
        if (isCaptcha) {
            R.string.search_verification_captcha_required
        } else {
            R.string.search_verification_human_required
        },
    )
    val action = stringResource(
        if (isCaptcha) {
            R.string.search_verification_captcha_action
        } else {
            R.string.search_verification_human_action
        },
    )
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp)
            .clip(shape)
            .background(V2Tokens.SurfaceMuted)
            .border(
                width = 1.dp,
                color = V2Tokens.Divider.copy(alpha = 0.45f),
                shape = shape,
            )
            .semantics(mergeDescendants = true) {
                stateDescription = status
            }
            .clickable(
                role = Role.Button,
                onClick = onVerify,
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Image(
            painter = painterResource(AppR.drawable.search_verification),
            contentDescription = null,
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(V2Theme.colors.accentContainer)
                .padding(2.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.source.source.label,
                style = MaterialTheme.typography.titleSmall,
                color = V2Tokens.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = status,
                style = MaterialTheme.typography.bodySmall,
                color = V2Tokens.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = action,
            style = MaterialTheme.typography.labelLarge,
            color = V2Theme.colors.accent,
            maxLines = 1,
            modifier = Modifier
                .background(
                    color = V2Theme.colors.accentContainer,
                    shape = RoundedCornerShape(50),
                )
                .padding(horizontal = 14.dp, vertical = 9.dp),
        )
    }
}

@Composable
private fun OverviewCoverV2(
    cover: CartoonCover,
    starred: Boolean,
    onClick: (CartoonCover) -> Unit,
    onStar: (CartoonCover) -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    CartoonCardWithCover(
        star = starred,
        cartoonCover = cover,
        onClick = onClick,
        onLongPress = {
            onStar(it)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        },
        v2Presentation = true,
    )
}

private fun LazyPagingItems<CartoonCover>.toLoadSnapshotV2(
    sourceKey: String,
): OverviewSourceLoadSnapshot = OverviewSourceLoadSnapshot(
    sourceKey = sourceKey,
    itemCount = itemCount,
    refresh = loadState.refresh.toOverviewPageLoadStateV2(),
    append = loadState.append.toOverviewPageLoadStateV2(),
)

private fun LoadState.toOverviewPageLoadStateV2(): OverviewPageLoadState = when (this) {
    LoadState.Loading -> OverviewPageLoadState.Loading
    is LoadState.Error -> OverviewPageLoadState.Error(error)
    is LoadState.NotLoading -> OverviewPageLoadState.Idle
}

private fun LazyPagingItems<CartoonCover>.refreshVerificationExceptionV2():
    SearchNeedVerificationBusinessException? {
    val error = (loadState.refresh as? LoadState.Error)?.error ?: return null
    return error.searchVerificationExceptionV2()
}

private fun Throwable.searchVerificationExceptionV2(): SearchNeedVerificationBusinessException? =
    (this as? ParserException)?.exception as? SearchNeedVerificationBusinessException

private fun overviewResultComparator(query: String): Comparator<V2OverviewResult> = compareBy(
    { titleMatchRank(it.cover.title, query) },
    { it.cover.title.length },
    { it.sourceIndex },
    { it.resultIndex },
)

private fun titleMatchRank(title: String, query: String): Int {
    val normalizedTitle = title.normalizeSearchText()
    val normalizedQuery = query.normalizeSearchText()
    return when {
        normalizedTitle == normalizedQuery -> 0
        normalizedTitle.startsWith(normalizedQuery) -> 1
        normalizedTitle.contains(normalizedQuery) -> 2
        else -> 3
    }
}

private fun String.normalizeSearchText(): String =
    lowercase().filterNot { it.isWhitespace() || it in "-_·・.，,。:：" }
