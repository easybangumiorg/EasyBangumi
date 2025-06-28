package org.easybangumi.next.libplayer.api

/**
 * Created by heyanle on 2025/5/27.
 */
object C {

    const val MEDIA_ITEM_OPTIONAL_KEY_HEADER = "media_item_optional_header"

    const val DEFAULT_ID = "default_id"

    const val MINE_TYPE_UNKNOWN = "unknown"

    enum class State {
        IDLE,
        PREPARING,
        BUFFERING,
        READY,
        ENDED,
    }

    const val TIME_UNSET = Long.MIN_VALUE + 1

    const val PIXEL_UNSET = Int.MIN_VALUE + 2

    val RATIO_UNSET = 0 to 0

    val VIDEO_SIZE_UNSET = VideoSize(
        width = PIXEL_UNSET,
        height = PIXEL_UNSET,
        ratio = RATIO_UNSET
    )

    enum class RendererScaleType {
        // 保持宽高比
        SCALE_SOURCE,              // 默认，原始尺寸
        SCALE_16_9,                 // 16/9
        SCALE_4_3,                  // 4/3
        SCALE_ADAPT,                // 适应屏幕，保持宽高比
        SCALE_FOR_HEIGHT,            // 以高度为准
        SCALE_FOR_WIDTH,             // 以宽度为准
        SCALE_CENTER_CROP,          // 平铺，从中心裁切，保证占满屏幕

        SCALE_MATCH_PARENT,         // 拉伸

    }


}

fun C.State.isMediaSet() = this == C.State.READY || this == C.State.BUFFERING || this == C.State.ENDED