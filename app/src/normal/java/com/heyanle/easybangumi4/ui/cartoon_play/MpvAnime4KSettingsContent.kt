package com.heyanle.easybangumi4.ui.cartoon_play

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.heyanle.easybangumi4.player.mpv.MpvAnime4KStatus
import com.heyanle.easybangumi4.setting.SettingPreferences

/** Anime4K-only player settings; this implementation is absent from the normal artifact. */
@Composable
internal fun MpvAnime4KSettingsContent(
    enabled: Boolean,
    preset: SettingPreferences.MpvAnime4KPreset,
    status: MpvAnime4KStatus,
    onEnabledChange: (Boolean) -> Unit,
    onPresetChange: (SettingPreferences.MpvAnime4KPreset) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SettingsGroupTitle("Anime4K")
        ListItem(
            headlineContent = { Text("启用 Anime4K") },
            supportingContent = { Text("使用 mpv GLSL Shader 实时增强画面") },
            trailingContent = {
                Switch(
                    checked = enabled,
                    onCheckedChange = onEnabledChange,
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onEnabledChange(!enabled) },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
        if (enabled) {
            SettingsGroupTitle("实时状态")
            ListItem(
                headlineContent = {
                    Text(
                        when (status.upscaleCnnActive) {
                            true -> "Upscale CNN 已触发"
                            false -> "Upscale CNN 未触发"
                            null -> "正在读取视频尺寸"
                        },
                    )
                },
                supportingContent = {
                    Text(
                        when {
                            status.inputWidth <= 0 || status.outputWidth <= 0 ->
                                "开始播放后将显示实际输入与输出尺寸"
                            status.upscaleCnnActive == true ->
                                "输入 ${status.inputWidth}×${status.inputHeight} → 输出 ${status.outputWidth}×${status.outputHeight}"
                            else ->
                                "输入 ${status.inputWidth}×${status.inputHeight} → 输出 ${status.outputWidth}×${status.outputHeight}；输出需至少比输入放大约 20%"
                        },
                    )
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            )
            SettingsGroupTitle("增强档位")
            listOf(
                SettingPreferences.MpvAnime4KPreset.FAST to ("效率档" to "更省电，适合日常观看"),
                SettingPreferences.MpvAnime4KPreset.QUALITY to ("质量档" to "更高画质，可能增加功耗和发热"),
                SettingPreferences.MpvAnime4KPreset.STRONG to ("强效档" to "低清老番增强更明显，可能明显发热或掉帧"),
            ).forEach { (value, content) ->
                val selected = preset == value
                ListItem(
                    headlineContent = { Text(content.first) },
                    supportingContent = { Text(content.second) },
                    trailingContent = {
                        RadioButton(
                            selected = selected,
                            onClick = { onPresetChange(value) },
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.RadioButton) { onPresetChange(value) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}
