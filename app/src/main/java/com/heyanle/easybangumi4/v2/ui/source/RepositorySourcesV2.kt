package com.heyanle.easybangumi4.v2.ui.source

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easybangumi4.plugin.source.repository.RepositoryEntry
import com.heyanle.easybangumi4.ui.common.MD3PullRefreshIndicator
import com.heyanle.easybangumi4.ui.source_manage.repository.RepositoryManageViewModel
import com.heyanle.easybangumi4.ui.source_manage.repository.RepositorySourceViewModel
import com.heyanle.easybangumi4.v2.theme.V2Tokens
import com.heyanle.easybangumi4.v2.theme.V2Theme
import com.heyanle.easybangumi4.v2.ui.story.StoryEmptyV2
import com.heyanle.easybangumi4.v2.ui.story.StoryLoadingV2

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun RepositorySourcesV2() {
    val viewModel = viewModel<RepositorySourceViewModel>()
    val state by viewModel.state.collectAsState()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = state.isLoading,
        onRefresh = viewModel::refresh,
    )

    RepositoryInstallStateDialogV2(
        state = state.installState,
        onDismiss = viewModel::clearInstallState,
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState),
    ) {
        when {
            state.entries.isNotEmpty() -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 0.dp,
                        top = 0.dp,
                        end = 0.dp,
                        bottom = 28.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    items(
                        items = state.entries,
                        key = { "${it.repoUrl}::${it.key}" },
                    ) { entry ->
                        val installedSource = state.installedSources[entry.key]
                        val installing = state.installState
                            .let { it as? RepositorySourceViewModel.InstallState.Installing }
                            ?.key == entry.key
                        RepositorySourceCardV2(
                            entry = entry,
                            installedSource = installedSource,
                            isInstalling = installing,
                            onInstall = { viewModel.installSource(entry) },
                        )
                    }
                }
            }
            state.isLoading -> StoryLoadingV2("正在读取番源仓库")
            state.error != null -> StoryEmptyV2(
                title = "仓库加载失败",
                subtitle = state.error ?: "请下拉重试",
                titleColor = V2Tokens.Error,
            )
            else -> StoryEmptyV2(
                title = "暂无可用番源",
                subtitle = "点击右上角添加仓库，或下拉刷新",
            )
        }

        MD3PullRefreshIndicator(
            refreshing = state.isLoading,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = V2Tokens.Surface,
            contentColor = V2Theme.colors.accent,
        )
    }
}

@Composable
private fun RepositorySourceCardV2(
    entry: RepositoryEntry,
    installedSource: RepositorySourceViewModel.InstalledSource?,
    isInstalling: Boolean,
    onInstall: () -> Unit,
) {
    val hasInstalledSource = installedSource != null
    val hasNewerVersion = installedSource != null && entry.versionCode > installedSource.versionCode
    val actionLabel = when {
        isInstalling && hasNewerVersion -> "更新中"
        isInstalling -> "安装中"
        !hasInstalledSource -> "安装"
        hasNewerVersion -> "更新"
        else -> "已安装"
    }
    val actionEnabled = !isInstalling && (!hasInstalledSource || hasNewerVersion)

    Column {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = V2Tokens.WarmBackground,
            contentColor = V2Tokens.TextPrimary,
            tonalElevation = 0.dp,
        ) {
            Row(
            modifier = Modifier.padding(horizontal = V2Tokens.ScreenHorizontalPadding, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                color = V2Theme.colors.accentContainer,
                shape = RoundedCornerShape(11.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Extension,
                    contentDescription = null,
                    modifier = Modifier.padding(10.dp),
                    tint = V2Theme.colors.accent,
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
            ) {
                Text(
                    text = entry.label,
                    color = V2Tokens.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val versionText = buildList {
                    repositoryVersionLabelV2(entry)?.let { add("仓库 $it") }
                    installedVersionLabelV2(installedSource)?.let { add("当前 $it") }
                }.joinToString(" · ")
                if (versionText.isNotBlank()) {
                    Text(
                        text = versionText,
                        modifier = Modifier.padding(top = 4.dp),
                        color = V2Tokens.TextSecondary,
                        fontSize = 12.sp,
                    )
                }
                if (!entry.describe.isNullOrBlank()) {
                    Text(
                        text = entry.describe.orEmpty(),
                        modifier = Modifier.padding(top = 3.dp),
                        color = V2Tokens.TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Button(
                onClick = onInstall,
                enabled = actionEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = V2Theme.colors.accent,
                    contentColor = V2Tokens.TextPrimary,
                    disabledContainerColor = V2Tokens.Divider,
                    disabledContentColor = V2Tokens.TextSecondary,
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            ) {
                if (isInstalling) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .size(14.dp),
                        color = V2Tokens.TextPrimary,
                        strokeWidth = 2.dp,
                    )
                }
                Text(actionLabel, fontWeight = FontWeight.SemiBold)
            }
        }
        }
        HorizontalDivider(modifier = Modifier.padding(start = 72.dp), color = V2Tokens.Divider)
    }
}

@Composable
private fun RepositoryInstallStateDialogV2(
    state: RepositorySourceViewModel.InstallState,
    onDismiss: () -> Unit,
) {
    when (state) {
        RepositorySourceViewModel.InstallState.Idle -> Unit
        is RepositorySourceViewModel.InstallState.Installing -> {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("正在安装", color = V2Tokens.TextPrimary) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = V2Theme.colors.accent,
                            strokeWidth = 2.dp,
                        )
                        Text(
                            text = "正在下载并加载番源…",
                            modifier = Modifier.padding(start = 12.dp),
                            color = V2Tokens.TextSecondary,
                        )
                    }
                },
                confirmButton = {},
                containerColor = V2Tokens.Surface,
            )
        }
        is RepositorySourceViewModel.InstallState.Success,
        is RepositorySourceViewModel.InstallState.Error -> {
            val isError = state is RepositorySourceViewModel.InstallState.Error
            val message = when (state) {
                is RepositorySourceViewModel.InstallState.Success -> state.message
                is RepositorySourceViewModel.InstallState.Error -> state.message
                else -> ""
            }
            AlertDialog(
                onDismissRequest = onDismiss,
                title = {
                    Text(
                        text = if (isError) "安装失败" else "安装完成",
                        color = if (isError) V2Tokens.Error else V2Tokens.TextPrimary,
                    )
                },
                text = { Text(message, color = V2Tokens.TextSecondary) },
                confirmButton = {
                    TextButton(onClick = onDismiss) {
                        Text("确定", color = V2Theme.colors.accent)
                    }
                },
                containerColor = V2Tokens.Surface,
            )
        }
    }
}

