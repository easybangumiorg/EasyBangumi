package com.heyanle.easybangumi4.ui.source_manage.extension

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Javascript
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.EXTENSION_PUSH
import com.heyanle.easybangumi4.EXTENSION_PUSH_V2
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.plugin.extension.ExtensionInfo
import com.heyanle.easybangumi4.plugin.extension.remote.ExtensionRemoteLocalInfo
import com.heyanle.easybangumi4.ui.common.EasyDeleteDialog
import com.heyanle.easybangumi4.ui.common.FastScrollToTopFab
import com.heyanle.easybangumi4.ui.common.LoadingPage
import com.heyanle.easybangumi4.ui.common.OkImage

/**
 * Created by heyanle on 2025/8/16
 * https://github.com/heyanLE
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtensionV2TopAppBar(behavior: TopAppBarScrollBehavior) {
    val nav = LocalNavController.current
    val vm = viewModel<ExtensionV2ViewModel>()
    val state = vm.stateFlow.collectAsState()
    val sta = state.value
    val isSearch = sta.searchKey != null
    val focusRequester = remember {
        FocusRequester()
    }
    when(sta.dialog) {
        is ExtensionV2ViewModel.Dialog.DeleteFile -> {
            val info = sta.dialog.info.label
            EasyDeleteDialog(
                show = true,
                message = {
                    Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.delete_confirmation) + " " + info)
                },
                onDelete = {
                    vm.onDelete(sta.dialog.info)
                    vm.dismissDialog()
                }
            ) {
                vm.dismissDialog()
            }
        }
        is ExtensionV2ViewModel.Dialog.ReadyToDownload -> {
            AlertDialog(
                text = {
                    Column {
                        Text("key: ${sta.dialog.remoteInfo.key}")
                        Text("名称: ${sta.dialog.remoteInfo.label}")
                        Text("版本: ${sta.dialog.remoteInfo.versionName}")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        vm.dismissDialog()
                    }) {
                        Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.cancel))
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        vm.onDownload(sta.dialog.remoteInfo)
                    }) {
                        Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.start_download))
                    }
                },
                onDismissRequest = {
                    vm.dismissDialog()
                }
            )
        }
        is ExtensionV2ViewModel.Dialog.Downloading -> {
            AlertDialog(
                text = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OkImage(modifier = Modifier.size(32.dp), image = Uri.parse("file:///android_asset/loading_ryo.gif"), isGif = true,contentDescription = "loading")
                        Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.downloading) + sta.dialog.remoteInfo.label)
                    }
                },
                confirmButton = {},
                onDismissRequest = {

                }
            )
        }
        else -> {}
    }
    BackHandler(isSearch) {
        vm.onSearchChange(null)
    }
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = {
                if (isSearch) {
                    vm.onSearchChange(null)
                } else {
                    nav.popBackStack()
                }
            }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack, stringResource(id = R.string.close)
                )
            }
        },
        title = {
            if (isSearch) {
                DisposableEffect(key1 = Unit){
                    kotlin.runCatching {
                        focusRequester.requestFocus()
                    }
                    onDispose {
                        kotlin.runCatching {
                            focusRequester.freeFocus()
                        }
                    }
                }
                TextField(keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {}),
                    maxLines = 1,
                    modifier = Modifier.focusRequester(focusRequester),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                    ),
                    value = sta.searchKey ?: "",
                    onValueChange = {
                        vm.onSearchChange(it)
                    },
                    placeholder = {
                        Text(
                            style = MaterialTheme.typography.titleLarge,
                            text = stringResource(id = R.string.please_input_keyword_to_search)
                        )
                    })
            } else {
                Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.manage))
            }

        },
        actions = {
            if (!isSearch) {
                IconButton(onClick = {
                    nav.navigate(EXTENSION_PUSH_V2)
                }) {
                    Icon(
                        imageVector = Icons.Filled.Add, stringResource(id = R.string.extension_push)
                    )
                }


                IconButton(onClick = {
                    vm.onSearchChange("")
                }) {
                    Icon(
                        imageVector = Icons.Filled.Search, stringResource(id = R.string.search)
                    )
                }
            } else {
                IconButton(onClick = {
                    vm.onSearchChange(null)
                }) {
                    Icon(
                        imageVector = Icons.Filled.Clear, stringResource(id = R.string.clear)
                    )
                }
            }

        },
        scrollBehavior = behavior
    )
}

@Composable
fun ExtensionV2() {
    val vm = viewModel<ExtensionV2ViewModel>()
    val state = vm.stateFlow.collectAsState()
    val listState = rememberLazyListState()
    val sta = state.value
    val haptic = LocalHapticFeedback.current
    val ctx = LocalContext.current
    if (sta.isLoading) {
        LoadingPage(modifier = Modifier.fillMaxSize())
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(0.dp, 0.dp, 0.dp, 60.dp),
            state = listState
        ) {
            if (sta.isRemoteLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OkImage(modifier = Modifier.size(32.dp), image = Uri.parse("file:///android_asset/loading_ryo.gif"), isGif = true,contentDescription = "loading")
                        Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.extension_repository_loading))
                    }
                }
            }
            items(sta.showList) {
                ExtensionInfoV2Item(it,
                    onClick = {
                        (ctx as? Activity)?.let { act ->
                            vm.onItemClick(it, act)
                        }

                    }, onLongPress = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        vm.onItemLongPress(it)
                    })
            }
        }

        FastScrollToTopFab(listState = listState)
    }
}



@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExtensionInfoV2Item(
    info: ExtensionRemoteLocalInfo,
    onClick: (ExtensionRemoteLocalInfo) -> Unit,
    onLongPress: ((ExtensionRemoteLocalInfo) -> Unit)?,
) {
    val local = info.localInfo
    val remote = info.remoteInfo
    // only local or hasn't update
    if (local != null && (remote == null || !info.hasUpdate)) {
        ListItem(
            modifier = Modifier.let {
                if (onLongPress == null) {
                    it.clickable {
                        onClick(info)
                    }
                } else {
                    it.combinedClickable(
                        onClick = {
                            onClick(info)
                        },
                        onLongClick = {
                            onLongPress.invoke(info)
                        }
                    )
                }
            },
            headlineContent = {
                Text(text = local.label)
            },
            supportingContent = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    // 版本
                    Text(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .clickable {
                            }
                            .padding(8.dp, 4.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.W900,
                        text = local.versionName,
                        fontSize = 12.sp
                    )
                }
            },
            trailingContent = {
                when (local) {
                    is ExtensionInfo.Installed -> {
                        Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.long_touch_to_delete))
                    }

                    is ExtensionInfo.InstallError -> {
                        Text(text = local.errMsg)
                    }
                }
            },
            leadingContent = {
                OkImage(
                    modifier = Modifier.size(40.dp),
                    image = local.icon,
                    contentDescription = local.label,
                    crossFade = false,
                    errorColor = null,
                    errorRes = null,
                    placeholderRes = null,
                    placeholderColor = null,
                )
            }
        )
    }
    // only remote
    if (remote != null && local == null) {
        ListItem(
            modifier = Modifier.let {
                if (onLongPress == null) {
                    it.clickable {
                        onClick(info)
                    }
                } else {
                    it.combinedClickable(
                        onClick = {
                            onClick(info)
                        },
                        onLongClick = {
                            onLongPress.invoke(info)
                        }
                    )
                }
            },
            headlineContent = {
                Text(text = remote.label)
            },
            supportingContent = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    // 版本
                    Text(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .clickable {
                            }
                            .padding(8.dp, 4.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.W900,
                        text = remote.versionName,
                        fontSize = 12.sp
                    )
                    Text(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .clickable {
                            }
                            .padding(8.dp, 4.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.W900,
                        text = "未下载",
                        fontSize = 12.sp
                    )
                }
            },
            trailingContent = {
                Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.click_install))
            },
            leadingContent = {
                OkImage(
                    modifier = Modifier.size(40.dp),
                    image = Icons.Default.Javascript,
                    contentDescription = remote.label,
                    crossFade = false,
                    errorColor = null,
                    errorRes = null,
                    placeholderRes = null,
                    placeholderColor = null,
                )
            }
        )
    }

    // has update
    if (local != null && remote != null && info.hasUpdate) {
        ListItem(
            modifier = Modifier.let {
                if (onLongPress == null) {
                    it.clickable {
                        onClick(info)
                    }
                } else {
                    it.combinedClickable(
                        onClick = {
                            onClick(info)
                        },
                        onLongClick = {
                            onLongPress.invoke(info)
                        }
                    )
                }
            },
            headlineContent = {
                Text(text = local.label)
            },
            supportingContent = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    // 版本
                    Text(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .clickable {
                            }
                            .padding(8.dp, 4.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.W900,
                        text = local.versionName,
                        fontSize = 12.sp
                    )
                    Text(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .clickable {
                            }
                            .padding(8.dp, 4.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.W900,
                        text = stringResource(R.string.need_update),
                        fontSize = 12.sp
                    )
                }
            },
            trailingContent = {
                when (local) {
                    is ExtensionInfo.Installed -> {
                        Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.click_to_update))
                    }

                    is ExtensionInfo.InstallError -> {
                        Text(text = local.errMsg)
                    }
                }
            },
            leadingContent = {
                OkImage(
                    modifier = Modifier.size(40.dp),
                    image = local.icon,
                    contentDescription = local.label,
                    crossFade = false,
                    errorColor = null,
                    errorRes = null,
                    placeholderRes = null,
                    placeholderColor = null,
                )
            }
        )
    }



}

