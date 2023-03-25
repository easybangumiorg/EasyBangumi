package com.heyanle.easybangumi4.ui.main.explore

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.heyanle.easybangumi4.ui.main.explore.extension.Extension
import com.heyanle.easybangumi4.ui.main.explore.extension.ExtensionTopAppBar
import com.heyanle.easybangumi4.ui.main.explore.source.Source
import com.heyanle.easybangumi4.ui.main.explore.source.SourceTopAppBar
import com.heyanle.okkv2.core.okkv
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/2/21 23:20.
 * https://github.com/heyanLE
 */

sealed class ExplorePage @OptIn(ExperimentalMaterial3Api::class) constructor(
    val tabLabel: @Composable (() -> Unit),
    val topAppBar: @Composable ((TopAppBarScrollBehavior) -> Unit),
    val content: @Composable (() -> Unit),
) {

    @OptIn(ExperimentalMaterial3Api::class)
    object SourcePage : ExplorePage(
        tabLabel = {
            Text(stringResource(id = com.heyanle.easy_i18n.R.string.source))
        },
        topAppBar = {
            SourceTopAppBar(it)
        },
        content = {
            Source()
        },
    )

    @OptIn(ExperimentalMaterial3Api::class)
    object ExtensionPage : ExplorePage(
        tabLabel = {
            Text(stringResource(id = com.heyanle.easy_i18n.R.string.extension))
        },
        topAppBar = {
            ExtensionTopAppBar(it)
        },
        content = {
            Extension()
        },
    )

}

val ExplorePageItems = listOf(
    ExplorePage.SourcePage,
    ExplorePage.ExtensionPage
)

var explorePageIndex by okkv("explorePageInitPageIndex", 0)

@OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Explore() {

    val pagerState = rememberPagerState(initialPage = explorePageIndex)
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val vm = viewModel<ExploreViewModel>()
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = Unit) {
        pagerState.scrollToPage(explorePageIndex)
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        CompositionLocalProvider(
            LocalViewModelStoreOwner provides vm.getViewModelStoreOwner(ExplorePageItems[pagerState.currentPage])
        ) {
            ExplorePageItems[pagerState.currentPage].topAppBar(scrollBehavior)
        }
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            modifier = Modifier.fillMaxWidth()
        ) {
            ExplorePageItems.forEachIndexed { index, explorePage ->
                Tab(selected = index == pagerState.currentPage,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = {
                        explorePage.tabLabel()
                    })
            }
        }

        HorizontalPager(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            count = ExplorePageItems.size,
            state = pagerState
        ) {
            val page = ExplorePageItems[it]
            CompositionLocalProvider(
                LocalViewModelStoreOwner provides vm.getViewModelStoreOwner(page)
            ) {
                page.content()
            }
        }

    }


}