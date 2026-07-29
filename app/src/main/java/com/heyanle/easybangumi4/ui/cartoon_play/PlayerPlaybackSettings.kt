package com.heyanle.easybangumi4.ui.cartoon_play

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heyanle.easybangumi4.danmaku.DanmakuDisplayConfig
import com.heyanle.easybangumi4.danmaku.DanmakuPlaybackState
import com.heyanle.easybangumi4.danmaku.DanmakuPlaybackStatus
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Player-only presentation state. It intentionally contains no source or renderer implementation
 * details, so the shared player controls remain usable by the legacy page.
 */
data class PlayerDanmakuControlState(
    val visualState: VisualState,
    val displayEnabled: Boolean,
    val contentDescription: String,
    val onClick: () -> Unit,
) {
    enum class VisualState {
        Available,
        Loading,
        Unavailable,
    }
}

internal fun DanmakuPlaybackState.toPlayerDanmakuControlState(
    displayEnabled: Boolean,
    onToggleDisplay: (Boolean) -> Unit,
    onManualMatch: () -> Unit,
    onRetry: () -> Unit,
    onOpenSourceSettings: () -> Unit,
): PlayerDanmakuControlState = when (val current = status) {
    DanmakuPlaybackStatus.Disabled -> PlayerDanmakuControlState(
        visualState = PlayerDanmakuControlState.VisualState.Unavailable,
        displayEnabled = false,
        contentDescription = "弹幕源未启用，点击前往设置",
        onClick = onOpenSourceSettings,
    )

    DanmakuPlaybackStatus.MatchingBangumi,
    DanmakuPlaybackStatus.MatchingEpisode,
    DanmakuPlaybackStatus.LoadingComments,
    -> PlayerDanmakuControlState(
        visualState = PlayerDanmakuControlState.VisualState.Loading,
        displayEnabled = false,
        contentDescription = "弹幕加载中",
        onClick = {},
    )

    is DanmakuPlaybackStatus.Matched -> PlayerDanmakuControlState(
        visualState = PlayerDanmakuControlState.VisualState.Available,
        displayEnabled = displayEnabled,
        contentDescription = if (displayEnabled) "关闭弹幕" else "开启弹幕",
        onClick = { onToggleDisplay(!displayEnabled) },
    )

    is DanmakuPlaybackStatus.Empty -> PlayerDanmakuControlState(
        visualState = PlayerDanmakuControlState.VisualState.Unavailable,
        displayEnabled = false,
        contentDescription = "当前选集暂无弹幕，点击更换番剧和选集",
        onClick = onManualMatch,
    )

    is DanmakuPlaybackStatus.Unmatched -> PlayerDanmakuControlState(
        visualState = PlayerDanmakuControlState.VisualState.Unavailable,
        displayEnabled = false,
        contentDescription = "尚未匹配弹幕，点击匹配番剧和选集",
        onClick = onManualMatch,
    )

    is DanmakuPlaybackStatus.Unavailable -> {
        val sourceNeedsConfiguration = current.message.contains("配置") ||
            current.message.contains("启用")
        PlayerDanmakuControlState(
            visualState = PlayerDanmakuControlState.VisualState.Unavailable,
            displayEnabled = false,
            contentDescription = if (sourceNeedsConfiguration) {
                "${current.message}，点击前往设置"
            } else {
                "${current.message}，点击重试"
            },
            onClick = if (sourceNeedsConfiguration) onOpenSourceSettings else onRetry,
        )
    }
}

internal enum class PlayerSettingsSection {
    Danmaku,
    Video,
}

internal object PlayerPlaybackSettingsTestTags {
    const val DANMAKU_TOGGLE = "player_danmaku_toggle"
    const val SETTINGS_PANEL = "player_settings_panel"
    const val DANMAKU_SECTION_TAB = "player_settings_tab_danmaku"
    const val VIDEO_SECTION_TAB = "player_settings_tab_video"
    const val OPEN_DANMAKU_SETTINGS = "player_open_danmaku_settings"
    const val DANMAKU_SECTION = "player_settings_danmaku"
    const val VIDEO_SECTION = "player_settings_video"
    const val RESET = "player_danmaku_reset"
    const val RESET_CONFIRM = "player_danmaku_reset_confirm"
    const val FONT_SIZE = "player_danmaku_font_size"
    const val LINE_HEIGHT = "player_danmaku_line_height"
    const val SCROLL_SPEED = "player_danmaku_scroll_speed"
    const val TIME_OFFSET = "player_danmaku_time_offset"

    fun videoScale(value: Int) = "player_video_scale_$value"
}

