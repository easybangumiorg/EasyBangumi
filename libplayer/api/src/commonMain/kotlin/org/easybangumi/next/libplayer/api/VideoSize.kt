package org.easybangumi.next.libplayer.api

/**
 * Created by heyanle on 2025/5/27.
 */
data class VideoSize (
    val width: Int = C.PIXEL_UNSET,
    val height: Int = C.PIXEL_UNSET,
    // 宽 to 高
    val ratio: Pair<Int, Int> = C.RATIO_UNSET
) {

    val logicSize: Pair<Int, Int>? by lazy {
        if (width == C.PIXEL_UNSET && height == C.PIXEL_UNSET) {
            null
        } else if (width != C.PIXEL_UNSET && height != C.PIXEL_UNSET) {
            Pair(width, height)
        } else if (ratio.isUnset()) {
            null
        } else if (width != C.PIXEL_UNSET) {
            Pair(width, (width * ratio.second) / ratio.first)
        } else if (height != C.PIXEL_UNSET) {
            Pair((height * ratio.first) / ratio.second, height)
        } else {
            null
        }
    }

    override fun toString(): String {
        return "VideoSize(width=$width, height=$height, ratio=$ratio)"
    }

}

private fun Pair<Int, Int>?.isUnset(): Boolean {
    return this == null || (first == C.PIXEL_UNSET && second == C.PIXEL_UNSET) || first <= 0 || second <= 0
}