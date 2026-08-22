package com.heyanle.easybangumi4.v2.ui.setting

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.easybangumi4.v2.theme.V2Tokens
import com.heyanle.easybangumi4.v2.theme.V2Theme
import com.heyanle.easybangumi4.v2.theme.V2ThemeColor
import com.heyanle.easybangumi4.v2.theme.V2ThemeController
import com.heyanle.easybangumi4.v2.ui.component.V2ActionRow
import com.heyanle.easybangumi4.v2.ui.component.V2Section
import com.heyanle.inject.core.Inject

@Composable
internal fun AppearanceSettingV2(
    modifier: Modifier = Modifier,
) {
    val v2ThemeController: V2ThemeController by Inject.injectLazy()
    val settingPreferences: SettingPreferences by Inject.injectLazy()
    val v2ThemeState by v2ThemeController.themeFlow.collectAsState()
    val padMode by settingPreferences.padMode.flow().collectAsState(settingPreferences.padMode.get())
    var showPadModeDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        V2Section(title = "主题色") {
            V2ThemeColorChoices(
                selected = v2ThemeState.themeColor,
                onSelected = v2ThemeController::changeThemeColor,
            )
        }

        V2Section(title = stringResource(R.string.show)) {
            V2ActionRow(
                icon = Icons.Filled.Devices,
                title = stringResource(R.string.pad_mode),
                subtitle = padMode.displayNameV2(),
                onClick = { showPadModeDialog = true },
            )
        }
        Box(Modifier.height(24.dp))
    }

    if (showPadModeDialog) {
        PadModeDialogV2(
            selected = padMode,
            onDismiss = { showPadModeDialog = false },
            onSelected = { selected ->
                settingPreferences.padMode.set(selected)
                showPadModeDialog = false
                stringRes(R.string.some_page_should_reboot).moeSnackBar()
            },
        )
    }
}

@Composable
private fun V2ThemeColorChoices(
    selected: V2ThemeColor,
    onSelected: (V2ThemeColor) -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(V2ThemeColor.entries, key = { it.storageKey }) { themeColor ->
            val isSelected = selected == themeColor
            val activeColors = themeColor.colors(V2Theme.colors.isDark)
            Surface(
                modifier = Modifier
                    .width(92.dp)
                    .clickable { onSelected(themeColor) },
                color = if (isSelected) activeColors.accentContainer else V2Tokens.Surface,
                contentColor = V2Tokens.TextPrimary,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isSelected) activeColors.accent else V2Tokens.Divider,
                ),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        ThemeColorSwatchV2(
                            color = themeColor.day.accent,
                            contentColor = themeColor.day.onAccent,
                            selected = isSelected && !V2Theme.colors.isDark,
                            description = "日间颜色",
                        )
                        ThemeColorSwatchV2(
                            color = themeColor.night.accent,
                            contentColor = themeColor.night.onAccent,
                            selected = isSelected && V2Theme.colors.isDark,
                            description = "夜间颜色",
                        )
                    }
                    Text(
                        text = themeColor.displayName,
                        modifier = Modifier.padding(top = 8.dp),
                        color = V2Tokens.TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeColorSwatchV2(
    color: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    selected: Boolean,
    description: String,
) {
    Surface(
        modifier = Modifier.size(28.dp),
        color = color,
        shape = CircleShape,
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = description,
                modifier = Modifier.padding(6.dp),
                tint = contentColor,
            )
        }
    }
}

@Composable
private fun PadModeDialogV2(
    selected: SettingPreferences.PadMode,
    onDismiss: () -> Unit,
    onSelected: (SettingPreferences.PadMode) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.pad_mode), color = V2Tokens.TextPrimary) },
        text = {
            Column {
                SettingPreferences.PadMode.values().forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelected(mode) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = selected == mode,
                            onClick = { onSelected(mode) },
                            colors = RadioButtonDefaults.colors(selectedColor = V2Theme.colors.accent),
                        )
                        Text(
                            text = mode.displayNameV2(),
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

@Composable
private fun SettingPreferences.PadMode.displayNameV2(): String {
    return when (this) {
        SettingPreferences.PadMode.AUTO -> stringResource(R.string.auto)
        SettingPreferences.PadMode.ENABLE -> stringResource(R.string.always_on)
        SettingPreferences.PadMode.DISABLE -> stringResource(R.string.always_off)
    }
}
