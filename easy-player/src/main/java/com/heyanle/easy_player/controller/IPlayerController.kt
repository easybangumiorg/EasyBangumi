package com.heyanle.easy_player.controller

import android.graphics.Bitmap

/**
 * 视频控制器，负责处理播放器的 暂停播放，倍速，全屏 等状态
 * Created by HeYanLe on 2022/10/23 16:54.
 * https://github.com/heyanLE
 */
interface IPlayerController {

    fun start()

    fun pause()

    fun getDuration(): Long

    fun getCurrentPosition(): Long

    fun seekTo(pos: Long)

    fun isPlaying(): Boolean

    fun getBufferedPercentage(): Int

    fun startFullScreen()

    fun stopFullScreen()

    fun isFullScreen(): Boolean

    fun setMute(isMute: Boolean)

    fun isMute(): Boolean

    fun setScreenScaleType(screenScaleType: Int)

    fun setSpeed(speed: Float)

    fun getSpeed(): Float

    fun replay(resetPosition: Boolean)

    fun setMirrorRotation(enable: Boolean): Boolean

    fun doScreenShot(): Bitmap?

    fun getVideoSize(): IntArray

    fun setRotation(rotation: Float): Boolean

    fun startTinyScreen(): Boolean

    fun stopTinyScreen()

    fun isTinyScreen(): Boolean
}