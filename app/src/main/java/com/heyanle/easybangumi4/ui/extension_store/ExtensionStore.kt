package com.heyanle.easybangumi4.ui.extension_store

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.extension.store.ExtensionStoreController
import com.heyanle.easybangumi4.extension.store.ExtensionStoreInfo
import com.heyanle.easybangumi4.ui.common.ErrorPage
import com.heyanle.easybangumi4.ui.common.LoadingPage
import com.heyanle.easybangumi4.ui.common.OkImage
import com.heyanle.easybangumi4.ui.common.moeDialog
import com.heyanle.easybangumi4.utils.openUrl
import com.heyanle.easybangumi4.utils.stringRes

/**
 * Created by heyanlin on 2023/11/20.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtensionStore() {

    val nav = LocalNavController.current
    val behavior = TopAppBarDefaults.pinnedScrollBehavior()

    val vm = viewModel<ExtensionStoreViewModel>()

    val keyWord = vm.searchKey.collectAsState()
    val state = vm.infoFlow.collectAsState()
    val show = vm.currentShow.collectAsState()


    val tabIndex = vm.currentTabIndex.collectAsState()


    val focusRequester = remember {
        FocusRequester()
    }
    
    Surface(
        contentColor = MaterialTheme.colorScheme.onBackground,
        color = MaterialTheme.colorScheme.background) {
        Column {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = {
                        if (keyWord.value == null) {
                            nav.popBackStack()
                        } else {
                            vm.onSearch(null)
                        }

                    }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack, stringResource(id = R.string.close)
                        )
                    }
                },
                title = {
                    LaunchedEffect(key1 = keyWord.value) {
                        if (keyWord.value != null) {
                            focusRequester.requestFocus()
                        }
                    }
                    if (keyWord.value == null) {
                        Text(text = stringResource(id = R.string.source_store))
                    } else {
                        TextField(keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(),
                            maxLines = 1,
                            modifier = Modifier.focusRequester(focusRequester),
                            colors = TextFieldDefaults.textFieldColors(
                                containerColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                            ),
                            value = keyWord.value ?: "",
                            onValueChange = {
                                vm.onSearch(it)
                            },
                            placeholder = {
                                Text(
                                    style = MaterialTheme.typography.titleLarge,
                                    text = stringResource(id = R.string.please_input_keyword_to_search)
                                )
                            })
                    }
                },
                scrollBehavior = behavior,
                actions = {

                    if (keyWord.value != null) {
                        IconButton(onClick = {
                            vm.onSearch("")
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Clear, stringResource(id = R.string.clear)
                            )
                        }
                    }

//                IconButton(onClick = {
//                    vm.dialogCreate()
//                }) {
//                    Icon(Icons.Filled.Add, stringResource(id = R.string.long_touch_to_drag))
//                }
                }
            )

            when (val sta = state.value) {
                is ExtensionStoreController.ExtensionStoreState.Loading -> {
                    LoadingPage(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )
                }

                is ExtensionStoreController.ExtensionStoreState.Info -> {
                    val list = remember {
                        listOf(stringRes(R.string.all_word), stringRes(R.string.filter_with_is_update), stringRes(R.string.download_completely), stringRes(R.string.downloading))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ){

                        Spacer(modifier = Modifier.size(8.dp))
                        repeat(4){
                            FilterChip(
                                selected = it == tabIndex.value,
                                onClick = {
                                    vm.onTabChange(it)
                                },
                                label = { Text(text = list.getOrNull(it)?:"") },
                                colors = FilterChipDefaults.filterChipColors(),
                            )
                        }
                        Spacer(modifier = Modifier.size(8.dp))

                    }
                    Divider()
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ){
                        items(show.value){
                            StoreInfoItem(extensionStoreInfo = it, onClick = {
                                vm.onClick(it)
                            })
                        }
                    }
                }

                is ExtensionStoreController.ExtensionStoreState.Error -> {
                    ErrorPage(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        errorMsg = sta.errorMsg,
                        other = {
                            Text(text = stringResource(id = R.string.need_vpn))
                        },
                        onClick = {
                            vm.refresh()
                        }
                    )
                }

                else -> {}
            }


        }
    }



}


@Composable
fun StoreInfoItem(
    extensionStoreInfo: ExtensionStoreInfo,
    onClick: (ExtensionStoreInfo) -> Unit,
) {

    ListItem(
        modifier = Modifier.clickable {
            onClick(extensionStoreInfo)
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
                    Spacer(modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight())
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
                    Text(text = stringResource(id = R.string.download_completely))
                }
                ExtensionStoreInfo.STATE_DOWNLOADING -> {
                    Box(modifier = Modifier.width(125.dp)){
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
                        enabled = false,
                        onClick = {
                            extensionStoreInfo.errorMsg?.moeDialog()
                        }) {
                        Text(text = if(extensionStoreInfo.errorMsg?.isNotEmpty() == true) extensionStoreInfo.errorMsg else stringResource(
                            id = R.string.download_error
                        ))
                    }
                }
            }
        },
        leadingContent = {
            OkImage(
                modifier = Modifier.size(40.dp),
                image = extensionStoreInfo.remote.iconUrl,
                contentDescription = extensionStoreInfo.remote.label
            )
        }
    )
}
