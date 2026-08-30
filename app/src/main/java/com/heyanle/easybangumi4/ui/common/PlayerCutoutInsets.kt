package com.heyanle.easybangumi4.ui.common

import android.app.Activity
import android.os.Build
import android.util.Log
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 全局刘海屏/挖孔屏适配工具。
 *
 * 现实约束：部分厂商系统（如 Flyme）会把窗口刘海模式强制为 ALWAYS 并抹掉下发给应用的
 * DisplayCutout 上报（insets 恒为 0），所以这里按"物理显示层 cutout + 屏幕旋转方向"
 * 推算挖孔真正所在的一侧，只避让那一侧；探测不到时不做任何避让。
 */
object PlayerCutoutInsets {

    /** 挖孔条宽度的预留值（以常见挖孔屏 92px/3x 物理密度换算约 33dp）。 */
    val SAFE_WIDTH: Dp = 36.dp

    enum class Side { NONE, LEFT, RIGHT, TOP }

    private const val TAG = "PlayerCutoutInsets"

    /**
     * 探测当前窗口方向下挖孔所在的一侧。
     * 1) 窗口上报的 displayCutout（标准设备，已按窗口方向换算）；
     * 2) 兜底读逻辑 Display 的 cutout 并按旋转方向映射（Flyme 等抹掉窗口上报的机型）。
     * 都探测不到返回 [Side.NONE]，调用方不做避让。
     */
    fun cutoutSide(activity: Activity): Side {
        activity.window.decorView.rootWindowInsets?.displayCutout?.let { windowCutout ->
            when {
                windowCutout.safeInsetLeft > 0 -> return logged(Side.LEFT)
                windowCutout.safeInsetRight > 0 -> return logged(Side.RIGHT)
                windowCutout.safeInsetTop > 0 -> return logged(Side.TOP)
            }
        }
        if (Build.VERSION.SDK_INT < 30) return logged(Side.NONE)
        // 逻辑 Display 的 cutout 坐标已按当前屏幕方向换算，直接读四边。
        val displayCutout = activity.display.cutout ?: return logged(Side.NONE)
        return when {
            displayCutout.safeInsetLeft > 0 -> logged(Side.LEFT)
            displayCutout.safeInsetRight > 0 -> logged(Side.RIGHT)
            displayCutout.safeInsetTop > 0 -> logged(Side.TOP)
            else -> logged(Side.NONE)
        }
    }

    private fun logged(side: Side): Side {
        Log.d(TAG, "cutoutSide = $side")
        return side
    }

    /** 组合侧：随屏幕方向/尺寸变化重新探测。 */
    @Composable
    fun rememberCutoutSide(activity: Activity): Side {
        val configuration = LocalConfiguration.current
        return remember(configuration.screenWidthDp, configuration.screenHeightDp) {
            cutoutSide(activity)
        }
    }

    /** 单侧避让边距：挖孔在 [cutoutSide] 且 [targetSide] 与之一致时返回 [width]，否则 0。 */
    fun paddingFor(cutoutSide: Side, targetSide: Side, width: Dp = SAFE_WIDTH): Dp =
        if (targetSide == cutoutSide) width else 0.dp

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
}
