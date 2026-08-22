package com.heyanle.easybangumi4.v2.ui.setting

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VerticalAlignCenter
import androidx.compose.material.icons.filled.ViewDay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.danmaku.DANDANPLAY_SOURCE_ID
import com.heyanle.easybangumi4.danmaku.DanmakuDisplayConfig
import com.heyanle.easybangumi4.danmaku.DanmakuDisplayPreferences
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.easybangumi4.ui.cartoon_play.speedConfig
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.easybangumi4.v2.theme.V2Tokens
import com.heyanle.easybangumi4.v2.theme.V2Theme
import com.heyanle.easybangumi4.v2.ui.component.V2ActionRow
import com.heyanle.easybangumi4.v2.ui.component.V2Section
import com.heyanle.easybangumi4.v2.ui.component.V2SectionDivider
import com.heyanle.inject.core.Inject
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

private enum class PlayerChoiceDialogV2 { Anime4KMode, Anime4KQuality, Anime4KScale, Orientation, Cache, Speed }
private enum class PlayerNumberDialogV2 { SeekWidthTime, FastTop, FastBottom }

@Composable
internal fun PlayerSettingV2(
    modifier: Modifier = Modifier,
) {
    val preferences: SettingPreferences by Inject.injectLazy()
    val danmakuPreferences: DanmakuDisplayPreferences by Inject.injectLazy()

    val externalPlayer by preferences.useExternalVideoPlayer.flow().collectAsState(
        preferences.useExternalVideoPlayer.get(),
    )
    val bottomPadding by preferences.playerBottomNavigationBarPadding.flow().collectAsState(
        preferences.playerBottomNavigationBarPadding.get(),
    )
    val anime4kEnabled by preferences.anime4kEnabled.flow().collectAsState(
        preferences.anime4kEnabled.get(),
    )
    val anime4kMode by preferences.anime4kMode.flow().collectAsState(
        preferences.anime4kMode.get(),
    )
    val anime4kQuality by preferences.anime4kQuality.flow().collectAsState(
        preferences.anime4kQuality.get(),
    )
    val anime4kScale by preferences.anime4kScale.flow().collectAsState(
        preferences.anime4kScale.get(),
    )
    val orientationMode by preferences.playerOrientationMode.flow().collectAsState(
        preferences.playerOrientationMode.get(),
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
        ?: preferences.fastWeightSelection.first()
    val rawFastTopWeight by preferences.fastWeightTopMolecule.flow().collectAsState(
        preferences.fastWeightTopMolecule.get(),
    )
    val fastTopWeight = rawFastTopWeight.takeIf {
        abs(it) in preferences.fastWeightTopMoleculeSelection
    } ?: preferences.fastWeightTopMoleculeSelection.first()
    val fastSeconds by preferences.fastSecond.flow().collectAsState(preferences.fastSecond.get())
    val fastTopSeconds by preferences.fastTopSecond.flow().collectAsState(
        preferences.fastTopSecond.get(),
    )

    val danmakuConfig by remember(danmakuPreferences) {
        danmakuPreferences.configFlow()
    }.collectAsState(danmakuPreferences.getConfig())
    val enabledProvenance by danmakuPreferences.enabledProvenance.flow().collectAsState(
        danmakuPreferences.enabledProvenance.get(),
    )

    var choiceDialog by remember { mutableStateOf<PlayerChoiceDialogV2?>(null) }
    var numberDialog by remember { mutableStateOf<PlayerNumberDialogV2?>(null) }
    var confirmDanmakuReset by remember { mutableStateOf(false) }

    LaunchedEffect(defaultSpeedStored, speedOptions) {
        if (speedOptions.none { it.first == defaultSpeedStored }) preferences.defaultSpeed.set(1f)
    }
    LaunchedEffect(rawFastWeight) {
        if (abs(rawFastWeight) !in preferences.fastWeightSelection) {
            preferences.fastWeight.set(preferences.fastWeightSelection.first())
        }
    }
    LaunchedEffect(rawFastTopWeight) {
        if (abs(rawFastTopWeight) !in preferences.fastWeightTopMoleculeSelection) {
            preferences.fastWeightTopMolecule.set(preferences.fastWeightTopMoleculeSelection.first())
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        V2Section(title = "播放方式") {
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

        V2Section(title = "画质增强") {
            V2ActionRow(
                icon = Icons.Filled.AutoAwesome,
                title = stringResource(R.string.anime4k_title),
                subtitle = stringResource(R.string.anime4k_summary),
                onClick = { preferences.anime4kEnabled.set(!anime4kEnabled) },
                trailing = {
                    PlayerSwitchV2(anime4kEnabled, preferences.anime4kEnabled::set)
                },
            )
            if (anime4kEnabled) {
                V2SectionDivider()
                V2ActionRow(
                    icon = Icons.Filled.HighQuality,
                    title = stringResource(R.string.anime4k_mode),
                    subtitle = com.heyanle.easybangumi4.anime4k.A4KChain.MODE_NAMES
                        .getOrElse(anime4kMode) {
                            com.heyanle.easybangumi4.anime4k.A4KChain.MODE_NAMES[
                                com.heyanle.easybangumi4.anime4k.A4KChain.DEFAULT_MODE
                            ]
                        },
                    onClick = { choiceDialog = PlayerChoiceDialogV2.Anime4KMode },
                )
                V2SectionDivider()
                V2ActionRow(
                    icon = Icons.Filled.AutoAwesome,
                    title = stringResource(R.string.anime4k_quality),
                    subtitle = anime4kQuality.qualityLabelV2(),
                    onClick = { choiceDialog = PlayerChoiceDialogV2.Anime4KQuality },
                )
                V2SectionDivider()
                V2ActionRow(
                    icon = Icons.Filled.ViewDay,
                    title = stringResource(R.string.anime4k_scale),
                    subtitle = anime4kScale.scaleLabelV2(),
                    onClick = { choiceDialog = PlayerChoiceDialogV2.Anime4KScale },
                )
            }
        }

        V2Section(title = "播放体验") {
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
            V2ActionRow(
                icon = Icons.Filled.Speed,
                title = stringResource(R.string.default_speed),
                subtitle = speedOptions.firstOrNull { it.first == defaultSpeed }?.second.orEmpty(),
                onClick = { choiceDialog = PlayerChoiceDialogV2.Speed },
            )
            V2SectionDivider()
            V2ActionRow(
                icon = Icons.Filled.Swipe,
                title = stringResource(R.string.player_seek_full_width_time_ms),
                subtitle = "$seekWidthTime ms",
                onClick = { numberDialog = PlayerNumberDialogV2.SeekWidthTime },
            )
        }

        DoubleTapFastSettingV2(
            preferences = preferences,
            fastWeight = fastWeight,
            fastTopWeight = fastTopWeight,
            fastSeconds = fastSeconds,
            fastTopSeconds = fastTopSeconds,
            onEditTopTime = { numberDialog = PlayerNumberDialogV2.FastTop },
            onEditBottomTime = { numberDialog = PlayerNumberDialogV2.FastBottom },
        )

        DanmakuDisplaySettingV2(
            config = danmakuConfig,
            dandanPlayEnabled = DANDANPLAY_SOURCE_ID in enabledProvenance,
            onConfigChange = danmakuPreferences::setConfig,
            onDandanPlayChange = { enabled ->
                danmakuPreferences.enabledProvenance.set(
                    enabledProvenance.toMutableSet().apply {
                        if (enabled) add(DANDANPLAY_SOURCE_ID) else remove(DANDANPLAY_SOURCE_ID)
                    },
                )
            },
            onReset = { confirmDanmakuReset = true },
        )
        Box(Modifier.height(24.dp))
    }

    when (choiceDialog) {
        PlayerChoiceDialogV2.Anime4KMode -> PlayerChoiceDialog(
            title = stringResource(R.string.anime4k_mode),
            options = com.heyanle.easybangumi4.anime4k.A4KChain.MODE_NAMES.mapIndexed { index, label ->
                index to label
            },
            selected = anime4kMode.coerceIn(
                0,
                com.heyanle.easybangumi4.anime4k.A4KChain.MODE_NAMES.lastIndex,
            ),
            onDismiss = { choiceDialog = null },
            onSelected = {
                preferences.anime4kMode.set(it)
                choiceDialog = null
            },
        )
        PlayerChoiceDialogV2.Anime4KQuality -> PlayerChoiceDialog(
            title = stringResource(R.string.anime4k_quality),
            options = com.heyanle.easybangumi4.anime4k.A4KChain.QUALITIES.map {
                it to it.qualityLabelV2()
            },
            selected = anime4kQuality.takeIf {
                it in com.heyanle.easybangumi4.anime4k.A4KChain.QUALITIES
            } ?: com.heyanle.easybangumi4.anime4k.A4KChain.DEFAULT_QUALITY,
            onDismiss = { choiceDialog = null },
            onSelected = {
                preferences.anime4kQuality.set(it)
                choiceDialog = null
            },
        )
        PlayerChoiceDialogV2.Anime4KScale -> PlayerChoiceDialog(
            title = stringResource(R.string.anime4k_scale),
            // 4× depends heavily on the current video's decoded size and live GPU limits. It is
            // offered only inside the playback panel after the controller completes that check.
            options = listOf(0, 1, 2).map { it to it.scaleLabelV2() },
            selected = anime4kScale.takeIf { it in listOf(0, 1, 2) } ?: 0,
            onDismiss = { choiceDialog = null },
            onSelected = {
                preferences.anime4kScale.set(it)
                choiceDialog = null
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
        null -> Unit
    }

    when (numberDialog) {
        PlayerNumberDialogV2.SeekWidthTime -> PlayerNumberDialog(
            title = stringResource(R.string.player_seek_full_width_time_ms),
            value = seekWidthTime,
            positiveOnly = false,
            onDismiss = { numberDialog = null },
            onConfirm = {
                preferences.playerSeekFullWidthTimeMS.set(it)
                numberDialog = null
            },
        )
        PlayerNumberDialogV2.FastTop -> PlayerNumberDialog(
            title = stringResource(R.string.fast_time_top),
            value = fastTopSeconds.toLong(),
            positiveOnly = true,
            onDismiss = { numberDialog = null },
            onConfirm = {
                preferences.fastTopSecond.set(it.toInt())
                numberDialog = null
            },
        )
        PlayerNumberDialogV2.FastBottom -> PlayerNumberDialog(
            title = stringResource(
                if (fastTopWeight > 0) R.string.fast_time_bottom else R.string.fast_time,
            ),
            value = fastSeconds.toLong(),
            positiveOnly = true,
            onDismiss = { numberDialog = null },
            onConfirm = {
                preferences.fastSecond.set(it.toInt())
                numberDialog = null
            },
        )
        null -> Unit
    }

    if (confirmDanmakuReset) {
        AlertDialog(
            onDismissRequest = { confirmDanmakuReset = false },
            title = { Text("恢复弹幕默认设置？", color = V2Tokens.TextPrimary) },
            text = {
                Text(
                    "将恢复显示类型、字体大小、行高、滚动速度和时间偏移。",
                    color = V2Tokens.TextSecondary,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmDanmakuReset = false
                    danmakuPreferences.resetToDefaults()
                }) {
                    Text("恢复", color = V2Theme.colors.accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDanmakuReset = false }) {
                    Text("取消", color = V2Tokens.TextSecondary)
                }
            },
            containerColor = V2Tokens.Surface,
        )
    }
}

@Composable
private fun DoubleTapFastSettingV2(
    preferences: SettingPreferences,
    fastWeight: Int,
    fastTopWeight: Int,
    fastSeconds: Int,
    fastTopSeconds: Int,
    onEditTopTime: () -> Unit,
    onEditBottomTime: () -> Unit,
) {
    val enabled = fastWeight > 0
    val topEnabled = fastTopWeight > 0

    V2Section(title = "双击快进快退") {
        V2ActionRow(
            icon = Icons.Filled.FastForward,
            title = stringResource(R.string.double_tap_fast),
            subtitle = "双击画面两侧快速后退或前进",
            onClick = { preferences.fastWeight.set(toggleSignedSetting(fastWeight, 5)) },
            trailing = {
                PlayerSwitchV2(
                    checked = enabled,
                    onCheckedChange = { preferences.fastWeight.set(toggleSignedSetting(fastWeight, 5)) },
                )
            },
        )
        if (enabled) {
            V2SectionDivider()
            V2ActionRow(
                icon = Icons.Filled.VerticalAlignCenter,
                title = stringResource(R.string.double_tap_fast_top),
                subtitle = "为画面上半部分设置独立快进时间",
                onClick = {
                    preferences.fastWeightTopMolecule.set(
                        toggleSignedSetting(fastTopWeight, preferences.fastWeightTopDenominator / 2),
                    )
                },
                trailing = {
                    PlayerSwitchV2(
                        checked = topEnabled,
                        onCheckedChange = {
                            preferences.fastWeightTopMolecule.set(
                                toggleSignedSetting(
                                    fastTopWeight,
                                    preferences.fastWeightTopDenominator / 2,
                                ),
                            )
                        },
                    )
                },
            )
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                PlayerValueSliderV2(
                    title = "两侧响应宽度",
                    valueLabel = "屏幕宽度的 1/$fastWeight",
                    value = preferences.fastWeightSelection.indexOf(fastWeight).coerceAtLeast(0).toFloat(),
                    valueRange = 0f..preferences.fastWeightSelection.lastIndex.toFloat(),
                    steps = preferences.fastWeightSelection.size - 2,
                    onValueChange = { position ->
                        preferences.fastWeightSelection.getOrNull(position.toInt())?.let {
                            preferences.fastWeight.set(it)
                        }
                    },
                )
                if (topEnabled) {
                    PlayerValueSliderV2(
                        title = "上区域高度",
                        valueLabel = "$fastTopWeight/${preferences.fastWeightTopDenominator}",
                        value = preferences.fastWeightTopMoleculeSelection
                            .indexOf(fastTopWeight)
                            .coerceAtLeast(0)
                            .toFloat(),
                        valueRange = 0f..preferences.fastWeightTopMoleculeSelection.lastIndex.toFloat(),
                        steps = preferences.fastWeightTopMoleculeSelection.size - 2,
                        onValueChange = { position ->
                            preferences.fastWeightTopMoleculeSelection
                                .getOrNull(position.toInt())
                                ?.let { preferences.fastWeightTopMolecule.set(it) }
                        },
                    )
                }
                DoubleTapPreviewV2(
                    fastWeight = fastWeight,
                    fastTopWeight = fastTopWeight,
                    topDenominator = preferences.fastWeightTopDenominator,
                )
            }
            V2SectionDivider()
            if (topEnabled) {
                V2ActionRow(
                    icon = Icons.Filled.Timer,
                    title = stringResource(R.string.fast_time_top),
                    subtitle = "$fastTopSeconds 秒",
                    onClick = onEditTopTime,
                )
                V2SectionDivider()
            }
            V2ActionRow(
                icon = Icons.Filled.Timer,
                title = stringResource(
                    if (topEnabled) R.string.fast_time_bottom else R.string.fast_time,
                ),
                subtitle = "$fastSeconds 秒",
                onClick = onEditBottomTime,
            )
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
        modifier = Modifier
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
private fun DanmakuDisplaySettingV2(
    config: DanmakuDisplayConfig,
    dandanPlayEnabled: Boolean,
    onConfigChange: (DanmakuDisplayConfig) -> Unit,
    onDandanPlayChange: (Boolean) -> Unit,
    onReset: () -> Unit,
) {
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
            PlayerValueSliderV2(
                title = "滚动速度",
                valueLabel = "${formatFactorV2(config.scrollSpeed)}x",
                value = config.scrollSpeed,
                valueRange = DanmakuDisplayConfig.SCROLL_SPEED_RANGE,
                steps = 5,
                onValueChange = {
                    onConfigChange(config.copy(scrollSpeed = it).normalized())
                },
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
) {
    Column(modifier = Modifier.padding(top = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                color = V2Tokens.TextPrimary,
                fontSize = 14.sp,
            )
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
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = V2Tokens.Surface,
            checkedTrackColor = V2Theme.colors.accent,
            uncheckedThumbColor = V2Tokens.Surface,
            uncheckedTrackColor = V2Tokens.Divider,
            uncheckedBorderColor = V2Tokens.Divider,
        ),
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
private fun PlayerNumberDialog(
    title: String,
    value: Long,
    positiveOnly: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
) {
    var text by remember(value) { mutableStateOf(value.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = V2Tokens.TextPrimary) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { input ->
                    if (input.isEmpty() || input.all(Char::isDigit)) text = input
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = V2Theme.colors.accent,
                    cursorColor = V2Theme.colors.accent,
                ),
            )
        },
        confirmButton = {
            TextButton(onClick = {
                val next = text.toLongOrNull() ?: 0L
                if (positiveOnly && (next <= 0L || next > Int.MAX_VALUE)) {
                    stringRes(R.string.please_input_right_speed).moeSnackBar()
                    onDismiss()
                } else {
                    onConfirm(next)
                }
            }) {
                Text("确定", color = V2Theme.colors.accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = V2Tokens.TextSecondary)
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

@Composable
private fun String.qualityLabelV2(): String = when (this) {
    com.heyanle.easybangumi4.anime4k.A4KChain.QUALITY_S -> stringResource(R.string.anime4k_quality_s)
    com.heyanle.easybangumi4.anime4k.A4KChain.QUALITY_L -> stringResource(R.string.anime4k_quality_l)
    else -> stringResource(R.string.anime4k_quality_m)
}

@Composable
private fun Int.scaleLabelV2(): String = when (this) {
    1 -> "1x"
    2 -> "2x"
    4 -> "4x"
    else -> stringResource(R.string.anime4k_scale_auto)
}

private fun toggleSignedSetting(value: Int, defaultValue: Int): Int {
    return if (value == 0) defaultValue else -value
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
