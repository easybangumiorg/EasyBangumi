package com.heyanle.easybangumi4.ui.extension_push

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.plugin.extension.push.ExtensionPushTask
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.utils.stringRes

/**
 * Created by HeYanLe on 2024/10/27 14:14.
 * https://github.com/heyanLE
 */

// 先放弃拓展性，直接写
sealed class ExtensionPushType(
    val identify: String,
    val label: @Composable ()-> String,
    val desc: @Composable ()-> String,
) {

    data object FromFileUrl: ExtensionPushType(
        identify = ExtensionPushTask.EXTENSION_PUSH_TASK_IDENTIFY_FROM_FILE_URL,
        label = @Composable { stringResource(R.string.js_file_url) },
        desc = @Composable { stringResource(R.string.js_file_url_desc) }
    )

    data object FromCode: ExtensionPushType(
        identify = ExtensionPushTask.EXTENSION_PUSH_TASK_IDENTIFY_FROM_CODE,
        label = { stringResource(R.string.js_code) },
        desc = { stringResource(R.string.js_code_desc) }
    )

    data object FromRepo: ExtensionPushType(
        identify = ExtensionPushTask.EXTENSION_PUSH_TASK_IDENTIFY_FROM_FILE_REPO,
        label = { stringResource(R.string.js_repo) },
        desc = { stringResource(R.string.js_repo_desc) }
    )

}

val extensionPushTypeList = listOf(
    ExtensionPushType.FromFileUrl,
    ExtensionPushType.FromCode,
    ExtensionPushType.FromRepo
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExtensionPush() {

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val nav = LocalNavController.current
    val vm = viewModel<ExtensionPushViewModel>()
    val state = vm.state.collectAsState()
    val sta = state.value
    val dialog = sta.dialog
    val fq = remember { FocusRequester() }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        Column {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.extension_push))
                },
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
                scrollBehavior = scrollBehavior
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
            ) {

                // 选择文件按钮
                item {
                    FilledTonalButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp, 8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        onClick = {
                            vm.chooseJSFile()
                        }
                    ) {
                        Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.extension_push_from_file))
                    }
                }

                item {
                    ListItem(headlineContent = {
                        Text(
                            text = stringResource(id = com.heyanle.easy_i18n.R.string.extension_push_from_input),
                            color = MaterialTheme.colorScheme.primary
                        )
                    })
                }

                item {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),

                        ) {
                        Spacer(modifier = Modifier.padding(4.dp))
                        extensionPushTypeList.forEach {
                            FilterChip(
                                selected = sta.currentType == it,
                                onClick = {
                                    vm.changeType(it)
                                },
                                label = { Text(text = it.label()) },
                                colors = FilterChipDefaults.filterChipColors(),
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(Dp.Unspecified, 300.dp)
                            .padding(16.dp)
                            .focusRequester(fq),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.None),
                        value = sta.text,
                        onValueChange = {
                            vm.changeText(it)
                        },
                        placeholder = {
                            Text(
                                text = sta.currentType.desc() ?: "",
                                maxLines = 1
                            )
                        })
                }

                item { Spacer(Modifier.height(300.dp)) }

            }
        }
    }

    when(sta.dialog) {
        is ExtensionPushViewModel.Dialog.Loading -> {
            AlertDialog(
                onDismissRequest = {
                    //vm.cancelCurrent()
                },
                confirmButton = {
                    TextButton(onClick = {
                        vm.cancelCurrent()
                    }) {
                        Text(text = stringResource(id = R.string.cancel))
                    }
                },
                text = {
                    Text( sta.dialog.msg)
                }
            )
        }
        is ExtensionPushViewModel.Dialog.ErrorOrCompletely -> {
            AlertDialog(
                onDismissRequest = {
                    vm.cleanErrorOrCompletely()
                },
                confirmButton = {
                    TextButton(onClick = {
                        vm.cleanErrorOrCompletely()
                    }) {
                        Text(text = stringResource(id = R.string.confirm))
                    }
                },
                text = {
                    Text( sta.dialog.msg)
                }
            )
        }
        else -> {}
    }


}
