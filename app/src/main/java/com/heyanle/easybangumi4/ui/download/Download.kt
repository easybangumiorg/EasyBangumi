package com.heyanle.easybangumi4.ui.download

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.heyanle.easybangumi4.download.DownloadController
import com.heyanle.easybangumi4.ui.common.TabIndicator
import com.heyanle.easybangumi4.ui.download.downloaded.Downloaded
import com.heyanle.easybangumi4.ui.download.downloaded.DownloadedTopBar
import com.heyanle.easybangumi4.ui.download.downloading.Downloading
import com.heyanle.easybangumi4.ui.download.downloading.DownloadingTopBar
import com.heyanle.injekt.core.Injekt
import kotlinx.coroutines.launch

/**
 * Created by heyanlin on 2023/11/2.
 */
sealed class DownloadPage(
    val tabLabel: @Composable (() -> Unit),
    val topAppBar: @Composable (() -> Unit),
    val content: @Composable (() -> Unit),
) {

    @OptIn(ExperimentalMaterial3Api::class)
    data object Downloading : DownloadPage(
        tabLabel = { Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.downloading)) },
        topAppBar = { DownloadingTopBar() },
        content = { Downloading() }
    )

    @OptIn(ExperimentalMaterial3Api::class)
    data object Downloaded : DownloadPage(
        tabLabel = { Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.finished)) },
        topAppBar = { DownloadedTopBar() },
        content = { Downloaded() }
    )

}

val DownloadPageItems = listOf<DownloadPage>(
    DownloadPage.Downloading,
    DownloadPage.Downloaded
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Download() {
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(0) { DownloadPageItems.size }
    LaunchedEffect(Unit) {
        val downloadController: DownloadController by Injekt.injectLazy()
        downloadController.tryShowFirstDownloadDialog()
    }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        Column {
            DownloadPageItems[pagerState.currentPage].topAppBar()
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                modifier = Modifier.fillMaxWidth(),
                indicator = {
                    TabIndicator(currentTabPosition = it[pagerState.currentPage])
                },
            ) {
                DownloadPageItems.forEachIndexed { index, downloadPage ->
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
                val page = DownloadPageItems[it]
                page.content()
            }
        }
    }
}