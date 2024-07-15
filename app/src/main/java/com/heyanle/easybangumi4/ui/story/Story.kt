package com.heyanle.easybangumi4.ui.story

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.ui.common.TabIndicator
import com.heyanle.easybangumi4.ui.story.download.Download
import com.heyanle.easybangumi4.ui.story.download.DownloadTopAppBar
import com.heyanle.easybangumi4.ui.story.local.Local
import com.heyanle.easybangumi4.ui.story.local.LocalTopAppBar
import kotlinx.coroutines.launch

/**
 * Created by heyanle on 2024/7/15.
 * https://github.com/heyanLE
 */
@OptIn(ExperimentalMaterial3Api::class)
sealed class StoryPage constructor(
    val tabLabel: @Composable (() -> Unit),
    val topAppBar: @Composable (() -> Unit),
    val content: @Composable (() -> Unit),
) {

    data object Download :
        StoryPage(tabLabel = { Text(text = stringResource(id = R.string.download_history)) },
            topAppBar = {
                DownloadTopAppBar()
            },
            content = {
                Download()
            })

    data object Local :
        StoryPage(tabLabel = { Text(text = stringResource(id = R.string.local_source)) },
            topAppBar = {
                LocalTopAppBar()
            },
            content = {
                Local()
            })

}

val StoryPageItems = listOf<StoryPage>(
    StoryPage.Download,
    StoryPage.Local,
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun Story() {
    val nav = LocalNavController.current
    val pagerState = rememberPagerState(0) { StoryPageItems.size }
    val scope = rememberCoroutineScope()
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        Column {
            StoryPageItems[pagerState.currentPage].topAppBar()
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                modifier = Modifier.fillMaxWidth(),
                indicator = {
                    TabIndicator(currentTabPosition = it[pagerState.currentPage])
                },
            ) {
                StoryPageItems.forEachIndexed { index, downloadPage ->
                    Tab(selected = index == pagerState.currentPage,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = {
                            downloadPage.tabLabel()
                        })
                }
            }

            HorizontalPager(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                state = pagerState,
            ) {
                val page = StoryPageItems[it]
                page.content()
            }
        }
    }
}