package com.heyanle.easybangumi4.ui.download

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.download.entity.DownloadItem
import com.heyanle.easybangumi4.download.entity.LocalCartoon
import com.heyanle.easybangumi4.ui.common.OkImage
import com.heyanle.easybangumi4.ui.common.TabIndicator
import kotlinx.coroutines.launch

/**
 * Created by heyanlin on 2023/8/9.
 * https://github.com/heyanLE
 */

sealed class DownloadPage(
    val tabLabel: @Composable (() -> Unit),
    val topAppBar: @Composable (() -> Unit),
    val content: @Composable (() -> Unit),
) {
    @OptIn(ExperimentalMaterial3Api::class)
    data object Downloading : DownloadPage(
        tabLabel = {
            Text(stringResource(R.string.downloading))
        },
        topAppBar = {
            val nav = LocalNavController.current
            TopAppBar(
                title = { Text(stringResource(R.string.local_download)) },
                navigationIcon = {
                    IconButton(onClick = {
                        nav.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            stringResource(id = R.string.back)
                        )
                    }
                },
            )
        },
        content = {
            val vm = viewModel<DownloadViewModel>()
            Downloading(vm)
        },
    )

    @OptIn(ExperimentalMaterial3Api::class)
    data object Downloaded : DownloadPage(
        tabLabel = {
            Text(stringResource(R.string.finished))
        },
        topAppBar = {
            var isSearch by remember {
                mutableStateOf(false)
            }
            val nav = LocalNavController.current

            val localVM = viewModel<LocalCartoonViewModel>()
            val keyword = localVM.keyword.collectAsState()
            TopAppBar(
                title = {
                    if (!isSearch) {
                        Text(stringResource(R.string.local_download))
                    } else {
                        TextField(keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = {
                                localVM.search(keyword.value)
                            }),
                            maxLines = 1,
                            modifier = Modifier.focusRequester(localVM.focusRequester),
                            colors = TextFieldDefaults.textFieldColors(
                                containerColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                            ),
                            value = keyword.value,
                            onValueChange = {
                                localVM.search(it)
                            },
                            placeholder = {
                                Text(
                                    style = MaterialTheme.typography.titleLarge,
                                    text = stringResource(id = R.string.please_input_keyword_to_search)
                                )
                            })
                    }

                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isSearch) {
                            isSearch = false
                        } else {
                            nav.popBackStack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            stringResource(id = R.string.back)
                        )
                    }
                },
            )
        },
        content = {
            val vm = viewModel<LocalCartoonViewModel>()
            LocalCartoonPage(vm)
        },
    )
}

val DownloadPageItems = listOf(
    DownloadPage.Downloading,
    DownloadPage.Downloaded,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Download() {
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(0) { 2 }
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

@Composable
fun LocalCartoonPage(
    localCartoonViewModel: LocalCartoonViewModel
){
    val list = localCartoonViewModel.localCartoonFlow.collectAsState()
    val state = rememberLazyListState()
    Box(modifier = Modifier.fillMaxSize()){

    }
}
@Composable
fun Downloading(
    downloadViewModel: DownloadViewModel,
) {
    val list by downloadViewModel.downloadingFlow.collectAsState()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        items(list) {
            DownloadItem(it, downloadViewModel) {
                downloadViewModel.click(it)
            }
        }
    }
}

@Composable
fun DownloadItem(
    downloadItem: DownloadItem,
    downloadViewModel: DownloadViewModel,
    onClick: (DownloadItem) -> Unit,
) {
    val info = downloadViewModel.info(downloadItem)
    Row(
        modifier = Modifier
            .padding(8.dp, 4.dp)
            .height(IntrinsicSize.Min)
            .clickable {
                onClick(downloadItem)
            }
    ) {
        OkImage(
            modifier = Modifier
                .width(95.dp)
                .aspectRatio(19 / 13.5F)
                .clip(RoundedCornerShape(4.dp)),
            image = downloadItem.cartoonCover,
            contentDescription = downloadItem.cartoonTitle
        )
        Spacer(modifier = Modifier.size(8.dp))
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                modifier = Modifier,
                text = (downloadItem.cartoonTitle),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                modifier = Modifier,
                text = "${downloadItem.episodeLabel}-${downloadItem.playLine.label}",
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(info.status.value)
                Text(info.subStatus.value)
            }
            if (info.process.value == -1f) {
                LinearProgressIndicator()
            } else {
                LinearProgressIndicator(info.process.value)
            }

        }
    }
}


@Composable
fun LocalCartoonItem(
    localCartoon: LocalCartoon,
    localCartoonViewModel: LocalCartoonViewModel,
    onClick: (LocalCartoon) -> Unit,
){
    val num = remember(localCartoon) {
        var res = 0
        localCartoon.playLines.forEach { res += it.list.size  }
        return@remember res
    }
    Row(
        modifier = Modifier
            .padding(8.dp, 4.dp)
            .height(IntrinsicSize.Min)
            .clickable {
                onClick(localCartoon)
            }
    ) {
        OkImage(
            modifier = Modifier
                .width(95.dp)
                .aspectRatio(19 / 13.5F)
                .clip(RoundedCornerShape(4.dp)),
            image = localCartoon.cartoonCover,
            contentDescription = localCartoon.cartoonTitle
        )
        Spacer(modifier = Modifier.size(8.dp))
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                modifier = Modifier,
                text = (localCartoon.cartoonTitle),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}