@Composable
internal fun PlayerDanmakuToggle(
    state: PlayerDanmakuControlState,
    modifier: Modifier = Modifier,
) {
    val isLoading = state.visualState == PlayerDanmakuControlState.VisualState.Loading
    val isAvailable = state.visualState == PlayerDanmakuControlState.VisualState.Available
    val containerColor = when {
        isAvailable && state.displayEnabled -> MaterialTheme.colorScheme.primary
        else -> Color.Black.copy(alpha = 0.55f)
    }
    val contentColor = when {
        isAvailable && state.displayEnabled -> MaterialTheme.colorScheme.onPrimary
        isAvailable -> Color.White
        else -> Color.White.copy(alpha = 0.55f)
    }

    IconButton(
        modifier = modifier
            .size(48.dp)
            .testTag(PlayerPlaybackSettingsTestTags.DANMAKU_TOGGLE)
            .semantics {
                contentDescription = state.contentDescription
                stateDescription = when {
                    isLoading -> "加载中"
                    !isAvailable -> "不可用"
                    state.displayEnabled -> "已开启"
                    else -> "已关闭"
                }
            },
        enabled = !isLoading,
        onClick = state.onClick,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp,
            )
        } else {
            Box(
                modifier = Modifier
                    .size(width = 30.dp, height = 24.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(containerColor)
                    .border(1.dp, contentColor.copy(alpha = 0.75f), RoundedCornerShape(7.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "弹",
                    color = contentColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (isAvailable && !state.displayEnabled) {
                        TextDecoration.LineThrough
                    } else {
                        TextDecoration.None
                    },
                )
            }
        }
    }
}

@Composable
internal fun OptionalPlayerDanmakuToggle(
    state: PlayerDanmakuControlState?,
    modifier: Modifier = Modifier,
) {
    state?.let { PlayerDanmakuToggle(state = it, modifier = modifier) }
}