@Composable
internal fun RepositoryManageDialogV2(
    onDismiss: () -> Unit,
) {
    val viewModel = viewModel<RepositoryManageViewModel>()
    val state by viewModel.state.collectAsState()
    var newUrl by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("仓库管理", color = V2Tokens.TextPrimary) },
        text = {
            Column {
                OutlinedTextField(
                    value = newUrl,
                    onValueChange = { newUrl = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("仓库地址") },
                    placeholder = { Text("https://…/index.jsonl") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = V2Theme.colors.accent,
                        focusedLabelColor = V2Theme.colors.accent,
                        cursorColor = V2Theme.colors.accent,
                    ),
                )
                Button(
                    onClick = {
                        if (newUrl.isNotBlank()) {
                            viewModel.addRepository(newUrl)
                            newUrl = ""
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    enabled = newUrl.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = V2Theme.colors.accent,
                        contentColor = V2Tokens.TextPrimary,
                    ),
                ) {
                    Text("添加", fontWeight = FontWeight.SemiBold)
                }
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = V2Tokens.Divider,
                )
                Text(
                    text = "已添加仓库",
                    color = V2Tokens.TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(6.dp))
                if (state.repositories.isEmpty()) {
                    Text(
                        text = "暂无仓库",
                        modifier = Modifier.padding(vertical = 24.dp),
                        color = V2Tokens.TextSecondary,
                        fontSize = 14.sp,
                    )
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 220.dp)) {
                        items(state.repositories, key = { it.url }) { repository ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = repository.label.ifBlank { "自定义仓库" },
                                        color = V2Tokens.TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                    )
                                    Text(
                                        text = repository.url,
                                        modifier = Modifier.padding(top = 2.dp),
                                        color = V2Tokens.TextSecondary,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                IconButton(onClick = { viewModel.removeRepository(repository) }) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "删除仓库",
                                        tint = V2Tokens.Error,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("完成", color = V2Theme.colors.accent)
            }
        },
        containerColor = V2Tokens.Surface,
    )
}

private fun repositoryVersionLabelV2(entry: RepositoryEntry): String? {
    return formatVersionV2(entry.version, entry.versionCode)
}

private fun installedVersionLabelV2(
    installedSource: RepositorySourceViewModel.InstalledSource?,
): String? {
    if (installedSource == null) return null
    return formatVersionV2(installedSource.version, installedSource.versionCode.toLong())
}

private fun formatVersionV2(version: String, versionCode: Long): String? {
    return when {
        version.isNotBlank() -> "v$version"
        versionCode > 0 -> "code $versionCode"
        else -> null
    }
}
