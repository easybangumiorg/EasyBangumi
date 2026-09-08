package com.heyanle.easybangumi4.danmaku

import com.heyanle.easybangumi4.base.preferences.PreferenceStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Discrete, user-facing tiers behind the settings sliders.
 *
 * Speed stays a plain multiplier on the config so persistence and every settings entry
 * (player panel, V2 settings, legacy settings which reuses the panel content) share one schema;
 * the sliders are index-based over [DANMAKU_SCROLL_SPEED_TIERS] and [normalized] snaps any
 * stored value (including pre-tier legacy data) to the nearest tier.
 */
val DANMAKU_SCROLL_SPEED_TIERS = listOf(
    0.25f, 0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f, 2.5f, 3f,
)

/** Visible danmaku canvas tiers, including a compact 10% strip for subtitle-heavy videos. */
val DANMAKU_AREA_RATIO_TIERS = listOf(0.1f, 0.25f, 0.5f, 0.75f, 1f)

fun Float.snapToDanmakuScrollSpeed(): Float {
    if (!isFinite()) return DanmakuDisplayConfig.DEFAULT_SCROLL_SPEED
    val clamped = coerceIn(
        DANMAKU_SCROLL_SPEED_TIERS.first(),
        DANMAKU_SCROLL_SPEED_TIERS.last(),
    )
    return DANMAKU_SCROLL_SPEED_TIERS.minBy { tier -> abs(tier - clamped) }
}

fun danmakuScrollSpeedLabel(speed: Float): String = when (speed) {
    0.25f -> "极慢"
    0.5f -> "很慢"
    0.75f -> "慢"
    1f -> "适中"
    1.25f -> "稍快"
    1.5f -> "较快"
    2f -> "快"
    2.5f -> "很快"
    3f -> "极快"
    else -> "适中"
}

fun danmakuAreaRatioLabel(ratio: Float): String =
    "${(danmakuAreaRatioPercent(ratio)).roundToInt()}%"

fun danmakuOpacityLabel(opacity: Float): String =
    "${(opacity.coerceIn(0f, 1f) * 100).roundToInt()}%"

private fun danmakuAreaRatioPercent(ratio: Float): Float =
    ratio.coerceIn(
        DANMAKU_AREA_RATIO_TIERS.first(),
        DANMAKU_AREA_RATIO_TIERS.last(),
    ) * 100

/**
 * Renderer-independent display configuration.
 *
 * Values loaded from persistent storage must pass through [normalized] before reaching the UI or
 * renderer. This keeps third-party renderer constraints out of the preference schema and makes old
 * or corrupted values safe to consume.
 */
