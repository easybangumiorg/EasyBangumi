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
    }

    @Test
    fun finiteOutOfRangeValuesClampAndNonFiniteValuesUseDefaults() {
        val clamped = DanmakuDisplayConfig(
            fontSizeSp = 1f,
            lineHeightFactor = 9f,
            scrollSpeed = 0.1f,
        ).normalized()

        assertEquals(DanmakuDisplayConfig.FONT_SIZE_SP_RANGE.start, clamped.fontSizeSp)
        assertEquals(
            DanmakuDisplayConfig.LINE_HEIGHT_FACTOR_RANGE.endInclusive,
            clamped.lineHeightFactor,
        )
        assertEquals(DanmakuDisplayConfig.SCROLL_SPEED_RANGE.start, clamped.scrollSpeed)

        val invalid = DanmakuDisplayConfig(
            fontSizeSp = Float.NaN,
            lineHeightFactor = Float.POSITIVE_INFINITY,
            scrollSpeed = Float.NEGATIVE_INFINITY,
        ).normalized()

        assertEquals(DanmakuDisplayConfig.DEFAULT_FONT_SIZE_SP, invalid.fontSizeSp)
        assertEquals(DanmakuDisplayConfig.DEFAULT_LINE_HEIGHT_FACTOR, invalid.lineHeightFactor)
        assertEquals(DanmakuDisplayConfig.DEFAULT_SCROLL_SPEED, invalid.scrollSpeed)
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
        assertEquals(1.75f, restored.scrollSpeed)
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
