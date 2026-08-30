package com.heyanle.easybangumi4.v2.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.LoadState
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.navigationDetailed
import com.heyanle.easybangumi4.plugin.api.entity.CartoonCover
import com.heyanle.easybangumi4.plugin.api.entity.toIdentify
import com.heyanle.easybangumi4.plugin.api.ParserException
import com.heyanle.easybangumi4.plugin.api.component.BusinessActionType
import com.heyanle.easybangumi4.plugin.api.component.SearchNeedVerificationBusinessException
import com.heyanle.easybangumi4.plugin.source.LocalSourceBundleController
import com.heyanle.easybangumi4.ui.common.CartoonCardWithCover
import com.heyanle.easybangumi4.ui.common.PagingCommonSourceSearch
import com.heyanle.easybangumi4.ui.common.cover_star.CoverStarViewModel
import com.heyanle.easybangumi4.ui.search_migrate.search.SearchViewModel
import com.heyanle.easybangumi4.ui.search_migrate.search.gather.GatherSearchViewModel
import com.heyanle.easybangumi4.ui.search_migrate.search.gather.GatherSearchViewModelFactory
import com.heyanle.easybangumi4.v2.ui.component.V2ScrollableTabs
import com.heyanle.easybangumi4.v2.theme.V2Theme
import com.heyanle.easybangumi4.v2.theme.V2Tokens

private data class V2OverviewResult(
    val sourceKey: String,
    val sourceIndex: Int,
    val resultIndex: Int,
    val cover: CartoonCover,
)

private data class V2OverviewVerification(
    val sourceLabel: String,
    val sourceKey: String,
    val exception: SearchNeedVerificationBusinessException,
    val retry: () -> Unit,
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
    val haptic = LocalHapticFeedback.current
    val selectedIndex = rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(request) { overviewViewModel.submitSearch(request) }
    LaunchedEffect(components) { overviewViewModel.updateSearchComponents(components) }

    val pages = sourceItems.orEmpty().map { sourceItem ->
        sourceItem to sourceItem.flow.collectAsLazyPagingItems()
    }
    val labels = listOf("全部") + components.map { it.source.label }
    LaunchedEffect(components.map { it.source.key }) {
        if (selectedIndex.intValue !in labels.indices) selectedIndex.intValue = 0
    }
    val allResults = pages.flatMapIndexed { sourceIndex, (sourceItem, page) ->
        page.itemSnapshotList.items.take(10).mapIndexed { resultIndex, cover ->
            V2OverviewResult(sourceItem.searchComponent.source.key, sourceIndex, resultIndex, cover)
        }
    }.sortedWith(overviewResultComparator(request.keyword))
    val selectedPair = pages.getOrNull(selectedIndex.intValue - 1)
    val allVerificationItems = pages.mapNotNull { (sourceItem, page) ->
        page.refreshVerificationExceptionV2()?.let { exception ->
            V2OverviewVerification(
                sourceLabel = sourceItem.searchComponent.source.label,
                sourceKey = sourceItem.searchComponent.source.key,
                exception = exception,
                retry = page::retry,
            )
        }
    }
    val visibleVerificationItems = if (selectedIndex.intValue == 0) {
        allVerificationItems
    } else {
        allVerificationItems.filter { it.sourceKey == components.getOrNull(selectedIndex.intValue - 1)?.source?.key }
    }
    val pendingFirstPageCount = if (selectedIndex.intValue == 0) {
        pages.count { (_, page) -> page.loadState.refresh is LoadState.Loading }
    } else 0

    Row(Modifier.fillMaxWidth().weight(1f)) {
        LazyColumn(
            modifier = Modifier
                .width(116.dp)
                .fillMaxHeight()
                .background(V2Tokens.Surface),
            contentPadding = PaddingValues(vertical = 8.dp),
        ) {
            item(key = "all") {
                OverviewSourceRailItemV2(
                    label = "全部",
                    selected = selectedIndex.intValue == 0,
                    loading = pages.isEmpty() || pages.any { (_, page) -> page.loadState.refresh is LoadState.Loading },
                ) {
                    selectedIndex.intValue = 0
                }
            }
            itemsIndexed(components, key = { _, item -> item.source.key }) { index, component ->
                OverviewSourceRailItemV2(
                    label = component.source.label,
                    selected = selectedIndex.intValue == index + 1,
                    loading = pages.getOrNull(index)?.second?.loadState?.refresh is LoadState.Loading,
                ) {
                    selectedIndex.intValue = index + 1
                }
            }
        }

        Box(Modifier.weight(1f).fillMaxHeight()) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(100.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp, 8.dp, 8.dp, 88.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(
                    items = visibleVerificationItems,
                    key = { "verification:${it.sourceKey}" },
                    span = { GridItemSpan(maxLineSpan) },
                ) { verification ->
                    OverviewVerificationHeaderV2(verification) {
                        overviewViewModel.onSearchNeedWebCheck(
                            sourceKey = verification.sourceKey,
                            searchNeedWebViewCheckBusinessException = verification.exception,
                            onRetry = verification.retry,
                        )
                    }
                }
                if (selectedPair == null) {
                    items(allResults, key = { "${it.sourceKey}:${it.resultIndex}:${it.cover.toIdentify()}" }) { result ->
                        OverviewCoverV2(result.cover, result.cover.toIdentify() in starred, nav::navigationDetailed, starViewModel::dispatchStar)
                    }
                    if (pendingFirstPageCount > 0) {
                        item(key = "pending-sources", span = { GridItemSpan(maxLineSpan) }) {
                            OverviewPendingSourcesV2(pendingFirstPageCount)
                        }
                    }
                } else {
                    val (_, selectedPage) = selectedPair
                    items(count = selectedPage.itemCount, key = { "selected:$it" }) { index ->
                        selectedPage[index]?.let { cover ->
                            OverviewCoverV2(cover, cover.toIdentify() in starred, nav::navigationDetailed, starViewModel::dispatchStar)
                        }
                    }
                }
            }

            selectedPair?.let { (sourceItem, page) ->
            PagingCommonSourceSearch(page) { exception ->
                overviewViewModel.onSearchNeedWebCheck(
                    sourceKey = sourceItem.searchComponent.source.key,
                    searchNeedWebViewCheckBusinessException = exception,
                    onRetry = page::retry,
                )
            }
            }
        }
    }
}