data class DanmakuDisplayConfig(
    val enabled: Boolean = true,
    val showScroll: Boolean = true,
    val showTop: Boolean = true,
    val showBottom: Boolean = true,
    val enabledProvenance: Set<String> = setOf(DANDANPLAY_SOURCE_ID),
    val timeOffsetMillis: Long = 0L,
    val fontSizeSp: Float = DEFAULT_FONT_SIZE_SP,
    val lineHeightFactor: Float = DEFAULT_LINE_HEIGHT_FACTOR,
    /** User-facing speed: larger values mean faster scrolling. */
    val scrollSpeed: Float = DEFAULT_SCROLL_SPEED,
    /** Global danmaku opacity, 0.1 (almost invisible) .. 1 (opaque). */
    val opacity: Float = DEFAULT_OPACITY,
    /**
     * 弹幕画布高度占视频高度的比例，吸附到 [DANMAKU_AREA_RATIO_TIERS]（0.1 .. 1）。
     * 由 Compose 布局承载（DanmakuView 顶部对齐、高度 = areaRatio × 视频高度）：
     * 滚动/顶部/底部三种类型的弹幕都被约束在画布内，与 show* 类型开关完全正交。
     */
    val areaRatio: Float = DEFAULT_AREA_RATIO,
    /**
     * 弹幕数量比例（0.1 .. 1）：作用于全部筛选之后的最终列表，按顺序等距抽样。
     * 0.5 表示每 2 条保留 1 条。
     */
    val densityRatio: Float = DEFAULT_DENSITY_RATIO,
    /** 复读合并窗口（0 .. 5000ms）：窗口内同文字弹幕只保留第一条；0 = 不合并。 */
    val mergeRepeatWindowMillis: Long = DEFAULT_MERGE_REPEAT_WINDOW_MILLIS,
    /** 屏蔽词总开关；关闭时保留规则，仅暂停匹配。 */
    val blockRulesEnabled: Boolean = true,
    /** 普通文本规则按忽略大小写的“包含”语义匹配。 */
    val blockedTextRules: Set<String> = emptySet(),
    /** 正则规则使用 Kotlin Regex 的 containsMatchIn 语义；非法表达式会被忽略。 */
    val blockedRegexRules: Set<String> = emptySet(),
    /** 播放倍速变化时同步调整滚动弹幕速度。 */
    val syncScrollSpeedWithPlayback: Boolean = true,
    /** 最终入场检查：同一行前一条尚未完全进入屏幕时等待，过期则丢弃。 */
    val preventScrollOcclusion: Boolean = false,
) {
    fun normalized(): DanmakuDisplayConfig = copy(
        enabledProvenance = enabledProvenance.toSet(),
        fontSizeSp = fontSizeSp.normalizedIn(FONT_SIZE_SP_RANGE, DEFAULT_FONT_SIZE_SP),
        lineHeightFactor = lineHeightFactor.normalizedIn(
            LINE_HEIGHT_FACTOR_RANGE,
            DEFAULT_LINE_HEIGHT_FACTOR,
        ),
        scrollSpeed = scrollSpeed.snapToDanmakuScrollSpeed(),
        opacity = opacity.normalizedIn(OPACITY_RANGE, DEFAULT_OPACITY),
        areaRatio = areaRatio.snapToAreaRatio(),
        densityRatio = densityRatio.normalizedIn(DENSITY_RATIO_RANGE, DEFAULT_DENSITY_RATIO),
        mergeRepeatWindowMillis = mergeRepeatWindowMillis.coerceIn(
            MERGE_REPEAT_WINDOW_RANGE.start,
            MERGE_REPEAT_WINDOW_RANGE.endInclusive,
        ),
        blockedTextRules = blockedTextRules.normalizeDanmakuRules(),
        blockedRegexRules = blockedRegexRules.normalizeDanmakuRules(),
    )

    companion object {
        const val DEFAULT_FONT_SIZE_SP = 18f
        const val DEFAULT_LINE_HEIGHT_FACTOR = 1.2f
        const val DEFAULT_SCROLL_SPEED = 1f
        const val DEFAULT_OPACITY = 1f
        const val DEFAULT_AREA_RATIO = 1f
        const val DEFAULT_DENSITY_RATIO = 1f
        const val DEFAULT_MERGE_REPEAT_WINDOW_MILLIS = 0L

        val FONT_SIZE_SP_RANGE: ClosedFloatingPointRange<Float> = 12f..36f
        val LINE_HEIGHT_FACTOR_RANGE: ClosedFloatingPointRange<Float> = 1f..2f
        val SCROLL_SPEED_RANGE: ClosedFloatingPointRange<Float> =
            DANMAKU_SCROLL_SPEED_TIERS.first()..DANMAKU_SCROLL_SPEED_TIERS.last()
        val OPACITY_RANGE: ClosedFloatingPointRange<Float> = 0.1f..1f
        val DENSITY_RATIO_RANGE: ClosedFloatingPointRange<Float> = 0.1f..1f
        val MERGE_REPEAT_WINDOW_RANGE: ClosedRange<Long> = 0L..5000L

        val DEFAULT = DanmakuDisplayConfig()
    }
}

private fun Float.normalizedIn(
    range: ClosedFloatingPointRange<Float>,
    defaultValue: Float,
): Float = if (isFinite()) coerceIn(range.start, range.endInclusive) else defaultValue

