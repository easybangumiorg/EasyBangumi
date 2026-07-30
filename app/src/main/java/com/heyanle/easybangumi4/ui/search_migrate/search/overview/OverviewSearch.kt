package com.heyanle.easybangumi4.ui.search_migrate.search.overview

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
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
import com.heyanle.easybangumi4.ui.common.OkImage
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

/**
 * A cross-source cover browser. "All" intentionally only uses each source's first page,
 * so switching sources stays fast and does not silently load the entire catalog.
 */
@Composable
fun ColumnScope.OverviewSearch(searchViewModel: SearchViewModel) {
    val nav = LocalNavController.current
    val sourceBundle = LocalSourceBundleController.current
    val searchComponents = sourceBundle.searches()
    val overviewVm = viewModel<GatherSearchViewModel>(
        factory = GatherSearchViewModelFactory(searchComponents)
    )
    val searchRequest by searchViewModel.searchRequestFlow.collectAsState()
    val searchKey = searchRequest.keyword
    val itemList by overviewVm.searchItemList.collectAsState()
    val starVm = viewModel<CoverStarViewModel>()
    val starred = starVm.stateFlow.collectAsState().value.identifySet
    val haptic = LocalHapticFeedback.current
    var selectedSourceKey by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(searchRequest) {
        overviewVm.newSearchKey(searchKey, force = true)
    }
    LaunchedEffect(searchComponents.map { it.source.key }) {
        if (selectedSourceKey != null && searchComponents.none { it.source.key == selectedSourceKey }) {
            selectedSourceKey = null
        }
    }

    val pagingItems = itemList.orEmpty().map { sourceItem ->
        sourceItem to sourceItem.flow.collectAsLazyPagingItems()
    }
    val allResults = pagingItems.flatMapIndexed { sourceIndex, (sourceItem, page) ->
        page.itemSnapshotList.items
            .take(FIRST_PAGE_SIZE)
            .mapIndexed { resultIndex, cover ->
                OverviewResult(cover, sourceItem.searchComponent, sourceIndex, resultIndex)
            }
    }.sortedWith(overviewResultComparator(searchKey))
    val shownResults = selectedSourceKey?.let { key ->
        allResults.filter { it.source.source.key == key }
    } ?: allResults
    // A verification failure belongs to a source, rather than to a result. Keeping it as
    // a separate item lets the All tab expose every blocked source exactly once.
    val verificationItems = pagingItems.mapNotNull { (sourceItem, page) ->
        page.searchVerificationException()?.let { exception ->
            OverviewVerificationItem(
                source = sourceItem.searchComponent,
                exception = exception,
                retry = { page.retry() },
            )
        }
    }.let { items ->
        selectedSourceKey?.let { key -> items.filter { it.source.source.key == key } } ?: items
    }

    Row(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
    ) {
        OverviewSourceRail(
            sources = searchComponents,
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
            // A source can legitimately return duplicate titles/identifiers. The source and
            // its first-page position are stable for this result set and unique in the grid.
            items(shownResults, key = { "${it.sourceIndex}:${it.resultIndex}" }) { item ->
                OverviewCoverCard(
                    result = item,
                    starred = starred.contains(item.cover.toIdentify()),
                    onClick = { nav.navigationDetailed(item.cover) },
                    onLongClick = {
                        starVm.dispatchStar(item.cover)
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                )
            }
        }
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
    selectedSourceKey: String?,
    onSourceSelected: (String?) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxHeight()
            .width(108.dp)
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        item {
            OverviewSourceTab(
                label = stringResource(R.string.all_word),
                selected = selectedSourceKey == null,
                onClick = { onSourceSelected(null) },
            )
        }
        items(sources.size, key = { sources[it].source.key }) { index ->
            val source = sources[index].source
            OverviewSourceTab(
                label = source.label,
                selected = selectedSourceKey == source.key,
                onClick = { onSourceSelected(source.key) },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun OverviewSourceTab(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
            .combinedClickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 10.dp),
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun OverviewCoverCard(
    result: OverviewResult,
    starred: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
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
 * Uses the same verification exception and retry path as the grouped search mode, but is
 * deliberately compact so one blocked source occupies one predictable overview grid slot.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun OverviewVerificationCard(
    item: OverviewVerificationItem,
    onVerify: () -> Unit,
) {
    val isCaptcha = item.exception.actionType == BusinessActionType.DIALOG_CAPTCHA
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(19 / 27f)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .combinedClickable(onClick = onVerify)
            .padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(AppR.drawable.overview_verification),
            contentDescription = "${item.source.source.label}${if (isCaptcha) "验证码" else "人机校验"}",
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )
        Spacer(Modifier.padding(top = 4.dp))
        Text(
            text = item.source.source.label,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.padding(top = 8.dp))
        Text(
            text = if (isCaptcha) "需要输入验证码" else "需要人机校验",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = if (isCaptcha) "点击输入验证码" else "点击跳转校验",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

private fun androidx.paging.compose.LazyPagingItems<CartoonCover>.searchVerificationException():
    SearchNeedVerificationBusinessException? {
    val refreshError = (loadState.refresh as? LoadState.Error)?.error ?: return null
    return (refreshError as? ParserException)?.exception as? SearchNeedVerificationBusinessException
}

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
