package com.heyanle.easybangumi4.v2.ui.setting

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.heyanle.easybangumi4.danmaku.DANDANPLAY_SOURCE_ID
import com.heyanle.easybangumi4.danmaku.DanmakuDisplayPreferences
import com.heyanle.easybangumi4.v2.theme.V2Theme
import com.heyanle.easybangumi4.v2.theme.V2Tokens
import com.heyanle.inject.core.Inject

/** Dedicated V2 page for renderer, filtering and danmaku-source display preferences. */
@Composable
internal fun DanmakuSettingV2(
    modifier: Modifier = Modifier,
) {
    val preferences: DanmakuDisplayPreferences by Inject.injectLazy()
    val config by remember(preferences) {
        preferences.configFlow()
    }.collectAsState(preferences.getConfig())
    val enabledProvenance by preferences.enabledProvenance.flow().collectAsState(
        preferences.enabledProvenance.get(),
    )
    var confirmReset by remember { mutableStateOf(false) }
    var showBlockRules by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        DanmakuDisplaySettingV2(
            config = config,
            dandanPlayEnabled = DANDANPLAY_SOURCE_ID in enabledProvenance,
            onConfigChange = preferences::setConfig,
            onDandanPlayChange = { enabled ->
                preferences.enabledProvenance.set(
                    enabledProvenance.toMutableSet().apply {
                        if (enabled) add(DANDANPLAY_SOURCE_ID) else remove(DANDANPLAY_SOURCE_ID)
                    },
                )
            },
            onReset = { confirmReset = true },
            onOpenBlockRules = { showBlockRules = true },
        )
        Box(Modifier.height(24.dp))
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("恢复弹幕默认设置？", color = V2Tokens.TextPrimary) },
            text = {
                Text(
                    "将恢复显示类型、显示区域、不透明度、字体大小、行高、滚动速度、倍速同步、弹幕防覆盖、数量、复读合并和时间偏移。屏蔽词及其开关状态不会改变。",
                    color = V2Tokens.TextSecondary,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmReset = false
                        preferences.resetToDefaults()
                    },
                ) {
                    Text("恢复", color = V2Theme.colors.accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmReset = false }) {
                    Text("取消", color = V2Tokens.TextSecondary)
                }
            },
            containerColor = V2Tokens.Surface,
        )
    }

    if (showBlockRules) {
        DanmakuBlockRulesBottomSheet(
            config = config,
            onUpdateConfig = preferences::updateConfig,
            onDismiss = { showBlockRules = false },
        )
    }
}
