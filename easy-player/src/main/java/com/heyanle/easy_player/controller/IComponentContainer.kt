package com.heyanle.easy_player.controller

import android.view.ViewGroup
import com.heyanle.easy_player.player.IEasyPlayer

/**
 * 视频控制控件的 控制器，负责播放控制控件的 展示，隐藏，锁定，等逻辑
 * 管理 Component
 * Created by HeYanLe on 2022/10/23 12:37.
 * https://github.com/heyanLE
 */
interface IComponentContainer {

    /**
     * 控制器开始消失计时器
     */
    fun startFadeOut()

    /**
     * 控制器停止消失计时器
     */
    fun stopFadeOut()

    /**
     * 是否在显示
     */
    fun isShowing(): Boolean

    /**
     * 设置锁定状态
     */
    fun setLocked(locked: Boolean)

    /**
     * 是否在锁定状态
     */
    fun isLocked(): Boolean

    /**
     * 开始更新 Progress，开始后将会不断调用 Component 的 onProgressUpdate 方法
     */
    fun startProgressUpdate()

    /**
     * 停止更新 Progress
     */
    fun stopProgressUpdate()

    /**
     * 隐藏控制器
     */
    fun hide()

    /**
     * 展示控制器，并开始计时
     */
    fun show()

    /**
     * 获取 控制器 view 的 Container
     */
    fun getViewContainer(): ViewGroup

    /**
     * 设置 播放器，由 controller 调用
     */
    fun setEasyPlayer(player: IEasyPlayer)

    /**
     * 获取播放器
     */
    fun getEasyPlayer(): IEasyPlayer?

    /**
     * 添加组件
     * @param isAddToViewGroup 是否添加到 控制器 ViewGroup，会将 component.getView 添加到 container 中
     */
    fun addComponents(isAddToViewGroup: Boolean, vararg component: IControlComponent)

    /**
     * 移除组件
     */
    fun removeComponents(vararg component: IControlComponent)

    /**
     * 移除所有没有添加 view 的组件
     */
    fun removeAllComponentsWithoutAddToViewGroup()

    /**
     * 移除所有组件
     */
    fun removeAllComponents()


}