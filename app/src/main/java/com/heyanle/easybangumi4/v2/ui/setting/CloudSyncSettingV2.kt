package com.heyanle.easybangumi4.v2.ui.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easybangumi4.cloudsync.model.CloudSyncConflictStrategy
import com.heyanle.easybangumi4.cloudsync.model.CloudSyncProvider
import com.heyanle.easybangumi4.cloudsync.ui.CloudSyncSettingViewModel
import com.heyanle.easybangumi4.v2.theme.V2Theme
import com.heyanle.easybangumi4.v2.theme.V2Tokens
import com.heyanle.easybangumi4.v2.ui.component.V2ActionRow
import com.heyanle.easybangumi4.v2.ui.component.V2Section
import com.heyanle.easybangumi4.v2.ui.component.V2SectionDivider
import java.text.DateFormat
import java.util.Date

@Composable
internal fun CloudSyncSettingV2(modifier: Modifier = Modifier) {
    val viewModel = viewModel<CloudSyncSettingViewModel>()
    val state by viewModel.state.collectAsState()
    // OneDrive is deferred. A previously selected hidden provider must not enable sync actions.
    val webDavSelected = state.provider == CloudSyncProvider.WEBDAV
    val canSync = !state.operation.isRunning && webDavSelected
    var showWebDavDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
    ) {
        Text(
            text = "当前仅提供手动上传和下载。收藏与历史按字段合并，番源详情、播放地址和本地文件不会上传。",
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            color = V2Tokens.TextSecondary,
            fontSize = 13.sp,
            lineHeight = 19.sp,
        )

        V2Section(title = "同步服务") {
            V2ActionRow(
                icon = Icons.Filled.CloudOff,
                title = "未启用",
                subtitle = "保留配置，但不选择云端服务",
                onClick = { viewModel.selectProvider(CloudSyncProvider.NONE) },
                trailing = {
                    RadioButton(
                        selected = !webDavSelected,
                        onClick = { viewModel.selectProvider(CloudSyncProvider.NONE) },
                        colors = RadioButtonDefaults.colors(selectedColor = V2Theme.colors.accent),
                    )
                },
            )
            V2SectionDivider()
            V2ActionRow(
                icon = Icons.Filled.Storage,
                title = "WebDAV",
                subtitle = webDavSummary(state.webDavUrl, state.webDavUsername, state.hasWebDavPassword),
                onClick = {
                    viewModel.selectProvider(CloudSyncProvider.WEBDAV)
                    showWebDavDialog = true
                },
                trailing = {
                    RadioButton(
                        selected = webDavSelected,
                        onClick = {
                            viewModel.selectProvider(CloudSyncProvider.WEBDAV)
                            showWebDavDialog = true
                        },
                        colors = RadioButtonDefaults.colors(selectedColor = V2Theme.colors.accent),
                    )
                },
            )
        }

        V2Section(title = "冲突处理") {
            conflictStrategies.forEachIndexed { index, item ->
                V2ActionRow(
                    icon = Icons.Filled.Lock,
                    title = item.title,
                    subtitle = item.subtitle,
                    onClick = { viewModel.setConflictStrategy(item.strategy) },
                    trailing = {
                        RadioButton(
                            selected = state.conflictStrategy == item.strategy,
                            onClick = { viewModel.setConflictStrategy(item.strategy) },
                            colors = RadioButtonDefaults.colors(selectedColor = V2Theme.colors.accent),
                        )
                    },
                )
                if (index != conflictStrategies.lastIndex) V2SectionDivider()
            }
        }

        V2Section(title = "手动同步") {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = viewModel::upload,
                        enabled = canSync,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = V2Theme.colors.accent,
                            contentColor = V2Theme.colors.onAccent,
                        ),
                    ) {
                        androidx.compose.material3.Icon(Icons.Filled.CloudUpload, contentDescription = null)
                        Text("上传", modifier = Modifier.padding(start = 6.dp))
                    }
                    OutlinedButton(
                        onClick = viewModel::download,
                        enabled = canSync,
                        modifier = Modifier.weight(1f),
                    ) {
                        androidx.compose.material3.Icon(Icons.Filled.CloudDownload, contentDescription = null)
                        Text("下载", modifier = Modifier.padding(start = 6.dp))
                    }
                }
                OutlinedButton(
                    onClick = viewModel::testConnection,
                    enabled = canSync,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                ) {
                    Text("测试连接")
                }
                Row(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (state.operation.isRunning) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 10.dp),
                            color = V2Theme.colors.accent,
                            strokeWidth = 2.dp,
                        )
                    }
                    Column {
                        Text(
                            text = state.operation.message.ifBlank { "尚未执行同步" },
                            color = if (state.operation.throwable == null) V2Tokens.TextPrimary else V2Tokens.Error,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                        )
                        val details = buildList {
                            if (state.pendingChanges > 0) add("待处理 ${state.pendingChanges} 项本机变更")
                            if (state.lastSuccessTime > 0L) {
                                add("上次成功：${DateFormat.getDateTimeInstance().format(Date(state.lastSuccessTime))}")
                            }
                        }.joinToString(" · ")
                        if (details.isNotBlank()) {
                            Text(details, color = V2Tokens.TextSecondary, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    if (showWebDavDialog) {
        WebDavConfigDialog(
            initialUrl = state.webDavUrl,
            initialUsername = state.webDavUsername,
            hasSavedPassword = state.hasWebDavPassword,
            onDismiss = { showWebDavDialog = false },
            onSave = { url, username, password ->
                viewModel.saveWebDav(url, username, password)
                showWebDavDialog = false
            },
        )
    }
}

@Composable
private fun WebDavConfigDialog(
    initialUrl: String,
    initialUsername: String,
    hasSavedPassword: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit,
) {
    var url by remember(initialUrl) { mutableStateOf(initialUrl) }
    var username by remember(initialUsername) { mutableStateOf(initialUsername) }
    var password by remember { mutableStateOf("") }
    val valid = url.trim().startsWith("http") && username.isNotBlank() &&
        (password.isNotEmpty() || hasSavedPassword)
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = V2Tokens.Surface,
        title = { Text("WebDAV 配置", color = V2Tokens.TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "填写目标目录地址。同步文件会保存为 easybangumi-sync-v1.json.gz。建议使用 HTTPS 和应用专用密码。",
                    color = V2Tokens.TextSecondary,
                    fontSize = 12.sp,
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("WebDAV 目录地址") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("用户名") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(if (hasSavedPassword) "密码（留空保持原密码）" else "密码或应用密码") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(url, username, password) }, enabled = valid) {
                Text("保存", color = V2Theme.colors.accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消", color = V2Tokens.TextPrimary) }
        },
    )
}

private data class ConflictStrategyPresentation(
    val strategy: CloudSyncConflictStrategy,
    val title: String,
    val subtitle: String,
)

private val conflictStrategies = listOf(
    ConflictStrategyPresentation(
        CloudSyncConflictStrategy.SMART_MERGE,
        "智能合并",
        "收藏和历史分别比较修改版本，推荐使用",
    ),
    ConflictStrategyPresentation(
        CloudSyncConflictStrategy.LOCAL_WINS,
        "本机优先",
        "发生冲突时保留本机数据",
    ),
    ConflictStrategyPresentation(
        CloudSyncConflictStrategy.REMOTE_WINS,
        "云端优先",
        "发生冲突时采用云端数据",
    ),
)

private fun webDavSummary(url: String, username: String, hasPassword: Boolean): String = when {
    url.isBlank() -> "尚未配置"
    username.isBlank() || !hasPassword -> "配置不完整"
    else -> "$username · $url"
}
