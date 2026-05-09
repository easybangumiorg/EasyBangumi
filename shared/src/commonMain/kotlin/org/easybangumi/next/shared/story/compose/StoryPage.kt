package org.easybangumi.next.shared.story.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import org.easybangumi.next.shared.story.StoryTab
import org.easybangumi.next.shared.story.StoryViewModel

/**
 * Story 页面
 */
@Composable
fun StoryPage(
    viewModel: StoryViewModel,
    onPlayLocal: (String) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState { 3 }

    Column(modifier = Modifier.fillMaxSize()) {
        // Tab 栏
        TabRow(selectedTabIndex = pagerState.currentPage) {
            Tab(
                selected = pagerState.currentPage == 0,
                onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                text = { Text("下载中 (${uiState.downloadingItems.size})") },
            )
            Tab(
                selected = pagerState.currentPage == 1,
                onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                text = { Text("已完成 (${uiState.completedItems.size})") },
            )
            Tab(
                selected = pagerState.currentPage == 2,
                onClick = { scope.launch { pagerState.animateScrollToPage(2) } },
                text = { Text("本地番剧 (${uiState.localItems.size})") },
            )
        }

        // Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            when (page) {
                0 -> DownloadingList(
                    items = uiState.downloadingItems,
                    onPause = { viewModel.pauseDownload(it) },
                    onResume = { viewModel.resumeDownload(it) },
                    onCancel = { viewModel.cancelDownload(it) },
                )
                1 -> CompletedList(
                    items = uiState.completedItems,
                    onRemove = { viewModel.removeCompleted(it) },
                )
                2 -> LocalAnimeList(
                    items = uiState.localItems,
                    onPlay = { onPlayLocal(it.itemId) },
                    onDelete = { viewModel.deleteLocalItem(it.itemId) },
                    onRefresh = { viewModel.refreshLocal() },
                )
            }
        }
    }
}
