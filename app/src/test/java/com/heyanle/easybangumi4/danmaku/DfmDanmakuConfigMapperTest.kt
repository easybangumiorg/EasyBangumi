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
                scrollPixelsPerMediaSecond = 200f,
            ),
            DanmakuDisplayConfig.DEFAULT.toDfmStyle(scaledDensity = 2f),
        )
    }

    @Test
    fun fasterUserSpeedMapsToGreaterLinearVelocity() {
        val slow = DanmakuDisplayConfig.DEFAULT
            .copy(scrollSpeed = 0.5f)
            .toDfmStyle(scaledDensity = 1f)
        val fast = DanmakuDisplayConfig.DEFAULT
            .copy(scrollSpeed = 2f)
            .toDfmStyle(scaledDensity = 1f)

        assertEquals(50f, slow.scrollPixelsPerMediaSecond, 0f)
        assertEquals(200f, fast.scrollPixelsPerMediaSecond, 0f)
    }

    @Test
    fun extendedSpeedTiersMapToTheFullFactorRange() {
        val slowest = DanmakuDisplayConfig.DEFAULT
            .copy(scrollSpeed = 0.25f)
            .toDfmStyle(scaledDensity = 1f)
        val fastest = DanmakuDisplayConfig.DEFAULT
            .copy(scrollSpeed = 3f)
            .toDfmStyle(scaledDensity = 1f)

        assertEquals(25f, slowest.scrollPixelsPerMediaSecond, 0f)
        assertEquals(300f, fastest.scrollPixelsPerMediaSecond, 0f)
    }

    @Test
    fun syncedMotionUsesMediaVelocityAndNaturallySpeedsUpOnTheWallClock() {
        val style = DanmakuDisplayConfig.DEFAULT.toDfmStyle(
            scaledDensity = 1f,
            playbackSpeed = 2f,
        )

        assertEquals(100f, style.scrollPixelsPerMediaSecond, 0f)
        assertEquals(200f, style.scrollPixelsPerMediaSecond * 2f, 0f)
    }

    @Test
    fun disablingSyncCancelsPlaybackSpeedButRetainsConfiguredSpeed() {
        val style = DanmakuDisplayConfig.DEFAULT.copy(
            scrollSpeed = 3f,
            syncScrollSpeedWithPlayback = false,
        ).toDfmStyle(scaledDensity = 3f, density = 2f, playbackSpeed = 4f)
        assertEquals(150f, style.scrollPixelsPerMediaSecond, 0f)
        assertEquals(600f, style.scrollPixelsPerMediaSecond * 4f, 0f)
        val largerFont = DanmakuDisplayConfig.DEFAULT.copy(fontSizeSp = 36f)
            .toDfmStyle(scaledDensity = 3f, density = 2f)
        assertEquals(200f, largerFont.scrollPixelsPerMediaSecond, 0f)
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
