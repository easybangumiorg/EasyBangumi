package com.heyanle.easybangumi4.ui.extension_push

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.plugin.extension.remote.ExtensionRemoteController
import com.heyanle.easybangumi4.ui.common.EasyDeleteDialog
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.utils.stringRes
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

/**
 * Created by heyanle on 2025/8/16
 * https://github.com/heyanLE
 */
@Composable
fun ExtensionRepository() {

    val nav = LocalNavController.current

    val vm = viewModel<ExtensionRepositoryViewModel>()

    val focusRequest = remember {
        FocusRequester()
    }


    val state = rememberReorderableLazyListState(onMove = { from, to ->
        vm.move(from.index, to.index)
    }, onDragEnd = { from, to ->
        vm.onDragEnd()
    })

    Column {

        Surface(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ) {
            LazyColumn(
                state = state.listState,
                modifier = Modifier
                    .fillMaxSize()
                    .reorderable(state)
            ) {
                items(vm.repository, ) { tag ->
                    ReorderableItem(
                        reorderableState = state,
                        key = tag,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {

                        Row(
                            modifier = Modifier
                                .padding(8.dp, 4.dp)
                                .clip(RoundedCornerShape(18.dp))

                                .run {
                                    if (it) {
                                        background(
                                            MaterialTheme.colorScheme.secondaryContainer
                                        )
                                    } else {
                                        background(MaterialTheme.colorScheme.secondary)
                                    }
                                }
                                .padding(8.dp, 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(
                                modifier = Modifier
                                    .detectReorder(state),
                                onClick = { }) {
                                Icon(
                                    Icons.Filled.DragHandle,
                                    contentDescription = stringResource(
                                        id = R.string.tag_manage
                                    ),
                                    tint = if (it) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSecondary
                                )
                            }
                            Text(
                                modifier = Modifier.weight(1f),
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                                text = tag.url,
                                color = if (it) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSecondary
                            )

                            if (tag != ExtensionRemoteController.officeReposotory) {
                                IconButton(onClick = {
                                    vm.dialogDelete(tag)
                                }) {
                                    Icon(
                                        Icons.Filled.Close,
                                        contentDescription = stringResource(
                                            id = R.string.delete_tag
                                        ),
                                        tint = if (it) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.fillMaxSize()) {
                ExtendedFloatingActionButton(
                    modifier = Modifier
                        .padding(16.dp, 40.dp),
                    text = {
                        Text(text = stringResource(id = R.string.new_js_repo))
                    },
                    icon = {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = stringResource(id = R.string.new_js_repo)
                        )
                    },
                    onClick = {
                        vm.dialogCreate()
                    }
                )
            }
        }

    }

    when (val dialog = vm.dialog) {
        is ExtensionRepositoryViewModel.Dialog.Create -> {
            var label by remember {
                mutableStateOf("")
            }
            LaunchedEffect(key1 = Unit) {
                runCatching {
                    focusRequest.requestFocus()
                }.onFailure {
                    it.printStackTrace()
                }
            }
            AlertDialog(
                onDismissRequest = { vm.dialogDismiss() },
                confirmButton = {
                    TextButton(onClick = {
                        if (label.isEmpty()) {
                            stringRes(R.string.is_empty).moeSnackBar()
                        } else if (vm.repository.any { it.url == label }) {
                            "重复添加".moeSnackBar()
                        } else {
                            vm.onCreate(label)
                            vm.dialogDismiss()
                        }

                    }) {
                        Text(text = stringResource(id = R.string.confirm))
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        vm.dialogDismiss()

                    }) {
                        Text(text = stringResource(id = R.string.cancel))
                    }
                },
                title = {
                    Text(text = stringResource(id = R.string.new_js_repo))
                },
                text = {
                    Column {
                        Text(text = "需要添加 V2 api 仓库")
                        OutlinedTextField(
                            modifier = Modifier.focusRequester(focusRequest),
                            value = label,
                            onValueChange = { label = it })
                    }

                }
            )
        }
        is ExtensionRepositoryViewModel.Dialog.Delete -> {
            EasyDeleteDialog(show = true, onDelete = {
                vm.onDelete(dialog.repository)
            }) {
                vm.dialogDismiss()
            }
        }
        else -> {

        }
    }

}