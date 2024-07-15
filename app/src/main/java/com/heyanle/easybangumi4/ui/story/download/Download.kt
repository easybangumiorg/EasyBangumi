package com.heyanle.easybangumi4.ui.story.download

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.R
import com.heyanle.easybangumi4.cartoon.entity.CartoonDownloadInfo
import com.heyanle.easybangumi4.ui.common.EasyDeleteDialog
import com.heyanle.easybangumi4.ui.common.FastScrollToTopFab
import com.heyanle.easybangumi4.ui.common.LoadingPage
import com.heyanle.easybangumi4.ui.common.OkImage
import com.heyanle.easybangumi4.ui.common.SelectionTopAppBar

/**
 * Created by heyanle on 2024/7/14.
 * https://github.com/heyanLE
 */


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadTopAppBar(){
    val nav = LocalNavController.current
    val vm = viewModel<DownloadViewModel>()
    val state = vm.state.collectAsState()
    val sta = state.value
    if (sta.selection.isEmpty())
        TopAppBar(
            title = {
                Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.local_download))
            },
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
        )
    else {
        SelectionTopAppBar(
            selectionItemsCount = sta.selection.size,
            onExit = {
                vm.clearSelection()
            },
            actions = {
                IconButton(onClick = { vm.showDeleteDialog() }) {
                    Icon(
                        Icons.Filled.Delete, contentDescription = stringResource(
                            id = com.heyanle.easy_i18n.R.string.delete
                        )
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun Download(
) {
    val nav = LocalNavController.current
    val vm = viewModel<DownloadViewModel>()
    val state = vm.state.collectAsState()
    val sta = state.value
    val haptic = LocalHapticFeedback.current
    val listState = rememberLazyListState()
    Box {
        if (sta.loading) {
            LoadingPage(
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(4.dp, 4.dp, 4.dp, 86.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                state = listState
            ) {
                items(sta.downloadInfo) {
                    DownloadItem(
                        downloadItem = it,
                        isSelect = sta.selection.contains(it),
                        onClick = {
                            if (sta.selection.isEmpty()) {
                                vm.clickDownloadInfo(it)
                            } else {
                                vm.selectDownloadInfo(it)
                            }

                        },
                        onLongPress = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            vm.onSelectionLongPress(it)
                        }
                    )
                }
            }

            FastScrollToTopFab(listState = listState)
        }
    }
    val dialog = sta.dialog
    when(dialog){
        is DownloadViewModel.Dialog.DeleteSelection -> {
            EasyDeleteDialog(show = true, onDelete = {
                vm.deleteDownload(dialog.selection)
                vm.dismissDialog()
            }) {
                vm.dismissDialog()
            }
        }
        else -> {

        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DownloadItem(
    downloadItem: CartoonDownloadInfo,
    isSelect: Boolean,
    onClick: (CartoonDownloadInfo) -> Unit,
    onLongPress: ((CartoonDownloadInfo) -> Unit)? = null,
) {
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
            image = downloadItem.req.localItem.cartoonCover.coverUrl,
            crossFade = false,
            contentDescription = downloadItem.req.localItem.title,
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
                text = (downloadItem.req.localItem.title),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isSelect) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
            )
            Text(
                modifier = Modifier,
                text = "${downloadItem.req.toEpisode}-${downloadItem.req.toEpisodeTitle}",
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

                if (downloadItem.runtime == null){
                    Text(
                        stringResource(com.heyanle.easy_i18n.R.string.download_stop_click_to_restart),
                        maxLines = 1,
                        color = if (isSelect) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                    )
                }else {
                    val info = downloadItem.runtime.getDownloadInfo()
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

            val info = downloadItem.runtime?.getDownloadInfo()

            if (info != null) {
                if (info.process.value == -1f) {
                    LinearProgressIndicator()
                } else {
                    LinearProgressIndicator(info.process.value)
                }
            } else {
                LinearProgressIndicator(0f)
            }

        }
    }
}