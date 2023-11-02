package com.heyanle.easybangumi4.ui.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Divider
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/7/30 14:44.
 * https://github.com/heyanLE
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ColumnScope.TabPage(
    initialPage: Int = 0,
    tabSize: Int,
    pagerState: PagerState = rememberPagerState(initialPage) {
        tabSize
    },
    onTabSelect: (Int) -> Unit,
    tabs: @Composable ColumnScope.(Int, Boolean) -> Unit,
    contents: @Composable PagerScope.(Int) -> Unit,
) {
    val scope = rememberCoroutineScope()
    LaunchedEffect(key1 = pagerState.currentPage){
        onTabSelect(pagerState.currentPage)
    }
    ScrollableTabRow(
        selectedTabIndex = pagerState.currentPage,
        indicator = {
            if(pagerState.currentPage in it.indices){
                TabIndicator(currentTabPosition = it[pagerState.currentPage])
            }

        },
        modifier = Modifier
            .fillMaxWidth(),
        edgePadding = 0.dp,
        divider = {}
    ) {
        repeat(tabSize) {
            Tab(selected = pagerState.currentPage == it, onClick = {
                onTabSelect(it)
                scope.launch {
                    pagerState.animateScrollToPage(it)
                }

            }, text = {
                tabs(it, pagerState.currentPage == it)
            })
        }
    }
    Divider()
    HorizontalPager(
        state = pagerState,
    ) {
        contents(it)
    }

}