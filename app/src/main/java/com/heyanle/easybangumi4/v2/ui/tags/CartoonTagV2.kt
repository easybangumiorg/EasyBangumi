package com.heyanle.easybangumi4.v2.ui.tags

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.cartoon.entity.CartoonTag
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.ui.tags.CartoonTagViewModel
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.easybangumi4.v2.theme.V2Tokens
import com.heyanle.easybangumi4.v2.theme.V2Theme
import com.heyanle.easybangumi4.v2.ui.component.V2SecondaryHeader
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@Composable
internal fun CartoonTagV2() {
    val navController = LocalNavController.current
    val viewModel = viewModel<CartoonTagViewModel>()
    val reorderState = rememberReorderableLazyListState(
        onMove = { from, to -> viewModel.move(from.index, to.index) },
        onDragEnd = { _, _ -> viewModel.onDragEnd() },
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(V2Tokens.WarmBackground)
            .navigationBarsPadding(),
    ) {
        V2SecondaryHeader(
            title = stringResource(R.string.tag_manage),
            onBack = navController::popBackStack,
            largeTitle = true,
            actions = {
                IconButton(onClick = viewModel::dialogCreate) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = stringResource(R.string.new_tag),
                        tint = V2Theme.colors.accent,
                    )
                }
            },
        )
        Text(
            text = "拖动调整显示顺序；至少保留一个可见标签",
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            color = V2Tokens.TextSecondary,
            fontSize = 13.sp,
        )
        LazyColumn(
            state = reorderState.listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .reorderable(reorderState),
        ) {
            items(viewModel.tags, key = { it.label }) { tag ->
                ReorderableItem(
                    reorderableState = reorderState,
                    key = tag.label,
                ) { dragging ->
                    CartoonTagRowV2(
                        tag = tag,
                        dragging = dragging,
                        dragModifier = Modifier.detectReorder(reorderState),
                        onRename = { viewModel.dialogRename(tag) },
                        onShowChange = { viewModel.onSetShow(tag, it) },
                        onDelete = { viewModel.dialogDelete(tag) },
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = V2Tokens.Divider)
            }
        }
        Spacer(Modifier.size(16.dp))
    }

    when (val dialog = viewModel.dialog) {
        is CartoonTagViewModel.Dialog.Create -> CartoonTagEditDialogV2(
            title = stringResource(R.string.new_tag),
            initialValue = "",
            onConfirm = { label ->
                if (label.isEmpty()) {
                    stringRes(R.string.is_empty).moeSnackBar()
                } else {
                    viewModel.onCreate(label)
                    viewModel.dialogDismiss()
                }
            },
            onDismiss = viewModel::dialogDismiss,
        )

        is CartoonTagViewModel.Dialog.Rename -> CartoonTagEditDialogV2(
            title = stringResource(R.string.rename_tag),
            initialValue = dialog.renameTag.label,
            onConfirm = { label ->
                if (label.isEmpty()) {
                    stringRes(R.string.is_empty).moeSnackBar()
                } else {
                    viewModel.onRename(dialog.renameTag, label)
                    viewModel.dialogDismiss()
                }
            },
            onDismiss = viewModel::dialogDismiss,
        )

        is CartoonTagViewModel.Dialog.Delete -> AlertDialog(
            onDismissRequest = viewModel::dialogDismiss,
            containerColor = V2Tokens.Surface,
            title = { Text("删除标签", color = V2Tokens.TextPrimary) },
            text = { Text("确定删除“${dialog.deleteTag.display}”吗？", color = V2Tokens.TextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onDelete(dialog.deleteTag)
                        viewModel.dialogDismiss()
                    },
                ) { Text(stringResource(R.string.delete), color = V2Tokens.Error) }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dialogDismiss) {
                    Text(stringResource(R.string.cancel), color = V2Tokens.TextPrimary)
                }
            },
        )

        else -> Unit
    }
}

@Composable
private fun CartoonTagRowV2(
    tag: CartoonTag,
    dragging: Boolean,
    dragModifier: Modifier,
    onRename: () -> Unit,
    onShowChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (dragging) V2Theme.colors.accentContainer else V2Tokens.WarmBackground)
            .clickable(enabled = !tag.isInner, onClick = onRename)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(modifier = dragModifier, onClick = {}) {
            Icon(Icons.Filled.DragHandle, contentDescription = "拖动排序", tint = V2Tokens.TextSecondary)
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = tag.display,
                    color = V2Tokens.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                )
                if (!tag.isInner) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = null,
                        tint = V2Tokens.TextSecondary,
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .size(15.dp),
                    )
                }
            }
            Text(
                text = if (tag.isInner) "内置标签" else "自定义标签",
                modifier = Modifier.padding(top = 3.dp),
                color = V2Tokens.TextSecondary,
                fontSize = 12.sp,
            )
        }
        Switch(
            checked = tag.show,
            onCheckedChange = onShowChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = V2Theme.colors.accent,
                checkedThumbColor = V2Tokens.Surface,
                uncheckedTrackColor = V2Tokens.Divider,
            ),
        )
        if (!tag.isInner) {
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.DeleteOutline, contentDescription = stringResource(R.string.delete_tag), tint = V2Tokens.Error)
            }
        } else {
            Spacer(Modifier.size(48.dp))
        }
    }
}

@Composable
private fun CartoonTagEditDialogV2(
    title: String,
    initialValue: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var value by remember(initialValue) { mutableStateOf(initialValue) }
    val focusRequester = remember { FocusRequester() }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = V2Tokens.Surface,
        title = { Text(title, color = V2Tokens.TextPrimary) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                modifier = Modifier.focusRequester(focusRequester),
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(value) }) {
                Text(stringResource(R.string.confirm), color = V2Theme.colors.accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = V2Tokens.TextPrimary)
            }
        },
    )
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
}
