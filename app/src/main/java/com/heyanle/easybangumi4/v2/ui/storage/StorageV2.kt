package com.heyanle.easybangumi4.v2.ui.storage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.ui.common.LoadingPage
import com.heyanle.easybangumi4.ui.common.OkImage
import com.heyanle.easybangumi4.ui.storage.StorageViewModel
import com.heyanle.easybangumi4.v2.theme.V2Tokens
import com.heyanle.easybangumi4.v2.theme.V2Theme
import com.heyanle.easybangumi4.v2.ui.component.V2SecondaryHeader
import com.heyanle.easybangumi4.v2.ui.component.V2Section
import com.heyanle.easybangumi4.v2.ui.component.V2SectionDivider

@Composable
internal fun StorageV2() {
    val navController = LocalNavController.current
    val viewModel = viewModel<StorageViewModel>()
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(V2Tokens.WarmBackground)
            .navigationBarsPadding(),
    ) {
        V2SecondaryHeader(
            title = stringResource(R.string.backup_and_store),
            onBack = navController::popBackStack,
            largeTitle = true,
        )
        if (state.isBackupDoing || state.isRestoreDoing) {
            LoadingPage(
                modifier = Modifier.fillMaxSize(),
                loadingMsg = if (state.isRestoreDoing) "正在恢复数据" else stringResource(R.string.backup_doing),
            )
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = "将数据保存为备份文件，或从已有文件恢复。恢复操作可能覆盖当前数据。",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                    color = V2Tokens.TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                )

                V2Section(title = "恢复") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            modifier = Modifier.size(42.dp),
                            color = V2Theme.colors.accentContainer,
                            shape = RoundedCornerShape(11.dp),
                        ) {
                            Icon(
                                Icons.Filled.Restore,
                                contentDescription = null,
                                tint = V2Theme.colors.accent,
                                modifier = Modifier.padding(9.dp),
                            )
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp),
                        ) {
                            Text("从文件恢复", color = V2Tokens.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            Text("选择 EasyBangumi 备份文件", modifier = Modifier.padding(top = 3.dp), color = V2Tokens.TextSecondary, fontSize = 12.sp)
                        }
                        OutlinedButton(onClick = viewModel::onRestoreClick) {
                            Text(stringResource(R.string.restore), color = V2Theme.colors.accent)
                        }
                    }
                }

                V2Section(title = stringResource(R.string.backup)) {
                    StorageChoiceRowV2(
                        icon = {
                            OkImage(
                                image = com.heyanle.easybangumi4.R.mipmap.logo_new,
                                contentDescription = stringResource(R.string.cartoon_data),
                                modifier = Modifier.size(36.dp),
                            )
                        },
                        title = stringResource(R.string.cartoon_data) +
                            if (state.cartoonCount > 0) "（${state.cartoonCount} 项）" else "",
                        subtitle = stringResource(R.string.cartoon_data_desc),
                        checked = state.needBackupCartoonData,
                        onCheckedChange = viewModel::setNeedBackupCartoonStar,
                    )
                    V2SectionDivider()
                    StorageChoiceRowV2(
                        icon = {
                            Surface(
                                modifier = Modifier.size(36.dp),
                                color = V2Theme.colors.accentContainer,
                                shape = RoundedCornerShape(9.dp),
                            ) {
                                Icon(
                                    Icons.Filled.Settings,
                                    contentDescription = null,
                                    tint = V2Theme.colors.accent,
                                    modifier = Modifier.padding(7.dp),
                                )
                            }
                        },
                        title = stringResource(R.string.preference_data),
                        subtitle = stringResource(R.string.preference_desc),
                        checked = state.needBackupPreferenceData,
                        onCheckedChange = viewModel::setNeedBackupPreferenceData,
                    )
                }
            }

            Button(
                onClick = viewModel::showBackupDialog,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = V2Theme.colors.accent,
                    contentColor = V2Theme.colors.onAccent,
                ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(Icons.Filled.Backup, contentDescription = null)
                Text(
                    text = stringResource(R.string.start_backup),
                    modifier = Modifier.padding(start = 8.dp),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }

    if (state.showBackupDialog) {
        StorageConfirmDialogV2(
            title = stringResource(R.string.backup),
            message = stringResource(R.string.sure_to_backup),
            confirmLabel = stringResource(R.string.confirm),
            destructive = false,
            onConfirm = viewModel::onBackup,
            onDismiss = viewModel::dismissBackupDialog,
        )
    }
    state.restoreDialogUri?.let { uri ->
        StorageConfirmDialogV2(
            title = stringResource(R.string.restore),
            message = stringResource(R.string.sure_to_restore),
            confirmLabel = stringResource(R.string.restore),
            destructive = true,
            onConfirm = { viewModel.onRestore(uri) },
            onDismiss = { viewModel.showRestoreDialog(null) },
        )
    }
}

@Composable
private fun StorageChoiceRowV2(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon()
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
        ) {
            Text(title, color = V2Tokens.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, modifier = Modifier.padding(top = 3.dp), color = V2Tokens.TextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
        }
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = V2Theme.colors.accent,
                checkmarkColor = V2Theme.colors.onAccent,
            ),
        )
    }
}

@Composable
private fun StorageConfirmDialogV2(
    title: String,
    message: String,
    confirmLabel: String,
    destructive: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = V2Tokens.Surface,
        title = { Text(title, color = V2Tokens.TextPrimary) },
        text = { Text(message, color = V2Tokens.TextSecondary) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmLabel, color = if (destructive) V2Tokens.Error else V2Theme.colors.accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = V2Tokens.TextPrimary)
            }
        },
    )
}
