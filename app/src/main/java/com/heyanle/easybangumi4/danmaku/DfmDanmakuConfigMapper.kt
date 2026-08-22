package com.heyanle.easybangumi4.danmaku

import kotlin.math.roundToInt

/**
 * DFM 0.9.25 uses a duration factor for scrolling items: a larger factor means a longer duration
 * and therefore slower movement. Keep that inverse API detail behind this renderer adapter.
 */
internal data class DfmDanmakuStyle(
    val textSizePx: Float,
    val marginPx: Int,
    val scrollDurationFactor: Float,
)

internal fun DanmakuDisplayConfig.toDfmStyle(scaledDensity: Float): DfmDanmakuStyle {
    val config = normalized()
    val safeDensity = scaledDensity.takeIf { it.isFinite() && it > 0f } ?: 1f
    val textSizePx = config.fontSizeSp * safeDensity
    return DfmDanmakuStyle(
        textSizePx = textSizePx,
        // DFM's global margin is the non-negative gap between neighboring retained tracks.
        marginPx = (textSizePx * (config.lineHeightFactor - 1f))
            .roundToInt()
            .coerceAtLeast(0),
        // Only DFM's shared scrolling Duration uses this factor. Fixed top/bottom use a separate
        // duration object, so their timing remains unchanged.
        scrollDurationFactor = (1f / config.scrollSpeed).coerceIn(0.5f, 2f),
    )
}
