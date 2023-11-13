package com.heyanle.easybangumi4.ui.download.downloaded

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.R
import com.heyanle.easybangumi4.cartoon_download.entity.LocalCartoon
import com.heyanle.easybangumi4.navigationLocalPlay
import com.heyanle.easybangumi4.ui.common.EasyDeleteDialog
import com.heyanle.easybangumi4.ui.common.FastScrollToTopFab
import com.heyanle.easybangumi4.ui.common.OkImage
import com.heyanle.easybangumi4.ui.common.SelectionTopAppBar

/**
 * Created by heyanlin on 2023/11/2.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Downloaded() {
    val downloadedViewModel = viewModel<DownloadedViewModel>()
    val list = downloadedViewModel.localCartoonFlow.collectAsState()
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
            contentPadding = PaddingValues(4.dp, 4.dp, 4.dp, 64.dp),
            state = state,
        ) {
            items(list.value) {
                DownloadedItem(localCartoon = it, vm = downloadedViewModel, onClick = {
                    if (downloadedViewModel.selection.isEmpty()) {
                        nav.navigationLocalPlay(it.uuid)
                    } else if (downloadedViewModel.selection.containsKey(it)) {
                        downloadedViewModel.selection.remove(it)
                    } else {
                        downloadedViewModel.selection[it] = true
                    }

                }, onLongPress = {
                    if (downloadedViewModel.selection.containsKey(it)) {
                        downloadedViewModel.selection.remove(it)
                    } else {
                        downloadedViewModel.selection[it] = true
                    }
                })
            }
        }

        if(downloadedViewModel.selection.isNotEmpty()){
            Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.fillMaxSize()) {
                val up = remember { derivedStateOf { state.firstVisibleItemIndex > 10 } }
                val downPadding by animateDpAsState(if (up.value) 80.dp else 40.dp, label = "")
                ExtendedFloatingActionButton(
                    modifier = Modifier
                        .padding(16.dp, downPadding),
                    text = {
                        Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.delete))
                    },
                    icon = {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = stringResource(id = com.heyanle.easy_i18n.R.string.delete)
                        )
                    },
                    onClick = {
                        downloadedViewModel.removeDownloadItem.value = downloadedViewModel.selection.keys.toList()
                        downloadedViewModel.selection.clear()
                    }
                )
            }
        }

        if(downloadedViewModel.removeDownloadItem.value?.isNotEmpty() == true){
            EasyDeleteDialog(
                message = {
                    Text(stringResource(com.heyanle.easy_i18n.R.string.delete_confirmation_num, downloadedViewModel.removeDownloadItem.value?.size?:0))
                },
                show = downloadedViewModel.removeDownloadItem.value?.isNotEmpty() == true,
                onDelete = {
                    downloadedViewModel.removeDownloadItem.value?.let {
                        downloadedViewModel.remove(it)
                    }
                    downloadedViewModel.removeDownloadItem.value = null
                },
                onDismissRequest = {
                    downloadedViewModel.removeDownloadItem.value = null
                }
            )
        }

        FastScrollToTopFab(listState = state)
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DownloadedTopBar() {
    val localVM = viewModel<DownloadedViewModel>()
    AnimatedContent(targetState = localVM.selection.isNotEmpty(), label = "") { isSelectionMode ->
        if(!isSelectionMode){
            DownloadedNormalTopBar()
        }else{
            BackHandler {
                localVM.onSelectExit()
            }
            SelectionTopAppBar(selectionItemsCount = localVM.selection.size, onExit = {
                localVM.onSelectExit()
            }, onSelectAll = {
                localVM.onSelectAll()
            }, onSelectInvert = {
                localVM.onSelectInvert()
            })
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DownloadedNormalTopBar(){
    var isSearch by remember {
        mutableStateOf(false)
    }
    val nav = LocalNavController.current

    val localVM = viewModel<DownloadedViewModel>()
    val keyword = localVM.keyword.collectAsState()
    TopAppBar(
        title = {
            if (!isSearch) {
                Text(stringResource(com.heyanle.easy_i18n.R.string.local_download))
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
                            text = stringResource(id = com.heyanle.easy_i18n.R.string.please_input_keyword_to_search)
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
                    stringResource(id = com.heyanle.easy_i18n.R.string.back)
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
                    stringResource(id = com.heyanle.easy_i18n.R.string.search)
                )
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DownloadedItem(
    localCartoon: LocalCartoon,
    vm: DownloadedViewModel,
    onClick: (LocalCartoon) -> Unit,
    onLongPress: ((LocalCartoon) -> Unit)? = null,
) {
    val isSelect = vm.selection.getOrElse(localCartoon) { false }

    val num = remember(localCartoon) {
        var res = 0
        localCartoon.playLines.forEach { res += it.list.size }
        return@remember res
    }
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
                    onClick(localCartoon)
                },
                onLongClick = {
                    onLongPress?.invoke(localCartoon)
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
            image = localCartoon.cartoonCover,
            errorRes = R.drawable.placeholder,
            crossFade = false,
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
                text = stringResource(id = com.heyanle.easy_i18n.R.string.video_num_x, num),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}