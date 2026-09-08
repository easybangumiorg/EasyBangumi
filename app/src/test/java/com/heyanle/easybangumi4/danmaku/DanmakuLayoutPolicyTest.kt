package com.heyanle.easybangumi4.danmaku

import org.junit.Assert.assertEquals
import org.junit.Test

class DanmakuLayoutPolicyTest {

    @Test
    fun ratioHeightIsKeptWhenItCanFitOneLine() {
        assertEquals(
            100,
            resolveDanmakuCanvasHeightPx(
                containerHeightPx = 1_000,
                areaRatio = 0.1f,
                minimumLineHeightPx = 40,
            ),
        )
    }

    @Test
    fun shortPortraitCanvasFallsBackToOneLine() {
        assertEquals(
            40,
            resolveDanmakuCanvasHeightPx(
                containerHeightPx = 240,
                areaRatio = 0.1f,
                minimumLineHeightPx = 40,
            ),
        )
    }

    @Test
    fun minimumLineNeverExceedsTheAvailableCanvas() {
        assertEquals(
            24,
            resolveDanmakuCanvasHeightPx(
                containerHeightPx = 24,
                areaRatio = 0.1f,
                minimumLineHeightPx = 40,
            ),
        )
    }
}
