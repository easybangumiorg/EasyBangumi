package com.heyanle.easybangumi4.v2.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.navigationDetailed
import com.heyanle.easybangumi4.plugin.api.entity.toIdentify
import com.heyanle.easybangumi4.plugin.source.LocalSourceBundleController
import com.heyanle.easybangumi4.ui.common.CartoonCardWithCover
import com.heyanle.easybangumi4.ui.common.PagingCommonSourceSearch
import com.heyanle.easybangumi4.ui.common.cover_star.CoverStarViewModel
import com.heyanle.easybangumi4.ui.common.pagingCommonSourceSearchHor
import com.heyanle.easybangumi4.ui.search_migrate.search.SearchViewModel
import com.heyanle.easybangumi4.ui.search_migrate.search.gather.GatherSearchViewModel
import com.heyanle.easybangumi4.ui.search_migrate.search.gather.GatherSearchViewModelFactory
import com.heyanle.easybangumi4.v2.theme.V2Tokens

/** V2-owned copy of V1's grouped-by-source search presentation. */
@Composable
internal fun ColumnScope.GatherSearchV1CopyV2(searchViewModel: SearchViewModel) {
    val nav = LocalNavController.current
    val keyboard = LocalSoftwareKeyboardController.current
    val components = LocalSourceBundleController.current.searches()
    val viewModel = viewModel<GatherSearchViewModel>(
        key = "v2-gather-search",
        factory = GatherSearchViewModelFactory(components),
    )
    val starViewModel = viewModel<CoverStarViewModel>()
    val request = searchViewModel.searchRequestFlow.collectAsState()
    LaunchedEffect(request.value) { viewModel.submitSearch(request.value) }
    LaunchedEffect(components) { viewModel.updateSearchComponents(components) }
    val items = viewModel.searchItemList.collectAsState().value

    HorizontalDivider(color = V2Tokens.Divider)
    LazyColumn(
        modifier = Modifier.fillMaxWidth().weight(1f).nestedScroll(object : NestedScrollConnection {
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                keyboard?.hide()
                return super.onPostScroll(consumed, available, source)
            }
        }),
    ) {
        items?.let { sourceItems ->
            items(sourceItems, key = { it.searchComponent.source.key }) { sourceItem ->
                val page = sourceItem.flow.collectAsLazyPagingItems()
                val starred = starViewModel.stateFlow.collectAsState().value.identifySet
                val haptic = LocalHapticFeedback.current
                Column(Modifier.fillMaxWidth().height(250.dp)) {
                    ListItem(
                        headlineContent = { Text(sourceItem.searchComponent.source.label) },
                        trailingContent = { Text(stringResource(R.string.long_press_to_star), color = V2Tokens.TextSecondary) },
                        colors = ListItemDefaults.colors(containerColor = V2Tokens.WarmBackground),
                    )
                    if (page.itemCount > 0) {
                        LazyRow(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            contentPadding = PaddingValues(4.dp),
                        ) {
                            items(page.itemCount) { index ->
                                page[index]?.let { cover ->
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
                                }
                            }
                            pagingCommonSourceSearchHor(page) { exception ->
                                viewModel.onSearchNeedWebCheck(
                                    sourceKey = sourceItem.searchComponent.source.key,
                                    searchNeedWebViewCheckBusinessException = exception,
                                    onRetry = page::retry,
                                )
                            }
                        }
                    }
                    PagingCommonSourceSearch(page) { exception ->
                        viewModel.onSearchNeedWebCheck(
                            sourceKey = sourceItem.searchComponent.source.key,
                            searchNeedWebViewCheckBusinessException = exception,
                            onRetry = page::retry,
                        )
                    }
                }
            }
        }
    }
}
