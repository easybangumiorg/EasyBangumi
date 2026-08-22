package com.heyanle.easybangumi4.v2.ui.setting

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heyanle.easybangumi4.danmaku.DanmakuPreferences
import com.heyanle.easybangumi4.danmaku.InnerDanmakuSourceRegistry
import com.heyanle.easybangumi4.v2.theme.V2Tokens
import com.heyanle.easybangumi4.v2.theme.V2Theme
import com.heyanle.easybangumi4.v2.ui.component.V2ActionRow
import com.heyanle.easybangumi4.v2.ui.component.V2Section
import com.heyanle.easybangumi4.v2.ui.component.V2SectionDivider
import com.heyanle.inject.core.Inject
import kotlinx.coroutines.launch

@Composable
internal fun DanmakuSourceSettingV2(
    modifier: Modifier = Modifier,
) {
    val preferences: DanmakuPreferences by Inject.injectLazy()
    val registry: InnerDanmakuSourceRegistry by Inject.injectLazy()
    val enabledSourceIds by preferences.enabledSourceIds.flow().collectAsState(
        preferences.enabledSourceIds.get(),
    )
    val defaultSourceId by preferences.defaultSourceId.flow().collectAsState(
        preferences.defaultSourceId.get(),
    )
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = V2Tokens.ScreenHorizontalPadding, vertical = 16.dp),
            color = V2Theme.colors.accentContainer,
            contentColor = V2Tokens.TextPrimary,
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(
                text = "这里只管理应用内置弹幕源，暂不支持添加或移除外部服务。",
                modifier = Modifier.padding(14.dp),
                color = V2Tokens.TextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp,
            )
        }

        registry.sources.forEach { source ->
            val metadata = source.metadata
            val enabled = metadata.id in enabledSourceIds
            val isDefault = metadata.id == defaultSourceId
            val availability = if (source.isAvailable()) {
                "内置 · 不可移除"
            } else {
                "尚未配置应用凭据 · 不可移除"
            }

            V2Section(title = metadata.displayName) {
                V2ActionRow(
                    icon = Icons.Filled.ClosedCaption,
                    title = "启用弹幕源",
                    subtitle = "${metadata.attribution} · $availability",
                    onClick = {
                        scope.launch {
                            updateDanmakuSourceEnabledV2(
                                preferences = preferences,
                                sourceId = metadata.id,
                                enabled = !enabled,
                                enabledSourceIds = enabledSourceIds,
                                isDefault = isDefault,
                            )
                        }
                    },
                    trailing = {
                        Switch(
                            checked = enabled,
                            onCheckedChange = { checked ->
                                scope.launch {
                                    updateDanmakuSourceEnabledV2(
                                        preferences = preferences,
                                        sourceId = metadata.id,
                                        enabled = checked,
                                        enabledSourceIds = enabledSourceIds,
                                        isDefault = isDefault,
                                    )
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = V2Tokens.Surface,
                                checkedTrackColor = V2Theme.colors.accent,
                                uncheckedThumbColor = V2Tokens.Surface,
                                uncheckedTrackColor = V2Tokens.Divider,
                                uncheckedBorderColor = V2Tokens.Divider,
                            ),
                        )
                    },
                )
                V2SectionDivider()
                V2ActionRow(
                    icon = Icons.Filled.Star,
                    title = "默认弹幕源",
                    subtitle = if (isDefault) "当前默认，匹配时优先使用" else "设为匹配时优先使用",
                    onClick = {
                        scope.launch {
                            preferences.enabledSourceIds.set(enabledSourceIds + metadata.id)
                            preferences.defaultSourceId.set(metadata.id)
                        }
                    },
                    trailing = {
                        RadioButton(
                            selected = isDefault,
                            onClick = {
                                scope.launch {
                                    preferences.enabledSourceIds.set(enabledSourceIds + metadata.id)
                                    preferences.defaultSourceId.set(metadata.id)
                                }
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = V2Theme.colors.accent),
                        )
                    },
                )
            }
        }
        Box(Modifier.height(24.dp))
    }
}

private fun updateDanmakuSourceEnabledV2(
    preferences: DanmakuPreferences,
    sourceId: String,
    enabled: Boolean,
    enabledSourceIds: Set<String>,
    isDefault: Boolean,
) {
    val next = enabledSourceIds.toMutableSet().apply {
        if (enabled) add(sourceId) else remove(sourceId)
    }
    preferences.enabledSourceIds.set(next)
    if (isDefault && next.isNotEmpty()) {
        preferences.defaultSourceId.set(next.first())
    }
}
