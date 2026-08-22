package com.heyanle.easybangumi4.v2.ui.setting

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.cartoon.story.download.CartoonDownloadPreference
import com.heyanle.easybangumi4.cartoon.story.local.LocalCartoonPreference
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.ui.setting.chooseDownloadFolder
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.easybangumi4.v2.theme.V2Tokens
import com.heyanle.easybangumi4.v2.theme.V2Theme
import com.heyanle.easybangumi4.v2.ui.component.V2ActionRow
import com.heyanle.easybangumi4.v2.ui.component.V2Section
import com.heyanle.easybangumi4.v2.ui.component.V2SectionDivider
import com.heyanle.inject.core.Inject

private enum class DownloadCountFieldV2 { Download, Transform }

@Composable
internal fun DownloadSettingV2(
    modifier: Modifier = Modifier,
) {
    val localPreference: LocalCartoonPreference by Inject.injectLazy()
    val downloadPreference: CartoonDownloadPreference by Inject.injectLazy()
    val usePrivate by localPreference.localUsePrivate.collectAsState()
    val path by localPreference.localPath.collectAsState()
    val noMedia by downloadPreference.localNoMedia.flow().collectAsState(
        downloadPreference.localNoMedia.get(),
    )
    val downloadMaxCount by downloadPreference.downloadMaxCountPref.flow().collectAsState(
        downloadPreference.downloadMaxCountPref.get(),
    )
    val transformMaxCount by downloadPreference.transformMaxCountPref.flow().collectAsState(
        downloadPreference.transformMaxCountPref.get(),
    )
    val downloadEncode by downloadPreference.downloadEncode.flow().collectAsState(
        downloadPreference.downloadEncode.get(),
    )
    var editingCount by remember { mutableStateOf<DownloadCountFieldV2?>(null) }
    var showEncodeDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        V2Section(title = "存储位置") {
            Column(modifier = Modifier.padding(14.dp)) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = V2Theme.colors.accentContainer,
                    contentColor = V2Tokens.TextPrimary,
                    shape = RoundedCornerShape(11.dp),
                ) {
                    Column(modifier = Modifier.padding(13.dp)) {
                        Text(
                            text = stringResource(R.string.choose_folder_to_bangumi),
                            color = V2Tokens.TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = stringResource(R.string.choose_folder_to_bangumi_msg),
                            modifier = Modifier.padding(top = 4.dp),
                            color = V2Tokens.TextSecondary,
                            fontSize = 12.sp,
                        )
                    }
                }
                Text(
                    text = "当前位置",
                    modifier = Modifier.padding(top = 14.dp),
                    color = V2Tokens.TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Filled.Folder,
                        contentDescription = null,
                        tint = V2Theme.colors.accent,
                    )
                    Text(
                        text = if (usePrivate) stringResource(R.string.private_folder) else path,
                        modifier = Modifier.padding(start = 8.dp),
                        color = V2Tokens.TextPrimary,
                        fontSize = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (usePrivate) {
                    Text(
                        text = stringResource(R.string.private_path_msg),
                        modifier = Modifier.padding(top = 6.dp),
                        color = V2Tokens.TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                    )
                }
                Row(
                    modifier = Modifier.padding(top = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    StorageLocationButtonV2(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.private_folder),
                        selected = usePrivate,
                        onClick = { localPreference.usePrivate(true) },
                    )
                    StorageLocationButtonV2(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.choose_folder),
                        selected = !usePrivate,
                        onClick = ::chooseDownloadFolder,
                    )
                }
            }
        }

        if (!usePrivate) {
            V2Section(title = "媒体扫描") {
                V2ActionRow(
                    icon = Icons.Filled.PhotoLibrary,
                    title = stringResource(R.string.local_no_media),
                    subtitle = stringResource(R.string.local_no_media_msg),
                    onClick = {
                        val next = !noMedia
                        downloadPreference.localNoMedia.set(next)
                        if (next) localPreference.createNoMedia() else localPreference.deleteNoMedia()
                    },
                    trailing = {
                        DownloadSwitchV2(
                            checked = noMedia,
                            onCheckedChange = { enabled ->
                                downloadPreference.localNoMedia.set(enabled)
                                if (enabled) localPreference.createNoMedia() else localPreference.deleteNoMedia()
                            },
                        )
                    },
                )
            }
        }

        V2Section(title = "任务数量") {
            V2ActionRow(
                icon = Icons.Filled.Download,
                title = stringResource(R.string.downloading_max_count),
                subtitle = downloadMaxCount.toString(),
                onClick = { editingCount = DownloadCountFieldV2.Download },
            )
            V2SectionDivider()
            V2ActionRow(
                icon = Icons.Filled.MovieFilter,
                title = stringResource(R.string.transforming_max_count),
                subtitle = transformMaxCount.toString(),
                onClick = { editingCount = DownloadCountFieldV2.Transform },
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            V2Section(title = "视频编码") {
                V2ActionRow(
                    icon = Icons.Filled.HighQuality,
                    title = stringResource(R.string.download_decode_type),
                    subtitle = downloadPreference.downloadEncodeSelection
                        .firstOrNull { it.first == downloadEncode }
                        ?.second
                        .orEmpty(),
                    onClick = { showEncodeDialog = true },
                )
            }
        }
        Box(Modifier.height(24.dp))
    }

    editingCount?.let { field ->
        val value = when (field) {
            DownloadCountFieldV2.Download -> downloadMaxCount
            DownloadCountFieldV2.Transform -> transformMaxCount
        }
        V2LongValueDialog(
            title = when (field) {
                DownloadCountFieldV2.Download -> stringResource(R.string.downloading_max_count)
                DownloadCountFieldV2.Transform -> stringResource(R.string.transforming_max_count)
            },
            value = value,
            onDismiss = { editingCount = null },
            onConfirm = { next ->
                when (field) {
                    DownloadCountFieldV2.Download -> downloadPreference.downloadMaxCountPref.set(next)
                    DownloadCountFieldV2.Transform -> downloadPreference.transformMaxCountPref.set(next)
                }
                editingCount = null
                stringRes(R.string.should_reboot).moeSnackBar()
            },
        )
    }

    if (showEncodeDialog) {
        V2EncodeDialog(
            selected = downloadEncode,
            options = downloadPreference.downloadEncodeSelection,
            onDismiss = { showEncodeDialog = false },
            onSelected = {
                downloadPreference.downloadEncode.set(it)
                showEncodeDialog = false
            },
        )
    }
}

