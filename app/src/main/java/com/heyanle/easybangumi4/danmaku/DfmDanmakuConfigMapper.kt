package com.heyanle.easybangumi4.danmaku

import kotlin.math.roundToInt

/** Text uses scaled density; motion uses physical density and is independent of text length. */
internal data class DfmDanmakuStyle(
    val textSizePx: Float,
    val marginPx: Int,
    val scrollPixelsPerMediaSecond: Float,
)

internal fun DanmakuDisplayConfig.toDfmStyle(
    scaledDensity: Float,
    playbackSpeed: Float = 1f,
    density: Float = scaledDensity,
): DfmDanmakuStyle {
    val config = normalized()
    val safeDensity = scaledDensity.takeIf { it.isFinite() && it > 0f } ?: 1f
    val textSizePx = config.fontSizeSp * safeDensity
    return DfmDanmakuStyle(
        textSizePx = textSizePx,
        // DFM's global margin is the non-negative gap between neighboring retained tracks.
        marginPx = (textSizePx * (config.lineHeightFactor - 1f))
            .roundToInt()
            .coerceAtLeast(0),
        // Motion is expressed per MEDIA second because DFM's timer follows player position.
        // With sync enabled, faster playback naturally produces faster screen movement. With sync
        // disabled, divide by playback speed to keep wall-clock movement constant.
        scrollPixelsPerMediaSecond = SCROLL_DANMAKU_BASE_SPEED_DP_PER_MEDIA_SECOND *
            (density.takeIf { it.isFinite() && it > 0f } ?: 1f) * config.scrollSpeed /
            (if (config.syncScrollSpeedWithPlayback) 1f else playbackSpeed.normalizedPlaybackSpeed()),
    )
}