private fun Float.snapToAreaRatio(): Float {
    if (!isFinite()) return DanmakuDisplayConfig.DEFAULT_AREA_RATIO
    val clamped = coerceIn(
        DANMAKU_AREA_RATIO_TIERS.first(),
        DANMAKU_AREA_RATIO_TIERS.last(),
    )
    return DANMAKU_AREA_RATIO_TIERS.minBy { tier -> abs(tier - clamped) }
}

/** User-selectable source state. Bindings and caches are stored separately from preferences. */
class DanmakuPreferences(
    preferenceStore: PreferenceStore,
) {
    val enabledSourceIds = preferenceStore.getStringSet(
        key = "danmaku_enabled_sources",
        defaultValue = setOf(DANDANPLAY_SOURCE_ID),
    )
    val defaultSourceId = preferenceStore.getString(
        key = "danmaku_default_source",
        default = DANDANPLAY_SOURCE_ID,
    )
}

/** Display preferences are independent from source enablement and apply immediately in playback. */
class DanmakuDisplayPreferences(
    preferenceStore: PreferenceStore,
) {
    val enabled = preferenceStore.getBoolean("danmaku_display_enabled", true)
    val showScroll = preferenceStore.getBoolean("danmaku_show_scroll", true)
    val showTop = preferenceStore.getBoolean("danmaku_show_top", true)
    val showBottom = preferenceStore.getBoolean("danmaku_show_bottom", true)
    val enabledProvenance = preferenceStore.getStringSet(
        key = "danmaku_enabled_provenance",
        defaultValue = setOf(DANDANPLAY_SOURCE_ID),
    )
    val timeOffsetMillis = preferenceStore.getLong("danmaku_time_offset_millis", 0L)
    val fontSizeSp = preferenceStore.getFloat(
        "danmaku_font_size_sp",
        DanmakuDisplayConfig.DEFAULT_FONT_SIZE_SP,
    )
    val lineHeightFactor = preferenceStore.getFloat(
        "danmaku_line_height_factor",
        DanmakuDisplayConfig.DEFAULT_LINE_HEIGHT_FACTOR,
    )
    val scrollSpeed = preferenceStore.getFloat(
        "danmaku_scroll_speed",
        DanmakuDisplayConfig.DEFAULT_SCROLL_SPEED,
    )
    val opacity = preferenceStore.getFloat(
        "danmaku_opacity",
        DanmakuDisplayConfig.DEFAULT_OPACITY,
    )
    val areaRatio = preferenceStore.getFloat(
        "danmaku_area_ratio",
        DanmakuDisplayConfig.DEFAULT_AREA_RATIO,
    )
    val densityRatio = preferenceStore.getFloat(
        "danmaku_density_ratio",
        DanmakuDisplayConfig.DEFAULT_DENSITY_RATIO,
    )
    val mergeRepeatWindowMillis = preferenceStore.getLong(
        "danmaku_merge_repeat_window_millis",
        DanmakuDisplayConfig.DEFAULT_MERGE_REPEAT_WINDOW_MILLIS,
    )
    val blockedTextRules = preferenceStore.getStringSet("danmaku_blocked_text_rules", emptySet())
    val blockedRegexRules = preferenceStore.getStringSet("danmaku_blocked_regex_rules", emptySet())
    val blockRulesEnabled = preferenceStore.getBoolean("danmaku_block_rules_enabled", true)
    val syncScrollSpeedWithPlayback = preferenceStore.getBoolean(
        "danmaku_sync_scroll_speed_with_playback",
        true,
    )
    val preventScrollOcclusion = preferenceStore.getBoolean("danmaku_prevent_scroll_occlusion", false)

    /** Returns one normalized snapshot for synchronous consumers. */
    fun getConfig(): DanmakuDisplayConfig = rawConfig().normalized()

    /** One canonical stream shared by the player, its settings panel and global settings. */
    fun configFlow(): Flow<DanmakuDisplayConfig> {
        val switches = combine(
            enabled.flow(),
            showScroll.flow(),
            showTop.flow(),
            showBottom.flow(),
            enabledProvenance.flow(),
        ) { enabled, showScroll, showTop, showBottom, enabledProvenance ->
            Switches(
                enabled = enabled,
                showScroll = showScroll,
                showTop = showTop,
                showBottom = showBottom,
                enabledProvenance = enabledProvenance,
            )
        }
        val visuals = combine(
            opacity.flow(),
            areaRatio.flow(),
            timeOffsetMillis.flow(),
        ) { opacity, areaRatio, offset ->
            Visuals(
                opacity = opacity,
                areaRatio = areaRatio,
                timeOffsetMillis = offset,
            )
        }
        val blockRules = combine(
            blockRulesEnabled.flow(),
            blockedTextRules.flow(),
            blockedRegexRules.flow(),
        ) { enabled, textRules, regexRules ->
            BlockRules(enabled, textRules, regexRules)
        }
        val filtering = combine(
            densityRatio.flow(),
            mergeRepeatWindowMillis.flow(),
            preventScrollOcclusion.flow(),
            blockRules,
        ) { density, mergeWindow, preventOcclusion, rules ->
            Filtering(density, mergeWindow, preventOcclusion, rules)
        }
        val style = combine(
            fontSizeSp.flow(),
            lineHeightFactor.flow(),
            scrollSpeed.flow(),
            syncScrollSpeedWithPlayback.flow(),
        ) { fontSize, lineHeight, speed, syncSpeed ->
            Style(fontSize, lineHeight, speed, syncSpeed)
        }
        return combine(
            switches,
            visuals,
            filtering,
            style,
        ) { switchesValue, visualsValue, filteringValue, styleValue ->
            DanmakuDisplayConfig(
                enabled = switchesValue.enabled,
                showScroll = switchesValue.showScroll,
                showTop = switchesValue.showTop,
                showBottom = switchesValue.showBottom,
                enabledProvenance = switchesValue.enabledProvenance,
                opacity = visualsValue.opacity,
                areaRatio = visualsValue.areaRatio,
                densityRatio = filteringValue.densityRatio,
                mergeRepeatWindowMillis = filteringValue.mergeRepeatWindowMillis,
                blockRulesEnabled = filteringValue.blockRules.enabled,
                blockedTextRules = filteringValue.blockRules.textRules,
                blockedRegexRules = filteringValue.blockRules.regexRules,
                timeOffsetMillis = visualsValue.timeOffsetMillis,
                fontSizeSp = styleValue.fontSizeSp,
                lineHeightFactor = styleValue.lineHeightFactor,
                scrollSpeed = styleValue.scrollSpeed,
                syncScrollSpeedWithPlayback = styleValue.syncScrollSpeedWithPlayback,
                preventScrollOcclusion = filteringValue.preventScrollOcclusion,
            ).normalized()
        }.distinctUntilChanged()
    }

    /** Persists a complete normalized snapshot while retaining all existing preference keys. */
    fun setConfig(config: DanmakuDisplayConfig) {
        val value = config.normalized()
        enabled.setIfChanged(value.enabled)
        showScroll.setIfChanged(value.showScroll)
        showTop.setIfChanged(value.showTop)
        showBottom.setIfChanged(value.showBottom)
        enabledProvenance.setIfChanged(value.enabledProvenance)
        timeOffsetMillis.setIfChanged(value.timeOffsetMillis)
        fontSizeSp.setIfChanged(value.fontSizeSp)
        lineHeightFactor.setIfChanged(value.lineHeightFactor)
        scrollSpeed.setIfChanged(value.scrollSpeed)
        opacity.setIfChanged(value.opacity)
        areaRatio.setIfChanged(value.areaRatio)
        densityRatio.setIfChanged(value.densityRatio)
        mergeRepeatWindowMillis.setIfChanged(value.mergeRepeatWindowMillis)
        blockedTextRules.setIfChanged(value.blockedTextRules)
        blockedRegexRules.setIfChanged(value.blockedRegexRules)
        blockRulesEnabled.setIfChanged(value.blockRulesEnabled)
        syncScrollSpeedWithPlayback.setIfChanged(value.syncScrollSpeedWithPlayback)
        preventScrollOcclusion.setIfChanged(value.preventScrollOcclusion)
    }

    fun updateConfig(transform: (DanmakuDisplayConfig) -> DanmakuDisplayConfig) {
        setConfig(transform(getConfig()))
    }

    /**
     * Restores the configurable display defaults required by the player settings.
     *
     * The user's current visibility choice and provenance filter are intentionally retained:
     * "restore display defaults" must not unexpectedly enable hidden danmaku or re-enable a source.
     */
    fun resetToDefaults() {
        updateConfig { current ->
            current.copy(
                showScroll = DanmakuDisplayConfig.DEFAULT.showScroll,
                showTop = DanmakuDisplayConfig.DEFAULT.showTop,
                showBottom = DanmakuDisplayConfig.DEFAULT.showBottom,
                timeOffsetMillis = DanmakuDisplayConfig.DEFAULT.timeOffsetMillis,
                fontSizeSp = DanmakuDisplayConfig.DEFAULT.fontSizeSp,
                lineHeightFactor = DanmakuDisplayConfig.DEFAULT.lineHeightFactor,
                scrollSpeed = DanmakuDisplayConfig.DEFAULT.scrollSpeed,
                opacity = DanmakuDisplayConfig.DEFAULT.opacity,
                areaRatio = DanmakuDisplayConfig.DEFAULT.areaRatio,
                densityRatio = DanmakuDisplayConfig.DEFAULT.densityRatio,
                mergeRepeatWindowMillis = DanmakuDisplayConfig.DEFAULT.mergeRepeatWindowMillis,
                syncScrollSpeedWithPlayback = DanmakuDisplayConfig.DEFAULT.syncScrollSpeedWithPlayback,
                preventScrollOcclusion = DanmakuDisplayConfig.DEFAULT.preventScrollOcclusion,
            )
        }
    }

    private fun rawConfig() = DanmakuDisplayConfig(
        enabled = enabled.get(),
        showScroll = showScroll.get(),
        showTop = showTop.get(),
        showBottom = showBottom.get(),
        enabledProvenance = enabledProvenance.get(),
        timeOffsetMillis = timeOffsetMillis.get(),
        fontSizeSp = fontSizeSp.get(),
        lineHeightFactor = lineHeightFactor.get(),
        scrollSpeed = scrollSpeed.get(),
        opacity = opacity.get(),
        areaRatio = areaRatio.get(),
        densityRatio = densityRatio.get(),
        mergeRepeatWindowMillis = mergeRepeatWindowMillis.get(),
        blockRulesEnabled = blockRulesEnabled.get(),
        blockedTextRules = blockedTextRules.get(),
        blockedRegexRules = blockedRegexRules.get(),
        syncScrollSpeedWithPlayback = syncScrollSpeedWithPlayback.get(),
        preventScrollOcclusion = preventScrollOcclusion.get(),
    )

    private data class Switches(
        val enabled: Boolean,
        val showScroll: Boolean,
        val showTop: Boolean,
        val showBottom: Boolean,
        val enabledProvenance: Set<String>,
    )

    private data class Visuals(
        val opacity: Float,
        val areaRatio: Float,
        val timeOffsetMillis: Long,
    )

    private data class Filtering(
        val densityRatio: Float,
        val mergeRepeatWindowMillis: Long,
        val preventScrollOcclusion: Boolean,
        val blockRules: BlockRules,
    )

    private data class BlockRules(
        val enabled: Boolean,
        val textRules: Set<String>,
        val regexRules: Set<String>,
    )

    private data class Style(
        val fontSizeSp: Float,
        val lineHeightFactor: Float,
        val scrollSpeed: Float,
        val syncScrollSpeedWithPlayback: Boolean,
    )
}

private fun Set<String>.normalizeDanmakuRules(): Set<String> = asSequence()
    .map(String::trim)
    .filter(String::isNotEmpty)
    .map { it.take(256) }
    .toCollection(linkedSetOf())

private fun <T> com.heyanle.easybangumi4.base.preferences.Preference<T>.setIfChanged(value: T) {
    if (get() != value) set(value)
}
