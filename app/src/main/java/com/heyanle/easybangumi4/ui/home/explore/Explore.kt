package com.heyanle.easybangumi4.ui.home.explore

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.heyanle.easybangumi.ui.home.animInitialPage
import com.heyanle.easybangumi4.ui.home.HomePage
import com.heyanle.easybangumi4.ui.home.explore.extension.Extension
import com.heyanle.easybangumi4.ui.home.explore.extension.ExtensionTopAppBar
import com.heyanle.easybangumi4.ui.home.explore.source.Source
import com.heyanle.easybangumi4.ui.home.explore.source.SourceTopAppBar
import com.heyanle.okkv2.core.okkv
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/2/21 23:20.
 * https://github.com/heyanLE
 */

sealed class ExplorePage(
    val tabLabel: @Composable (() -> Unit),
    val topAppBar: @Composable (() -> Unit),
    val content: @Composable (() -> Unit),
    val viewModelStore: ViewModelStore = ViewModelStore()
) {

    object SourcePage : ExplorePage(
        tabLabel = {
            Text(stringResource(id = com.heyanle.easy_i18n.R.string.source))
        },
        topAppBar = {
            SourceTopAppBar()
        },
        content = {
            Source()
        },
    )

    object ExtensionPage : ExplorePage(
        tabLabel = {
            Text(stringResource(id = com.heyanle.easy_i18n.R.string.extension))
        },
        topAppBar = {
            ExtensionTopAppBar()
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
    Scaffold(
        topBar = {
            val page = ExplorePageItems[pagerState.currentPage]
            CompositionLocalProvider(
                LocalViewModelStoreOwner provides vm.getViewModelStoreOwner(page)
            ) {
                page.topAppBar()
            }
        },
        content = { padding ->
            Column(
                modifier = Modifier.padding(padding)
            ) {
                TabRow(selectedTabIndex = pagerState.currentPage) {
                    ExplorePageItems.forEachIndexed { index, explorePage ->
                        Tab(selected = index == pagerState.currentPage,
                            onClick = {
                                scope.launch {
                                    pagerState.scrollToPage(index)
                                }
                            }) {
                            explorePage.tabLabel()
                        }
                    }
                }
                HorizontalPager(
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
    )


}