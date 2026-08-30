package com.heyanle.easybangumi4.v2.ui.search

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easybangumi4.plugin.source.LocalSourceBundleController
import com.heyanle.easybangumi4.ui.search_migrate.search.SearchViewModel
import com.heyanle.easybangumi4.ui.search_migrate.search.normal.NormalSearchViewModel
import com.heyanle.easybangumi4.ui.search_migrate.search.normal.NormalSearchViewModelFactory
import com.heyanle.easybangumi4.v2.ui.component.V2ScrollableTabs
import kotlinx.coroutines.launch

/** V2-owned copy of V1's single-source tab/pager shell. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ColumnScope.NormalSearchV1CopyV2(
    initialSourceKey: String,
    searchViewModel: SearchViewModel,
) {
    val components = LocalSourceBundleController.current.searches()
    val initialIndex = components.indexOfFirst { it.source.key == initialSourceKey }
        .coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = initialIndex) { components.size }
    val scope = rememberCoroutineScope()

    V2ScrollableTabs(
        labels = components.map { it.source.label },
        selectedIndex = pagerState.currentPage,
        onSelected = { scope.launch { pagerState.animateScrollToPage(it) } },
    )
    HorizontalPager(
        modifier = Modifier.fillMaxWidth().weight(1f),
        state = pagerState,
    ) { page ->
        components.getOrNull(page)?.let { component ->
            val pageViewModel = viewModel<NormalSearchViewModel>(
                factory = NormalSearchViewModelFactory(component),
                viewModelStoreOwner = searchViewModel.viewModelOwnerMap.getViewModelStoreOwner(component.source.key),
            )
            NormalSearchPageV1CopyV2(
                visible = page == pagerState.currentPage,
                searchViewModel = searchViewModel,
                pageViewModel = pageViewModel,
            )
        }
    }
}
