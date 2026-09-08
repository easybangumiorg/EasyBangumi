package com.heyanle.easybangumi4.v2.ui.setting

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import com.heyanle.easybangumi4.danmaku.SCROLL_OCCLUSION_HELP
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VerticalAlignCenter
import androidx.compose.material.icons.filled.ViewDay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heyanle.easybangumi4.BuildConfig
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.danmaku.DANMAKU_AREA_RATIO_TIERS
import com.heyanle.easybangumi4.danmaku.DANMAKU_SCROLL_SPEED_TIERS
import com.heyanle.easybangumi4.danmaku.DanmakuDisplayConfig
import com.heyanle.easybangumi4.danmaku.danmakuAreaRatioLabel
import com.heyanle.easybangumi4.danmaku.danmakuOpacityLabel
import com.heyanle.easybangumi4.danmaku.danmakuScrollSpeedLabel
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.easybangumi4.ui.cartoon_play.speedConfig
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.easybangumi4.v2.theme.V2Tokens
import com.heyanle.easybangumi4.v2.theme.V2Theme
import com.heyanle.easybangumi4.v2.ui.component.V2ActionRow
import com.heyanle.easybangumi4.v2.ui.component.V2Section
import com.heyanle.easybangumi4.v2.ui.component.V2SectionDivider
import com.heyanle.easybangumi4.v2.ui.component.V2Switch
import com.heyanle.inject.core.Inject
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

private enum class PlayerChoiceDialogV2 {
    Engine,
    Orientation,
    Cache,
    Speed,
    FullscreenControls,
    CutoutAvoidance,
}

