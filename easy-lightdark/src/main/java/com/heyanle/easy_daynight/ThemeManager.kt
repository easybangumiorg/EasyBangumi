package com.heyanle.easy_daynight

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import com.heyanle.easy_media.MediaHelper
import com.heyanle.okkv2.core.okkv

/**
 * Created by HeYanLe on 2022/9/27 21:54.
 * https://github.com/heyanLE
 */
/**
 * 当切换夜间模式时，需要 recreate 后台 activity，
 * 同时前台 activity 会 finish 后在 start
 * 如果前台 activity 实现了该接口，将会用该接口返回的 装饰器装饰过的 intent 启动新的 activity
 */
interface DarkChangeSaveIntent{
    fun decoIntent(intent: Intent)
}

/**
 * 主题模式，自动 - 日间 - 夜间
 * 安卓 Q 以下不支持 Auto
 */
enum class DarkLightMode(val value: Int) {
    AUTO(0),LIGHT(1),DARK(2)
}

object ThemeManager {

    private var modeId by okkv("themeModeId", 0)
    private val activitySet = hashSetOf<Activity>()

    // 白名单，不受主题控制的 Activity
    val whiteList = hashSetOf<Class<out Activity>>()

    private val darkThemeId = R.style.Theme_EasyBangumi_Dark
    private val lightThemeId = R.style.Theme_EasyBangumi_Light



    val isSupportAutoMode = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    val isSupportChangeMode = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

    fun init(application: Application){
        if(!isSupportChangeMode || getIsRealDarkMode(application)){
            application.setTheme(darkThemeId)
        }else{
            application.setTheme(lightThemeId)
        }
        application.registerActivityLifecycleCallbacks(activityCallback)
    }

    /**
     * 当前是否是真的夜间模式
     * AUTO 时可判断是否夜间
     */
    fun getIsRealDarkMode(context: Context): Boolean {
        return getMode().let {
            if(it == DarkLightMode.AUTO){
                if(!isSupportAutoMode && modeId == 0){
                    modeId = 2
                    true
                }else{
                    when(context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK){
                        Configuration.UI_MODE_NIGHT_YES -> {
                            true
                        }
                        Configuration.UI_MODE_NIGHT_NO -> {
                            false
                        }
                        else -> {
                            true
                        }
                    }
                }
            }else{
                it == DarkLightMode.DARK
            }
        }
    }

    /**
     * 获取当前主题配置模式
     * 安卓 Q 以下不支持 Auto
     */
    fun getMode(): DarkLightMode {
        // 安卓 Q 以下不支持 Auto
        if(!isSupportAutoMode && modeId == 0){
            modeId = 2
        }
        return when(modeId){
            0 -> DarkLightMode.AUTO
            1 -> DarkLightMode.LIGHT
            else -> DarkLightMode.DARK
        }
    }

    /**
     * 设置主题配置
     * 安卓 Q 以下不支持 Auto
     * self: 触发重建，栈里所有其他 Activity 都执行 recreate，但 self 会 finish 后重新 start
     */
    fun setMode(darkLightMode: DarkLightMode, self: Activity) {
        setModeWithoutApply(darkLightMode)
        if(!isSupportChangeMode || getIsRealDarkMode(self.application)){
            self.application.setTheme(darkThemeId)
        }else{
            self.application.setTheme(lightThemeId)
        }
        activitySet.forEach {
            if(it != self){
                it.recreate()
            }
        }
        val intent = Intent(self, self::class.java)
        if(self is DarkChangeSaveIntent){
            self.decoIntent(intent)
        }
        self.startActivity(intent)
        self.overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
        self.finish()

    }

    /**
     * 设置主题配置，但是不触发任何重建
     */
    fun setModeWithoutApply(darkLightMode: DarkLightMode) {
        modeId = darkLightMode.value
    }

    /**
     * 获取当前主题下的颜色
     */
    fun getAttrColor(context: Context, attrId: Int): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(attrId, typedValue, true)
        return typedValue.data
    }

    /**
     * 设置某个 activity 的主题
     * 安卓 M 以下不支持切换默认夜间主题，因为状态栏颜色会出问题
     */
    private fun theme(activity: Activity) {
        if (isSupportChangeMode && !getIsRealDarkMode(activity)) {
            // 日间模式
            activity.setTheme(lightThemeId)
            MediaHelper.setIsAppearanceLightNavBars(activity, true)
            MediaHelper.setIsAppearanceLightStatusBars(activity, true)
        }else{
            // 夜间模式
            activity.setTheme(darkThemeId)

            // 安卓 M 以下一些手机可能失效，这里直接限制只能夜间模式
            MediaHelper.setIsAppearanceLightNavBars(activity, false)
            MediaHelper.setIsAppearanceLightStatusBars(activity, false)
        }
        MediaHelper.setIsDecorFitsSystemWindows(activity, false)
        MediaHelper.setNavBarColor(activity,getAttrColor(activity, Color.TRANSPARENT))
        MediaHelper.setStatusBarColor(activity, getAttrColor(activity, Color.TRANSPARENT))


    }

    private val activityCallback = object: Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            for(clazz in whiteList){
                if(clazz.isInstance(activity)){
                    return
                }
            }
            activitySet.add(activity)
            theme(activity)
        }

        override fun onActivityStarted(activity: Activity) {}

        override fun onActivityResumed(activity: Activity) {}

        override fun onActivityPaused(activity: Activity) {}

        override fun onActivityStopped(activity: Activity) {}

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

        override fun onActivityDestroyed(activity: Activity) {
            activitySet.remove(activity)
        }

    }

}