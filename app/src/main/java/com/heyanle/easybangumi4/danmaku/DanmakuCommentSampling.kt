package com.heyanle.easybangumi4.danmaku

import kotlin.math.roundToInt

/**
 * 最终筛选之后的展示变换（数量抽样 + 复读合并）。
 *
 * 两个变换都保持弹幕的时间顺序，输出可直接交给时间轴渲染；
 * 顺序约定为先合并复读、再做数量抽样，即数量比例作用于去重后的结果。
 */

/** 复读合并：[windowMillis] 内同文字的弹幕只保留时间最早的一条；<= 0 表示不合并。 */
fun List<DanmakuComment>.mergedRepeats(windowMillis: Long): List<DanmakuComment> {
    if (windowMillis <= 0L || isEmpty()) return this
    val lastKeptTimeByText = HashMap<String, Long>()
    val kept = mutableListOf<DanmakuComment>()
    // 注意不要写成 buildList { forEach { ... } }：内层接收者会遮蔽外层 List，
    // 导致迭代的是 buildList 自己的空列表。
    forEach { comment ->
        val lastKept = lastKeptTimeByText[comment.text]
        if (lastKept == null || comment.timeMillis - lastKept > windowMillis) {
            lastKeptTimeByText[comment.text] = comment.timeMillis
            kept += comment
        }
    }
    return kept
}

/**
 * 数量抽样：[densityRatio] (0.1 .. 1) 为保留比例，按时间顺序等距采样。
 * 0.5 表示每 2 条保留 1 条；>= 1 原样返回。
 */
fun List<DanmakuComment>.sampledByDensity(densityRatio: Float): List<DanmakuComment> {
    val ratio = densityRatio.coerceIn(0.1f, 1f)
    if (ratio >= 1f || isEmpty()) return this
    val step = (1f / ratio).roundToInt().coerceAtLeast(2)
    return filterIndexed { index, _ -> index % step == 0 }
}

/** 渲染入口：先合并复读，再按数量比例抽样。 */
fun List<DanmakuComment>.applyDisplaySampling(
    densityRatio: Float,
    mergeRepeatWindowMillis: Long,
): List<DanmakuComment> = sortedBy { it.timeMillis }
    .mergedRepeats(mergeRepeatWindowMillis)
    .sampledByDensity(densityRatio)
