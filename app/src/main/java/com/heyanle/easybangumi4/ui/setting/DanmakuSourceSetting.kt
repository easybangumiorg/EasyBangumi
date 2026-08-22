package com.heyanle.easybangumi4.ui.setting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.heyanle.easybangumi4.danmaku.DanmakuPreferences
import com.heyanle.easybangumi4.danmaku.InnerDanmakuSourceRegistry
import com.heyanle.inject.core.Inject
import kotlinx.coroutines.launch

/** Settings for application-packaged sources only; no plug-in registration is exposed here. */
@Composable
fun ColumnScope.DanmakuSourceSetting(
    nestedScrollConnection: NestedScrollConnection,
) {
    val preferences: DanmakuPreferences by Inject.injectLazy()
    val registry: InnerDanmakuSourceRegistry by Inject.injectLazy()
    val enabledSourceIds by preferences.enabledSourceIds.flow()
        .collectAsState(preferences.enabledSourceIds.get())
    val defaultSourceId by preferences.defaultSourceId.flow()
        .collectAsState(preferences.defaultSourceId.get())
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .weight(1f)
            .nestedScroll(nestedScrollConnection)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            text = "仅管理应用内置弹幕源；暂不支持添加或移除外部弹幕源。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        registry.sources.forEach { source ->
            val metadata = source.metadata
            val enabled = metadata.id in enabledSourceIds
            val isDefault = metadata.id == defaultSourceId
            ListItem(
                headlineContent = { Text(metadata.displayName) },
                supportingContent = {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(metadata.attribution)
                        Text(
                            if (source.isAvailable()) "内置 · 不可移除" else "尚未配置应用凭据 · 不可移除",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                leadingContent = {
                    Icon(Icons.Filled.ClosedCaption, contentDescription = metadata.displayName)
                },
                trailingContent = {
                    Switch(
                        checked = enabled,
                        onCheckedChange = { checked ->
                            scope.launch {
                                val next = enabledSourceIds.toMutableSet().apply {
                                    if (checked) add(metadata.id) else remove(metadata.id)
                                }
                                preferences.enabledSourceIds.set(next)
                                if (isDefault && next.isNotEmpty()) {
                                    preferences.defaultSourceId.set(next.first())
                                }
                            }
                        },
                    )
                },
            )
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        scope.launch {
                            preferences.enabledSourceIds.set(enabledSourceIds + metadata.id)
                            preferences.defaultSourceId.set(metadata.id)
                        }
                    },
                headlineContent = { Text("设为默认弹幕源") },
                supportingContent = { Text(if (isDefault) "当前默认" else "匹配时优先使用") },
                leadingContent = {
                    RadioButton(
                        selected = isDefault,
                        onClick = {
                            scope.launch {
                                preferences.enabledSourceIds.set(enabledSourceIds + metadata.id)
                                preferences.defaultSourceId.set(metadata.id)
                            }
                        },
                    )
                },
                trailingContent = { Text("内置") },
            )
        }
    }
}