@Composable
internal fun PlayerSettingV2(
    modifier: Modifier = Modifier,
) {
    val preferences: SettingPreferences by Inject.injectLazy()

    val externalPlayer by preferences.useExternalVideoPlayer.flow().collectAsState(
        preferences.useExternalVideoPlayer.get(),
    )
    val bottomPadding by preferences.playerBottomNavigationBarPadding.flow().collectAsState(
        preferences.playerBottomNavigationBarPadding.get(),
    )
    val playbackEngine by preferences.playbackEngine.flow().collectAsState(
        preferences.playbackEngine.get(),
    )
    val mpvAvailable = BuildConfig.HAS_MPV && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    val effectivePlaybackEngine = playbackEngine.takeIf {
        it != SettingPreferences.PlaybackEngine.MPV || mpvAvailable
    } ?: SettingPreferences.PlaybackEngine.EXO_PLAYER
    val orientationMode by preferences.playerOrientationMode.flow().collectAsState(
        preferences.playerOrientationMode.get(),
    )
    val fullscreenControlPosition by preferences.fullscreenControlPosition.flow().collectAsState(
        preferences.fullscreenControlPosition.get(),
    )
    val cutoutAvoidanceMode by preferences.playerCutoutAvoidanceMode.flow().collectAsState(
        preferences.playerCutoutAvoidanceMode.get(),
    )
    val cutoutManualPadding by preferences.playerCutoutManualPaddingDp.flow().collectAsState(
        preferences.playerCutoutManualPaddingDp.get(),
    )
    val cacheSize by preferences.cacheSize.flow().collectAsState(preferences.cacheSize.get())
    val seekWidthTime by preferences.playerSeekFullWidthTimeMS.flow().collectAsState(
        preferences.playerSeekFullWidthTimeMS.get(),
    )
    val customSpeed by preferences.customSpeed.flow().collectAsState(preferences.customSpeed.get())
    val defaultSpeedStored by preferences.defaultSpeed.flow().collectAsState(
        preferences.defaultSpeed.get(),
    )
    val speedOptions = remember(customSpeed) {
        speedConfig.map { (label, value) -> value to label } + (-1f to "自定义 (${customSpeed}X)")
    }
    val defaultSpeed = defaultSpeedStored.takeIf { value -> speedOptions.any { it.first == value } } ?: 1f

    val rawFastWeight by preferences.fastWeight.flow().collectAsState(preferences.fastWeight.get())
    val fastWeight = rawFastWeight.takeIf { abs(it) in preferences.fastWeightSelection }
        ?: 5
    val rawFastTopWeight by preferences.fastWeightTopMolecule.flow().collectAsState(
        preferences.fastWeightTopMolecule.get(),
    )
    val fastTopWeight = rawFastTopWeight.takeIf {
        abs(it) in preferences.fastWeightTopMoleculeSelection
    } ?: -(preferences.fastWeightTopDenominator / 2)
    val fastSeconds by preferences.fastSecond.flow().collectAsState(preferences.fastSecond.get())
    val fastTopSeconds by preferences.fastTopSecond.flow().collectAsState(
        preferences.fastTopSecond.get(),
    )

    var choiceDialog by remember { mutableStateOf<PlayerChoiceDialogV2?>(null) }

    LaunchedEffect(defaultSpeedStored, speedOptions) {
        if (speedOptions.none { it.first == defaultSpeedStored }) preferences.defaultSpeed.set(1f)
    }
    LaunchedEffect(rawFastWeight) {
        if (abs(rawFastWeight) !in preferences.fastWeightSelection) {
            preferences.fastWeight.set(5)
        }
    }
    LaunchedEffect(rawFastTopWeight) {
        if (abs(rawFastTopWeight) !in preferences.fastWeightTopMoleculeSelection) {
            preferences.fastWeightTopMolecule.set(-(preferences.fastWeightTopDenominator / 2))
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        V2Section(title = "播放方式") {
            V2ActionRow(
                icon = Icons.Filled.PlayCircle,
                title = "播放引擎",
                subtitle = when (effectivePlaybackEngine) {
                    SettingPreferences.PlaybackEngine.EXO_PLAYER -> "ExoPlayer · 截图、片段录制、缓存与广告探测"
                    SettingPreferences.PlaybackEngine.MPV -> "mpv · Anime4K 与无缝旋转，不支持截图和录制"
                },
                onClick = { choiceDialog = PlayerChoiceDialogV2.Engine },
            )
            V2SectionDivider()
            V2ActionRow(
                icon = Icons.AutoMirrored.Filled.OpenInNew,
                title = stringResource(R.string.use_external_player),
                subtitle = "播放时直接调用系统或第三方播放器",
                onClick = { preferences.useExternalVideoPlayer.set(!externalPlayer) },
                trailing = {
                    PlayerSwitchV2(externalPlayer, preferences.useExternalVideoPlayer::set)
                },
            )
            V2SectionDivider()
            V2ActionRow(
                icon = Icons.Filled.ViewDay,
                title = stringResource(R.string.player_bottom_nav_padding),
                subtitle = "避免播放器控制栏被系统导航区域遮挡",
                onClick = { preferences.playerBottomNavigationBarPadding.set(!bottomPadding) },
                trailing = {
                    PlayerSwitchV2(bottomPadding, preferences.playerBottomNavigationBarPadding::set)
                },
            )
            V2SectionDivider()
            V2ActionRow(
                icon = Icons.Filled.ScreenRotation,
                title = stringResource(R.string.player_orientation_mode),
                subtitle = orientationMode.orientationLabelV2(),
                onClick = { choiceDialog = PlayerChoiceDialogV2.Orientation },
            )
        }

        V2Section(title = "播放体验") {
            if (effectivePlaybackEngine != SettingPreferences.PlaybackEngine.MPV) {
                V2ActionRow(
                    icon = Icons.Filled.Cached,
                    title = stringResource(R.string.max_cache_size),
                    subtitle = preferences.cacheSizeSelection
                        .firstOrNull { it.first == cacheSize }
                        ?.second
                        ?: preferences.cacheSizeSelection.first().second,
                    onClick = { choiceDialog = PlayerChoiceDialogV2.Cache },
                )
                V2SectionDivider()
            }
            V2ActionRow(
                icon = Icons.Filled.Speed,
                title = stringResource(R.string.default_speed),
                subtitle = speedOptions.firstOrNull { it.first == defaultSpeed }?.second.orEmpty(),
                onClick = { choiceDialog = PlayerChoiceDialogV2.Speed },
            )
        }

        FullscreenControlSettingV2(
            preferences = preferences,
            controlPosition = fullscreenControlPosition,
            cutoutMode = cutoutAvoidanceMode,
            manualPadding = cutoutManualPadding,
            onControlPositionClick = {
                choiceDialog = PlayerChoiceDialogV2.FullscreenControls
            },
            onCutoutModeClick = {
                choiceDialog = PlayerChoiceDialogV2.CutoutAvoidance
            },
        )

        GestureSettingV2(
            preferences = preferences,
            seekWidthTime = seekWidthTime,
            fastWeight = fastWeight,
            fastTopWeight = fastTopWeight,
            fastSeconds = fastSeconds,
            fastTopSeconds = fastTopSeconds,
        )

        Box(Modifier.height(24.dp))
    }

    when (choiceDialog) {
        PlayerChoiceDialogV2.Engine -> PlayerChoiceDialog(
            title = "播放引擎",
            options = buildList {
                add(SettingPreferences.PlaybackEngine.EXO_PLAYER to "ExoPlayer（截图、录制、缓存与广告探测）")
                if (mpvAvailable) add(SettingPreferences.PlaybackEngine.MPV to "mpv（Anime4K、无缝旋转；不支持截图和录制）")
            },
            selected = effectivePlaybackEngine,
            onDismiss = { choiceDialog = null },
            onSelected = {
                preferences.playbackEngine.set(it)
                choiceDialog = null
                "重新进入播放页后生效".moeSnackBar()
            },
        )
        PlayerChoiceDialogV2.Orientation -> PlayerChoiceDialog(
            title = stringResource(R.string.player_orientation_mode),
            options = SettingPreferences.PlayerOrientationMode.values().map {
                it to it.orientationLabelV2()
            },
            selected = orientationMode,
            onDismiss = { choiceDialog = null },
            onSelected = {
                preferences.playerOrientationMode.set(it)
                choiceDialog = null
            },
        )
        PlayerChoiceDialogV2.Cache -> PlayerChoiceDialog(
            title = stringResource(R.string.max_cache_size),
            options = preferences.cacheSizeSelection,
            selected = cacheSize,
            onDismiss = { choiceDialog = null },
            onSelected = {
                preferences.cacheSize.set(it)
                choiceDialog = null
                stringRes(R.string.should_reboot).moeSnackBar()
            },
        )
        PlayerChoiceDialogV2.Speed -> PlayerChoiceDialog(
            title = stringResource(R.string.default_speed),
            options = speedOptions,
            selected = defaultSpeed,
            onDismiss = { choiceDialog = null },
            onSelected = {
                preferences.defaultSpeed.set(it)
                choiceDialog = null
            },
        )
        PlayerChoiceDialogV2.FullscreenControls -> PlayerChoiceDialog(
            title = "全屏侧边按钮",
            options = listOf(
                SettingPreferences.FullscreenControlPosition.AUTO to "自动（跟随唤出控制器时的点击侧）",
                SettingPreferences.FullscreenControlPosition.LEFT to "固定左侧",
                SettingPreferences.FullscreenControlPosition.RIGHT to "固定右侧",
            ),
            selected = fullscreenControlPosition,
            onDismiss = { choiceDialog = null },
            onSelected = {
                preferences.fullscreenControlPosition.set(it)
                choiceDialog = null
            },
        )
        PlayerChoiceDialogV2.CutoutAvoidance -> {
            val supportedMode = SettingPreferences.PlayerCutoutAvoidanceMode.normalizeForSdk(
                cutoutAvoidanceMode,
            )
            PlayerChoiceDialog(
                title = "刘海避让",
                options = SettingPreferences.PlayerCutoutAvoidanceMode.selectableValues().map {
                    it to it.cutoutLabelV2()
                },
                selected = supportedMode,
                onDismiss = { choiceDialog = null },
                onSelected = {
                    preferences.playerCutoutAvoidanceMode.set(it)
                    choiceDialog = null
                },
            )
        }
        null -> Unit
    }

}

@Composable
private fun FullscreenControlSettingV2(
    preferences: SettingPreferences,
    controlPosition: SettingPreferences.FullscreenControlPosition,
    cutoutMode: SettingPreferences.PlayerCutoutAvoidanceMode,
    manualPadding: Int,
    onControlPositionClick: () -> Unit,
    onCutoutModeClick: () -> Unit,
) {
    val supportedCutoutMode = SettingPreferences.PlayerCutoutAvoidanceMode.normalizeForSdk(
        cutoutMode,
    )
    var showControlHelp by remember { mutableStateOf(false) }

    V2Section(title = "全屏控制") {
        V2ActionRow(
            icon = Icons.Filled.VerticalAlignCenter,
            title = "全屏侧边按钮",
            subtitle = controlPosition.fullscreenControlLabelV2(),
            onClick = onControlPositionClick,
            trailing = {
                IconButton(onClick = { showControlHelp = true }) {
                    Icon(
                        Icons.AutoMirrored.Outlined.HelpOutline,
                        contentDescription = "全屏侧边按钮 说明",
                        tint = V2Tokens.TextSecondary,
                    )
                }
            },
        )
        V2SectionDivider()
        V2ActionRow(
            icon = Icons.Filled.ScreenRotation,
            title = "刘海避让",
            subtitle = supportedCutoutMode.cutoutLabelV2(),
            onClick = onCutoutModeClick,
        )
        if (supportedCutoutMode == SettingPreferences.PlayerCutoutAvoidanceMode.MANUAL) {
            V2SectionDivider()
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                PlayerValueSliderV2(
                    title = "安全距离",
                    valueLabel = "${manualPadding.coerceIn(0, 96)} dp",
                    value = manualPadding.coerceIn(0, 96).toFloat(),
                    valueRange = 0f..96f,
                    steps = 23,
                    onValueChange = {
                        preferences.playerCutoutManualPaddingDp.set((it / 4f).roundToInt() * 4)
                    },
                )
            }
        }
    }

    if (showControlHelp) {
        AlertDialog(
            onDismissRequest = { showControlHelp = false },
            title = { Text("全屏侧边按钮", color = V2Tokens.TextPrimary) },
            text = {
                Text(
                    "控制横屏全屏时截图、倍速、弹幕、选集和锁定按钮出现在哪一侧。自动模式跟随本次唤出控制器时的点击侧；固定模式始终停靠在指定一侧。",
                    color = V2Tokens.TextSecondary,
                )
            },
            confirmButton = {
                TextButton(onClick = { showControlHelp = false }) {
                    Text("知道了", color = V2Theme.colors.accent)
                }
            },
            containerColor = V2Tokens.Surface,
        )
    }
}

