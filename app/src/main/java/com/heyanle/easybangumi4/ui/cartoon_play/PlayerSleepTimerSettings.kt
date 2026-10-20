package com.heyanle.easybangumi4.ui.cartoon_play

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@Composable
internal fun PlayerSleepTimerSettingsContent(
    state: PlayerSleepTimerState,
    onOptionSelected: (PlayerSleepTimerOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().selectableGroup()) {
        Text(
            text = "睡眠定时",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 24.dp, top = 16.dp, end = 24.dp),
        )
        Text(
            text = state.statusText(),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .testTag("player_sleep_timer_status"),
        )
        PlayerSleepTimerOption.entries.forEach { option ->
            val selected = state.option == option
            ListItem(
                headlineContent = { Text(option.displayName()) },
                supportingContent = if (option == PlayerSleepTimerOption.END_OF_EPISODE) {
                    { Text("播完当前集后停止，不自动播放下一集") }
                } else null,
                trailingContent = { RadioButton(selected = selected, onClick = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("player_sleep_timer_${option.name.lowercase()}")
                    .selectable(
                        selected = selected,
                        role = Role.RadioButton,
                        onClick = { onOptionSelected(option) },
                    ),
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            )
        }
        Text(
            text = "仅在当前播放会话生效，离开播放页后关闭。暂停播放时也会继续计时。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        )
    }
}

private fun PlayerSleepTimerOption.displayName(): String = when (this) {
    PlayerSleepTimerOption.OFF -> "关闭定时"
    PlayerSleepTimerOption.END_OF_EPISODE -> "本集结束后停止"
    else -> "$minutes 分钟后暂停"
}

internal fun PlayerSleepTimerState.statusText(): String = when {
    stoppedByTimer -> "已按睡眠定时暂停播放"
    option == PlayerSleepTimerOption.OFF -> "未开启"
    option == PlayerSleepTimerOption.END_OF_EPISODE -> "将在本集结束后停止"
    else -> {
        val seconds = (remainingMs.coerceAtLeast(0L) + 999L) / 1_000L
        "${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')} 后暂停播放"
    }
}