/**
 * Material 3 adaptive shell for player-scoped settings.
 *
 * A compact portrait uses a modal bottom sheet. Landscape and expanded layouts retain the video
 * as context and slide a bounded settings surface in from the right.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AdaptivePlayerSettingsPanel(
    visible: Boolean,
    selectedSection: PlayerSettingsSection,
    onSectionSelected: (PlayerSettingsSection) -> Unit,
    onDismiss: () -> Unit,
    danmakuConfig: DanmakuDisplayConfig,
    danmakuSummary: String?,
    onDanmakuConfigChange: (DanmakuDisplayConfig) -> Unit,
    onResetDanmaku: () -> Unit,
    videoScaleType: Int,
    videoScaleOptions: List<Pair<Int, Int>>,
    onVideoScaleSelected: (Int) -> Unit,
) {
    if (!visible) return

    BackHandler(onBack = onDismiss)
    val configuration = LocalConfiguration.current
    val useSidePanel = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE ||
        configuration.screenWidthDp >= 600

    if (useSidePanel) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.52f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss,
                    ),
            ) {
                AnimatedVisibility(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    visible = true,
                    enter = slideInHorizontally { it },
                    exit = slideOutHorizontally { it },
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(0.45f)
                            .widthIn(max = 360.dp)
                            .testTag(PlayerPlaybackSettingsTestTags.SETTINGS_PANEL)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {},
                            ),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        tonalElevation = 6.dp,
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .navigationBarsPadding(),
                        ) {
                            PlayerSettingsContent(
                                initialSelectedSection = selectedSection,
                                onSectionSelected = onSectionSelected,
                                onDismiss = onDismiss,
                                danmakuConfig = danmakuConfig,
                                danmakuSummary = danmakuSummary,
                                onDanmakuConfigChange = onDanmakuConfigChange,
                                onResetDanmaku = onResetDanmaku,
                                videoScaleType = videoScaleType,
                                videoScaleOptions = videoScaleOptions,
                                onVideoScaleSelected = onVideoScaleSelected,
                            )
                        }
                    }
                }
            }
        }
    } else {
        ModalBottomSheet(
            modifier = Modifier.testTag(PlayerPlaybackSettingsTestTags.SETTINGS_PANEL),
            onDismissRequest = onDismiss,
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((configuration.screenHeightDp * 0.86f).dp),
            ) {
                PlayerSettingsContent(
                    initialSelectedSection = selectedSection,
                    onSectionSelected = onSectionSelected,
                    onDismiss = onDismiss,
                    danmakuConfig = danmakuConfig,
                    danmakuSummary = danmakuSummary,
                    onDanmakuConfigChange = onDanmakuConfigChange,
                    onResetDanmaku = onResetDanmaku,
                    videoScaleType = videoScaleType,
                    videoScaleOptions = videoScaleOptions,
                    onVideoScaleSelected = onVideoScaleSelected,
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.PlayerSettingsContent(
    initialSelectedSection: PlayerSettingsSection,
    onSectionSelected: (PlayerSettingsSection) -> Unit,
    onDismiss: () -> Unit,
    danmakuConfig: DanmakuDisplayConfig,
    danmakuSummary: String?,
    onDanmakuConfigChange: (DanmakuDisplayConfig) -> Unit,
    onResetDanmaku: () -> Unit,
    videoScaleType: Int,
    videoScaleOptions: List<Pair<Int, Int>>,
    onVideoScaleSelected: (Int) -> Unit,
) {
    // Keep the visual selection in the same retained subcomposition as the tab controls. The
    // caller still receives every change and provides the initial value the next time this panel
    // enters composition.
    var selectedSection by remember { mutableStateOf(initialSelectedSection) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, top = 12.dp, end = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "播放设置",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onDismiss) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "关闭播放设置",
            )
        }
    }
    PlayerSettingsSectionSelector(
        selected = selectedSection,
        onSelected = { section ->
            selectedSection = section
            onSectionSelected(section)
        },
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
    )
    HorizontalDivider(modifier = Modifier.padding(top = 8.dp))

    val danmakuScrollState = rememberScrollState()
    val videoScrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .weight(1f)
            .verticalScroll(
                when (selectedSection) {
                    PlayerSettingsSection.Danmaku -> danmakuScrollState
                    PlayerSettingsSection.Video -> videoScrollState
                },
            )
            .padding(vertical = 8.dp),
    ) {
        when (selectedSection) {
            PlayerSettingsSection.Danmaku -> DanmakuDisplaySettingsContent(
                config = danmakuConfig,
                onConfigChange = onDanmakuConfigChange,
                onReset = onResetDanmaku,
                matchSummary = danmakuSummary,
                modifier = Modifier.testTag(PlayerPlaybackSettingsTestTags.DANMAKU_SECTION),
            )

            PlayerSettingsSection.Video -> VideoScaleSettingsContent(
                selectedScaleType = videoScaleType,
                options = videoScaleOptions,
                onScaleSelected = onVideoScaleSelected,
                modifier = Modifier.testTag(PlayerPlaybackSettingsTestTags.VIDEO_SECTION),
            )
        }
    }
}

@Composable
private fun PlayerSettingsSectionSelector(
    selected: PlayerSettingsSection,
    onSelected: (PlayerSettingsSection) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        shape = RoundedCornerShape(14.dp),
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            PlayerSettingsSection.entries.forEach { section ->
                val isSelected = section == selected
                val contentColor = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                Color.Transparent
                            },
                        )
                        .testTag(
                            when (section) {
                                PlayerSettingsSection.Danmaku ->
                                    PlayerPlaybackSettingsTestTags.DANMAKU_SECTION_TAB

                                PlayerSettingsSection.Video ->
                                    PlayerPlaybackSettingsTestTags.VIDEO_SECTION_TAB
                            },
                        )
                        .selectable(
                            selected = isSelected,
                            role = Role.Tab,
                            onClick = { onSelected(section) },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = when (section) {
                            PlayerSettingsSection.Danmaku -> "弹幕"
                            PlayerSettingsSection.Video -> "画面"
                        },
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = contentColor,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

@Composable
internal fun DanmakuDisplaySettingsContent(
    config: DanmakuDisplayConfig,
    onConfigChange: (DanmakuDisplayConfig) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
    matchSummary: String? = null,
) {
    var confirmReset by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        ListItem(
            headlineContent = { Text("显示弹幕") },
            supportingContent = {
                Text(matchSummary ?: "在播放画面上显示已匹配的弹幕")
            },
            trailingContent = {
                Switch(
                    checked = config.enabled,
                    onCheckedChange = { onConfigChange(config.copy(enabled = it)) },
                )
            },
            modifier = Modifier.clickable {
                onConfigChange(config.copy(enabled = !config.enabled))
            },
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent,
            ),
        )
        SettingsGroupTitle("显示类型")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DanmakuTypeChip(
                label = "滚动",
                selected = config.showScroll,
                onClick = { onConfigChange(config.copy(showScroll = !config.showScroll)) },
            )
            DanmakuTypeChip(
                label = "顶部",
                selected = config.showTop,
                onClick = { onConfigChange(config.copy(showTop = !config.showTop)) },
            )
            DanmakuTypeChip(
                label = "底部",
                selected = config.showBottom,
                onClick = { onConfigChange(config.copy(showBottom = !config.showBottom)) },
            )
        }

        SettingsGroupTitle("样式")
        DanmakuValueSlider(
            title = "字体大小",
            valueLabel = "${config.fontSizeSp.roundToInt()} sp",
            value = config.fontSizeSp,
            valueRange = DanmakuDisplayConfig.FONT_SIZE_SP_RANGE,
            steps = 23,
            onValueChange = {
                onConfigChange(config.copy(fontSizeSp = it).normalized())
            },
            modifier = Modifier.testTag(PlayerPlaybackSettingsTestTags.FONT_SIZE),
            sliderTestTag = "${PlayerPlaybackSettingsTestTags.FONT_SIZE}_slider",
        )
        DanmakuValueSlider(
            title = "行高",
            valueLabel = formatFactor(config.lineHeightFactor),
            value = config.lineHeightFactor,
            valueRange = DanmakuDisplayConfig.LINE_HEIGHT_FACTOR_RANGE,
            steps = 9,
            onValueChange = {
                onConfigChange(config.copy(lineHeightFactor = it).normalized())
            },
            modifier = Modifier.testTag(PlayerPlaybackSettingsTestTags.LINE_HEIGHT),
            sliderTestTag = "${PlayerPlaybackSettingsTestTags.LINE_HEIGHT}_slider",
        )
        DanmakuValueSlider(
            title = "滚动速度",
            valueLabel = "${formatFactor(config.scrollSpeed)}x",
            value = config.scrollSpeed,
            valueRange = DanmakuDisplayConfig.SCROLL_SPEED_RANGE,
            steps = 5,
            onValueChange = {
                onConfigChange(config.copy(scrollSpeed = it).normalized())
            },
            modifier = Modifier.testTag(PlayerPlaybackSettingsTestTags.SCROLL_SPEED),
            sliderTestTag = "${PlayerPlaybackSettingsTestTags.SCROLL_SPEED}_slider",
        )

        SettingsGroupTitle("时间校准")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .testTag(PlayerPlaybackSettingsTestTags.TIME_OFFSET)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 8.dp),
                onClick = {
                    onConfigChange(
                        config.copy(timeOffsetMillis = config.timeOffsetMillis - 500L),
                    )
                },
            ) {
                Text("−0.5s")
            }
            Text(
                text = formatOffset(config.timeOffsetMillis),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
            )
            OutlinedButton(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 8.dp),
                onClick = {
                    onConfigChange(
                        config.copy(timeOffsetMillis = config.timeOffsetMillis + 500L),
                    )
                },
            ) {
                Text("+0.5s")
            }
        }
        TextButton(
            modifier = Modifier
                .align(Alignment.End)
                .padding(horizontal = 12.dp)
                .testTag(PlayerPlaybackSettingsTestTags.RESET),
            onClick = { confirmReset = true },
        ) {
            Text("恢复默认")
        }
        Spacer(Modifier.height(12.dp))
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("恢复弹幕默认设置？") },
            text = { Text("将恢复显示类型、字体大小、行高、滚动速度和时间偏移。") },
            confirmButton = {
                TextButton(
                    modifier = Modifier.testTag(PlayerPlaybackSettingsTestTags.RESET_CONFIRM),
                    onClick = {
                        confirmReset = false
                        onReset()
                    },
                ) {
                    Text("恢复")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmReset = false }) {
                    Text("取消")
                }
            },
        )
    }
}

@Composable
private fun DanmakuTypeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
    )
}

@Composable
private fun SettingsGroupTitle(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(start = 16.dp, top = 20.dp, end = 16.dp, bottom = 4.dp),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun DanmakuValueSlider(
    title: String,
    valueLabel: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    sliderTestTag: String? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics { stateDescription = valueLabel }
            .padding(horizontal = 16.dp, vertical = 6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = valueLabel,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge,
            )
        }
        Slider(
            modifier = if (sliderTestTag == null) {
                Modifier
            } else {
                Modifier.testTag(sliderTestTag)
            },
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
        )
    }
}

@Composable
private fun VideoScaleSettingsContent(
    selectedScaleType: Int,
    options: List<Pair<Int, Int>>,
    onScaleSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        options.forEach { (value, labelRes) ->
            val selected = value == selectedScaleType
            ListItem(
                headlineContent = { Text(stringResource(labelRes)) },
                supportingContent = if (selected) {
                    { Text("当前画面模式") }
                } else {
                    null
                },
                trailingContent = {
                    RadioButton(
                        selected = selected,
                        onClick = { onScaleSelected(value) },
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(PlayerPlaybackSettingsTestTags.videoScale(value))
                    .clickable(role = Role.RadioButton) { onScaleSelected(value) },
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent,
                ),
            )
        }
        Spacer(Modifier.height(16.dp))
    }
}

private fun formatFactor(value: Float): String = String.format(Locale.US, "%.1f", value)

private fun formatOffset(valueMillis: Long): String {
    val seconds = valueMillis / 1_000f
    return when {
        valueMillis == 0L -> "0.0s"
        valueMillis > 0L -> "+${String.format(Locale.US, "%.1f", seconds)}s"
        else -> "${String.format(Locale.US, "%.1f", seconds)}s"
    }
}
