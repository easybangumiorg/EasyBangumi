package org.easybangumi.next.shared.compose.tags

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import org.easybangumi.next.shared.cartoon.displayName
import org.easybangumi.next.shared.foundation.image.AsyncImage
import org.easybangumi.next.shared.foundation.stringRes
import org.easybangumi.next.shared.foundation.view_model.vm
import org.easybangumi.next.shared.resources.Res

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagsManager(
    onBack: () -> Unit,
) {
    val viewModel = vm(::TagsManagerVM)
    val state by viewModel.ui
    val behavior = TopAppBarDefaults.pinnedScrollBehavior()

    Column {
        TopAppBar(
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = stringRes(Res.strings.back))
                }
            },
            title = { Text(text = stringRes(Res.strings.tag_manage)) },
            scrollBehavior = behavior,
        )

        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(behavior.nestedScrollConnection)
            ) {
                items(state.tagList, key = { it.label }) { tag ->
                    val isInner = tag.isDefault || tag.isBangumi
                    Row(
                        modifier = Modifier
                            .padding(8.dp, 4.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .then(
                                if (!isInner) Modifier.clickable { viewModel.dialogRename(tag) }
                                else Modifier
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (tag.isBangumi) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                AsyncImage(Res.images.bangumi_small, contentDescription = "bangumi", modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = stringRes(tag.displayName()),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        } else {
                            Text(
                                text = stringRes(tag.displayName()),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }

                        if (!isInner) {
                            Spacer(modifier = Modifier.size(8.dp))
                            Icon(
                                Icons.Filled.Edit,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                contentDescription = stringRes(Res.strings.rename_tag)
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))


                        Switch(
                            checked = tag.show,
                            onCheckedChange = { viewModel.onSetShow(tag, it) }
                        )

                        if (!isInner) {
                            IconButton(onClick = { viewModel.dialogDelete(tag) }) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = stringRes(Res.strings.delete_tag),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        } else {
                            // placeholder to keep row aligned
                            IconButton(onClick = {}, enabled = false) {
                                Icon(
                                    Icons.Filled.Close,
                                    tint = Color.Transparent,
                                    contentDescription = null,
                                )
                            }
                        }
                    }
                }
            }

            ExtendedFloatingActionButton(
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp, 40.dp),
                text = { Text(text = stringRes(Res.strings.new_tag)) },
                icon = { Icon(Icons.Filled.Add, contentDescription = stringRes(Res.strings.new_tag)) },
                onClick = { viewModel.dialogCreate() }
            )
        }
    }

    // Dialogs
    val focusRequester = remember { FocusRequester() }

    when (val dialog = state.dialog) {
        is TagsManagerVM.Dialog.Create -> {
            var label by remember { mutableStateOf("") }
            LaunchedEffect(Unit) {
                runCatching { focusRequester.requestFocus() }
            }
            AlertDialog(
                onDismissRequest = { viewModel.dialogDismiss() },
                confirmButton = {
                    TextButton(onClick = {
                        if (label.isNotBlank()) {
                            viewModel.onCreate(label)
                            viewModel.dialogDismiss()
                        }
                    }) { Text(text = stringRes(Res.strings.confirm)) }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dialogDismiss() }) {
                        Text(text = stringRes(Res.strings.cancel))
                    }
                },
                title = { Text(text = stringRes(Res.strings.new_tag)) },
                text = {
                    OutlinedTextField(
                        modifier = Modifier.focusRequester(focusRequester),
                        value = label,
                        onValueChange = { label = it }
                    )
                }
            )
        }
        is TagsManagerVM.Dialog.Rename -> {
            var label by remember { mutableStateOf(dialog.renameTag.label) }
            LaunchedEffect(Unit) {
                runCatching { focusRequester.requestFocus() }
            }
            AlertDialog(
                onDismissRequest = { viewModel.dialogDismiss() },
                confirmButton = {
                    TextButton(onClick = {
                        if (label.isNotBlank()) {
                            viewModel.onRename(dialog.renameTag, label)
                            viewModel.dialogDismiss()
                        }
                    }) { Text(text = stringRes(Res.strings.confirm)) }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dialogDismiss() }) {
                        Text(text = stringRes(Res.strings.cancel))
                    }
                },
                title = { Text(text = stringRes(Res.strings.rename_tag)) },
                text = {
                    OutlinedTextField(
                        modifier = Modifier.focusRequester(focusRequester),
                        value = label,
                        onValueChange = { label = it }
                    )
                }
            )
        }
        is TagsManagerVM.Dialog.Delete -> {
            AlertDialog(
                onDismissRequest = { viewModel.dialogDismiss() },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.onDelete(dialog.deleteTag)
                        viewModel.dialogDismiss()
                    }) { Text(text = stringRes(Res.strings.confirm)) }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dialogDismiss() }) {
                        Text(text = stringRes(Res.strings.cancel))
                    }
                },
                title = { Text(text = stringRes(Res.strings.delete_tag)) },
                text = { Text(text = stringRes(Res.strings.delete_confirmation)) },
            )
        }
        null -> {}
    }
}
