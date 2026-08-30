package com.heyanle.easybangumi4.ui.search_migrate.search.overview

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
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
import com.heyanle.easybangumi4.ui.common.EmptyPage
import com.heyanle.easybangumi4.ui.common.ErrorPage
import com.heyanle.easybangumi4.ui.common.LoadingPage
import com.heyanle.easybangumi4.ui.common.OkImage
import com.heyanle.easybangumi4.ui.common.CartoonCardWithCover
import com.heyanle.easybangumi4.ui.common.cover_star.CoverStarViewModel
import com.heyanle.easybangumi4.ui.search_migrate.search.SearchViewModel
import com.heyanle.easybangumi4.ui.search_migrate.search.gather.GatherSearchViewModel
import com.heyanle.easybangumi4.ui.search_migrate.search.gather.GatherSearchViewModelFactory

private const val FIRST_PAGE_SIZE = 10

private data class OverviewResult(
    val cover: CartoonCover,
    val source: SearchComponent,
    val sourceIndex: Int,
    val resultIndex: Int,
)

/** A source-specific verification failure represented as a grid item. */
private data class OverviewVerificationItem(
    val source: SearchComponent,
    val exception: SearchNeedVerificationBusinessException,
    val retry: () -> Unit,
)

private data class OverviewSourcePage(
    val sourceItem: GatherSearchViewModel.GatherSearchItem,
    val page: LazyPagingItems<CartoonCover>,
)

/**
 * A cross-source cover browser. "All" intentionally only uses each source's first page,
 * so switching sources stays fast and does not silently load the entire catalog.
 */