@Composable
private fun OverviewSourceRailItemV2(
    label: String,
    selected: Boolean,
    loading: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) V2Theme.colors.accentContainer else V2Tokens.Surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            color = if (selected) V2Theme.colors.accent else V2Tokens.TextSecondary,
            maxLines = 2,
        )
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.width(15.dp),
                color = V2Theme.colors.accent,
                strokeWidth = 2.dp,
            )
        }
    }
}

@Composable
private fun OverviewVerificationHeaderV2(
    item: V2OverviewVerification,
    onVerify: () -> Unit,
) {
    val captcha = item.exception.actionType == BusinessActionType.DIALOG_CAPTCHA
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onVerify),
        color = V2Tokens.SurfaceMuted,
        contentColor = V2Tokens.TextPrimary,
        shape = RoundedCornerShape(10.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(Modifier.weight(1f)) {
                Text(item.sourceLabel, color = V2Tokens.TextPrimary, maxLines = 1)
                Text(
                    if (captcha) "该来源需要验证码" else "该来源需要完成人机校验",
                    modifier = Modifier.padding(top = 3.dp),
                    color = V2Tokens.TextSecondary,
                )
            }
            Text(
                "继续验证",
                color = V2Theme.colors.accent,
            )
        }
    }
}

@Composable
private fun OverviewPendingSourcesV2(count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.width(20.dp),
            color = V2Theme.colors.accent,
            strokeWidth = 2.dp,
        )
        Text(
            "$count 个来源仍在加载",
            modifier = Modifier.padding(start = 10.dp),
            color = V2Tokens.TextSecondary,
        )
    }
}

private fun androidx.paging.compose.LazyPagingItems<CartoonCover>.refreshVerificationExceptionV2():
    SearchNeedVerificationBusinessException? {
    val error = (loadState.refresh as? LoadState.Error)?.error ?: return null
    return (error as? ParserException)?.exception as? SearchNeedVerificationBusinessException
}

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
