package com.heyanle.easybangumi4.danmaku

import com.heyanle.easybangumi4.base.preferences.Preference
import com.heyanle.easybangumi4.base.preferences.PreferenceStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DanmakuDisplayPreferencesTest {

    @Test
    fun newStoreUsesBackwardCompatibleDefaults() {
        val preferences = DanmakuDisplayPreferences(FakePreferenceStore())

        assertEquals(DanmakuDisplayConfig.DEFAULT, preferences.getConfig())
        assertEquals("danmaku_display_enabled", preferences.enabled.key())
        assertEquals("danmaku_show_scroll", preferences.showScroll.key())
        assertEquals("danmaku_show_top", preferences.showTop.key())
        assertEquals("danmaku_show_bottom", preferences.showBottom.key())
        assertEquals("danmaku_enabled_provenance", preferences.enabledProvenance.key())
        assertEquals("danmaku_time_offset_millis", preferences.timeOffsetMillis.key())
        assertEquals("danmaku_opacity", preferences.opacity.key())
        assertEquals("danmaku_area_ratio", preferences.areaRatio.key())
        assertEquals("danmaku_density_ratio", preferences.densityRatio.key())
        assertEquals("danmaku_merge_repeat_window_millis", preferences.mergeRepeatWindowMillis.key())
    }

    @Test
    fun finiteOutOfRangeValuesClampAndNonFiniteValuesUseDefaults() {
        val clamped = DanmakuDisplayConfig(
            fontSizeSp = 1f,
            lineHeightFactor = 9f,
            scrollSpeed = 0.1f,
            opacity = 0.01f,
            areaRatio = 0.1f,
            densityRatio = 0.01f,
            mergeRepeatWindowMillis = -100L,
        ).normalized()

        assertEquals(DanmakuDisplayConfig.FONT_SIZE_SP_RANGE.start, clamped.fontSizeSp)
        assertEquals(
            DanmakuDisplayConfig.LINE_HEIGHT_FACTOR_RANGE.endInclusive,
            clamped.lineHeightFactor,
        )
        assertEquals(DanmakuDisplayConfig.SCROLL_SPEED_RANGE.start, clamped.scrollSpeed)
        assertEquals(DanmakuDisplayConfig.OPACITY_RANGE.start, clamped.opacity)
        assertEquals(0.25f, clamped.areaRatio)
        assertEquals(DanmakuDisplayConfig.DENSITY_RATIO_RANGE.start, clamped.densityRatio)
        assertEquals(0L, clamped.mergeRepeatWindowMillis)

        val invalid = DanmakuDisplayConfig(
            fontSizeSp = Float.NaN,
            lineHeightFactor = Float.POSITIVE_INFINITY,
            scrollSpeed = Float.NEGATIVE_INFINITY,
            opacity = Float.NaN,
            areaRatio = Float.POSITIVE_INFINITY,
        ).normalized()

        assertEquals(DanmakuDisplayConfig.DEFAULT_FONT_SIZE_SP, invalid.fontSizeSp)
        assertEquals(DanmakuDisplayConfig.DEFAULT_LINE_HEIGHT_FACTOR, invalid.lineHeightFactor)
        assertEquals(DanmakuDisplayConfig.DEFAULT_SCROLL_SPEED, invalid.scrollSpeed)
        assertEquals(DanmakuDisplayConfig.DEFAULT_OPACITY, invalid.opacity)
        assertEquals(DanmakuDisplayConfig.DEFAULT_AREA_RATIO, invalid.areaRatio)
        assertEquals(DanmakuDisplayConfig.DEFAULT_DENSITY_RATIO, invalid.densityRatio)
        assertEquals(DanmakuDisplayConfig.DEFAULT_MERGE_REPEAT_WINDOW_MILLIS, invalid.mergeRepeatWindowMillis)
    }

    @Test
    fun areaRatioSnapsToTheNearestExposedTier() {
        assertEquals(0.25f, DanmakuDisplayConfig(areaRatio = 0.3f).normalized().areaRatio)
        assertEquals(0.5f, DanmakuDisplayConfig(areaRatio = 0.6f).normalized().areaRatio)
        assertEquals(0.75f, DanmakuDisplayConfig(areaRatio = 0.7f).normalized().areaRatio)
        assertEquals(1f, DanmakuDisplayConfig(areaRatio = 0.9f).normalized().areaRatio)
    }

    @Test
    fun scrollSpeedTiersCoverSlowerThanBeforeAndExposeBilibiliStyleLabels() {
        assertEquals(0.25f, DANMAKU_SCROLL_SPEED_TIERS.first())
        assertEquals(3f, DANMAKU_SCROLL_SPEED_TIERS.last())
        assertEquals("极慢", danmakuScrollSpeedLabel(0.25f))
        assertEquals("适中", danmakuScrollSpeedLabel(1f))
        assertEquals("极快", danmakuScrollSpeedLabel(3f))
        assertEquals("50%", danmakuAreaRatioLabel(0.5f))
        assertEquals("80%", danmakuOpacityLabel(0.8f))
    }

    @Test
    fun setConfigNormalizesAndCanBeRestoredByAnotherPreferencesInstance() {
        val store = FakePreferenceStore()
        val preferences = DanmakuDisplayPreferences(store)

        preferences.setConfig(
            DanmakuDisplayConfig(
                enabled = false,
                showScroll = false,
                showTop = true,
                showBottom = false,
                enabledProvenance = setOf("source-a"),
                timeOffsetMillis = 2_500L,
                fontSizeSp = 40f,
                lineHeightFactor = 1.5f,
                scrollSpeed = 1.75f,
                opacity = 0.3f,
                areaRatio = 0.6f,
            ),
        )

        val restored = DanmakuDisplayPreferences(store).getConfig()
        assertFalse(restored.enabled)
        assertFalse(restored.showScroll)
        assertTrue(restored.showTop)
        assertFalse(restored.showBottom)
        assertEquals(setOf("source-a"), restored.enabledProvenance)
        assertEquals(2_500L, restored.timeOffsetMillis)
        assertEquals(36f, restored.fontSizeSp)
        assertEquals(1.5f, restored.lineHeightFactor)
        // 旧版本持久化的非档位速度值会吸附到最近的档位（1.75 -> 1.5）。
        assertEquals(1.5f, restored.scrollSpeed)
        assertEquals(0.3f, restored.opacity)
        assertEquals(0.5f, restored.areaRatio)
    }

    @Test
    fun legacySpeedValuesMigrateToTheNearestPersistedTier() {
        assertEquals(1.5f, DanmakuDisplayConfig(scrollSpeed = 1.75f).normalized().scrollSpeed)
        assertEquals(2.5f, DanmakuDisplayConfig(scrollSpeed = 2.75f).normalized().scrollSpeed)
        assertEquals(0.25f, DanmakuDisplayConfig(scrollSpeed = 0.1f).normalized().scrollSpeed)
        assertEquals(3f, DanmakuDisplayConfig(scrollSpeed = 99f).normalized().scrollSpeed)
    }

    @Test
    fun configFlowReturnsTheSameNormalizedSourceOfTruth() = runBlocking {
        val store = FakePreferenceStore().apply {
            getFloat("danmaku_font_size_sp", 18f).set(Float.NaN)
            getFloat("danmaku_line_height_factor", 1.2f).set(4f)
        }
        val preferences = DanmakuDisplayPreferences(store)

        assertEquals(preferences.getConfig(), preferences.configFlow().first())
        assertEquals(18f, preferences.configFlow().first().fontSizeSp)
        assertEquals(2f, preferences.configFlow().first().lineHeightFactor)
    }

    @Test
    fun resetRestoresDisplayDefaultsWithoutEnablingHiddenDanmakuOrSources() {
        val preferences = DanmakuDisplayPreferences(FakePreferenceStore())
        preferences.setConfig(
            DanmakuDisplayConfig(
                enabled = false,
                showScroll = false,
                showTop = false,
                showBottom = false,
                enabledProvenance = setOf("source-a"),
                timeOffsetMillis = 12_345L,
                fontSizeSp = 30f,
                lineHeightFactor = 1.8f,
                scrollSpeed = 1.9f,
            ),
        )

        preferences.resetToDefaults()

        assertEquals(
            DanmakuDisplayConfig.DEFAULT.copy(
                enabled = false,
                enabledProvenance = setOf("source-a"),
            ),
            preferences.getConfig(),
        )
    }

    private class FakePreferenceStore : PreferenceStore {
        private val values = mutableMapOf<String, FakePreference<*>>()

        override fun getString(key: String, default: String) = preference(key, default)
        override fun getInt(key: String, default: Int) = preference(key, default)
        override fun getLong(key: String, default: Long) = preference(key, default)
        override fun getFloat(key: String, default: Float) = preference(key, default)
        override fun getBoolean(key: String, default: Boolean) = preference(key, default)
        override fun getStringSet(key: String, defaultValue: Set<String>) =
            preference(key, defaultValue)

        override fun <T> getObject(
            key: String,
            defaultValue: T,
            serializer: (T) -> String,
            deserializer: (String) -> T,
        ) = preference(key, defaultValue)

        override fun keySet(): Set<String> = values.keys

        @Suppress("UNCHECKED_CAST")
        private fun <T> preference(key: String, defaultValue: T): FakePreference<T> {
            return values.getOrPut(key) { FakePreference(key, defaultValue) } as FakePreference<T>
        }
    }

    private class FakePreference<T>(
        private val key: String,
        private val defaultValue: T,
    ) : Preference<T> {
        private val state = MutableStateFlow(defaultValue)
        private var isSet = false

        override fun key(): String = key
        override fun get(): T = state.value
        override fun set(value: T) {
            isSet = true
            state.value = value
        }

        override fun defaultValue(): T = defaultValue
        override fun isSet(): Boolean = isSet
        override fun delete() {
            isSet = false
            state.value = defaultValue
        }

        override fun flow(): Flow<T> = state
        override fun stateIn(scope: CoroutineScope): StateFlow<T> = state
    }
}