@Composable
fun ColumnScope.OverviewSearch(searchViewModel: SearchViewModel, v2Presentation: Boolean = false) {
    val nav = LocalNavController.current
    val sourceBundle = LocalSourceBundleController.current
    val searchComponents = sourceBundle.searches()
    val overviewVm = viewModel<GatherSearchViewModel>(
        key = "overview-search",
        factory = GatherSearchViewModelFactory(searchComponents),
    )
    val searchRequest by searchViewModel.searchRequestFlow.collectAsState()
    val searchKey = searchRequest.keyword
    val itemList by overviewVm.searchItemList.collectAsState()
    val starVm = viewModel<CoverStarViewModel>()
    val starred = starVm.stateFlow.collectAsState().value.identifySet
    val haptic = LocalHapticFeedback.current
    var selectedSourceKey by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(searchRequest) {
        overviewVm.submitSearch(searchRequest)
    }
    LaunchedEffect(searchComponents) {
        overviewVm.updateSearchComponents(searchComponents)
    }
    LaunchedEffect(searchComponents.map { it.source.key }) {
        if (selectedSourceKey != null && searchComponents.none { it.source.key == selectedSourceKey }) {
            selectedSourceKey = null
        }
    }

    val pagingItems = itemList.orEmpty().map { sourceItem ->
        OverviewSourcePage(sourceItem, sourceItem.flow.collectAsLazyPagingItems())
    }
    val loadSnapshots = pagingItems.map { sourcePage ->
        sourcePage.page.toLoadSnapshot(sourcePage.sourceItem.searchComponent.source.key)
    }
    val allResults = pagingItems.flatMapIndexed { sourceIndex, sourcePage ->
        sourcePage.page.itemSnapshotList.items
            .take(FIRST_PAGE_SIZE)
            .mapIndexed { resultIndex, cover ->
                OverviewResult(
                    cover = cover,
                    source = sourcePage.sourceItem.searchComponent,
                    sourceIndex = sourceIndex,
                    resultIndex = resultIndex,
                )
            }
    }.sortedWith(overviewResultComparator(searchKey))
    val selectedSourcePage = selectedSourceKey?.let { key ->
        pagingItems.firstOrNull { it.sourceItem.searchComponent.source.key == key }
    }
    // A verification failure belongs to a source, rather than to a result. Keeping it as
    // a separate item lets the All tab expose every blocked source exactly once.
    val verificationItems = pagingItems.mapNotNull { sourcePage ->
        sourcePage.page.refreshVerificationException()?.let { exception ->
            OverviewVerificationItem(
                source = sourcePage.sourceItem.searchComponent,
                exception = exception,
                retry = { sourcePage.page.retry() },
            )
        }
    }.let { items ->
        selectedSourceKey?.let { key -> items.filter { it.source.source.key == key } } ?: items
    }
    val contentState = if (loadSnapshots.isEmpty() && searchComponents.isNotEmpty()) {
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

    Row(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
    ) {
        OverviewSourceRail(
            sources = searchComponents,
            loadSnapshots = loadSnapshots,
            selectedSourceKey = selectedSourceKey,
            onSourceSelected = { selectedSourceKey = it },
        )
        LazyVerticalGrid(
            // Match the home-page card policy: card width, rather than a fixed column count,
            // determines how many results fit in the available result pane.
            columns = GridCells.Adaptive(100.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp, 8.dp, 4.dp, 88.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            // The description scrolls away with the results, leaving more room for covers.
            item(key = "overview-header", span = { GridItemSpan(maxLineSpan) }) {
                OverviewResultsHeader(
                    selectedSourceKey = selectedSourceKey,
                    sources = searchComponents,
                )
            }
            // Verification is deliberately before covers so that All exposes every blocked
            // source at the top. Filtering makes a source tab show only its own item.
            items(
                verificationItems,
                key = { "verification:${it.source.source.key}" },
                span = { GridItemSpan(maxLineSpan) },
            ) { item ->
                OverviewVerificationCard(
                    item = item,
                    onVerify = {
                        overviewVm.onSearchNeedWebCheck(
                            sourceKey = item.source.source.key,
                            searchNeedWebViewCheckBusinessException = item.exception,
                            onRetry = item.retry,
                        )
                    },
                )
            }
            when (contentState) {
                OverviewContentState.Content -> {
                    if (selectedSourcePage == null) {
                        // All deliberately reads snapshots only and never drives append.
                        items(
                            allResults,
                            key = {
                                "${searchRequest.sequence}:${it.source.source.key}:${it.resultIndex}"
                            },
                        ) { item ->
                            OverviewCoverCard(
                                result = item,
                                starred = starred.contains(item.cover.toIdentify()),
                                v2Presentation = v2Presentation,
                                onClick = { nav.navigationDetailed(item.cover) },
                                onLongClick = {
                                    starVm.dispatchStar(item.cover)
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                },
                            )
                        }
                        if (pendingFirstPageCount > 0) {
                            item(
                                key = "overview-pending-sources",
                                span = { GridItemSpan(maxLineSpan) },
                            ) {
                                OverviewPendingSourcesFooter(pendingFirstPageCount)
                            }
                        }
                    } else {
                        val sourceIndex = pagingItems.indexOf(selectedSourcePage)
                        val sourceKey = selectedSourcePage.sourceItem.searchComponent.source.key
                        // Index access is intentional: unlike itemSnapshotList, it notifies
                        // Paging of viewport demand and therefore triggers append.
                        items(
                            count = selectedSourcePage.page.itemCount,
                            key = { index -> "${searchRequest.sequence}:$sourceKey:$index" },
                        ) { index ->
                            selectedSourcePage.page[index]?.let { cover ->
                                val item = OverviewResult(
                                    cover = cover,
                                    source = selectedSourcePage.sourceItem.searchComponent,
                                    sourceIndex = sourceIndex,
                                    resultIndex = index,
                                )
                                OverviewCoverCard(
                                    result = item,
                                    starred = starred.contains(cover.toIdentify()),
                                    v2Presentation = v2Presentation,
                                    onClick = { nav.navigationDetailed(cover) },
                                    onLongClick = {
                                        starVm.dispatchStar(cover)
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    },
                                )
                            }
                        }
                        item(
                            key = "overview-append:$sourceKey",
                            span = { GridItemSpan(maxLineSpan) },
                        ) {
                            OverviewAppendState(
                                sourcePage = selectedSourcePage,
                                onVerificationRequired = { exception ->
                                    overviewVm.onSearchNeedWebCheck(
                                        sourceKey = sourceKey,
                                        searchNeedWebViewCheckBusinessException = exception,
                                        onRetry = { selectedSourcePage.page.retry() },
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
                    OverviewFullPaneState(
                        state = contentState,
                        onRetry = {
                            if (selectedSourcePage != null) {
                                selectedSourcePage.page.retry()
                            } else {
                                pagingItems.forEach { it.page.retry() }
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun OverviewFullPaneState(
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
private fun OverviewPendingSourcesFooter(pendingCount: Int) {
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
            strokeWidth = 2.dp,
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = "$pendingCount 个来源仍在加载",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun OverviewAppendState(
    sourcePage: OverviewSourcePage,
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
            val verificationException = append.error.searchVerificationException()
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
private fun OverviewResultsHeader(
    selectedSourceKey: String?,
    sources: List<SearchComponent>,
) {
    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
        Text(
            text = selectedSourceKey?.let { key ->
                sources.firstOrNull { it.source.key == key }?.source?.label
            } ?: stringResource(R.string.search_overview_all_results),
            style = MaterialTheme.typography.titleMedium,
        )
        if (selectedSourceKey == null) {
            Text(
                text = stringResource(R.string.search_overview_match_order),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun OverviewSourceRail(
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
            OverviewSourceTab(
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
            OverviewSourceTab(
                label = source.label,
                selected = selectedSourceKey == source.key,
                loading = loadSnapshot?.isFirstPageLoading ?: true,
                onClick = { onSourceSelected(source.key) },
            )
        }
    }
}

@Composable
private fun OverviewSourceTab(
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
            .background(if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
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
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(15.dp),
                strokeWidth = 2.dp,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun OverviewCoverCard(
    result: OverviewResult,
    starred: Boolean,
    v2Presentation: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    if (v2Presentation) {
        CartoonCardWithCover(
            star = starred,
            cartoonCover = result.cover,
            onClick = { onClick() },
            onLongPress = { onLongClick() },
            v2Presentation = true,
        )
        return
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(2.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(19 / 27f)
                .clip(RoundedCornerShape(4.dp)),
        ) {
            OkImage(
                modifier = Modifier.fillMaxSize(),
                image = result.cover.coverUrl.orEmpty(),
                contentDescription = result.cover.title,
            )
            Text(
                text = result.source.source.label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(0.dp, 0.dp, 4.dp, 0.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp),
            )
            if (starred) {
                Text(
                    text = stringResource(R.string.stared_min),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(0.dp, 4.dp, 0.dp, 0.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                )
            }
        }
        Spacer(Modifier.padding(top = 2.dp))
        Text(
            text = result.cover.title,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * A source-level verification prompt. Each blocked source owns one full-width row and keeps
 * its own retry callback, so multiple verification requests remain independently actionable.
 */
@Composable
private fun OverviewVerificationCard(
    item: OverviewVerificationItem,
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
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
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
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(2.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.source.source.label,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = status,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = action,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            maxLines = 1,
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(50),
                )
                .padding(horizontal = 14.dp, vertical = 9.dp),
        )
    }
}

private fun LazyPagingItems<CartoonCover>.toLoadSnapshot(
    sourceKey: String,
): OverviewSourceLoadSnapshot = OverviewSourceLoadSnapshot(
    sourceKey = sourceKey,
    itemCount = itemCount,
    refresh = loadState.refresh.toOverviewPageLoadState(),
    append = loadState.append.toOverviewPageLoadState(),
)

private fun LoadState.toOverviewPageLoadState(): OverviewPageLoadState = when (this) {
    LoadState.Loading -> OverviewPageLoadState.Loading
    is LoadState.Error -> OverviewPageLoadState.Error(error)
    is LoadState.NotLoading -> OverviewPageLoadState.Idle
}

private fun LazyPagingItems<CartoonCover>.refreshVerificationException():
    SearchNeedVerificationBusinessException? {
    val refreshError = (loadState.refresh as? LoadState.Error)?.error ?: return null
    return refreshError.searchVerificationException()
}

private fun Throwable.searchVerificationException(): SearchNeedVerificationBusinessException? =
    (this as? ParserException)?.exception as? SearchNeedVerificationBusinessException

private fun overviewResultComparator(query: String): Comparator<OverviewResult> = compareBy<OverviewResult>(
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
