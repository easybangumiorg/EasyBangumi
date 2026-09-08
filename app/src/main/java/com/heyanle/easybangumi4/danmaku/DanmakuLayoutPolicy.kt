package com.heyanle.easybangumi4.danmaku

import kotlin.math.roundToInt

/**
 * Resolves the visible danmaku canvas height without mutating the persisted area ratio.
 * The configured ratio wins whenever it can fit at least one current text line.
 */
internal fun resolveDanmakuCanvasHeightPx(
    containerHeightPx: Int,
    areaRatio: Float,
    minimumLineHeightPx: Int,
): Int {
    if (containerHeightPx <= 0) return 0
    val safeRatio = areaRatio.takeIf(Float::isFinite)?.coerceIn(0f, 1f) ?: 1f
    val ratioHeight = (containerHeightPx * safeRatio).roundToInt()
    return maxOf(ratioHeight, minimumLineHeightPx.coerceAtLeast(1))
        .coerceAtMost(containerHeightPx)
}
