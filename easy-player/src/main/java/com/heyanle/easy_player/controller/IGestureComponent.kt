package com.heyanle.easy_player.controller

/**
 * Create by heyanlin on 2022/10/24
 */
interface IGestureComponent: IControlComponent {

    /**
     * 开始滑动
     */
    fun onStartSlide()

    /**
     * 结束滑动
     */
    fun onStopSlide()

    /**
     * 滑动调整进度
     * @param slidePosition 滑动进度
     * @param currentPosition 当前播放进度
     * @param duration 视频总长度
     */
    fun onPositionChange(slidePosition: Long, currentPosition: Long, duration: Long)

    /**
     * 滑动调整亮度
     * @param percent 亮度百分比
     */
    fun onBrightnessChange(percent: Int)

    /**
     * 滑动调整音量
     * @param percent 音量百分比
     */
    fun onVolumeChange(percent: Int)

}