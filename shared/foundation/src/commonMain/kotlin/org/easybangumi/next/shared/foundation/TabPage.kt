package org.easybangumi.next.shared.foundation

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun ColumnScope.TabPage(
    pagerModifier: Modifier = Modifier,
    pagerState: PagerState,
    beyondBoundsPageCount: Int = 0,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    onTabSelect: (Int) -> Unit,
    tabs: @Composable ColumnScope.(Int, Boolean) -> Unit,
    contents: @Composable PagerScope.(Int) -> Unit,
) {
    val scope = rememberCoroutineScope()
    LaunchedEffect(key1 = pagerState.currentPage){
        onTabSelect(pagerState.currentPage)
    }
    CompositionLocalProvider(
        LocalContentColor provides contentColor
    ) {
        ScrollableTabRow(
            containerColor = containerColor,
            contentColor = contentColor,
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
            repeat(pagerState.pageCount) {
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
        HorizontalDivider()
        HorizontalPager(
            modifier = pagerModifier,
            beyondViewportPageCount = beyondBoundsPageCount,
            verticalAlignment = Alignment.Top,
            state = pagerState,
        ) {
            contents(it)
        }
    }


}

@Composable
fun TabIndicator(currentTabPosition: TabPosition) {
    TabRowDefaults.SecondaryIndicator(
        Modifier
            .tabIndicatorOffset(currentTabPosition)
            .padding(horizontal = 8.dp)
            .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)),
    )
}