package com.heyanle.easybangumi4.ui.storage.restore

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easybangumi4.R
import com.heyanle.easybangumi4.ui.common.LoadingPage

/**
 * Created by heyanlin on 2024/4/29.
 */
@Composable
fun Restore() {

    val vm = viewModel<RestoreViewModel>()
    val state = vm.state.collectAsState()
    val sta = state.value

    DisposableEffect(key1 = Unit) {
        vm.onLaunch()
        onDispose {
            vm.onDisposed()
        }
    }

    if (sta.restoreDialogFile != null) {
        AlertDialog(
            onDismissRequest = { vm.dismissRestoreDialog() },
            title = {
                Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.sure_to_restore))
            },
            text = {
                Text(text = sta.restoreDialogFile.name)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.restore(sta.restoreDialogFile)
                    }
                ) {
                    Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        vm.dismissRestoreDialog()
                    }
                ) {
                    Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.cancel))
                }
            },
        )
    } else if (sta.deleteDialogFile != null) {
        AlertDialog(
            onDismissRequest = { vm.dismissDeleteDialog() },
            title = {
                Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.sure_to_deleted_restore))
            },
            text = {
                Text(text = sta.deleteDialogFile.name)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.delete(sta.deleteDialogFile)
                    }
                ) {
                    Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        vm.dismissDeleteDialog()
                    }
                ) {
                    Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.cancel))
                }
            },
        )
    }
    if (sta.isRestoreDoing) {
        LoadingPage(
            modifier = Modifier.fillMaxSize(),
            loadingMsg = stringResource(id = com.heyanle.easy_i18n.R.string.restore_doing)
        )
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(sta.backupFileList) {
                    Column(
                        modifier = Modifier
                            .clickable {
                                vm.showRestoreDialog(it)
                            }
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(
                                    0.6f
                                ),
                                RoundedCornerShape(16.dp)
                            )
                            .fillMaxWidth(),
                        ) {

                        Row(
                            modifier = Modifier.padding(16.dp, 8.dp, 16.dp, 0.dp).align(Alignment.Start)
                        ) {
                            Text(text = it.name, maxLines = 2, overflow = TextOverflow.Ellipsis, )
                        }

                        Row(
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            IconButton(onClick = {
                                vm.share(it)
                            }) {
                                Icon(
                                    Icons.Filled.Share,
                                    contentDescription = stringResource(id = com.heyanle.easy_i18n.R.string.share)
                                )
                            }
                            IconButton(onClick = {
                                vm.saveToDownload(it)
                            }) {
                                Icon(
                                    Icons.Filled.Save,
                                    contentDescription = stringResource(id = com.heyanle.easy_i18n.R.string.restore)
                                )
                            }
                            IconButton(onClick = {
                                vm.showDeleteDialog(it)
                            }) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = stringResource(id = com.heyanle.easy_i18n.R.string.delete)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}