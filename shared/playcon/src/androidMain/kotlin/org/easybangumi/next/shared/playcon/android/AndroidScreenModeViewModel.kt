package org.easybangumi.next.shared.playcon.android

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.provider.Settings
import android.view.OrientationEventListener
import androidx.annotation.UiThread
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.shared.foundation.ActivityController
import org.easybangumi.next.shared.foundation.view_model.BaseViewModel
import org.easybangumi.next.shared.preference.AndroidPlayerPreference
import org.koin.core.component.inject

/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  全屏/非全屏切换
 */
class AndroidScreenModeViewModel: BaseViewModel() {

    private val logger = logger()

    private val activityController: ActivityController by inject()
    private val playerPreference: AndroidPlayerPreference by inject()
    private val autoFullScreenPref = playerPreference.autoFullScreenMode

    // event state 模型还是用 handler 把
    private val handler: Handler by lazy {
        object: Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                val event = msg.obj as? Event ?: return
                when(event.what) {
                    Event.WHAT_USER_FULL_SCREEN_CHANGE -> {
                        val e = event as? Event.UserFullScreenChange ?: return
                        handleUserFullScreenChange(e.isFullScreen)
                    }

                    Event.WHAT_ORIENTATION_CHANGE -> {
                        val e = event as? Event.OrientationChange ?: return
                        handleOrientationChange(e.orientation)
                    }
                }
            }
        }
    }

    // 状态机
    data class LogicState (
        val isFullScreen: Boolean = false,
        val isReserve: Boolean = false,
        val lastFullScreenReason: Int = 0, // 0 用户点击， 1 传感器
        val isTabletMod: Boolean = false,
    )
    private val _logicState = MutableStateFlow(LogicState())
    val logic = _logicState.asStateFlow()



    sealed class Event(
        val what: Int
    ) {
        companion object {
            const val WHAT_USER_FULL_SCREEN_CHANGE = 1
            const val WHAT_ORIENTATION_CHANGE = 2
        }
        // 用户手动点击切换全屏
        class UserFullScreenChange(
            val isFullScreen: Boolean
        ): Event(WHAT_USER_FULL_SCREEN_CHANGE)

        // 传感器方向变化 - orientation 已经规整，只会是 0, 90, 180, 270
        class OrientationChange(
            val orientation: Int
        ): Event(WHAT_ORIENTATION_CHANGE)
    }

    // 发送用户点击切换全屏事件
    fun fireUserFullScreenChange(
        isFullScreen: Boolean
    ) {
        logger.info("fireUserFullScreenChange: $isFullScreen")
        // 用户点击必须优先处理，处理中会读取单次传感器数据
        handler.removeMessages(Event.WHAT_ORIENTATION_CHANGE)
        handler.dispatchMessage(Message.obtain().apply {
            what = Event.WHAT_USER_FULL_SCREEN_CHANGE
            obj = Event.UserFullScreenChange(isFullScreen)
        })
    }

    @UiThread
    private fun handleUserFullScreenChange(
        isFullScreen: Boolean
    ) {
        logger.info("handleUserFullScreenChange: $isFullScreen")
        val act = activityController.showingActivity() ?: return
        val curState = _logicState.value
        val tabletMod = curState.isTabletMod
        val reverse = orientationTemp == 270 || orientationTemp == 90
        // 用户操作无论如何都要执行屏幕翻转
        changeRequestedOrientation(
            fullScreen = isFullScreen,
            reverse = reverse,
            isTabletMod = tabletMod,
            ctx = act
        )
        _logicState.update {
            it.copy(
                isFullScreen = isFullScreen,
                isReserve = reverse,
                lastFullScreenReason = Event.WHAT_USER_FULL_SCREEN_CHANGE
            )
        }
    }

    private fun fireOrientationChange(
        orientation: Int
    ) {
//        logger.info("fireOrientationChange: $orientation")
        handler.removeMessages(Event.WHAT_ORIENTATION_CHANGE)
        handler.dispatchMessage(Message.obtain().apply {
            what = Event.WHAT_ORIENTATION_CHANGE
            obj = Event.OrientationChange(orientation)
        })
    }

    private fun handleOrientationChange(
        orientation: Int
    ) {
//        logger.info("handleOrientationChange: $orientation")
        val act = activityController.showingActivity() ?: return
        val curState = _logicState.value
        val tabletMod = curState.isTabletMod

        val needFullScreen = orientation == 90 || orientation == 270
        val needReverse = orientation == 270 || orientation == 90
        // act.canAutoChangeFullScreenMode() 为 false 代表不允许横竖屏的自动切换
        if (needFullScreen != curState.isFullScreen && !act.canAutoChangeFullScreenMode()) {
            return
        }
        // 横屏和横屏翻转，竖屏和竖屏翻转的切换总是允许
        changeRequestedOrientation(
            fullScreen = needFullScreen,
            reverse = needReverse,
            isTabletMod = tabletMod,
            ctx = act
        )
        _logicState.update {
            it.copy(
                isFullScreen = needFullScreen,
                isReserve = needReverse,
                lastFullScreenReason = Event.WHAT_ORIENTATION_CHANGE
            )
        }
    }

    // 传感器事件回调
    private var orientationEventBlock: Boolean = false
    fun blockOrientationEvent(block: Boolean) {
        orientationEventBlock = block
    }

    private var orientationTemp: Int = -1
    fun onOrientationEvent(orientation: Int) {
//        logger.info("onOrientationEvent: $orientation")
        if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
            return
        }
        var targetOrientation = -1
        // 1. 规整数据
        if (orientation !in 10..350) {
            //0度，用户竖直拿着手机
            targetOrientation = 0
        } else if (orientation in 81..99 ) {
            // 90度，用户左横屏拿着手机
            targetOrientation = 90
        } else if (orientation in 171..189) {
            // 180度，用户倒立拿着手机
            targetOrientation = 180
        } else if (orientation in 261..279) {
            // 270度，用户右横屏拿着手机
            targetOrientation = 270
        }

        orientationTemp = targetOrientation
        if (orientationEventBlock) {
            return
        }
        fireOrientationChange(targetOrientation)
    }

    private fun changeRequestedOrientation(
        fullScreen: Boolean,
        reverse: Boolean = false,
        isTabletMod: Boolean,
        ctx: Activity
    ){
//        logger.info("changeRequestedOrientation: fullScreen=$fullScreen, reverse=$reverse, isTabletMod=$isTabletMod")
        if (!fullScreen) {
            if (isTabletMod) {
                // 全屏平板模式走未指定，交给系统
                ctx.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            } else {
                // 非全屏手机模式
                if (reverse) {
                    ctx.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                } else {
                    ctx.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
            }
        } else {
            // 全屏模式无论如何都是横屏
            ctx.requestedOrientation = if (reverse) ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
            else ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }

    }


    // 综合判断是否允许切换全屏
    private fun Activity.canAutoChangeFullScreenMode(): Boolean {
        val autoFullScreenMode = autoFullScreenPref.get()
        return when(autoFullScreenMode) {
            AndroidPlayerPreference.AutoFullScreeMode.AUTO -> {
                isAutoRotateOn()
            }

            AndroidPlayerPreference.AutoFullScreeMode.ENABLE -> {
                true
            }

            AndroidPlayerPreference.AutoFullScreeMode.DISABLE -> {
                false
            }
        }
    }

    // 获取用户锁定屏幕方向设置（锁定屏幕方向 = 不允许旋转）
    private fun Activity.isAutoRotateOn(): Boolean {
        //获取系统是否允许自动旋转屏幕
        return Settings.System.getInt(
            contentResolver,
            Settings.System.ACCELEROMETER_ROTATION,
            0
        ) == 1
    }

}