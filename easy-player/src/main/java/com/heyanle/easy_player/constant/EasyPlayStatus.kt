package com.heyanle.easy_player.constant

/**
 * 播放器状态，这里从播放器核心抽离出来，最终在 IEasyPlayer 中维护
 * Created by HeYanLe on 2022/10/23 15:25.
 * https://github.com/heyanLE
 */
object EasyPlayStatus {
    const val STATE_ERROR = -1              // 错误
    const val STATE_IDLE = 0                // 空闲
    const val STATE_PREPARING = 1           // 正在准备（解析播放源，非缓冲）
    const val STATE_PREPARED = 2            // 准备完毕（可以开始播放）
    const val STATE_PLAYING = 3             // 正在播放
    const val STATE_PAUSED = 4              // 暂停
    const val STATE_PLAYBACK_COMPLETED = 5  // 回放成功
    const val STATE_BUFFERING = 6           // 正在缓冲
    const val STATE_BUFFERED = 7            // 缓冲完毕，如果设置了 playWhenReady 则马上会跳到播放状态
    const val STATE_START_ABORT = 8         // 开始播放中止

}