package com.heyanle.easy_player.controller

import android.view.View
import android.view.animation.Animation
import android.widget.RelativeLayout

/**
 * 视频控制布局的 某个 元素
 * Created by HeYanLe on 2022/10/22 21:54.
 * https://github.com/heyanLE
 */
interface IControlComponent {

    fun handleEvent(event: ControlEvent)

    fun getView(): View?

    fun getLayoutParam(): RelativeLayout.LayoutParams?

    fun onAttachToController(controller: ControllerWrapper)

}

sealed class ControlEvent {
    data class OnPlayerStateChanged(val playerState: Int) : ControlEvent()
    data class OnPlayStateChanged(val playState: Int): ControlEvent()
    data class OnVisibleChanged(val isVisible: Boolean, val anim: Animation): ControlEvent()
    data class OnProgressUpdate(val duration: Long, val position: Long): ControlEvent()
    data class OnLockStateChange(val boolean: Boolean): ControlEvent()
}