@Composable
private fun GestureSettingV2(
    preferences: SettingPreferences,
    seekWidthTime: Long,
    fastWeight: Int,
    fastTopWeight: Int,
    fastSeconds: Int,
    fastTopSeconds: Int,
) {
    val enabled = fastWeight > 0
    val topEnabled = fastTopWeight > 0
    val widthOptions = preferences.fastWeightSelection
    val widthValue = abs(fastWeight).takeIf { it in widthOptions } ?: widthOptions.first()
    val widthIndex = widthOptions.indexOf(widthValue)
    val topOptions = preferences.fastWeightTopMoleculeSelection
    val topValue = abs(fastTopWeight).takeIf { it in topOptions }
        ?: preferences.fastWeightTopDenominator / 2
    val topIndex = topOptions.indexOf(topValue).coerceAtLeast(0)
    val seekSeconds = (seekWidthTime / 1_000L).coerceIn(60L, 1_800L)

    V2Section(title = "手势设置") {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            PlayerValueSliderV2(
                title = "横滑满屏时长",
                valueLabel = "${seekSeconds / 60} 分钟",
                value = seekSeconds.toFloat(),
                valueRange = 60f..1_800f,
                steps = 28,
                onValueChange = {
                    val seconds = (it / 60f).roundToInt().coerceIn(1, 30) * 60L
                    preferences.playerSeekFullWidthTimeMS.set(seconds * 1_000L)
                },
                helpDescription = "表示手指横向滑过整个播放器宽度时对应的进度跨度。数值越小，滑动跳转越快；数值越大，进度调整越精细。",
            )
        }
        V2SectionDivider()
        V2ActionRow(
            icon = Icons.Filled.FastForward,
            title = "启用双击手势",
            subtitle = "点击画面两侧快速后退或前进",
            onClick = {
                preferences.fastWeight.set(signedSettingValue(widthValue, !enabled))
            },
            trailing = {
                PlayerSwitchV2(enabled) { checked ->
                    preferences.fastWeight.set(signedSettingValue(widthValue, checked))
                }
            },
        )
        if (enabled) {
            V2SectionDivider()
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                PlayerValueSliderV2(
                    title = "两侧响应宽度",
                    valueLabel = "各占屏幕 1/$widthValue",
                    value = widthIndex.toFloat(),
                    valueRange = 0f..widthOptions.lastIndex.toFloat(),
                    steps = (widthOptions.size - 2).coerceAtLeast(0),
                    onValueChange = { position ->
                        preferences.fastWeight.set(
                            widthOptions[position.roundToInt().coerceIn(widthOptions.indices)],
                        )
                    },
                )
                PlayerValueSliderV2(
                    title = "两侧快进快退时长",
                    valueLabel = "${fastSeconds.coerceIn(5, 60)} 秒",
                    value = fastSeconds.coerceIn(5, 60).toFloat(),
                    valueRange = 5f..60f,
                    steps = 10,
                    onValueChange = {
                        preferences.fastSecond.set(
                            (it / 5f).roundToInt().coerceIn(1, 12) * 5,
                        )
                    },
                )
            }
            V2SectionDivider()
            V2ActionRow(
                icon = Icons.Filled.VerticalAlignCenter,
                title = "顶部独立区域",
                subtitle = "顶部区域可使用不同的跳转时长",
                onClick = {
                    preferences.fastWeightTopMolecule.set(
                        signedSettingValue(topValue, !topEnabled),
                    )
                },
                trailing = {
                    PlayerSwitchV2(topEnabled) { checked ->
                        preferences.fastWeightTopMolecule.set(
                            signedSettingValue(topValue, checked),
                        )
                    }
                },
            )
            if (topEnabled) {
                V2SectionDivider()
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    PlayerValueSliderV2(
                        title = "顶部区域高度",
                        valueLabel = "屏幕高度的 $topValue/${preferences.fastWeightTopDenominator}",
                        value = topIndex.toFloat(),
                        valueRange = 0f..topOptions.lastIndex.toFloat(),
                        steps = (topOptions.size - 2).coerceAtLeast(0),
                        onValueChange = { position ->
                            preferences.fastWeightTopMolecule.set(
                                topOptions[position.roundToInt().coerceIn(topOptions.indices)],
                            )
                        },
                    )
                    PlayerValueSliderV2(
                        title = "顶部快进快退时长",
                        valueLabel = "${fastTopSeconds.coerceIn(5, 120)} 秒",
                        value = fastTopSeconds.coerceIn(5, 120).toFloat(),
                        valueRange = 5f..120f,
                        steps = 22,
                        onValueChange = {
                            preferences.fastTopSecond.set(
                                (it / 5f).roundToInt().coerceIn(1, 24) * 5,
                            )
                        },
                    )
                }
            }
            V2SectionDivider()
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Text(
                    "区域预览",
                    color = V2Tokens.TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                DoubleTapPreviewV2(
                    fastWeight = widthValue,
                    fastTopWeight = if (topEnabled) topValue else -1,
                    topDenominator = preferences.fastWeightTopDenominator,
                )
            }
        }
    }
}

