package com.heyanle.easybangumi4.danmaku

import org.junit.Assert.assertEquals
import org.junit.Test

class DanmakuCommentSamplingTest {

    private fun comment(timeMillis: Long, text: String = "弹幕") = DanmakuComment(
        id = timeMillis,
        timeMillis = timeMillis,
        mode = DanmakuDisplayMode.SCROLL,
        colorArgb = 0xFFFFFF,
        userId = null,
        text = text,
    )

    @Test
    fun mergeRepeatsKeepsFirstOccurrenceInsideWindow() {
        val comments = listOf(
            comment(0, "复读"),
            comment(1000, "复读"),
            comment(2000, "复读"),
            comment(5000, "复读"),
        )

        val merged = comments.mergedRepeats(windowMillis = 3000L)

        assertEquals(listOf(0L, 5000L), merged.map { it.timeMillis })
    }

    @Test
    fun mergeRepeatsWithZeroWindowIsDisabled() {
        val comments = listOf(
            comment(0, "复读"),
            comment(0, "复读"),
            comment(0, "复读"),
        )

        assertEquals(comments, comments.mergedRepeats(windowMillis = 0L))
    }

    @Test
    fun densitySamplingKeepsEvenStride() {
        val comments = (0 until 10).map { comment(it.toLong()) }

        val sampled = comments.sampledByDensity(densityRatio = 0.5f)

        assertEquals((0 until 10 step 2).map { it.toLong() }, sampled.map { it.id })
    }

    @Test
    fun densitySamplingFullyKeepsOrReducesAtBounds() {
        val comments = (0 until 5).map { comment(it.toLong()) }

        assertEquals(comments, comments.sampledByDensity(densityRatio = 1f))
        assertEquals(listOf(comment(0L)), comments.sampledByDensity(densityRatio = 0.1f))
    }

    @Test
    fun displaySamplingMergesFirstThenSamples() {
        val comments = listOf(
            comment(0, "复读"),
            comment(500, "复读"),
            comment(1500, "独白"),
            comment(3000, "独白"),
            comment(4500, "独白"),
            comment(6000, "独白"),
        )

        // 先合并复读（500 落入 0 的窗口被丢弃），再按 50% 等距抽样。
        val result = comments.applyDisplaySampling(
            densityRatio = 0.5f,
            mergeRepeatWindowMillis = 1000L,
        )

        assertEquals(listOf(0L, 3000L, 6000L), result.map { it.timeMillis })
    }

    @Test
    fun emptyInputStaysEmptyThroughBothTransforms() {
        val result = emptyList<DanmakuComment>().applyDisplaySampling(
            densityRatio = 0.1f,
            mergeRepeatWindowMillis = 5000L,
        )

        assertEquals(emptyList<DanmakuComment>(), result)
    }
}
