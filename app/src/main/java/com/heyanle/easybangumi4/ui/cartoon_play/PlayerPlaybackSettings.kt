package com.heyanle.easybangumi4.ui.cartoon_play

import android.app.Activity
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heyanle.easybangumi4.BuildConfig
import com.heyanle.easybangumi4.danmaku.DANMAKU_AREA_RATIO_TIERS
import com.heyanle.easybangumi4.danmaku.DANMAKU_SCROLL_SPEED_TIERS
import com.heyanle.easybangumi4.danmaku.DanmakuDisplayConfig
import com.heyanle.easybangumi4.danmaku.DanmakuPlaybackState
import com.heyanle.easybangumi4.danmaku.DanmakuPlaybackStatus
import com.heyanle.easybangumi4.danmaku.danmakuAreaRatioLabel
import com.heyanle.easybangumi4.danmaku.danmakuOpacityLabel
import com.heyanle.easybangumi4.danmaku.danmakuScrollSpeedLabel
import com.heyanle.easybangumi4.player.mpv.MpvAnime4KStatus
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.easybangumi4.ui.common.PlayerCutoutInsets
import com.heyanle.easybangumi4.ui.common.TabIndicator
import com.heyanle.inject.core.Inject
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
    val onLongClick: () -> Unit = {},
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
        onLongClick = onOpenSourceSettings,
    )

    DanmakuPlaybackStatus.MatchingBangumi,
    DanmakuPlaybackStatus.MatchingEpisode,
    DanmakuPlaybackStatus.LoadingComments,
    -> PlayerDanmakuControlState(
        visualState = PlayerDanmakuControlState.VisualState.Loading,
        displayEnabled = false,
        contentDescription = "弹幕加载中，长按重新匹配",
        onClick = {},
        onLongClick = onManualMatch,
    )

    is DanmakuPlaybackStatus.Matched -> PlayerDanmakuControlState(
        visualState = PlayerDanmakuControlState.VisualState.Available,
        displayEnabled = displayEnabled,
        contentDescription = if (displayEnabled) {
            "关闭弹幕，长按重新匹配"
        } else {
            "开启弹幕，长按重新匹配"
        },
        onClick = { onToggleDisplay(!displayEnabled) },
        onLongClick = onManualMatch,
    )

    is DanmakuPlaybackStatus.Empty -> PlayerDanmakuControlState(
        visualState = PlayerDanmakuControlState.VisualState.Unavailable,
        displayEnabled = false,
        contentDescription = "当前选集暂无弹幕，点击更换番剧和选集",
        onClick = onManualMatch,
        onLongClick = onManualMatch,
    )

    is DanmakuPlaybackStatus.Unmatched -> PlayerDanmakuControlState(
        visualState = PlayerDanmakuControlState.VisualState.Unavailable,
        displayEnabled = false,
        contentDescription = "尚未匹配弹幕，点击匹配番剧和选集",
        onClick = onManualMatch,
        onLongClick = onManualMatch,
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
            onLongClick = if (sourceNeedsConfiguration) onOpenSourceSettings else onManualMatch,
        )
    }
}

internal enum class PlayerSettingsSection {
    Danmaku,
    Video,
    Controls,
    Anime4K,
    AdBlock,
}

