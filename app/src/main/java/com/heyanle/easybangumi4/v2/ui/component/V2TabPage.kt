package com.heyanle.easybangumi4.v2.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.heyanle.easybangumi4.ui.common.TabIndicator
import com.heyanle.easybangumi4.v2.theme.V2Tokens
import kotlinx.coroutines.launch

/** V2-owned copy of the V1 TabPage contract, mapped exclusively to V2 semantic colors. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ColumnScope.V2TabPage(
    pagerModifier: Modifier = Modifier,
    tabSize: Int,
    pagerState: PagerState = rememberPagerState { tabSize },
    onTabSelect: (Int) -> Unit,
    tabs: @Composable ColumnScope.(Int, Boolean) -> Unit,
    contents: @Composable PagerScope.(Int) -> Unit,
) {
    val scope = rememberCoroutineScope()
    LaunchedEffect(pagerState.currentPage) { onTabSelect(pagerState.currentPage) }
    CompositionLocalProvider(LocalContentColor provides V2Tokens.TextPrimary) {
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = V2Tokens.WarmBackground,
            contentColor = V2Tokens.TextPrimary,
            edgePadding = 0.dp,
            divider = {},
            indicator = { positions ->
                if (pagerState.currentPage in positions.indices) TabIndicator(positions[pagerState.currentPage])
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            repeat(tabSize) { index ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    text = { tabs(index, pagerState.currentPage == index) },
                )
            }
        }
        HorizontalDivider(color = V2Tokens.Divider)
        HorizontalPager(
            state = pagerState,
            modifier = pagerModifier,
            verticalAlignment = Alignment.Top,
        ) { contents(it) }
    }
}
