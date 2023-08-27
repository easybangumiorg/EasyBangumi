package com.heyanle.easybangumi4.ui.tags

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.ui.common.EasyDeleteDialog
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.utils.stringRes
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

/**
 * Created by HeYanLe on 2023/8/6 16:48.
 * https://github.com/heyanLE
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CartoonTag() {
    val nav = LocalNavController.current
    val behavior = TopAppBarDefaults.pinnedScrollBehavior()

    val vm = viewModel<CartoonTagViewModel>()

    val focusRequest = remember {
        FocusRequester()
    }


    val state = rememberReorderableLazyListState(onMove = { from, to ->
        vm.move(from.index, to.index)
    }, onDragEnd = { from, to ->
        vm.onDragEnd()
    })

    Column {

        TopAppBar(
            navigationIcon = {
                IconButton(onClick = {
                    nav.popBackStack()
                }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack, stringResource(id = R.string.close)
                    )
                }
            },
            title = { Text(text = stringResource(id = R.string.tag_manage)) },
            scrollBehavior = behavior,
            actions = {
//                IconButton(onClick = {
//                    vm.dialogCreate()
//                }) {
//                    Icon(Icons.Filled.Add, stringResource(id = R.string.long_touch_to_drag))
//                }
            }
        )

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
                    .nestedScroll(behavior.nestedScrollConnection)
            ) {
                items(vm.tags, key = { it.id }) { tag ->
                    ReorderableItem(
                        reorderableState = state,
                        key = tag.id,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {

                        Row(
                            modifier = Modifier
                                .padding(8.dp, 4.dp)
                                .clip(RoundedCornerShape(18.dp))
//                                .border(
//                                    1.dp,
//                                    MaterialTheme.colorScheme.background,
//                                    RoundedCornerShape(4.dp)
//                                )
                                .clickable {
                                    vm.dialogRename(tag)
                                }

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
                                    tint = if(it) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSecondary
                                )
                            }
                            Text(
                                text = tag.label,
                                color = if(it) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSecondary
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(onClick = {
                                vm.dialogDelete(tag)
                            }) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = stringResource(
                                        id = R.string.delete_tag
                                    ),
                                    tint = if(it) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSecondary
                                )
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
                        Text(text = stringResource(id = R.string.new_tag))
                    },
                    icon = {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = stringResource(id = R.string.new_tag)
                        )
                    },
                    onClick = {
                        vm.dialogCreate()
                    }
                )
//                FloatingActionButton(
//                    onClick = {
//                        vm.dialogCreate()
//                    },
//                    modifier = Modifier
//                        .padding(20.dp),
////                    containerColor = MaterialTheme.colorScheme.secondary,
////                    elevation = FloatingActionButtonDefaults.elevation(16.dp)
//                ) {
//                    Icon(Icons.Filled.Add, contentDescription = stringResource(id = R.string.new_tag))
//                }
            }
        }

    }

    when (val dialog = vm.dialog) {
        is CartoonTagViewModel.Dialog.Create -> {
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
                    Text(text = stringResource(id = R.string.new_tag))
                },
                text = {

                    OutlinedTextField(
                        modifier = Modifier.focusRequester(focusRequest),
                        value = label,
                        onValueChange = { label = it })
                }
            )
        }

        is CartoonTagViewModel.Dialog.Rename -> {
            var label by remember {
                mutableStateOf(dialog.renameTag.label)
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
                        } else {
                            vm.onRename(dialog.renameTag, label)
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
                    Text(text = stringResource(id = R.string.rename_tag))
                },
                text = {
                    OutlinedTextField(
                        modifier = Modifier.focusRequester(focusRequest),
                        value = label,
                        onValueChange = { label = it })
                }
            )
        }

        is CartoonTagViewModel.Dialog.Delete -> {
            EasyDeleteDialog(show = true, onDelete = {
                vm.onDelete(dialog.deleteTag)
            }) {
                vm.dialogDismiss()
            }
        }

        else -> {

        }
    }

}