package com.heyanle.easybangumi4.danmaku

import com.heyanle.easybangumi4.base.preferences.PreferenceStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

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
) {
    fun normalized(): DanmakuDisplayConfig = copy(
        enabledProvenance = enabledProvenance.toSet(),
        fontSizeSp = fontSizeSp.normalizedIn(FONT_SIZE_SP_RANGE, DEFAULT_FONT_SIZE_SP),
        lineHeightFactor = lineHeightFactor.normalizedIn(
            LINE_HEIGHT_FACTOR_RANGE,
            DEFAULT_LINE_HEIGHT_FACTOR,
        ),
        scrollSpeed = scrollSpeed.normalizedIn(SCROLL_SPEED_RANGE, DEFAULT_SCROLL_SPEED),
    )

    companion object {
        const val DEFAULT_FONT_SIZE_SP = 18f
        const val DEFAULT_LINE_HEIGHT_FACTOR = 1.2f
        const val DEFAULT_SCROLL_SPEED = 1f

        val FONT_SIZE_SP_RANGE: ClosedFloatingPointRange<Float> = 12f..36f
        val LINE_HEIGHT_FACTOR_RANGE: ClosedFloatingPointRange<Float> = 1f..2f
        val SCROLL_SPEED_RANGE: ClosedFloatingPointRange<Float> = 0.5f..2f

        val DEFAULT = DanmakuDisplayConfig()
    }
}

private fun Float.normalizedIn(
    range: ClosedFloatingPointRange<Float>,
    defaultValue: Float,
): Float = if (isFinite()) coerceIn(range.start, range.endInclusive) else defaultValue

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
        return combine(
            switches,
            timeOffsetMillis.flow(),
            fontSizeSp.flow(),
            lineHeightFactor.flow(),
            scrollSpeed.flow(),
        ) { switchesValue, offset, fontSize, lineHeight, speed ->
            DanmakuDisplayConfig(
                enabled = switchesValue.enabled,
                showScroll = switchesValue.showScroll,
                showTop = switchesValue.showTop,
                showBottom = switchesValue.showBottom,
                enabledProvenance = switchesValue.enabledProvenance,
                timeOffsetMillis = offset,
                fontSizeSp = fontSize,
                lineHeightFactor = lineHeight,
                scrollSpeed = speed,
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
    )

    private data class Switches(
        val enabled: Boolean,
        val showScroll: Boolean,
        val showTop: Boolean,
        val showBottom: Boolean,
        val enabledProvenance: Set<String>,
    )
}

private fun <T> com.heyanle.easybangumi4.base.preferences.Preference<T>.setIfChanged(value: T) {
    if (get() != value) set(value)
}
