@file:OptIn(ExperimentalMaterial3Api::class)

package com.heyanle.easybangumi4.ui.source_manage

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.heyanle.easybangumi4.ui.common.TabIndicator
import com.heyanle.easybangumi4.ui.source_manage.extension.Extension
import com.heyanle.easybangumi4.ui.source_manage.extension.ExtensionTopAppBar
import com.heyanle.easybangumi4.ui.source_manage.source.Source
import com.heyanle.easybangumi4.ui.source_manage.source.SourceTopAppBar
import com.heyanle.okkv2.core.okkv
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/2/21 23:20.
 * https://github.com/heyanLE
 */

sealed class ExplorePage constructor(
    val tabLabel: @Composable (() -> Unit),
    val topAppBar: @Composable ((TopAppBarScrollBehavior) -> Unit),
    val content: @Composable (() -> Unit),
) {

    data object SourcePage : ExplorePage(
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

    data object ExtensionPage : ExplorePage(
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SourceManager(
    defIndex: Int = -1
) {

    val pagerState =
        rememberPagerState(initialPage = if (defIndex == -1) explorePageIndex else defIndex, 0f) {
            ExplorePageItems.size
        }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = Unit) {
        pagerState.scrollToPage(if (defIndex == -1) explorePageIndex else defIndex)
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        ExplorePageItems[pagerState.currentPage].topAppBar(scrollBehavior)
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            modifier = Modifier.fillMaxWidth(),
            indicator = {
                TabIndicator(currentTabPosition = it[pagerState.currentPage])
            },
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
            state = pagerState,
        ) {
            val page = ExplorePageItems[it]
            page.content()
        }

    }


}