@Composable
private fun DoubleTapPreviewV2(
    fastWeight: Int,
    fastTopWeight: Int,
    topDenominator: Int,
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .padding(top = 8.dp)
                .aspectRatio(16f / 9f)
                .background(V2Tokens.Divider, RoundedCornerShape(12.dp)),
        ) {
            DoubleTapPreviewSideV2(
                modifier = Modifier.align(Alignment.CenterStart),
                widthFraction = 1f / fastWeight,
                topFraction = if (fastTopWeight > 0) fastTopWeight.toFloat() / topDenominator else null,
                icon = Icons.Filled.FastRewind,
            )
            DoubleTapPreviewSideV2(
                modifier = Modifier.align(Alignment.CenterEnd),
                widthFraction = 1f / fastWeight,
                topFraction = if (fastTopWeight > 0) fastTopWeight.toFloat() / topDenominator else null,
                icon = Icons.Filled.FastForward,
            )
        }
    }
}

@Composable
private fun DoubleTapPreviewSideV2(
    modifier: Modifier,
    widthFraction: Float,
    topFraction: Float?,
    icon: ImageVector,
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .fillMaxWidth(widthFraction)
            .background(V2Theme.colors.accentContainer),
    ) {
        if (topFraction != null) {
            Box(
                modifier = Modifier
                    .weight(topFraction)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = V2Theme.colors.accent)
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(V2Theme.colors.accent))
            Box(
                modifier = Modifier
                    .weight(1f - topFraction)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = V2Theme.colors.accent)
            }
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = V2Theme.colors.accent)
            }
        }
    }
}

