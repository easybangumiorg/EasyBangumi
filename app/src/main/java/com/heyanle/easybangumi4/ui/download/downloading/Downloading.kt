package com.heyanle.easybangumi4.ui.download.downloading

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Help
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.R
import com.heyanle.easybangumi4.cartoon_download.CartoonDownloadController
import com.heyanle.easybangumi4.cartoon_download.entity.DownloadItem
import com.heyanle.easybangumi4.ui.common.EasyDeleteDialog
import com.heyanle.easybangumi4.ui.common.FastScrollToTopFab
import com.heyanle.easybangumi4.ui.common.OkImage
import com.heyanle.easybangumi4.ui.common.SelectionTopAppBar
import com.heyanle.injekt.core.Injekt

/**
 * Created by heyanlin on 2023/11/2.
 */
@Composable
fun Downloading(){
    val vm = viewModel<DownloadingViewModel>()
    val list by vm.downloadingFlow.collectAsState()
    val listState = rememberLazyListState()
    Box(modifier = Modifier.fillMaxSize()){
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(4.dp, 4.dp, 4.dp, 86.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(list) {
                DownloadItem(it, vm, onClick = {
                    if (vm.selection.isEmpty()) {
                        vm.click(it)
                    } else {
                        if(vm.selection.containsKey(it)){
                            vm.selection.remove(it)
                        }else{
                            vm.selection.put(it, true)
                        }
                    }
                }, onLongPress = {
                    vm.selection.put(it, true)
                })
            }
        }

        if(vm.selection.isNotEmpty()){
            Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.fillMaxSize()) {
                val up = remember { derivedStateOf { listState.firstVisibleItemIndex > 10 } }
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
                        vm.removeDownloadItem.value = vm.selection.keys.toList()
                        vm.selection.clear()
                    }
                )
            }
        }

        if(vm.removeDownloadItem.value?.isNotEmpty() == true){
            EasyDeleteDialog(
                message = {
                    Text(stringResource(com.heyanle.easy_i18n.R.string.delete_confirmation_num, vm.removeDownloadItem.value?.size?:0))
                },
                show = vm.removeDownloadItem.value?.isNotEmpty() == true,
                onDelete = {
                    vm.removeDownloadItem.value?.let {
                        vm.remove(it)
                    }
                    vm.removeDownloadItem.value = null
                },
                onDismissRequest = {
                    vm.removeDownloadItem.value = null
                }
            )
        }

        FastScrollToTopFab(listState = listState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadingTopBar(){


    val nav = LocalNavController.current
    val vm = viewModel<DownloadingViewModel>()

    AnimatedContent(targetState = vm.selection.isNotEmpty(), label = "") { isSelectionMode ->
        if(!isSelectionMode){
            TopAppBar(
                title = { Text(stringResource(com.heyanle.easy_i18n.R.string.local_download)) },
                navigationIcon = {
                    IconButton(onClick = {
                        nav.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            stringResource(id = com.heyanle.easy_i18n.R.string.back)
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
                                stringResource(id = com.heyanle.easy_i18n.R.string.delete)
                            )
                        }
                    }else{
                        IconButton(onClick = {
                            val cartoonDownloadController: CartoonDownloadController by Injekt.injectLazy()
                            cartoonDownloadController.showDownloadHelpDialog()
                        }){
                            Icon(
                                imageVector = Icons.Filled.Help,
                                stringResource(id = com.heyanle.easy_i18n.R.string.download)
                            )
                        }
                    }
                }
            )
        }else{
            BackHandler {
                vm.onSelectExit()
            }
            SelectionTopAppBar(selectionItemsCount = vm.selection.size, onExit = {
                vm.onSelectExit()
            }, onSelectAll = {
                vm.onSelectAll()
            }, onSelectInvert = {
                vm.onSelectInvert()
            })
        }
    }



}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DownloadItem(
    downloadItem: DownloadItem,
    downloadViewModel: DownloadingViewModel,
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
            crossFade = false,
            contentDescription = downloadItem.cartoonTitle,
            errorRes = R.drawable.placeholder,
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
                text = "${downloadItem.episode.label}-${downloadItem.playLine.label}",
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
                        stringResource(com.heyanle.easy_i18n.R.string.download_error),
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