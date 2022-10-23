package com.heyanle.easy_player.controller

import android.view.ViewGroup
import com.heyanle.easy_player.player.IEasyPlayer

/**
 * 视频控制控件的 控制器，负责播放控制控件的 展示，隐藏，锁定，添加元素 等逻辑
 * Created by HeYanLe on 2022/10/23 12:37.
 * https://github.com/heyanLE
 */
interface IComponentContainer {

    fun startFadeOut()

    fun stopFadeOut()

    fun isShowing(): Boolean

    fun setLocked(locked: Boolean)

    fun isLocked(): Boolean

    fun startProgressUpdate()

    fun stopProgressUpdate()

    fun hide()

    fun show()

    fun getViewContainer(): ViewGroup

    fun setEasyPlayer(player: IEasyPlayer)

    fun getEasyPlayer(): IEasyPlayer?

    fun addComponents(isAddToViewGroup: Boolean, vararg component: IControlComponent)

    fun removeComponents(vararg component: IControlComponent)

    fun removeAllComponentsWithoutAddToViewGroup()

    fun removeAllComponents()


}