@Composable
private fun StorageLocationButtonV2(
    modifier: Modifier,
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) V2Theme.colors.accent else V2Theme.colors.accentContainer,
            contentColor = V2Tokens.TextPrimary,
        ),
    ) {
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun DownloadSwitchV2(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = V2Tokens.Surface,
            checkedTrackColor = V2Theme.colors.accent,
            uncheckedThumbColor = V2Tokens.Surface,
            uncheckedTrackColor = V2Tokens.Divider,
            uncheckedBorderColor = V2Tokens.Divider,
        ),
    )
}

@Composable
private fun V2LongValueDialog(
    title: String,
    value: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
) {
    var text by remember(value) { mutableStateOf(value.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = V2Tokens.TextPrimary) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { input ->
                    if (input.isEmpty() || input.all(Char::isDigit)) text = input
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = V2Theme.colors.accent,
                    cursorColor = V2Theme.colors.accent,
                ),
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text.toLongOrNull() ?: 0L) }) {
                Text(stringResource(R.string.confirm), color = V2Theme.colors.accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = V2Tokens.TextSecondary)
            }
        },
        containerColor = V2Tokens.Surface,
    )
}

@Composable
private fun V2EncodeDialog(
    selected: CartoonDownloadPreference.DownloadEncode,
    options: List<Pair<CartoonDownloadPreference.DownloadEncode, String>>,
    onDismiss: () -> Unit,
    onSelected: (CartoonDownloadPreference.DownloadEncode) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.download_decode_type), color = V2Tokens.TextPrimary) },
        text = {
            Column {
                options.forEach { (value, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelected(value) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = selected == value,
                            onClick = { onSelected(value) },
                            colors = RadioButtonDefaults.colors(selectedColor = V2Theme.colors.accent),
                        )
                        Text(
                            text = label,
                            modifier = Modifier.padding(start = 8.dp),
                            color = V2Tokens.TextPrimary,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = V2Theme.colors.accent)
            }
        },
        containerColor = V2Tokens.Surface,
    )
}
