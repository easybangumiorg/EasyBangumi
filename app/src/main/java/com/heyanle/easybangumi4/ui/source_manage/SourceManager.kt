@file:OptIn(ExperimentalMaterial3Api::class)

package com.heyanle.easybangumi4.ui.source_manage

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.heyanle.easybangumi4.ui.source_manage.source.Source
import com.heyanle.easybangumi4.ui.source_manage.source.SourceTopAppBar
import com.heyanle.okkv2.core.okkv

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

}

val ExplorePageItems: List<ExplorePage> by lazy {
    listOf(ExplorePage.SourcePage)
}

var explorePageIndex by okkv("explorePageInitPageIndex", 0)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SourceManager(
    defIndex: Int = -1
) {
    val initialPage = (if (defIndex == -1) explorePageIndex else defIndex)
        .coerceIn(0, ExplorePageItems.lastIndex)

    val pagerState =
        rememberPagerState(initialPage = initialPage, 0f) {
            ExplorePageItems.size
        }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        ExplorePageItems[pagerState.currentPage].topAppBar(scrollBehavior)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            ExplorePageItems[pagerState.currentPage].content()
        }

    }


}
