package com.heyanle.easybangumi4.ui.source_manage.extension

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.extension.ExtensionInfo
import com.heyanle.easybangumi4.extension.store.ExtensionStoreInfo
import com.heyanle.easybangumi4.ui.common.EasyDeleteDialog
import com.heyanle.easybangumi4.ui.common.FastScrollToTopFab
import com.heyanle.easybangumi4.ui.common.LoadingPage
import com.heyanle.easybangumi4.ui.common.OkImage
import com.heyanle.easybangumi4.ui.common.moeDialog
import com.heyanle.easybangumi4.utils.openUrl
import com.heyanle.easybangumi4.utils.stringRes

/**
 * Created by HeYanLe on 2023/2/21 23:33.
 * https://github.com/heyanLE
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtensionTopAppBar(behavior: TopAppBarScrollBehavior) {
    val nav = LocalNavController.current
    val vm = viewModel<ExtensionViewModel>()
    val state = vm.stateFlow.collectAsState()
    val sta = state.value
    val isSearch = sta.searchKey != null
    val focusRequester = remember {
        FocusRequester()
    }
    EasyDeleteDialog(
        show = sta.readyToDeleteFile != null,
        message = {
            Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.delete_confirmation) + " " + sta.readyToDeleteFile?.name)
        },
        onDelete = {
            sta.readyToDeleteFile?.delete()
            vm.dismissDialog()
        }) {
        vm.dismissDialog()
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
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.Transparent,
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
                    vm.onSearchChange("")
                }) {
                    Icon(
                        imageVector = Icons.Filled.Search, stringResource(id = R.string.search)
                    )
                }

                IconButton(onClick = {
                    vm.refresh()
                }) {
                    Icon(
                        imageVector = Icons.Filled.Refresh, stringResource(id = R.string.refresh)
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
fun Extension() {
    val vm = viewModel<ExtensionViewModel>()
    val state = vm.stateFlow.collectAsState()
    val listState = rememberLazyListState()
    val sta = state.value
    val haptic = LocalHapticFeedback.current
    val ctx = LocalContext.current
    if (sta.isLoading) {
        LoadingPage(modifier = Modifier.fillMaxSize())
    } else {
        Column {
            val list = remember {
                listOf(
                    stringRes(R.string.all_word),
                    stringRes(R.string.filter_with_is_update),
                    stringRes(R.string.download_completely),
                    stringRes(R.string.downloading)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState(), enabled = true),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Spacer(modifier = Modifier.size(8.dp))
                repeat(4) {
                    FilterChip(
                        selected = it == sta.currentTab,
                        onClick = {
                            vm.onTabClick(it)
                        },
                        label = { Text(text = list.getOrNull(it) ?: "") },
                        colors = FilterChipDefaults.filterChipColors(),
                    )
                }
                Spacer(modifier = Modifier.size(8.dp))
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(0.dp, 0.dp, 0.dp, 60.dp),
                state = listState
            ) {
                items(sta.showList) {
                    ExtensionInfoItem(item = it,
                        onClick = {
                            (ctx as? Activity)?.let { act ->
                                vm.onItemClick(it, act)
                            }

                            when (it) {
                                is ExtensionViewModel.ExtensionItem.ExtensionInfo -> {

                                }

                                is ExtensionViewModel.ExtensionItem.StoreInfo -> {

                                }

                                is ExtensionViewModel.ExtensionItem.StoreExtensionInfo -> {

                                }
                            }
                        }, onLongPress = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            vm.onItemLongPress(it)
                        })
                }
            }
        }

        FastScrollToTopFab(listState = listState)
    }
}


@Composable
fun ExtensionInfoItem(
    item: ExtensionViewModel.ExtensionItem,
    onClick: (ExtensionViewModel.ExtensionItem) -> Unit,
    onLongPress: ((ExtensionViewModel.ExtensionItem) -> Unit)?,
) {
    when (item) {
        is ExtensionViewModel.ExtensionItem.ExtensionInfo -> {
            ExtensionInfoItem(item = item, onClick = onClick, onLongPress = onLongPress)
        }

        is ExtensionViewModel.ExtensionItem.StoreExtensionInfo -> {
            StoreExtensionInfoItem(item = item, onClick = onClick, onLongPress = onLongPress)
        }

        is ExtensionViewModel.ExtensionItem.StoreInfo -> {
            StoreInfoItem(item = item, onClick = onClick, onLongPress = onLongPress)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StoreInfoItem(
    item: ExtensionViewModel.ExtensionItem.StoreInfo,
    onClick: (ExtensionViewModel.ExtensionItem) -> Unit,
    onLongPress: ((ExtensionViewModel.ExtensionItem) -> Unit)?,
) {
    val extensionStoreInfo = item.info

    ListItem(
        modifier = Modifier.let {
            if (onLongPress == null) {
                it.clickable {
                    onClick(item)
                }
            } else {
                it.combinedClickable(
                    onClick = {
                        onClick(item)
                    },
                    onLongClick = {
                        onLongPress.invoke(item)
                    }
                )
            }
        },
        headlineContent = {
            Text(text = extensionStoreInfo.remote.label)
        },
        supportingContent = {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
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
                    text = extensionStoreInfo.remote.versionName,
                    fontSize = 12.sp
                )

                // 作者
                Row(modifier = Modifier
                    .height(IntrinsicSize.Min)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .clickable {
                        extensionStoreInfo.remote.gitUrl.openUrl()
                    }
                    .padding(8.dp, 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Person,
                        modifier = Modifier
                            .size(16.dp),
                        contentDescription = stringResource(id = R.string.author)
                    )
                    Spacer(
                        modifier = Modifier
                            .width(2.dp)
                            .fillMaxHeight()
                    )
                    Text(
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.W900,
                        text = extensionStoreInfo.remote.author,
                        fontSize = 12.sp
                    )
                }


                // github
                OkImage(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .clickable {
                            extensionStoreInfo.remote.gitUrl.openUrl()
                        }
                        .padding(8.dp, 4.dp)
                        .size(16.dp),
                    image = com.heyanle.easybangumi4.R.drawable.github,
                    contentDescription = stringResource(id = R.string.github),
                    crossFade = false,
                    errorColor = null,
                    errorRes = null,
                    placeholderRes = null,
                    placeholderColor = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 介绍
                Text(
                    modifier = Modifier
                        .widthIn(0.dp, 100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .clickable {
                            extensionStoreInfo.remote.releaseDesc.moeDialog()
                        }
                        .padding(8.dp, 4.dp),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.W900,
                    text = extensionStoreInfo.remote.releaseDesc,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    fontSize = 12.sp
                )
            }
        },
        trailingContent = {
            when (extensionStoreInfo.state) {
                ExtensionStoreInfo.STATE_INSTALLED -> {
                    Text(text = stringResource(id = R.string.click_install))
                }

                ExtensionStoreInfo.STATE_DOWNLOADING -> {

                    val info = remember(extensionStoreInfo) {
                        extensionStoreInfo.downloadInfo
                    }
                    if (info?.process?.value == -1f || info == null) {
                        CircularProgressIndicator()
                    } else {
                        CircularProgressIndicator(info.process.value)
                    }

                }

                ExtensionStoreInfo.STATE_NEED_UPDATE -> {
                    Text(
                        fontSize = 13.sp,
                        text = stringResource(id = R.string.click_to_update),
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(8.dp, 4.dp)
                    )
                }

                ExtensionStoreInfo.STATE_ERROR -> {
                    TextButton(
                        enabled = false,
                        onClick = {
                            extensionStoreInfo.errorMsg?.moeDialog()
                        }) {
                        Text(
                            modifier = Modifier.width(32.dp),
                            text = if (extensionStoreInfo.errorMsg?.isNotEmpty() == true) extensionStoreInfo.errorMsg else stringResource(
                                id = R.string.download_error
                            ),
                            maxLines = 2
                        )
                    }
                }
            }
        },
        leadingContent = {
            OkImage(
                modifier = Modifier.size(40.dp),
                image = extensionStoreInfo.remote.iconUrl,
                contentDescription = extensionStoreInfo.remote.label,
                crossFade = false,
                errorColor = null,
                errorRes = null,
                placeholderRes = null,
                placeholderColor = null,
            )
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StoreExtensionInfoItem(
    item: ExtensionViewModel.ExtensionItem.StoreExtensionInfo,
    onClick: (ExtensionViewModel.ExtensionItem) -> Unit,
    onLongPress: ((ExtensionViewModel.ExtensionItem) -> Unit)?,
) {
    val extensionStoreInfo = item.info
    ListItem(
        modifier = Modifier.let {
            if (onLongPress == null) {
                it.clickable {
                    onClick(item)
                }
            } else {
                it.combinedClickable(
                    onClick = {
                        onClick(item)
                    },
                    onLongClick = {
                        onLongPress.invoke(item)
                    }
                )
            }
        },
        headlineContent = {
            Text(text = extensionStoreInfo.remote.label)
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
                    text = extensionStoreInfo.remote.versionName,
                    fontSize = 12.sp
                )

                // 作者
                Row(modifier = Modifier
                    .height(IntrinsicSize.Min)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .clickable {
                        extensionStoreInfo.remote.gitUrl.openUrl()
                    }
                    .padding(8.dp, 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Person,
                        modifier = Modifier
                            .size(16.dp),
                        contentDescription = stringResource(id = R.string.author)
                    )
                    Spacer(
                        modifier = Modifier
                            .width(2.dp)
                            .fillMaxHeight()
                    )
                    Text(
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.W900,
                        text = extensionStoreInfo.remote.author,
                        fontSize = 12.sp
                    )
                }


                // github
                OkImage(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .clickable {
                            extensionStoreInfo.remote.gitUrl.openUrl()
                        }
                        .padding(8.dp, 4.dp)
                        .size(16.dp),
                    image = com.heyanle.easybangumi4.R.drawable.github,
                    contentDescription = stringResource(id = R.string.github),
                    crossFade = false,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 介绍
                Text(
                    modifier = Modifier
                        .widthIn(0.dp, 100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .clickable {
                            extensionStoreInfo.remote.releaseDesc.moeDialog()
                        }
                        .padding(8.dp, 4.dp),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.W900,
                    text = extensionStoreInfo.remote.releaseDesc,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    fontSize = 12.sp
                )
            }
        },
        trailingContent = {
            when (extensionStoreInfo.state) {
                ExtensionStoreInfo.STATE_INSTALLED -> {
                    if (item.extensionInfo is ExtensionInfo.Installed) {
                        Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.detailed))
                    } else if (item.extensionInfo is ExtensionInfo.InstallError) {
                        Text(text = item.extensionInfo.errMsg)
                    }
                }

                ExtensionStoreInfo.STATE_DOWNLOADING -> {
                    Box(modifier = Modifier.width(125.dp)) {
                        val info = remember(extensionStoreInfo) {
                            extensionStoreInfo.downloadInfo
                        }
                        if (info?.process?.value == -1f || info == null) {
                            LinearProgressIndicator()
                        } else {
                            LinearProgressIndicator(info.process.value)
                        }
                    }

                }

                ExtensionStoreInfo.STATE_NEED_UPDATE -> {
                    Text(
                        fontSize = 13.sp,
                        text = stringResource(id = R.string.click_to_update),
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(8.dp, 4.dp)
                    )
                }

                ExtensionStoreInfo.STATE_ERROR -> {
                    TextButton(
                        onClick = {
                            extensionStoreInfo.errorMsg?.moeDialog()
                        }) {
                        Text(
                            text = if (extensionStoreInfo.errorMsg?.isNotEmpty() == true) extensionStoreInfo.errorMsg else stringResource(
                                id = R.string.download_error
                            )
                        )
                    }
                }
            }
        },
        leadingContent = {
            if (item.extensionInfo.icon == null) {
                OkImage(
                    modifier = Modifier.size(40.dp),
                    image = extensionStoreInfo.remote.iconUrl,
                    contentDescription = extensionStoreInfo.remote.label,
                    crossFade = false,
                    errorColor = null,
                    errorRes = null,
                    placeholderRes = null,
                    placeholderColor = null,
                )
            } else {
                OkImage(
                    modifier = Modifier.size(40.dp),
                    image = item.extensionInfo.icon,
                    contentDescription = extensionStoreInfo.remote.label,
                    crossFade = false,
                    errorColor = null,
                    errorRes = null,
                    placeholderRes = null,
                    placeholderColor = null,
                )
            }

        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExtensionInfoItem(
    item: ExtensionViewModel.ExtensionItem.ExtensionInfo,
    onClick: (ExtensionViewModel.ExtensionItem) -> Unit,
    onLongPress: ((ExtensionViewModel.ExtensionItem) -> Unit)?,
) {
    val extension = item.extensionInfo
    ListItem(
        modifier = Modifier.let {
            if (onLongPress == null) {
                it.clickable {
                    onClick(item)
                }
            } else {
                it.combinedClickable(
                    onClick = {
                        onClick(item)
                    },
                    onLongClick = {
                        onLongPress.invoke(item)
                    }
                )
            }
        },
        headlineContent = {
            Text(text = extension.label)
        },
        supportingContent = {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                item {
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
                        text = extension.versionName,
                        fontSize = 12.sp
                    )
                }
                item {
                    // 加载方式
                    Text(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .clickable {
                            }
                            .padding(8.dp, 4.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.W900,
                        text = when(extension.loadType){
                            ExtensionInfo.TYPE_APP -> {stringResource(id = com.heyanle.easy_i18n.R.string.load_type_installed)}
                            ExtensionInfo.TYPE_FILE -> {stringResource(id = com.heyanle.easy_i18n.R.string.load_type_file)}
                            else -> {stringResource(id = com.heyanle.easy_i18n.R.string.load_type_installed)}
                        },
                        fontSize = 12.sp
                    )
                }
//                item {
//                    Text(
//                        modifier = Modifier
//                            .clip(CircleShape)
//                            .background(MaterialTheme.colorScheme.secondaryContainer)
//                            .clickable {
//                            }
//                            .padding(8.dp, 4.dp),
//                        color = MaterialTheme.colorScheme.onSecondaryContainer,
//                        fontWeight = FontWeight.W900,
//                        text = when(extension.loadType){
//                            Extension.TYPE_APP -> {extension.pkgName}
//                            Extension.TYPE_FILE -> {extension.fileName}
//                            else -> {stringResource(id = com.heyanle.easy_i18n.R.string.load_type_installed)}
//                        },
//                        fontSize = 12.sp
//                    )
//                }
            }
        },
        trailingContent = {
            when (extension) {
                is ExtensionInfo.Installed -> {
                    if (extension.loadType == ExtensionInfo.TYPE_APP) {
                        Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.detailed))
                    }else{
                        Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.long_touch_to_delete))
                    }
                }

                is ExtensionInfo.InstallError -> {
                    Text(text = extension.errMsg)
                }
            }
        },
        leadingContent = {
            OkImage(
                modifier = Modifier.size(40.dp),
                image = extension.icon,
                contentDescription = extension.label,
                crossFade = false,
                errorColor = null,
                errorRes = null,
                placeholderRes = null,
                placeholderColor = null,
            )
        }
    )
}

@Composable
fun ExtensionItem(
    extensionInfo: ExtensionInfo,
    onClick: (ExtensionInfo) -> Unit,
    onAction: (ExtensionInfo) -> Unit,
) {

    ListItem(
        modifier = Modifier.clickable {
            onClick(extensionInfo)
        },
        headlineContent = {
            Text(text = extensionInfo.label)
        },
        supportingContent = {
            Text(
                text = extensionInfo.versionName,
            )
        },
        trailingContent = {
            when (extensionInfo) {
                is ExtensionInfo.Installed -> {
                    TextButton(onClick = {
                        onAction(extensionInfo)
                    }) {
                        Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.detailed))
                    }
                }

                is ExtensionInfo.InstallError -> {
                    TextButton(
                        enabled = false,
                        onClick = {

                        }) {
                        Text(text = extensionInfo.errMsg)
                    }
                }
            }
        },
        leadingContent = {
            OkImage(
                modifier = Modifier.size(40.dp),
                image = extensionInfo.icon,
                contentDescription = extensionInfo.label,
                crossFade = false,
                errorColor = null,
                errorRes = null,
                placeholderRes = null,
                placeholderColor = null,
            )
        }
    )

}