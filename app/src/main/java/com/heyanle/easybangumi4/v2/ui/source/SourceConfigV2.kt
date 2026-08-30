package com.heyanle.easybangumi4.v2.ui.source

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.plugin.api.component.preference.PreferenceComponent
import com.heyanle.easybangumi4.plugin.api.component.preference.SourcePreference
import com.heyanle.easybangumi4.plugin.api.utils.api.PreferenceHelper
import com.heyanle.easybangumi4.plugin.source.SourceInfo
import com.heyanle.easybangumi4.plugin.source.bundle.get
import com.heyanle.easybangumi4.ui.common.SourceContainerBase
import com.heyanle.easybangumi4.v2.theme.V2Tokens
import com.heyanle.easybangumi4.v2.theme.V2Theme
import com.heyanle.easybangumi4.v2.ui.component.V2SecondaryHeader
import com.heyanle.easybangumi4.v2.ui.component.V2Switch
import com.heyanle.easybangumi4.v2.ui.component.V2Section
import com.heyanle.easybangumi4.v2.ui.component.V2SectionDivider

/** V2 editor for source-defined switch, text and selection preferences. */
@Composable
internal fun SourceConfigV2(sourceKey: String) {
    val navController = LocalNavController.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(V2Tokens.WarmBackground),
    ) {
        V2SecondaryHeader(
            title = stringResource(R.string.source_config),
            onBack = navController::popBackStack,
        )
        SourceContainerBase(
            modifier = Modifier.weight(1f),
            hasSource = { it.preference(sourceKey) != null },
            errorContainerColor = V2Tokens.WarmBackground,
        ) { bundle ->
            val sourceInfo = bundle.sourceInfo(sourceKey)
            val configComponent = bundle.preference(sourceKey)
            if (sourceInfo is SourceInfo.Loaded && configComponent != null) {
                val preferenceHelper = sourceInfo.componentBundle.get<PreferenceHelper>()
                if (preferenceHelper == null) {
                    SourceConfigMessageV2("该番源未提供可用的配置存储")
                } else {
                    SourceConfigListV2(
                        modifier = Modifier.fillMaxSize(),
                        sourceLabel = sourceInfo.source.label,
                        preferenceHelper = preferenceHelper,
                        configComponent = configComponent,
                    )
                }
            }
        }
    }
}

@Composable
private fun SourceConfigListV2(
    modifier: Modifier,
    sourceLabel: String,
    preferenceHelper: PreferenceHelper,
    configComponent: PreferenceComponent,
) {
    val preferences = remember(configComponent) { configComponent.register() }
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 28.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.source_config_need_reboot),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(V2Theme.colors.accentContainer)
                    .padding(horizontal = V2Tokens.ScreenHorizontalPadding, vertical = 11.dp),
                color = V2Tokens.TextPrimary,
                fontSize = 12.sp,
            )
        }
        item {
            V2Section(title = sourceLabel) {
                preferences.forEachIndexed { index, preference ->
                    when (preference) {
                        is SourcePreference.Switch -> SourceSwitchPreferenceV2(
                            preference = preference,
                            preferenceHelper = preferenceHelper,
                        )
                        is SourcePreference.Edit -> SourceEditPreferenceV2(
                            preference = preference,
                            preferenceHelper = preferenceHelper,
                        )
                        is SourcePreference.Selection -> SourceSelectionPreferenceV2(
                            preference = preference,
                            preferenceHelper = preferenceHelper,
                        )
                    }
                    if (index != preferences.lastIndex) V2SectionDivider()
                }
            }
        }
    }
}

@Composable
private fun SourceSwitchPreferenceV2(
    preference: SourcePreference.Switch,
    preferenceHelper: PreferenceHelper,
) {
    var checked by remember(preference) {
        mutableStateOf(
            preferenceHelper.get(preference.key, preference.def).toBooleanStrictOrNull()
                ?: preference.def.toBooleanStrictOrNull()
                ?: false
        )
    }
    fun update(value: Boolean) {
        preferenceHelper.put(preference.key, value.toString())
        checked = value
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { update(!checked) }
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = preference.label,
            modifier = Modifier.weight(1f),
            color = V2Tokens.TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
        )
        V2Switch(
            checked = checked,
            onCheckedChange = ::update,
        )
    }
}

@Composable
private fun SourceEditPreferenceV2(
    preference: SourcePreference.Edit,
    preferenceHelper: PreferenceHelper,
) {
    var value by remember(preference) {
        mutableStateOf(preferenceHelper.get(preference.key, preference.def))
    }
    var draft by remember(preference) { mutableStateOf(value) }
    var showDialog by remember(preference) { mutableStateOf(false) }

    SourceValuePreferenceRowV2(
        label = preference.label,
        value = value,
        onClick = {
            draft = value
            showDialog = true
        },
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = V2Tokens.Surface,
            title = { Text(preference.label, color = V2Tokens.TextPrimary) },
            text = {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = V2Theme.colors.accent,
                        unfocusedBorderColor = V2Tokens.Divider,
                    ),
                )
            },
            dismissButton = {
                TextButton(onClick = { draft = preference.def }) {
                    Text(stringResource(R.string.default_value), color = V2Tokens.TextSecondary)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        preferenceHelper.put(preference.key, draft)
                        value = draft
                        showDialog = false
                    },
                ) { Text(stringResource(R.string.confirm), color = V2Theme.colors.accent) }
            },
        )
    }
}

@Composable
private fun SourceSelectionPreferenceV2(
    preference: SourcePreference.Selection,
    preferenceHelper: PreferenceHelper,
) {
    var value by remember(preference) {
        val stored = preferenceHelper.get(preference.key, preference.def)
        mutableStateOf(
            stored.takeIf { it in preference.selections }
                ?: preference.def.takeIf { it in preference.selections }
                ?: preference.selections.firstOrNull().orEmpty()
        )
    }
    var showDialog by remember(preference) { mutableStateOf(false) }

    SourceValuePreferenceRowV2(
        label = preference.label,
        value = if (preference.selections.isEmpty()) "无可选项" else value,
        enabled = preference.selections.isNotEmpty(),
        onClick = { showDialog = true },
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = V2Tokens.Surface,
            title = { Text(preference.label, color = V2Tokens.TextPrimary) },
            text = {
                LazyColumn {
                    items(preference.selections) { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    preferenceHelper.put(preference.key, option)
                                    value = option
                                    showDialog = false
                                }
                                .padding(vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = option,
                                modifier = Modifier.weight(1f),
                                color = V2Tokens.TextPrimary,
                            )
                            RadioButton(
                                selected = value == option,
                                onClick = {
                                    preferenceHelper.put(preference.key, option)
                                    value = option
                                    showDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = V2Theme.colors.accent),
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.cancel), color = V2Tokens.TextSecondary)
                }
            },
        )
    }
}

@Composable
private fun SourceValuePreferenceRowV2(
    label: String,
    value: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = V2Tokens.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = value,
                modifier = Modifier.padding(top = 3.dp),
            color = V2Tokens.TextSecondary.copy(alpha = if (enabled) 1f else 0.5f),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = V2Tokens.TextSecondary,
        )
    }
}

@Composable
private fun SourceConfigMessageV2(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(message, color = V2Tokens.TextSecondary)
    }
}
