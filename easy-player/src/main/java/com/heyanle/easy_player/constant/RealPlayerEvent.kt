package com.heyanle.easy_player.constant

/**
 * 具体播放器的一些事件，例如 ExoPlayer, IJKPlayer 等
 * Created by HeYanLe on 2022/10/23 15:24.
 * https://github.com/heyanLE
 */
object RealPlayerEvent {
    /**
     * 视频/音频开始渲染
     */
    const val REAL_PLAYER_EVENT_RENDERING_START = 3

    /**
     * 缓冲开始
     */
    const val REAL_PLAYER_EVENT_BUFFERING_START = 701

    /**
     * 缓冲结束
     */
    const val REAL_PLAYER_EVENT_BUFFERING_END = 702

    /**
     * 视频旋转信息
     */
    const val REAL_PLAYER_EVENT_VIDEO_ROTATION_CHANGED = 10001
}