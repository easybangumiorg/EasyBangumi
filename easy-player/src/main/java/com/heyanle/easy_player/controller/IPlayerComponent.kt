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

    fun onPlayerStateChanged(playerState: Int)

    fun onPlayStateChanged(playState: Int)

    fun onVisibleChanged(isVisible: Boolean, anim: Animation)

    fun onProgressUpdate(duration: Long, position: Long)

    fun onLockStateChange(isLocked: Boolean)

    fun getView(): View?

    fun getLayoutParam(): RelativeLayout.LayoutParams?

    fun onAttachToController(controller: ControllerWrapper)

}