@Composable
internal fun DanmakuDisplaySettingV2(
    config: DanmakuDisplayConfig,
    dandanPlayEnabled: Boolean,
    onConfigChange: (DanmakuDisplayConfig) -> Unit,
    onDandanPlayChange: (Boolean) -> Unit,
    onReset: () -> Unit,
    onOpenBlockRules: () -> Unit,
) {
    var showOcclusionHelp by remember { mutableStateOf(false) }
    if (showOcclusionHelp) {
        AlertDialog(
            onDismissRequest = { showOcclusionHelp = false },
            title = { Text("弹幕防覆盖") },
            text = { Text(SCROLL_OCCLUSION_HELP) },
            confirmButton = {
                TextButton(onClick = { showOcclusionHelp = false }) { Text("知道了") }
            },
            containerColor = V2Tokens.Surface,
        )
    }
    V2Section(title = stringResource(R.string.danmaku_setting)) {
        V2ActionRow(
            icon = Icons.Filled.ViewDay,
            title = "显示弹幕",
            subtitle = "在播放画面上显示已匹配的弹幕",
            onClick = { onConfigChange(config.copy(enabled = !config.enabled)) },
            trailing = {
                PlayerSwitchV2(
                    checked = config.enabled,
                    onCheckedChange = { onConfigChange(config.copy(enabled = it)) },
                )
            },
        )
        V2SectionDivider()
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = "显示类型",
                color = V2Tokens.TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Row(
                modifier = Modifier.padding(top = 9.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DanmakuTypeChipV2("滚动", config.showScroll) {
                    onConfigChange(config.copy(showScroll = !config.showScroll))
                }
                DanmakuTypeChipV2("顶部", config.showTop) {
                    onConfigChange(config.copy(showTop = !config.showTop))
                }
                DanmakuTypeChipV2("底部", config.showBottom) {
                    onConfigChange(config.copy(showBottom = !config.showBottom))
                }
            }
            val areaIndex = DANMAKU_AREA_RATIO_TIERS.indexOf(config.areaRatio)
                .takeIf { it >= 0 }
                ?: DANMAKU_AREA_RATIO_TIERS.lastIndex
            PlayerValueSliderV2(
                title = "显示区域",
                valueLabel = danmakuAreaRatioLabel(config.areaRatio),
                value = areaIndex.toFloat(),
                valueRange = 0f..DANMAKU_AREA_RATIO_TIERS.lastIndex.toFloat(),
                steps = DANMAKU_AREA_RATIO_TIERS.size - 2,
                onValueChange = {
                    onConfigChange(
                        config.copy(
                            areaRatio = DANMAKU_AREA_RATIO_TIERS[
                                it.roundToInt().coerceIn(DANMAKU_AREA_RATIO_TIERS.indices)
                            ],
                        ),
                    )
                },
            )
            PlayerValueSliderV2(
                title = "不透明度",
                valueLabel = danmakuOpacityLabel(config.opacity),
                value = config.opacity,
                valueRange = DanmakuDisplayConfig.OPACITY_RANGE,
                steps = 0,
                onValueChange = {
                    onConfigChange(config.copy(opacity = it).normalized())
                },
            )
            PlayerValueSliderV2(
                title = "字体大小",
                valueLabel = "${config.fontSizeSp.roundToInt()} sp",
                value = config.fontSizeSp,
                valueRange = DanmakuDisplayConfig.FONT_SIZE_SP_RANGE,
                steps = 23,
                onValueChange = {
                    onConfigChange(config.copy(fontSizeSp = it).normalized())
                },
            )
            PlayerValueSliderV2(
                title = "行高",
                valueLabel = formatFactorV2(config.lineHeightFactor),
                value = config.lineHeightFactor,
                valueRange = DanmakuDisplayConfig.LINE_HEIGHT_FACTOR_RANGE,
                steps = 9,
                onValueChange = {
                    onConfigChange(config.copy(lineHeightFactor = it).normalized())
                },
            )
            // 速度档位不等距，滑条改为"档位索引"式（与播放器面板一致）。
            val speedIndex = DANMAKU_SCROLL_SPEED_TIERS.indexOf(config.scrollSpeed)
                .takeIf { it >= 0 }
                ?: DANMAKU_SCROLL_SPEED_TIERS.indexOf(DanmakuDisplayConfig.DEFAULT_SCROLL_SPEED)
            PlayerValueSliderV2(
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
            )
            PlayerValueSliderV2(
                title = "弹幕数量",
                valueLabel = "${(config.densityRatio * 100).roundToInt()}%",
                value = config.densityRatio,
                valueRange = DanmakuDisplayConfig.DENSITY_RATIO_RANGE,
                steps = 8,
                onValueChange = {
                    onConfigChange(config.copy(densityRatio = it).normalized())
                },
                helpDescription = "在完成类型和屏蔽规则过滤后，按时间顺序等距保留指定比例的弹幕。",
            )
            PlayerValueSliderV2(
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
                helpDescription = "在所选时间窗口内遇到相同文字时只保留第一条；设为不合并可完整显示复读弹幕。",
            )
            Text(
                text = "时间校准",
                modifier = Modifier.padding(top = 6.dp),
                color = V2Tokens.TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                DanmakuOffsetButtonV2("−0.5s", Modifier.weight(1f)) {
                    onConfigChange(config.copy(timeOffsetMillis = config.timeOffsetMillis - 500L))
                }
                Text(
                    text = formatOffsetV2(config.timeOffsetMillis),
                    modifier = Modifier.weight(1f),
                    color = V2Tokens.TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
                DanmakuOffsetButtonV2("+0.5s", Modifier.weight(1f)) {
                    onConfigChange(config.copy(timeOffsetMillis = config.timeOffsetMillis + 500L))
                }
            }
            TextButton(
                onClick = onReset,
                modifier = Modifier.align(Alignment.End),
            ) {
                Text("恢复默认", color = V2Theme.colors.accent)
            }
        }
        V2SectionDivider()
        V2ActionRow(
            icon = Icons.Filled.Block,
            title = "弹幕屏蔽词",
            subtitle = if (config.blockRulesEnabled) {
                "已开启 · ${config.blockedTextRules.size} 条文本 · ${config.blockedRegexRules.size} 条正则"
            } else {
                "已关闭 · 规则已保留"
            },
            onClick = onOpenBlockRules,
        )
        V2SectionDivider()
        V2ActionRow(
            icon = Icons.Filled.Speed,
            title = "倍速同步弹幕速度",
            subtitle = "手动倍速和长按快进时同步加速滚动弹幕",
            onClick = {
                onConfigChange(
                    config.copy(syncScrollSpeedWithPlayback = !config.syncScrollSpeedWithPlayback),
                )
            },
            trailing = {
                PlayerSwitchV2(config.syncScrollSpeedWithPlayback) {
                    onConfigChange(config.copy(syncScrollSpeedWithPlayback = it))
                }
            },
        )
        V2SectionDivider()
        V2ActionRow(
            icon = Icons.Filled.ViewDay,
            title = "弹幕防覆盖",
            subtitle = "前一条完全入场后再显示下一条，超时丢弃",
            onClick = { onConfigChange(config.copy(preventScrollOcclusion = !config.preventScrollOcclusion)) },
            trailing = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showOcclusionHelp = true }) {
                        Icon(Icons.AutoMirrored.Outlined.HelpOutline, contentDescription = "弹幕防覆盖说明")
                    }
                    PlayerSwitchV2(config.preventScrollOcclusion) {
                        onConfigChange(config.copy(preventScrollOcclusion = it))
                    }
                }
            },
        )
        V2SectionDivider()
        V2ActionRow(
            icon = Icons.Filled.ViewDay,
            title = "弹弹play 弹幕",
            subtitle = "按数据来源筛选显示的弹幕",
            onClick = { onDandanPlayChange(!dandanPlayEnabled) },
            trailing = {
                PlayerSwitchV2(dandanPlayEnabled, onDandanPlayChange)
            },
        )
    }
}

