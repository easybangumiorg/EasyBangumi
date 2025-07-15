package org.easybangumi.next.shared.utils

import android.app.Activity
import android.view.View
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

object MediaUtils {

    /**
     * 设置 状态栏反色模式
     */
    fun setIsAppearanceLightStatusBars(activity: Activity, isLight: Boolean) {
        WindowCompat
            .getInsetsController(activity.window, activity.window.decorView)
            .isAppearanceLightStatusBars = isLight
    }

    /**
     * 设置 导航栏反色模式
     */
    fun setIsAppearanceLightNavBars(activity: Activity, isLight: Boolean) {
        WindowCompat
            .getInsetsController(activity.window, activity.window.decorView)
            .isAppearanceLightNavigationBars = isLight
    }

    /**
     * 设置状态栏是否显示
     */
    fun setIsStatusBarsShow(activity: Activity, isShow: Boolean) {
        WindowCompat
            .getInsetsController(activity.window, activity.window.decorView)
            .let {
                if (isShow) {
                    it.show(WindowInsetsCompat.Type.statusBars())
                } else {
                    it.hide(WindowInsetsCompat.Type.systemBars())
                }
                it.systemBarsBehavior
            }
    }

    /**
     * 设置 导航栏是否显示
     */
    fun setIsNavBarsShow(activity: Activity, isShow: Boolean) {
        WindowCompat
            .getInsetsController(activity.window, activity.window.decorView)
            .let {
                if (isShow) {
                    it.show(WindowInsetsCompat.Type.navigationBars())
                } else {
                    it.hide(WindowInsetsCompat.Type.navigationBars())
                }
            }
    }

    /**
     * 隐藏状态栏和导航栏并在特定条件恢复
     * @see WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
     * @see WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_TOUCH
     * @see WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_SWIPE
     *
     */
    fun setSystemBarsBehavior(activity: Activity, behavior: Int) {

        WindowCompat
            .getInsetsController(activity.window, activity.window.decorView)
            .systemBarsBehavior = behavior
    }

    /**
     * 给 view 加上 导航栏和状态栏的 padding
     */
    fun setIsFitSystemWindows(view: View, isFit: Boolean) {
        view.fitsSystemWindows = isFit
    }

    /**
     * 设置状态栏颜色
     */
    fun setStatusBarColor(activity: Activity, color: Int) {
        // 需要取消半透明颜色
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        activity.window.statusBarColor = color
    }

    /***
     * 设置导航栏颜色
     */
    fun setNavBarColor(activity: Activity, color: Int) {
        // 需要取消半透明颜色
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        activity.window.navigationBarColor = color
    }

    /**
     * 是否将布局绕开状态栏
     * true 为不嵌入状态栏
     * false 为嵌入状态栏
     */
    fun setIsDecorFitsSystemWindows(activity: Activity, isFits: Boolean) {
        WindowCompat.setDecorFitsSystemWindows(activity.window, isFits)
    }

}