internal object PlayerPlaybackSettingsTestTags {
    const val DANMAKU_TOGGLE = "player_danmaku_toggle"
    const val SETTINGS_PANEL = "player_settings_panel"
    const val DANMAKU_SECTION_TAB = "player_settings_tab_danmaku"
    const val VIDEO_SECTION_TAB = "player_settings_tab_video"
    const val CONTROLS_SECTION_TAB = "player_settings_tab_controls"
    const val ANIME4K_SECTION_TAB = "player_settings_tab_anime4k"
    const val ADBLOCK_SECTION_TAB = "player_settings_tab_adblock"
    const val OPEN_DANMAKU_SETTINGS = "player_open_danmaku_settings"
    const val DANMAKU_SECTION = "player_settings_danmaku"
    const val VIDEO_SECTION = "player_settings_video"
    const val CONTROLS_SECTION = "player_settings_controls"
    const val ANIME4K_SECTION = "player_settings_anime4k"
    const val ADBLOCK_SECTION = "player_settings_adblock"
    const val RESET = "player_danmaku_reset"
    const val RESET_CONFIRM = "player_danmaku_reset_confirm"
    const val FONT_SIZE = "player_danmaku_font_size"
    const val LINE_HEIGHT = "player_danmaku_line_height"
    const val SCROLL_SPEED = "player_danmaku_scroll_speed"
    const val DENSITY = "player_danmaku_density"
    const val MERGE_REPEAT = "player_danmaku_merge_repeat"
    const val OPACITY = "player_danmaku_opacity"
    const val AREA = "player_danmaku_area"
    const val TIME_OFFSET = "player_danmaku_time_offset"
    fun videoScale(value: Int) = "player_video_scale_$value"
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun PlayerDanmakuToggle(
    state: PlayerDanmakuControlState,
    modifier: Modifier = Modifier,
    buttonSize: Dp = 48.dp,
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

    Box(
        modifier = modifier
            .size(buttonSize)
            .testTag(PlayerPlaybackSettingsTestTags.DANMAKU_TOGGLE)
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = state.onClick,
                onLongClickLabel = "强制匹配弹幕",
                onLongClick = state.onLongClick,
            )
            .semantics {
                contentDescription = state.contentDescription
                stateDescription = when {
                    isLoading -> "加载中"
                    !isAvailable -> "不可用"
                    state.displayEnabled -> "已开启"
                    else -> "已关闭"
                }
            },
        contentAlignment = Alignment.Center,
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
    buttonSize: Dp = 48.dp,
) {
    state?.let {
        PlayerDanmakuToggle(state = it, modifier = modifier, buttonSize = buttonSize)
    }
}

/**
 * Shared fullscreen side-panel shell used by player-scoped overlays.
 *
 * Keeping the scrim, motion and safe-area behavior here prevents episode selection and playback
 * settings from drifting into two visually similar but behaviorally different implementations.
 */
@Composable
internal fun FullscreenPlayerSidePanel(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    panelModifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val activity = LocalContext.current as Activity
    val preferences: SettingPreferences by Inject.injectLazy()
    val cutoutMode by preferences.playerCutoutAvoidanceMode.flow().collectAsState(
        preferences.playerCutoutAvoidanceMode.get(),
    )
    val cutoutManualPaddingDp by preferences.playerCutoutManualPaddingDp.flow().collectAsState(
        preferences.playerCutoutManualPaddingDp.get(),
    )
    val cutoutInsets = PlayerCutoutInsets.rememberResolved(
        activity = activity,
        mode = cutoutMode,
        manualPaddingDp = cutoutManualPaddingDp,
    )
    BackHandler(enabled = visible, onBack = onDismiss)
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(durationMillis = 180)),
            exit = fadeOut(animationSpec = tween(durationMillis = 180)),
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
            )
        }
        AnimatedVisibility(
            modifier = Modifier.align(Alignment.CenterEnd),
            visible = visible,
            enter = slideInHorizontally(
                animationSpec = tween(durationMillis = 280),
                initialOffsetX = { it },
            ),
            exit = slideOutHorizontally(
                animationSpec = tween(durationMillis = 240),
                targetOffsetX = { it },
            ),
        ) {
            Surface(
                modifier = panelModifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.45f)
                    .widthIn(max = 360.dp)
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
                        // 面板底色可以延伸到挖孔区，但内部所有操作必须保持可点击。
                        .padding(
                            end = cutoutInsets.paddingFor(PlayerCutoutInsets.Side.RIGHT),
                        )
                        .navigationBarsPadding(),
                    content = content,
                )
            }
        }
    }
}

