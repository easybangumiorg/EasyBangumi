package com.heyanle.easybangumi4.danmaku

import kotlin.math.ceil

/** Default visual speed: a typical 360dp-wide phone is crossed in roughly 4–6 seconds. */
internal const val SCROLL_DANMAKU_BASE_SPEED_DP_PER_MEDIA_SECOND = 100f
internal const val SCROLL_DANMAKU_MAX_WAIT_MILLIS = 1_000L
internal const val SCROLL_OCCLUSION_HELP = "开启后，同一行前一条滚动弹幕完全进入屏幕，才允许下一条进入。此检查在屏蔽词、来源、复读合并、数量等筛选之后执行；最多等待 1 秒，超时会直接丢弃部分弹幕。只影响滚动弹幕。"

/** Linear motion with a movable anchor: changing speed must not move the current position. */
internal class ScrollDanmakuMotion(
    private var anchorTimeMillis: Long,
    private var anchorLeftPx: Float,
    var pixelsPerMediaSecond: Float,
) {
    fun leftAt(timeMillis: Long): Float = anchorLeftPx -
        (timeMillis - anchorTimeMillis).coerceAtLeast(0L) * pixelsPerMediaSecond / 1_000f

    fun changeSpeed(timeMillis: Long, speed: Float) {
        anchorLeftPx = leftAt(timeMillis)
        anchorTimeMillis = maxOf(anchorTimeMillis, timeMillis)
        pixelsPerMediaSecond = speed
    }

    fun endTimeMillis(textWidthPx: Float): Long = anchorTimeMillis +
        ceil((anchorLeftPx + textWidthPx).coerceAtLeast(0f) * 1_000.0 / pixelsPerMediaSecond)
            .toLong()
}

internal enum class ScrollAdmission { ENTER, WAIT, DROP }

/** Deadline is based on the original timestamp, not on the first attempted frame. */
internal fun scrollAdmission(
    timeMillis: Long,
    scheduledTimeMillis: Long,
    rowAvailable: Boolean,
): ScrollAdmission = when {
    timeMillis - scheduledTimeMillis > SCROLL_DANMAKU_MAX_WAIT_MILLIS -> ScrollAdmission.DROP
    rowAvailable -> ScrollAdmission.ENTER
    else -> ScrollAdmission.WAIT
}
