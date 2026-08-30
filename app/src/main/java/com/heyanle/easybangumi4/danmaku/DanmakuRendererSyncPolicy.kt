package com.heyanle.easybangumi4.danmaku

/** Commands understood by the Android/DFM adapter. They deliberately contain no Android types. */
internal sealed interface DanmakuRendererCommand {
    data object ReleaseAttachedView : DanmakuRendererCommand
    data object PrepareAttachedView : DanmakuRendererCommand
    data object ReplaceItems : DanmakuRendererCommand
    data object ClearItems : DanmakuRendererCommand
    data class StartAt(val positionMillis: Long) : DanmakuRendererCommand
    data class StartPausedAt(val positionMillis: Long) : DanmakuRendererCommand
    data class SeekTo(val positionMillis: Long) : DanmakuRendererCommand
    data class SeekPausedTo(val positionMillis: Long) : DanmakuRendererCommand
    data object Pause : DanmakuRendererCommand
    data object Resume : DanmakuRendererCommand
}

/**
 * The kind of native renderer state affected by a display-config transition.
 *
 * DFM can apply all of these changes to its existing item set. Keeping the classification
 * independent from DFM still lets the adapter update only the affected native state.
 */
internal enum class DanmakuRendererConfigEffect {
    NONE,
    VISIBILITY_ONLY,
    CONTENT,
    STYLE,
    REPLACE_ITEMS,
}

internal fun classifyDanmakuConfigChange(
    previous: DanmakuDisplayConfig,
    next: DanmakuDisplayConfig,
): DanmakuRendererConfigEffect {
    if (previous == next) return DanmakuRendererConfigEffect.NONE

    // 显示区域由 Compose 布局承载（弹幕画布高度 = areaRatio × 视频高度），
    // 变化时视图自身重排，不需要任何原生渲染命令。
    if (previous.areaRatio != next.areaRatio && previous.copy(areaRatio = next.areaRatio) == next) {
        return DanmakuRendererConfigEffect.NONE
    }

    if (
        previous.densityRatio != next.densityRatio ||
        previous.mergeRepeatWindowMillis != next.mergeRepeatWindowMillis
    ) {
        // 数量抽样与复读合并作用于弹幕条目集本身，必须重建时间轴；
        // 该效果优先级最高，与样式字段同时变化时由 STYLE 路径顺带应用原生样式。
        return DanmakuRendererConfigEffect.REPLACE_ITEMS
    }

    if (
        previous.fontSizeSp != next.fontSizeSp ||
        previous.lineHeightFactor != next.lineHeightFactor ||
        previous.scrollSpeed != next.scrollSpeed ||
        previous.opacity != next.opacity
    ) {
        // 透明度走 DFM 全局 paint alpha，归入 STYLE 走整套应用路径。
        return DanmakuRendererConfigEffect.STYLE
    }

    if (
        previous.showScroll != next.showScroll ||
        previous.showTop != next.showTop ||
        previous.showBottom != next.showBottom ||
        previous.enabledProvenance != next.enabledProvenance ||
        previous.timeOffsetMillis != next.timeOffsetMillis
    ) {
        return DanmakuRendererConfigEffect.CONTENT
    }

    return DanmakuRendererConfigEffect.VISIBILITY_ONLY
}

/**
 * Pure lifecycle and playback-clock decisions for the DFM adapter.
 *
 * DFM owns a clock separate from ExoPlayer. In particular, pause/resume must not be represented as
 * seek, while a recreated Android view must start from the latest player position. Keeping these
 * decisions here makes fullscreen reattachment and playback transitions testable on the JVM.
 */
internal class DanmakuRendererSyncPolicy {
    private var attached = false
    private var prepared = false
    private var desiredPositionMillis = 0L
    private var playing = false

    fun onAttach(
        replacingView: Boolean,
        positionMillis: Long,
        isPlaying: Boolean,
    ): List<DanmakuRendererCommand> {
        desiredPositionMillis = positionMillis.coerceAtLeast(0L)
        if (!replacingView && attached) {
            return onPlaybackChanged(isPlaying)
        }

        val commands = buildList {
            if (attached) add(DanmakuRendererCommand.ReleaseAttachedView)
            add(DanmakuRendererCommand.PrepareAttachedView)
        }
        attached = true
        prepared = false
        playing = isPlaying
        return commands
    }

    fun onPrepared(): List<DanmakuRendererCommand> {
        if (!attached || prepared) return emptyList()
        prepared = true
        return buildList {
            add(DanmakuRendererCommand.ReplaceItems)
            add(
                if (playing) {
                    DanmakuRendererCommand.StartAt(desiredPositionMillis)
                } else {
                    DanmakuRendererCommand.StartPausedAt(desiredPositionMillis)
                },
            )
        }
    }

    fun onCommentsChanged(
        contentsChanged: Boolean,
        positionMillis: Long,
    ): List<DanmakuRendererCommand> {
        desiredPositionMillis = positionMillis.coerceAtLeast(0L)
        if (!prepared || !contentsChanged) return emptyList()
        return replaceItemsAtDesiredPosition()
    }

    fun onConfigurationChanged(
        effect: DanmakuRendererConfigEffect,
        positionMillis: Long,
    ): List<DanmakuRendererCommand> {
        desiredPositionMillis = positionMillis.coerceAtLeast(0L)
        if (!prepared || effect == DanmakuRendererConfigEffect.NONE) return emptyList()
        /*
         * A display preference changes only native style, filters, or each item's time offset.
         * The adapter applies those properties in place. Replacing the item set here used to clear
         * the screen before thousands of add commands reached DFM's draw thread, which exposed a
         * visible empty interval after every settings change.
         *
         * It is equally important not to seek: DFM 0.9.25 implements seek as a stop-and-resume
         * transition and can briefly move a paused timeline.
         *
         * Exception: density sampling and repeat merging transform the item set itself, so they
         * explicitly request a rebuild. The brief refresh is the expected feedback for those
         * two sliders.
         */
        if (effect == DanmakuRendererConfigEffect.REPLACE_ITEMS) {
            return replaceItemsAtDesiredPosition()
        }
        return emptyList()
    }

    private fun replaceItemsAtDesiredPosition(): List<DanmakuRendererCommand> {
        return buildList {
            add(DanmakuRendererCommand.ReplaceItems)
            add(
                if (playing) {
                    DanmakuRendererCommand.SeekTo(desiredPositionMillis)
                } else {
                    DanmakuRendererCommand.SeekPausedTo(desiredPositionMillis)
                },
            )
        }
    }

    fun onPlaybackChanged(isPlaying: Boolean): List<DanmakuRendererCommand> {
        if (playing == isPlaying) return emptyList()
        playing = isPlaying
        if (!prepared) return emptyList()
        return listOf(
            if (isPlaying) DanmakuRendererCommand.Resume else DanmakuRendererCommand.Pause,
        )
    }

    fun onPositionDiscontinuity(positionMillis: Long): List<DanmakuRendererCommand> {
        desiredPositionMillis = positionMillis.coerceAtLeast(0L)
        if (!prepared) return emptyList()
        return listOf(
            if (playing) {
                DanmakuRendererCommand.SeekTo(desiredPositionMillis)
            } else {
                DanmakuRendererCommand.SeekPausedTo(desiredPositionMillis)
            },
        )
    }

    fun onClear(): List<DanmakuRendererCommand> {
        return if (prepared) listOf(DanmakuRendererCommand.ClearItems) else emptyList()
    }

    fun onRelease() {
        attached = false
        prepared = false
        desiredPositionMillis = 0L
        playing = false
    }
}
