package com.heyanle.easybangumi4.ui.download

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.download.DownloadController
import com.heyanle.easybangumi4.download.entity.DownloadItem
import com.heyanle.easybangumi4.download.entity.LocalCartoon
import com.heyanle.easybangumi4.navigationLocalPlay
import com.heyanle.easybangumi4.ui.common.EasyClearDialog
import com.heyanle.easybangumi4.ui.common.EasyDeleteDialog
import com.heyanle.easybangumi4.ui.common.FastScrollToTopFab
import com.heyanle.easybangumi4.ui.common.OkImage
import com.heyanle.easybangumi4.ui.common.TabIndicator
import com.heyanle.injekt.core.Injekt
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
            val vm = viewModel<DownloadViewModel>()
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
                actions = {
                    if (vm.selection.isNotEmpty()) {
                        IconButton(onClick = {
                            val set = mutableSetOf<DownloadItem>()
                            set.addAll(vm.selection.keys)
                            vm.removeDownloadItem.value = set
                            vm.selection.clear()
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                stringResource(id = R.string.delete)
                            )
                        }
                    }else{
                        IconButton(onClick = {
                            val downloadController: DownloadController by Injekt.injectLazy()
                            downloadController.showDownloadHelpDialog()
                        }){
                            Icon(
                                imageVector = Icons.Filled.Help,
                                stringResource(id = R.string.download)
                            )
                        }
                    }
                }
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
                        LaunchedEffect(key1 = isSearch) {
                            if (isSearch) {
                                runCatching {
                                    localVM.focusRequester.requestFocus()
                                }.onFailure {
                                    it.printStackTrace()
                                }
                            }
                        }
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
                actions = {
                    IconButton(onClick = {
                        if (!isSearch) {
                            isSearch = true
                            try {
                                localVM.focusRequester.requestFocus()

                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        } else {
                            localVM.search(keyword.value)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            stringResource(id = R.string.search)
                        )
                    }
                }
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LocalCartoonPage(
    localCartoonViewModel: LocalCartoonViewModel
) {
    val list = localCartoonViewModel.localCartoonFlow.collectAsState()
    val state = rememberLazyListState()
    val keyboard = LocalSoftwareKeyboardController.current
    val nav = LocalNavController.current
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(object : NestedScrollConnection {
                    override fun onPreScroll(
                        available: Offset,
                        source: NestedScrollSource
                    ): Offset {
                        keyboard?.hide()
                        return super.onPreScroll(available, source)
                    }
                }),
            state = state,
        ) {
            items(list.value) {
                LocalCartoonItem(localCartoon = it, onClick = {
                    nav.navigationLocalPlay(it.uuid)
                })
            }
        }

        FastScrollToTopFab(listState = state)
    }
}

@Composable
fun Downloading(
    downloadViewModel: DownloadViewModel,
) {
    val list by downloadViewModel.downloadingFlow.collectAsState()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(list) {
            DownloadItem(it, downloadViewModel, onClick = {
                if (downloadViewModel.selection.isEmpty()) {
                    downloadViewModel.click(it)
                } else {
                    if(downloadViewModel.selection.containsKey(it)){
                        downloadViewModel.selection.remove(it)
                    }else{
                        downloadViewModel.selection.put(it, true)
                    }
                }
            }, onLongPress = {
                downloadViewModel.selection.put(it, true)
            })
        }
    }

    if(downloadViewModel.removeDownloadItem.value?.isNotEmpty() == true){
        EasyDeleteDialog(
            message = {
                      Text(stringResource(R.string.delete_confirmation_num, downloadViewModel.removeDownloadItem.value?.size?:0))
            },
            show = downloadViewModel.removeDownloadItem.value?.isNotEmpty() == true,
            onDelete = {
                downloadViewModel.removeDownloadItem.value?.let {
                    downloadViewModel.remove(it)
                }
                downloadViewModel.removeDownloadItem.value = null
            },
            onDismissRequest = {
                downloadViewModel.removeDownloadItem.value = null
            }
        )
    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DownloadItem(
    downloadItem: DownloadItem,
    downloadViewModel: DownloadViewModel,
    onClick: (DownloadItem) -> Unit,
    onLongPress: ((DownloadItem) -> Unit)? = null,
) {
    val info = downloadViewModel.info(downloadItem)
    val isSelect = downloadViewModel.selection.getOrElse(downloadItem) { false }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .run {
                if (isSelect) {
                    background(MaterialTheme.colorScheme.primary)
                } else {
                    this
                }
            }
            .combinedClickable(
                onClick = {
                    onClick(downloadItem)
                },
                onLongClick = {
                    onLongPress?.invoke(downloadItem)
                }
            )
            .padding(8.dp, 4.dp)
            .height(IntrinsicSize.Min)

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
                color = if (isSelect) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
            )
            Text(
                modifier = Modifier,
                text = "${downloadItem.episodeLabel}-${downloadItem.playLine.label}",
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isSelect) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (downloadItem.state == -1) {
                    Text(
                        stringResource(R.string.download_error),
                        maxLines = 1,
                        color = if (isSelect) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        downloadItem.errorMsg,
                        maxLines = 1,
                        color = if (isSelect) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                    )
                } else {
                    Text(
                        info.status.value,
                        maxLines = 1,
                        color = if (isSelect) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        info.subStatus.value,
                        maxLines = 1,
                        color = if (isSelect) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                    )
                }

            }
            if (downloadItem.state == -1) {
                LinearProgressIndicator(0f)
            } else {
                if (info.process.value == -1f) {
                    LinearProgressIndicator()
                } else {
                    LinearProgressIndicator(info.process.value)
                }
            }
        }
    }
}


@Composable
fun LocalCartoonItem(
    localCartoon: LocalCartoon,
    onClick: (LocalCartoon) -> Unit,
) {
    val num = remember(localCartoon) {
        var res = 0
        localCartoon.playLines.forEach { res += it.list.size }
        return@remember res
    }
    Row(
        modifier = Modifier
            .padding(8.dp, 4.dp)
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(4.dp))
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
            Text(
                modifier = Modifier,
                text = localCartoon.sourceLabel,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                modifier = Modifier,
                text = stringResource(id = R.string.video_num_x, num),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}