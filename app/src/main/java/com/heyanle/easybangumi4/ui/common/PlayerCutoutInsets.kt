package com.heyanle.easybangumi4.ui.common

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp

/**
 * 全局刘海屏/挖孔屏适配工具。
 *
 * 注意：部分厂商系统（如 Flyme）会把窗口刘海模式强制为 ALWAYS 并抹掉下发给应用的
 * DisplayCutout 上报（insets 恒为 0），因此这里不能只依赖系统上报——水平方向保底预留
 * [FALLBACK_HORIZONTAL]（约等于挖孔条宽度），系统真实上报了刘海时取两者较大值。
 */
object PlayerCutoutInsets {

    /** 挖孔条宽度的保底预留（以常见挖孔屏 92px/3x 密度换算约 33dp）。 */
    val FALLBACK_HORIZONTAL: Dp = 36.dp

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

    /**
     * 弹幕画布安全边距：竖屏避让顶部挖孔条；全屏横屏避让左右两侧。
     * 两个方向都取 max(系统上报的刘海边距, [FALLBACK_HORIZONTAL])。
     */
    @Composable
    fun canvasPaddingValues(isFullScreen: Boolean): PaddingValues {
        val cutout = WindowInsets.displayCutout.asPaddingValues()
        val layoutDirection = LocalLayoutDirection.current
        return if (isFullScreen) {
            PaddingValues(
                start = cutout.calculateLeftPadding(layoutDirection).coerceAtLeast(FALLBACK_HORIZONTAL),
                end = cutout.calculateRightPadding(layoutDirection).coerceAtLeast(FALLBACK_HORIZONTAL),
            )
        } else {
            PaddingValues(top = cutout.calculateTopPadding().coerceAtLeast(FALLBACK_HORIZONTAL))
        }
    }

    /**
     * 全屏控制层/弹幕画布的水平安全边距：左右各取
     * max(系统上报的刘海边距, [FALLBACK_HORIZONTAL])。
     */
    @Composable
    fun horizontalPaddingValues(): PaddingValues {
        val cutout = WindowInsets.displayCutout.asPaddingValues()
        val layoutDirection = LocalLayoutDirection.current
        return PaddingValues(
            start = cutout.calculateLeftPadding(layoutDirection).coerceAtLeast(FALLBACK_HORIZONTAL),
            end = cutout.calculateRightPadding(layoutDirection).coerceAtLeast(FALLBACK_HORIZONTAL),
        )
    }
}