@Composable
private fun DanmakuTypeChipV2(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        color = if (selected) V2Theme.colors.accentContainer else V2Tokens.Surface,
        contentColor = V2Tokens.TextPrimary,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, if (selected) V2Theme.colors.accent else V2Tokens.Divider),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 8.dp),
            color = V2Tokens.TextPrimary,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

@Composable
private fun PlayerValueSliderV2(
    title: String,
    valueLabel: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (Float) -> Unit,
    helpDescription: String? = null,
) {
    var showHelp by remember { mutableStateOf(false) }
    Column(modifier = Modifier.padding(top = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                color = V2Tokens.TextPrimary,
                fontSize = 14.sp,
            )
            if (helpDescription != null) {
                IconButton(
                    onClick = { showHelp = true },
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(
                        Icons.AutoMirrored.Outlined.HelpOutline,
                        contentDescription = "$title 说明",
                        tint = V2Tokens.TextSecondary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
            Box(Modifier.weight(1f))
            Text(
                text = valueLabel,
                color = V2Tokens.TextSecondary,
                fontSize = 12.sp,
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = V2Theme.colors.accent,
                activeTrackColor = V2Theme.colors.accent,
                inactiveTrackColor = V2Tokens.Divider,
                activeTickColor = V2Theme.colors.accentContainer,
                inactiveTickColor = V2Tokens.TextSecondary,
            ),
        )
    }
    if (showHelp && helpDescription != null) {
        AlertDialog(
            onDismissRequest = { showHelp = false },
            title = { Text(title, color = V2Tokens.TextPrimary) },
            text = { Text(helpDescription, color = V2Tokens.TextSecondary) },
            confirmButton = {
                TextButton(onClick = { showHelp = false }) {
                    Text("知道了", color = V2Theme.colors.accent)
                }
            },
            containerColor = V2Tokens.Surface,
        )
    }
}

private enum class DanmakuBlockRuleMode { Text, Regex }

@Composable
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
internal fun DanmakuBlockRulesBottomSheet(
    config: DanmakuDisplayConfig,
    onUpdateConfig: ((DanmakuDisplayConfig) -> DanmakuDisplayConfig) -> Unit,
    onDismiss: () -> Unit,
) {
    var mode by remember { mutableStateOf(DanmakuBlockRuleMode.Text) }
    var input by remember { mutableStateOf("") }
    val normalizedInput = input.trim()
    val regexError = mode == DanmakuBlockRuleMode.Regex &&
        normalizedInput.isNotEmpty() && runCatching { Regex(normalizedInput) }.isFailure
    val rules = when (mode) {
        DanmakuBlockRuleMode.Text -> config.blockedTextRules
        DanmakuBlockRuleMode.Regex -> config.blockedRegexRules
    }.sorted()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = V2Tokens.Surface,
        contentColor = V2Tokens.TextPrimary,
        tonalElevation = 0.dp,
        scrimColor = Color.Black.copy(alpha = 0.56f),
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = V2Tokens.Divider)
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 640.dp)
                .padding(horizontal = 20.dp),
        ) {
            Text(
                "弹幕屏蔽词",
                color = V2Tokens.TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "文本模式忽略大小写并匹配包含关系；正则模式按完整正则表达式查找。非法正则不会保存。",
                color = V2Tokens.TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 6.dp),
            )
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .clickable {
                        onUpdateConfig { current ->
                            current.copy(blockRulesEnabled = !current.blockRulesEnabled)
                        }
                    },
                shape = RoundedCornerShape(16.dp),
                color = V2Tokens.SurfaceMuted,
                contentColor = V2Tokens.TextPrimary,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("启用屏蔽词", fontWeight = FontWeight.SemiBold)
                        Text(
                            "关闭后保留全部规则，但暂停匹配",
                            color = V2Tokens.TextSecondary,
                            fontSize = 13.sp,
                        )
                    }
                    PlayerSwitchV2(config.blockRulesEnabled) { enabled ->
                        onUpdateConfig { current -> current.copy(blockRulesEnabled = enabled) }
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 12.dp),
            ) {
                FilterChip(
                    selected = mode == DanmakuBlockRuleMode.Text,
                    onClick = {
                        mode = DanmakuBlockRuleMode.Text
                        input = ""
                    },
                    label = { Text("文本匹配") },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = V2Tokens.SurfaceMuted,
                        labelColor = V2Tokens.TextSecondary,
                        selectedContainerColor = V2Theme.colors.accentContainer,
                        selectedLabelColor = V2Theme.colors.onAccentContainer,
                    ),
                )
                FilterChip(
                    selected = mode == DanmakuBlockRuleMode.Regex,
                    onClick = {
                        mode = DanmakuBlockRuleMode.Regex
                        input = ""
                    },
                    label = { Text("正则匹配") },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = V2Tokens.SurfaceMuted,
                        labelColor = V2Tokens.TextSecondary,
                        selectedContainerColor = V2Theme.colors.accentContainer,
                        selectedLabelColor = V2Theme.colors.onAccentContainer,
                    ),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it.take(256) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    isError = regexError,
                    label = { Text(if (mode == DanmakuBlockRuleMode.Text) "屏蔽文字" else "正则表达式") },
                    supportingText = if (regexError) {
                        { Text("正则表达式无效") }
                    } else {
                        null
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = V2Tokens.TextPrimary,
                        unfocusedTextColor = V2Tokens.TextPrimary,
                        focusedBorderColor = V2Theme.colors.accent,
                        unfocusedBorderColor = V2Tokens.Divider,
                        focusedLabelColor = V2Theme.colors.accent,
                        unfocusedLabelColor = V2Tokens.TextSecondary,
                        cursorColor = V2Theme.colors.accent,
                        errorBorderColor = V2Tokens.Error,
                        errorLabelColor = V2Tokens.Error,
                        errorSupportingTextColor = V2Tokens.Error,
                    ),
                )
                IconButton(
                    enabled = normalizedInput.isNotEmpty() && !regexError,
                    onClick = {
                        when (mode) {
                            DanmakuBlockRuleMode.Text -> onUpdateConfig { current ->
                                current.copy(blockedTextRules = current.blockedTextRules + normalizedInput)
                            }
                            DanmakuBlockRuleMode.Regex -> onUpdateConfig { current ->
                                current.copy(blockedRegexRules = current.blockedRegexRules + normalizedInput)
                            }
                        }
                        input = ""
                    },
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "添加屏蔽规则")
                }
            }
            if (rules.isEmpty()) {
                Text(
                    "当前模式暂无屏蔽规则",
                    color = V2Tokens.TextSecondary,
                    modifier = Modifier.padding(vertical = 24.dp),
                )
            } else {
                LazyColumn(modifier = Modifier.weight(1f, fill = false).padding(vertical = 8.dp)) {
                    items(rules, key = { it }) { rule ->
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = V2Tokens.SurfaceMuted,
                            contentColor = V2Tokens.TextPrimary,
                        ) {
                            Row(
                                modifier = Modifier.padding(start = 14.dp, end = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    rule,
                                    modifier = Modifier.weight(1f),
                                    color = V2Tokens.TextPrimary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                IconButton(onClick = {
                                    when (mode) {
                                        DanmakuBlockRuleMode.Text -> onUpdateConfig { current ->
                                            current.copy(blockedTextRules = current.blockedTextRules - rule)
                                        }
                                        DanmakuBlockRuleMode.Regex -> onUpdateConfig { current ->
                                            current.copy(blockedRegexRules = current.blockedRegexRules - rule)
                                        }
                                    }
                                }) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "删除规则",
                                        tint = V2Tokens.Error,
                                    )
                                }
                            }
                        }
                    }
                }
            }
            TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                Text("完成", color = V2Theme.colors.accent)
            }
        }
    }
}

