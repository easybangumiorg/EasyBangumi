package com.heyanle.easy_player.player

import android.content.res.AssetFileDescriptor
import android.view.Surface
import android.view.SurfaceHolder

/**
 * Created by HeYanLe on 2022/10/23 12:58.
 * https://github.com/heyanLE
 */
interface IEasyPlayer {

    companion object {
        /**
         * 视频/音频开始渲染
         */
        const val MEDIA_INFO_RENDERING_START = 3

        /**
         * 缓冲开始
         */
        const val MEDIA_INFO_BUFFERING_START = 701

        /**
         * 缓冲结束
         */
        const val MEDIA_INFO_BUFFERING_END = 702

        /**
         * 视频旋转信息
         */
        const val MEDIA_INFO_VIDEO_ROTATION_CHANGED = 10001
    }

    fun init()

    fun setVideoSource(url: String, headers: Map<String, String>?)

    fun setVideoSource(fd: AssetFileDescriptor)

    fun prepareAsync()

    fun start()

    fun pause()

    fun stop()

    fun reset()

    fun isPlaying(): Boolean

    fun seekTo(time: Long)

    fun release()

    fun getCurrentPosition(): Long

    fun getDuration(): Long

    fun getBufferedPercentage(): Int

    fun setSurface(surface: Surface)

    fun setSurfaceHolder(holder: SurfaceHolder)

    fun setVolume(left: Float, right: Float)

    fun setLooping(isLooping: Boolean)

    fun setSpeed(speed: Float)

    fun getSpeed(): Float

    fun setEventListener(eventListener: EventListener)

    fun removeEventListener(eventListener: EventListener)

    interface EventListener {

        fun onError()

        fun onCompletion()

        fun onPrepared()

        fun onVideoSizeChanged(width: Int, height:Int)

        /**
         * 具体播放器监听了某些事件，
         * 例如 exoplayer 的 onVideoSizeChanged 也会调用该方法并传入
         * MEDIA_INFO_VIDEO_ROTATION_CHANGED 和 VideoSize 对象
         */
        fun onRealPlayerEvent(event: Int, vararg args: Any)

    }

}