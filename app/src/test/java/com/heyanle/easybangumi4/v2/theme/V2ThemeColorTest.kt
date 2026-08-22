package com.heyanle.easybangumi4.v2.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.max
import kotlin.math.min

class V2ThemeColorTest {

    @Test
    fun storageKeys_areUniqueAndRoundTrip() {
        assertEquals(
            V2ThemeColor.entries.size,
            V2ThemeColor.entries.map(V2ThemeColor::storageKey).toSet().size,
        )
        V2ThemeColor.entries.forEach { color ->
            assertSame(color, V2ThemeColor.fromStorage(color.storageKey))
        }
    }

    @Test
    fun unknownStorageKey_fallsBackToBrandYellow() {
        assertSame(V2ThemeColor.BrandYellow, V2ThemeColor.fromStorage("unknown"))
    }

    @Test
    fun everyThemeColor_hasAReadableName() {
        assertTrue(V2ThemeColor.entries.all { it.displayName.isNotBlank() })
    }

    @Test
    fun everyThemeColor_hasDistinctDayAndNightPalettes() {
        V2ThemeColor.entries.forEach { themeColor ->
            assertTrue(themeColor.day.accent != themeColor.night.accent)
            assertTrue(themeColor.day.accentContainer != themeColor.night.accentContainer)
            assertSame(themeColor.day, themeColor.colors(isDark = false))
            assertSame(themeColor.night, themeColor.colors(isDark = true))
        }
    }

    @Test
    fun nightAccents_remainVisibleOnV2DarkBackground() {
        val darkBackground = Color(0xFF121212)
        V2ThemeColor.entries.forEach { themeColor ->
            assertTrue(
                "${themeColor.displayName} night accent is too dim",
                contrastRatio(themeColor.night.accent, darkBackground) >= 4.5,
            )
        }
    }

    @Test
    fun dayAccents_remainVisibleOnV2LightBackground() {
        val lightBackground = Color(0xFFFBFAF7)
        V2ThemeColor.entries.forEach { themeColor ->
            assertTrue(
                "${themeColor.displayName} day accent is too light",
                contrastRatio(themeColor.day.accent, lightBackground) >= 4.5,
            )
        }
    }

    @Test
    fun accentForegrounds_areReadableInBothModes() {
        V2ThemeColor.entries.forEach { themeColor ->
            listOf(themeColor.day, themeColor.night).forEach { colors ->
                assertTrue(
                    "${themeColor.displayName} onAccent is unreadable",
                    contrastRatio(colors.accent, colors.onAccent) >= 4.5,
                )
                assertTrue(
                    "${themeColor.displayName} onAccentContainer is unreadable",
                    contrastRatio(colors.accentContainer, colors.onAccentContainer) >= 4.5,
                )
            }
        }
    }

    private fun contrastRatio(foreground: Color, background: Color): Double {
        val foregroundLuminance = relativeLuminance(foreground)
        val backgroundLuminance = relativeLuminance(background)
        return (max(foregroundLuminance, backgroundLuminance) + 0.05) /
            (min(foregroundLuminance, backgroundLuminance) + 0.05)
    }

    private fun relativeLuminance(color: Color): Double {
        fun channel(value: Double): Double {
            return if (value <= 0.04045) value / 12.92 else Math.pow((value + 0.055) / 1.055, 2.4)
        }
        return 0.2126 * channel(color.red.toDouble()) +
            0.7152 * channel(color.green.toDouble()) +
            0.0722 * channel(color.blue.toDouble())
    }
}
