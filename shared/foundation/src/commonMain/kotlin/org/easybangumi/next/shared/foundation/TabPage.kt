package org.easybangumi.next.shared.foundation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.easybangumi.next.shared.scheme.EasyScheme


@Composable
fun EasyTab(
    modifier: Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    scrollable: Boolean = false,
    size: Int,
    selection: Int,
    tabWidth: Dp = EasyScheme.size.tabWidth,
    onSelected: (Int) -> Unit,
    tabs: @Composable (Int, Boolean) -> Unit,
) {
    if (scrollable) {
        ScrollableTabRow(
            modifier = modifier,
            containerColor = containerColor,
            contentColor = contentColor,
            selectedTabIndex = selection,
            indicator = {
                if(selection in it.indices){
                    TabIndicator(currentTabPosition = it[selection])
                }
            },
            edgePadding = 0.dp,
            divider = {}
        ) {
            repeat(size) {
                Tab(
                    modifier = Modifier.widthIn(tabWidth),
                    selected = selection == it, onClick = {
                        onSelected(it)
                    }, text = {
                        tabs(it, selection == it)
                    })
            }
        }
    } else {
        TabRow(
            modifier = modifier,
            containerColor = containerColor,
            contentColor = contentColor,
            selectedTabIndex = selection,
            indicator = {
                if(selection in it.indices){
                    TabIndicator(currentTabPosition = it[selection])
                }
            },
//        edgePadding = 0.dp,
            divider = {}
        ) {
            repeat(size) {
                Tab(
                    modifier = Modifier.widthIn(tabWidth),
                    selected = selection == it, onClick = {
                        onSelected(it)
                    }, text = {
                        tabs(it, selection == it)
                    })
            }
        }
    }

}

@Composable
fun TabPage(
    modifier: Modifier = Modifier,
    pagerModifier: Modifier = Modifier,
    pagerState: PagerState,
    beyondBoundsPageCount: Int = 0,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    onTabSelect: (Int) -> Unit,
    tabs: @Composable (Int, Boolean) -> Unit,
    contents: @Composable PagerScope.(Int) -> Unit,
) {
    val scope = rememberCoroutineScope()
    LaunchedEffect(key1 = pagerState.currentPage){
        onTabSelect(pagerState.currentPage)
    }
    CompositionLocalProvider(
        LocalContentColor provides contentColor
    ) {
        Column(
            modifier
        ) {
            EasyTab(
                modifier = Modifier
                    .fillMaxWidth(),
                containerColor = containerColor,
                contentColor = contentColor,
                size = pagerState.pageCount,
                selection = pagerState.currentPage,
                onSelected = {
                    onTabSelect(it)
                    scope.launch {
                        pagerState.animateScrollToPage(it)
                    }
                },
                tabs = tabs
            )
            HorizontalDivider()
            HorizontalPager(
                modifier = pagerModifier.weight(1f),
                beyondViewportPageCount = beyondBoundsPageCount,
                verticalAlignment = Alignment.Top,
                state = pagerState,
            ) {
                contents(it)
            }
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