package com.heyanle.easybangumi4.ui.source_manage.repository

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.plugin.source.repository.RepositoryEntry
import com.heyanle.easybangumi4.ui.common.MD3PullRefreshIndicator
import com.heyanle.easybangumi4.ui.common.OkImage

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun RepositoryTopAppBar(behavior: TopAppBarScrollBehavior) {
    val nav = LocalNavController.current
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = {
                nav.popBackStack()
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(id = R.string.close)
                )
            }
        },
        title = { Text(text = "番源仓库") },
        scrollBehavior = behavior,
        actions = {
            IconButton(onClick = {
                // Signal to open manage dialog — handled in RepositorySource
                RepositoryTopAppBarActionHandler.openManageDialog()
            }) {
                Icon(Icons.Filled.Add, contentDescription = "添加仓库")
            }
        }
    )
}

// Simple singleton to bridge the top app bar action to the content below
object RepositoryTopAppBarActionHandler {
    var onOpenManageDialog: (() -> Unit)? = null

    fun openManageDialog() {
        onOpenManageDialog?.invoke()
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RepositorySource() {
    val vm = viewModel<RepositorySourceViewModel>()
    val state by vm.state.collectAsState()
    var showManageDialog by remember { mutableStateOf(false) }

    // Wire up the top bar action
    remember {
        RepositoryTopAppBarActionHandler.onOpenManageDialog = { showManageDialog = true }
    }

    val isRefreshing = state.isLoading
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { vm.refresh() }
    )

    // Install dialogs
    when (val installState = state.installState) {
        is RepositorySourceViewModel.InstallState.Success -> {
            AlertDialog(
                onDismissRequest = { vm.clearInstallState() },
                confirmButton = {
                    TextButton(onClick = { vm.clearInstallState() }) {
                        Text(text = stringResource(R.string.confirm))
                    }
                },
                text = { Text(installState.message) }
            )
        }
        is RepositorySourceViewModel.InstallState.Error -> {
            AlertDialog(
                onDismissRequest = { vm.clearInstallState() },
                confirmButton = {
                    TextButton(onClick = { vm.clearInstallState() }) {
                        Text(text = stringResource(R.string.confirm))
                    }
                },
                text = { Text(installState.message) }
            )
        }
        is RepositorySourceViewModel.InstallState.Installing -> {
            AlertDialog(
                onDismissRequest = {},
                confirmButton = {},
                text = { Text("加载中...") }
            )
        }
        else -> {}
    }

    if (showManageDialog) {
        RepositoryManageDialog(onDismiss = { showManageDialog = false })
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        if (state.entries.isEmpty() && !state.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                if (state.error != null) {
                    Text(text = state.error ?: "", color = MaterialTheme.colorScheme.error)
                } else {
                    Text(text = stringResource(R.string.no_source))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.entries, key = { "${it.repoUrl}::${it.key}" }) { entry ->
                    RepositorySourceItem(
                        entry = entry,
                        isInstalling = state.installState is RepositorySourceViewModel.InstallState.Installing
                                && (state.installState as RepositorySourceViewModel.InstallState.Installing).key == entry.key,
                        onInstall = { vm.installSource(entry) }
                    )
                }
            }
        }

        MD3PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Composable
fun RepositorySourceItem(
    entry: RepositoryEntry,
    isInstalling: Boolean,
    onInstall: () -> Unit,
) {
    ListItem(
        headlineContent = {
            Text(text = entry.label)
        },
        supportingContent = {
            Column {
                if (entry.version.isNotBlank()) {
                    Text(text = "v${entry.version}")
                }
                if (entry.describe != null) {
                    Text(text = entry.describe, maxLines = 2)
                }
            }
        },
        leadingContent = {
            OkImage(
                modifier = Modifier.size(40.dp),
                image = null,
                contentDescription = entry.label,
                crossFade = false,
                placeholderColor = null,
                errorColor = null,
            )
        },
        trailingContent = {
            IconButton(
                onClick = onInstall,
                enabled = !isInstalling,
            ) {
                Icon(
                    Icons.Filled.Download,
                    contentDescription = "安装",
                    tint = if (isInstalling) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary,
                )
            }
        },
    )
}
