package com.heyanle.easybangumi4.ui.source_manage.repository

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.plugin.source.repository.RepositoryInfo

@Composable
fun RepositoryManageDialog(
    onDismiss: () -> Unit,
) {
    val vm = viewModel<RepositoryManageViewModel>()
    val state by vm.state.collectAsState()
    var newUrl by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "仓库管理")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = newUrl,
                    onValueChange = { newUrl = it },
                    placeholder = { Text("请输入仓库地址") },
                    label = { Text("添加仓库") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Button(
                    onClick = {
                        if (newUrl.isNotBlank()) {
                            vm.addRepository(newUrl)
                            newUrl = ""
                        }
                    },
                    enabled = newUrl.isNotBlank(),
                    modifier = Modifier.padding(vertical = 8.dp),
                ) {
                    Text(text = "添加")
                }

                HorizontalDivider()

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "仓库列表",
                    style = MaterialTheme.typography.titleSmall,
                )
                Spacer(modifier = Modifier.height(4.dp))

                LazyColumn(modifier = Modifier.height(200.dp)) {
                    items(state.repositories, key = { it.url }) { repo ->
                        ListItem(
                            headlineContent = {
                                Text(text = repo.label.ifBlank { repo.url })
                            },
                            supportingContent = {
                                Text(text = repo.url, maxLines = 1)
                            },
                            trailingContent = {
                                IconButton(onClick = { vm.removeRepository(repo) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete))
                                }
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.close))
            }
        },
    )
}