@Composable
private fun DanmakuOffsetButtonV2(
    label: String,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        border = BorderStroke(1.dp, V2Theme.colors.accent),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = V2Tokens.TextPrimary),
        contentPadding = PaddingValues(horizontal = 8.dp),
    ) {
        Text(label)
    }
}

@Composable
private fun PlayerSwitchV2(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    V2Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
    )
}

@Composable
private fun <T> PlayerChoiceDialog(
    title: String,
    options: List<Pair<T, String>>,
    selected: T,
    onDismiss: () -> Unit,
    onSelected: (T) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = V2Tokens.TextPrimary) },
        text = {
            Column {
                options.forEach { (value, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelected(value) }
                            .padding(vertical = 7.dp),
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
                Text("取消", color = V2Theme.colors.accent)
            }
        },
        containerColor = V2Tokens.Surface,
    )
}

@Composable
private fun SettingPreferences.PlayerOrientationMode.orientationLabelV2(): String {
    return when (this) {
        SettingPreferences.PlayerOrientationMode.Auto -> stringResource(R.string.auto)
        SettingPreferences.PlayerOrientationMode.Enable -> stringResource(R.string.always_on)
        SettingPreferences.PlayerOrientationMode.Disable -> stringResource(R.string.always_off)
    }
}

private fun SettingPreferences.FullscreenControlPosition.fullscreenControlLabelV2(): String =
    when (this) {
        SettingPreferences.FullscreenControlPosition.AUTO -> "自动 · 跟随点击侧"
        SettingPreferences.FullscreenControlPosition.LEFT -> "固定左侧"
        SettingPreferences.FullscreenControlPosition.RIGHT -> "固定右侧"
    }

private fun SettingPreferences.PlayerCutoutAvoidanceMode.cutoutLabelV2(): String = when (this) {
    SettingPreferences.PlayerCutoutAvoidanceMode.AUTO -> "自动识别刘海位置"
    SettingPreferences.PlayerCutoutAvoidanceMode.DISABLED -> "关闭避让"
    SettingPreferences.PlayerCutoutAvoidanceMode.MANUAL -> "手动设置安全距离"
}

private fun signedSettingValue(magnitude: Int, enabled: Boolean): Int {
    val normalized = abs(magnitude).coerceAtLeast(1)
    return if (enabled) normalized else -normalized
}

private fun formatFactorV2(value: Float): String = String.format(Locale.US, "%.1f", value)

private fun formatOffsetV2(valueMillis: Long): String {
    val seconds = valueMillis / 1_000f
    return when {
        valueMillis == 0L -> "0.0s"
        valueMillis > 0L -> "+${String.format(Locale.US, "%.1f", seconds)}s"
        else -> "${String.format(Locale.US, "%.1f", seconds)}s"
    }
}