/**
 * Material 3 adaptive shell for player-scoped settings.
 *
 * Portrait always uses a modal bottom sheet so the panel follows the control entry vertically.
 * Landscape retains the video as context and slides a bounded settings surface in from the right.
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
    isMpvEngine: Boolean = false,
    mpvAnime4kEnabled: Boolean = false,
    mpvAnime4kPreset: SettingPreferences.MpvAnime4KPreset = SettingPreferences.MpvAnime4KPreset.FAST,
    mpvAnime4kStatus: MpvAnime4KStatus = MpvAnime4KStatus(),
    onMpvAnime4kEnabledChange: (Boolean) -> Unit = {},
    onMpvAnime4kPresetChange: (SettingPreferences.MpvAnime4KPreset) -> Unit = {},
    isExoPlayerEngine: Boolean = true,
    exoAdAudioProbeEnabled: Boolean = false,
    exoAdAudioProbeRulesUrl: String = "",
    onExoAdAudioProbeEnabledChange: (Boolean) -> Unit = {},
    onExoAdAudioProbeRulesUrlChange: (String) -> Unit = {},
) {
    val configuration = LocalConfiguration.current
    val useSidePanel = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (useSidePanel) {
        FullscreenPlayerSidePanel(
            visible = visible,
            onDismiss = onDismiss,
            panelModifier = Modifier.testTag(PlayerPlaybackSettingsTestTags.SETTINGS_PANEL),
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
                isMpvEngine = isMpvEngine,
                mpvAnime4kEnabled = mpvAnime4kEnabled,
                mpvAnime4kPreset = mpvAnime4kPreset,
                mpvAnime4kStatus = mpvAnime4kStatus,
                onMpvAnime4kEnabledChange = onMpvAnime4kEnabledChange,
                onMpvAnime4kPresetChange = onMpvAnime4kPresetChange,
                isExoPlayerEngine = isExoPlayerEngine,
                exoAdAudioProbeEnabled = exoAdAudioProbeEnabled,
                exoAdAudioProbeRulesUrl = exoAdAudioProbeRulesUrl,
                onExoAdAudioProbeEnabledChange = onExoAdAudioProbeEnabledChange,
                onExoAdAudioProbeRulesUrlChange = onExoAdAudioProbeRulesUrlChange,
            )
        }
    } else if (visible) {
        BackHandler(onBack = onDismiss)
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
                    isMpvEngine = isMpvEngine,
                    mpvAnime4kEnabled = mpvAnime4kEnabled,
                    mpvAnime4kPreset = mpvAnime4kPreset,
                    mpvAnime4kStatus = mpvAnime4kStatus,
                    onMpvAnime4kEnabledChange = onMpvAnime4kEnabledChange,
                    onMpvAnime4kPresetChange = onMpvAnime4kPresetChange,
                    isExoPlayerEngine = isExoPlayerEngine,
                    exoAdAudioProbeEnabled = exoAdAudioProbeEnabled,
                    exoAdAudioProbeRulesUrl = exoAdAudioProbeRulesUrl,
                    onExoAdAudioProbeEnabledChange = onExoAdAudioProbeEnabledChange,
                    onExoAdAudioProbeRulesUrlChange = onExoAdAudioProbeRulesUrlChange,
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
    isMpvEngine: Boolean,
    mpvAnime4kEnabled: Boolean,
    mpvAnime4kPreset: SettingPreferences.MpvAnime4KPreset,
    mpvAnime4kStatus: MpvAnime4KStatus,
    onMpvAnime4kEnabledChange: (Boolean) -> Unit,
    onMpvAnime4kPresetChange: (SettingPreferences.MpvAnime4KPreset) -> Unit,
    isExoPlayerEngine: Boolean,
    exoAdAudioProbeEnabled: Boolean,
    exoAdAudioProbeRulesUrl: String,
    onExoAdAudioProbeEnabledChange: (Boolean) -> Unit,
    onExoAdAudioProbeRulesUrlChange: (String) -> Unit,
) {
    // Keep the visual selection in the same retained subcomposition as the tab controls. The
    // caller still receives every change and provides the initial value the next time this panel
    // enters composition.
    val sections = buildList {
        add(PlayerSettingsSection.Danmaku)
        add(PlayerSettingsSection.Video)
        add(PlayerSettingsSection.Controls)
        if (BuildConfig.HAS_MPV && isMpvEngine) add(PlayerSettingsSection.Anime4K)
        if (isExoPlayerEngine) add(PlayerSettingsSection.AdBlock)
    }
    var selectedSection by remember { mutableStateOf(initialSelectedSection) }
    if (selectedSection !in sections) selectedSection = sections.first()
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
        sections = sections,
        selected = selectedSection,
        onSelected = { section ->
            selectedSection = section
            onSectionSelected(section)
        },
        modifier = Modifier.padding(horizontal = 20.dp),
    )
    HorizontalDivider()

    val danmakuScrollState = rememberScrollState()
    val videoScrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .weight(1f)
            .verticalScroll(
                when (selectedSection) {
                    PlayerSettingsSection.Danmaku -> danmakuScrollState
                    PlayerSettingsSection.Video -> videoScrollState
                    PlayerSettingsSection.Controls -> videoScrollState
                    PlayerSettingsSection.Anime4K,
                    PlayerSettingsSection.AdBlock,
                    -> videoScrollState
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
                modifier = Modifier.testTag(PlayerPlaybackSettingsTestTags.VIDEO_SECTION),
                selectedScaleType = videoScaleType,
                options = videoScaleOptions,
                onScaleSelected = onVideoScaleSelected,
            )

            PlayerSettingsSection.Controls -> PlayerControlSettingsContent(
                modifier = Modifier.testTag(PlayerPlaybackSettingsTestTags.CONTROLS_SECTION),
            )

            PlayerSettingsSection.Anime4K -> {
                if (BuildConfig.HAS_MPV) {
                    MpvAnime4KSettingsContent(
                        enabled = mpvAnime4kEnabled,
                        preset = mpvAnime4kPreset,
                        status = mpvAnime4kStatus,
                        onEnabledChange = onMpvAnime4kEnabledChange,
                        onPresetChange = onMpvAnime4kPresetChange,
                        modifier = Modifier.testTag(PlayerPlaybackSettingsTestTags.ANIME4K_SECTION),
                    )
                }
            }

            PlayerSettingsSection.AdBlock -> ExoAdAudioProbeSettingsContent(
                enabled = exoAdAudioProbeEnabled,
                rulesUrl = exoAdAudioProbeRulesUrl,
                onEnabledChange = onExoAdAudioProbeEnabledChange,
                onRulesUrlChange = onExoAdAudioProbeRulesUrlChange,
                modifier = Modifier.testTag(PlayerPlaybackSettingsTestTags.ADBLOCK_SECTION),
            )
        }
    }
}

@Composable
private fun PlayerSettingsSectionSelector(
    sections: List<PlayerSettingsSection>,
    selected: PlayerSettingsSection,
    onSelected: (PlayerSettingsSection) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedIndex = sections.indexOf(selected)
    ScrollableTabRow(
        modifier = modifier.fillMaxWidth(),
        selectedTabIndex = selectedIndex,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.primary,
        edgePadding = 0.dp,
        indicator = { positions ->
            if (selectedIndex in positions.indices) {
                TabIndicator(currentTabPosition = positions[selectedIndex])
            }
        },
        divider = {},
    ) {
        sections.forEach { section ->
            val isSelected = section == selected
            Tab(
                selected = isSelected,
                onClick = { onSelected(section) },
                modifier = Modifier
                    .testTag(
                        when (section) {
                            PlayerSettingsSection.Danmaku ->
                                PlayerPlaybackSettingsTestTags.DANMAKU_SECTION_TAB

                            PlayerSettingsSection.Video ->
                                PlayerPlaybackSettingsTestTags.VIDEO_SECTION_TAB

                            PlayerSettingsSection.Controls ->
                                PlayerPlaybackSettingsTestTags.CONTROLS_SECTION_TAB

                            PlayerSettingsSection.Anime4K ->
                                PlayerPlaybackSettingsTestTags.ANIME4K_SECTION_TAB

                            PlayerSettingsSection.AdBlock ->
                                PlayerPlaybackSettingsTestTags.ADBLOCK_SECTION_TAB
                        },
                    )
                    .semantics {
                        stateDescription = if (isSelected) "已选择" else "未选择"
                    },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                text = {
                    Text(
                        text = when (section) {
                            PlayerSettingsSection.Danmaku -> "弹幕"
                            PlayerSettingsSection.Video -> "画面"
                            PlayerSettingsSection.Controls -> "控制"
                            PlayerSettingsSection.Anime4K -> {
                                if (BuildConfig.HAS_MPV) "Anime4K" else ""
                            }
                            PlayerSettingsSection.AdBlock -> "去广告"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (isSelected) {
                            FontWeight.SemiBold
                        } else {
                            FontWeight.Medium
                        },
                    )
                },
            )
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
    var helpTopic by remember { mutableStateOf<DanmakuSettingHelp?>(null) }

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

        SettingsGroupTitle("显示区域与样式")
        DanmakuValueSlider(
            title = "显示区域",
            valueLabel = danmakuAreaRatioLabel(config.areaRatio),
            value = config.areaRatio,
            valueRange = DANMAKU_AREA_RATIO_TIERS.first()..DANMAKU_AREA_RATIO_TIERS.last(),
            steps = DANMAKU_AREA_RATIO_TIERS.size - 2,
            onValueChange = {
                onConfigChange(config.copy(areaRatio = it).normalized())
            },
            modifier = Modifier.testTag(PlayerPlaybackSettingsTestTags.AREA),
            sliderTestTag = "${PlayerPlaybackSettingsTestTags.AREA}_slider",
        )
        DanmakuValueSlider(
            title = "不透明度",
            valueLabel = danmakuOpacityLabel(config.opacity),
            value = config.opacity,
            valueRange = DanmakuDisplayConfig.OPACITY_RANGE,
            steps = 0,
            onValueChange = {
                onConfigChange(config.copy(opacity = it).normalized())
            },
            modifier = Modifier.testTag(PlayerPlaybackSettingsTestTags.OPACITY),
            sliderTestTag = "${PlayerPlaybackSettingsTestTags.OPACITY}_slider",
        )
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
        // 速度档位不等距，滑条改为"档位索引"式：刻度均匀落在索引上，回写时映射回档位值。
        val speedIndex = DANMAKU_SCROLL_SPEED_TIERS.indexOf(config.scrollSpeed)
            .takeIf { it >= 0 }
            ?: DANMAKU_SCROLL_SPEED_TIERS.indexOf(DanmakuDisplayConfig.DEFAULT_SCROLL_SPEED)
        DanmakuValueSlider(
            title = "滚动速度",
            valueLabel = danmakuScrollSpeedLabel(config.scrollSpeed),
            value = speedIndex.toFloat(),
            valueRange = 0f..DANMAKU_SCROLL_SPEED_TIERS.lastIndex.toFloat(),
            steps = DANMAKU_SCROLL_SPEED_TIERS.size - 2,
            onValueChange = {
                val tier = DANMAKU_SCROLL_SPEED_TIERS[
                    it.roundToInt().coerceIn(DANMAKU_SCROLL_SPEED_TIERS.indices),
                ]
                onConfigChange(config.copy(scrollSpeed = tier))
            },
            modifier = Modifier.testTag(PlayerPlaybackSettingsTestTags.SCROLL_SPEED),
            sliderTestTag = "${PlayerPlaybackSettingsTestTags.SCROLL_SPEED}_slider",
        )
        DanmakuValueSlider(
            title = "弹幕数量",
            valueLabel = "${(config.densityRatio * 100).roundToInt()}%",
            value = config.densityRatio,
            valueRange = DanmakuDisplayConfig.DENSITY_RATIO_RANGE,
            steps = 8,
            onValueChange = {
                onConfigChange(config.copy(densityRatio = it).normalized())
            },
            modifier = Modifier.testTag(PlayerPlaybackSettingsTestTags.DENSITY),
            sliderTestTag = "${PlayerPlaybackSettingsTestTags.DENSITY}_slider",
            onHelpClick = { helpTopic = DanmakuSettingHelp.Density },
        )
        DanmakuValueSlider(
            title = "复读合并",
            valueLabel = if (config.mergeRepeatWindowMillis <= 0L) {
                "不合并"
            } else {
                "${config.mergeRepeatWindowMillis / 1000}s"
            },
            value = config.mergeRepeatWindowMillis.toFloat(),
            valueRange = DanmakuDisplayConfig.MERGE_REPEAT_WINDOW_RANGE.start.toFloat()
                ..DanmakuDisplayConfig.MERGE_REPEAT_WINDOW_RANGE.endInclusive.toFloat(),
            steps = 4,
            onValueChange = {
                onConfigChange(
                    config.copy(
                        mergeRepeatWindowMillis = (it / 1000f).roundToInt() * 1000L,
                    ),
                )
            },
            modifier = Modifier.testTag(PlayerPlaybackSettingsTestTags.MERGE_REPEAT),
            sliderTestTag = "${PlayerPlaybackSettingsTestTags.MERGE_REPEAT}_slider",
            onHelpClick = { helpTopic = DanmakuSettingHelp.MergeRepeat },
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
            text = { Text("将恢复显示类型、显示区域、不透明度、字体大小、行高、滚动速度、数量、复读合并和时间偏移。") },
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
    helpTopic?.let { topic ->
        AlertDialog(
            onDismissRequest = { helpTopic = null },
            title = { Text(topic.title) },
            text = { Text(topic.description) },
            confirmButton = {
                TextButton(onClick = { helpTopic = null }) {
                    Text("知道了")
                }
            },
        )
    }
}

private enum class DanmakuSettingHelp(
    val title: String,
    val description: String,
) {
    Density(
        title = "弹幕数量",
        description = "控制实际显示的弹幕比例。降低比例会均匀减少同一时间段内的弹幕，适合弹幕过密或性能有限的设备。",
    ),
    MergeRepeat(
        title = "复读合并",
        description = "在选定时间范围内合并内容相同的弹幕。范围越大，重复弹幕越少；选择“不合并”会保留全部重复内容。",
    ),
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
internal fun SettingsGroupTitle(
    text: String,
    onHelpClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 20.dp, end = 16.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
        )
        if (onHelpClick != null) {
            Spacer(Modifier.widthIn(min = 4.dp))
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clickable(
                        role = Role.Button,
                        onClickLabel = "查看${text}说明",
                        onClick = onHelpClick,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                    contentDescription = "$text 说明",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
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
    onHelpClick: (() -> Unit)? = null,
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
                style = MaterialTheme.typography.bodyLarge,
            )
            if (onHelpClick != null) {
                Spacer(Modifier.widthIn(min = 4.dp))
                Box(
                    modifier = Modifier
                        // 与正文行高相同，不使用默认 48dp IconButton，避免标题行被抬高。
                        .size(24.dp)
                        .clickable(
                            role = Role.Button,
                            onClickLabel = "查看${title}说明",
                            onClick = onHelpClick,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                        contentDescription = "$title 说明",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.weight(1f))
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
internal fun VideoScaleSettingsContent(
    selectedScaleType: Int,
    options: List<Pair<Int, Int>>,
    onScaleSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SettingsGroupTitle("画面比例")
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

@Composable
private fun PlayerControlSettingsContent(
    modifier: Modifier = Modifier,
) {
    val preferences: SettingPreferences by Inject.injectLazy()
    val controlPosition by preferences.fullscreenControlPosition.flow().collectAsState(
        preferences.fullscreenControlPosition.get(),
    )
    val cutoutMode by preferences.playerCutoutAvoidanceMode.flow().collectAsState(
        preferences.playerCutoutAvoidanceMode.get(),
    )
    val manualPadding by preferences.playerCutoutManualPaddingDp.flow().collectAsState(
        preferences.playerCutoutManualPaddingDp.get(),
    )
    val seekWidthMs by preferences.playerSeekFullWidthTimeMS.flow().collectAsState(
        preferences.playerSeekFullWidthTimeMS.get(),
    )
    val fastWeight by preferences.fastWeight.flow().collectAsState(preferences.fastWeight.get())
    val fastTopWeight by preferences.fastWeightTopMolecule.flow().collectAsState(
        preferences.fastWeightTopMolecule.get(),
    )
    val fastSeconds by preferences.fastSecond.flow().collectAsState(preferences.fastSecond.get())
    val fastTopSeconds by preferences.fastTopSecond.flow().collectAsState(
        preferences.fastTopSecond.get(),
    )
    var helpTopic by remember { mutableStateOf<PlayerControlSettingHelp?>(null) }

    Column(modifier = modifier.fillMaxWidth()) {
        SettingsGroupTitle(
            text = "全屏侧边按钮",
            onHelpClick = { helpTopic = PlayerControlSettingHelp.FullscreenSideButtons },
        )
        listOf(
            SettingPreferences.FullscreenControlPosition.AUTO to ("自动" to "跟随唤出控制器时的点击侧"),
            SettingPreferences.FullscreenControlPosition.LEFT to ("固定左侧" to "侧边按钮始终停靠左边"),
            SettingPreferences.FullscreenControlPosition.RIGHT to ("固定右侧" to "侧边按钮始终停靠右边"),
        ).forEach { (value, copy) ->
            val selected = controlPosition == value
            ListItem(
                headlineContent = { Text(copy.first) },
                supportingContent = { Text(copy.second) },
                trailingContent = {
                    RadioButton(selected = selected, onClick = { preferences.fullscreenControlPosition.set(value) })
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.RadioButton) { preferences.fullscreenControlPosition.set(value) },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            )
        }

        SettingsGroupTitle("刘海避让")
        val supportedCutoutMode =
            SettingPreferences.PlayerCutoutAvoidanceMode.normalizeForSdk(cutoutMode)
        SettingPreferences.PlayerCutoutAvoidanceMode.selectableValues().forEach { value ->
            val copy = when (value) {
                SettingPreferences.PlayerCutoutAvoidanceMode.AUTO ->
                    "自动" to "识别刘海位置并避让"
                SettingPreferences.PlayerCutoutAvoidanceMode.DISABLED ->
                    "关闭" to "控制器允许进入刘海区域"
                SettingPreferences.PlayerCutoutAvoidanceMode.MANUAL ->
                    "手动" to "两侧使用自定义安全距离"
            }
            val selected = supportedCutoutMode == value
            ListItem(
                headlineContent = { Text(copy.first) },
                supportingContent = { Text(copy.second) },
                trailingContent = {
                    RadioButton(selected = selected, onClick = { preferences.playerCutoutAvoidanceMode.set(value) })
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.RadioButton) { preferences.playerCutoutAvoidanceMode.set(value) },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            )
        }
        if (supportedCutoutMode == SettingPreferences.PlayerCutoutAvoidanceMode.MANUAL) {
            PlayerControlSlider(
                title = "安全距离",
                valueText = "${manualPadding.coerceIn(0, 96)} dp",
                value = manualPadding.coerceIn(0, 96).toFloat(),
                valueRange = 0f..96f,
                steps = 23,
                onValueChange = { preferences.playerCutoutManualPaddingDp.set((it / 4f).roundToInt() * 4) },
            )
        }

        SettingsGroupTitle("滑动手势")
        val seekSeconds = (seekWidthMs / 1_000L).coerceIn(60L, 1_800L)
        PlayerControlSlider(
            title = "横滑满屏时长",
            valueText = if (seekSeconds >= 60) "${seekSeconds / 60} 分钟" else "$seekSeconds 秒",
            value = seekSeconds.toFloat(),
            valueRange = 60f..1_800f,
            steps = 28,
            onValueChange = {
                val seconds = (it / 60f).roundToInt().coerceIn(1, 30) * 60L
                preferences.playerSeekFullWidthTimeMS.set(seconds * 1_000L)
            },
            onHelpClick = { helpTopic = PlayerControlSettingHelp.HorizontalSeekDuration },
        )

        SettingsGroupTitle("双击快进快退")
        val doubleTapEnabled = fastWeight > 0
        ListItem(
            headlineContent = { Text("启用双击手势") },
            supportingContent = { Text("点击画面两侧快速快退或快进") },
            trailingContent = {
                Switch(
                    checked = doubleTapEnabled,
                    onCheckedChange = {
                        preferences.fastWeight.set(if (it) kotlin.math.abs(fastWeight).coerceIn(2, 6) else -kotlin.math.abs(fastWeight).coerceIn(2, 6))
                    },
                )
            },
            modifier = Modifier.fillMaxWidth().clickable {
                preferences.fastWeight.set(if (doubleTapEnabled) -kotlin.math.abs(fastWeight).coerceIn(2, 6) else kotlin.math.abs(fastWeight).coerceIn(2, 6))
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
        if (doubleTapEnabled) {
            val widthOptions = preferences.fastWeightSelection
            val widthIndex = widthOptions.indexOf(kotlin.math.abs(fastWeight)).coerceAtLeast(0)
            PlayerControlSlider(
                title = "两侧响应宽度",
                valueText = "各占屏幕 1/${widthOptions[widthIndex]}",
                value = widthIndex.toFloat(),
                valueRange = 0f..widthOptions.lastIndex.toFloat(),
                steps = (widthOptions.size - 2).coerceAtLeast(0),
                onValueChange = { preferences.fastWeight.set(widthOptions[it.roundToInt().coerceIn(widthOptions.indices)]) },
            )
            PlayerControlSlider(
                title = "两侧快进快退时长",
                valueText = "${fastSeconds.coerceIn(5, 60)} 秒",
                value = fastSeconds.coerceIn(5, 60).toFloat(),
                valueRange = 5f..60f,
                steps = 10,
                onValueChange = { preferences.fastSecond.set((it / 5f).roundToInt().coerceIn(1, 12) * 5) },
            )
            val topEnabled = fastTopWeight > 0
            ListItem(
                headlineContent = { Text("顶部独立区域") },
                supportingContent = { Text("顶部区域可使用不同的跳转时长") },
                trailingContent = {
                    Switch(
                        checked = topEnabled,
                        onCheckedChange = {
                            val value = kotlin.math.abs(fastTopWeight).coerceIn(1, 5)
                            preferences.fastWeightTopMolecule.set(if (it) value else -value)
                        },
                    )
                },
                modifier = Modifier.fillMaxWidth().clickable {
                    val value = kotlin.math.abs(fastTopWeight).coerceIn(1, 5)
                    preferences.fastWeightTopMolecule.set(if (topEnabled) -value else value)
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            )
            if (topEnabled) {
                PlayerControlSlider(
                    title = "顶部区域高度",
                    valueText = "屏幕高度的 ${fastTopWeight.coerceIn(1, 5)}/6",
                    value = fastTopWeight.coerceIn(1, 5).toFloat(),
                    valueRange = 1f..5f,
                    steps = 3,
                    onValueChange = { preferences.fastWeightTopMolecule.set(it.roundToInt().coerceIn(1, 5)) },
                )
                PlayerControlSlider(
                    title = "顶部快进快退时长",
                    valueText = "${fastTopSeconds.coerceIn(5, 120)} 秒",
                    value = fastTopSeconds.coerceIn(5, 120).toFloat(),
                    valueRange = 5f..120f,
                    steps = 22,
                    onValueChange = { preferences.fastTopSecond.set((it / 5f).roundToInt().coerceIn(1, 24) * 5) },
                )
            }
            PlayerControlDoubleTapPreview(
                fastWeight = widthOptions[widthIndex],
                fastTopWeight = if (topEnabled) fastTopWeight.coerceIn(1, 5) else -1,
                topDenominator = preferences.fastWeightTopDenominator,
            )
        }
        Spacer(Modifier.height(16.dp))
    }
    helpTopic?.let { topic ->
        AlertDialog(
            onDismissRequest = { helpTopic = null },
            title = { Text(topic.title) },
            text = { Text(topic.description) },
            confirmButton = {
                TextButton(onClick = { helpTopic = null }) {
                    Text("知道了")
                }
            },
        )
    }
}

@Composable
private fun PlayerControlDoubleTapPreview(
    fastWeight: Int,
    fastTopWeight: Int,
    topDenominator: Int,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(12.dp))
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.78f),
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(12.dp),
            ),
    ) {
        PlayerControlDoubleTapPreviewSide(
            modifier = Modifier.align(Alignment.CenterStart),
            widthFraction = 1f / fastWeight.coerceAtLeast(2),
            topFraction = fastTopWeight.takeIf { it > 0 }
                ?.toFloat()
                ?.div(topDenominator.toFloat()),
            icon = Icons.Filled.FastRewind,
        )
        PlayerControlDoubleTapPreviewSide(
            modifier = Modifier.align(Alignment.CenterEnd),
            widthFraction = 1f / fastWeight.coerceAtLeast(2),
            topFraction = fastTopWeight.takeIf { it > 0 }
                ?.toFloat()
                ?.div(topDenominator.toFloat()),
            icon = Icons.Filled.FastForward,
        )
    }
}

@Composable
private fun PlayerControlDoubleTapPreviewSide(
    modifier: Modifier,
    widthFraction: Float,
    topFraction: Float?,
    icon: ImageVector,
) {
    val areaColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
    val accent = MaterialTheme.colorScheme.primary
    Column(
        modifier = modifier
            .fillMaxHeight()
            .fillMaxWidth(widthFraction)
            .background(areaColor),
    ) {
        if (topFraction != null) {
            Box(
                modifier = Modifier.weight(topFraction).fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = accent)
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(accent))
            Box(
                modifier = Modifier.weight(1f - topFraction).fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = accent)
            }
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = accent)
            }
        }
    }
}

private enum class PlayerControlSettingHelp(
    val title: String,
    val description: String,
) {
    FullscreenSideButtons(
        title = "全屏侧边按钮",
        description = "控制横屏全屏时截图、倍速、弹幕、选集和锁定按钮出现在哪一侧。自动模式会跟随本次唤出控制器时的点击侧；固定模式始终停靠在指定一侧。",
    ),
    HorizontalSeekDuration(
        title = "横滑满屏时长",
        description = "表示手指横向滑过整个播放器宽度时对应的进度跨度。数值越小，滑动跳转越快；数值越大，进度调整越精细。",
    ),
}

@Composable
private fun PlayerControlSlider(
    title: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (Float) -> Unit,
    onHelpClick: (() -> Unit)? = null,
) {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (onHelpClick != null) {
                Spacer(Modifier.widthIn(min = 4.dp))
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clickable(
                            role = Role.Button,
                            onClickLabel = "查看${title}说明",
                            onClick = onHelpClick,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                        contentDescription = "$title 说明",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            Text(valueText, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
        }
        Slider(
            value = value.coerceIn(valueRange.start, valueRange.endInclusive),
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
        )
    }
}

@Composable
private fun ExoAdAudioProbeSettingsContent(
    enabled: Boolean,
    rulesUrl: String,
    onEnabledChange: (Boolean) -> Unit,
    onRulesUrlChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showRulesEditor by remember { mutableStateOf(false) }
    Column(modifier = modifier.fillMaxWidth()) {
        SettingsGroupTitle("音频广告探测")
        ListItem(
            headlineContent = { Text("去广告") },
            supportingContent = {
                Text(
                    if (rulesUrl.isBlank()) "需配置广告指纹规则后才会自动跳过"
                    else "仅对 ExoPlayer 的 HLS/MP4 点播生效",
                )
            },
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
        ListItem(
            headlineContent = { Text("广告规则地址") },
            supportingContent = {
                Text(
                    rulesUrl.ifBlank { "未配置" },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showRulesEditor = true },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
        Spacer(Modifier.height(16.dp))
    }
    if (showRulesEditor) {
        var draft by remember(rulesUrl) { mutableStateOf(rulesUrl) }
        AlertDialog(
            onDismissRequest = { showRulesEditor = false },
            title = { Text("广告规则地址") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("使用 HTTPS rules-v1 JSON；留空会停用自动跳过，仅保留播放。")
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { draft = it },
                        singleLine = true,
                        label = { Text("https://...") },
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onRulesUrlChange(draft)
                    showRulesEditor = false
                }) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = { showRulesEditor = false }) { Text("取消") }
            },
        )
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
