package com.heyanle.easybangumi4.danmaku

import org.junit.Assert.assertEquals
import org.junit.Test

class DfmDanmakuConfigMapperTest {

    @Test
    fun defaultStyleMapsSpMarginAndNeutralSpeed() {
        assertEquals(
            DfmDanmakuStyle(
                textSizePx = 36f,
                marginPx = 7,
                scrollDurationFactor = 1f,
            ),
            DanmakuDisplayConfig.DEFAULT.toDfmStyle(scaledDensity = 2f),
        )
    }

    @Test
    fun fasterUserSpeedMapsToShorterDfmScrollDuration() {
        val slow = DanmakuDisplayConfig.DEFAULT
            .copy(scrollSpeed = 0.5f)
            .toDfmStyle(scaledDensity = 1f)
        val fast = DanmakuDisplayConfig.DEFAULT
            .copy(scrollSpeed = 2f)
            .toDfmStyle(scaledDensity = 1f)

        assertEquals(2f, slow.scrollDurationFactor, 0f)
        assertEquals(0.5f, fast.scrollDurationFactor, 0f)
    }

    @Test
    fun lineHeightRangeMapsToNonNegativeTrackMargin() {
        val compact = DanmakuDisplayConfig.DEFAULT
            .copy(fontSizeSp = 12f, lineHeightFactor = 1f)
            .toDfmStyle(scaledDensity = 3f)
        val spacious = DanmakuDisplayConfig.DEFAULT
            .copy(fontSizeSp = 36f, lineHeightFactor = 2f)
            .toDfmStyle(scaledDensity = 3f)

        assertEquals(0, compact.marginPx)
        assertEquals(108, spacious.marginPx)
    }

    @Test
    fun invalidDensityFallsBackToOne() {
        assertEquals(
            18f,
            DanmakuDisplayConfig.DEFAULT.toDfmStyle(Float.NaN).textSizePx,
            0f,
        )
    }
}
