package com.heyanle.easybangumi4.ui.common

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.runtime.Composable

/**
 * 全局刘海屏/挖孔屏适配工具。
 *
 * 使用约定：全屏播放进入时调 [enableShortEdges] 允许视频画面延伸进刘海区域，
 * 控制层（顶栏/侧栏/底栏）用 [safePadding] 取刘海安全边距，避免按钮被遮挡。
 */
object PlayerCutoutInsets {

    /** 允许窗口内容延伸进刘海/挖孔区域（SHORT_EDGES）。重复调用无副作用。 */
    fun enableShortEdges(activity: Activity) {
        activity.window.attributes = activity.window.attributes.apply {
            layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
    }

    /** 恢复系统默认的刘海避让模式（退出全屏时调用）。 */
    fun resetShortEdges(activity: Activity) {
        activity.window.attributes = activity.window.attributes.apply {
            layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
        }
    }

    /** 刘海/挖孔安全边距：全屏下系统栏隐藏，安全边距即刘海在四边的占位。 */
    @Composable
    fun safePadding(): PaddingValues = WindowInsets.displayCutout.asPaddingValues()
}
