package com.heyanle.easybangumi.utils

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import com.heyanle.easybangumi.R
import android.view.View

import android.os.Build
import android.text.BoringLayout
import androidx.appcompat.app.AppCompatDelegate
import com.heyanle.easybangumi.EasyApplication


/**
 * Created by HeYanLe on 2021/9/19 10:37.
 * https://github.com/heyanLE
 */

/**
 * 当切换夜间模式时，需要 recreate 后台 activity，
 * 同时前台 activity 会 finish 后在 start
 * 如果前台 activity 实现了该接口，将会用该接口返回的 装饰器装饰过的 intent 启动新的 activity
 */
interface DarkChangeSaveIntent{
    fun getIntentDecorator(intent: Intent)
}
object DarkUtils {

    private var isDark by oksp("is_dark", false)
    private var autoDark by oksp("auto_dark", true)

    private val themeId: Int
    get() = if(isDark) R.style.Theme_EasyBangumi_Dark else R.style.Theme_EasyBangumi_Light

    private val activities = arrayListOf<Activity>()

    fun theme(activity: Activity){
        auto(activity)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!isDark){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    activity.window.decorView.systemUiVisibility =
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                }else{
                    activity.window.decorView.systemUiVisibility =
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    activity.window.navigationBarColor = Color.BLACK
                }
            }

        }else{
            if(!isDark){
                dark(true)
            }
        }
        activity.setTheme(themeId)
        activities += activity

    }

    fun destroy(activity: Activity){
        activities -= activity
    }

    fun dark():Boolean{
        return isDark
    }

    fun dark(isDark: Boolean){
        if(isDark == this.isDark){
            return
        }
        this.isDark = isDark
        activities.forEach {
            it.recreate()
        }
    }

    fun dark(isDark: Boolean, activity: Activity){
        if(isDark == this.isDark){
            return
        }
        this.isDark = isDark
        activities.clear()
        activities.forEach {
            if(it != activity){
                it.recreate()
            }
        }
        activity.application.setTheme(themeId)
        if(activity is DarkChangeSaveIntent){
            activity.start(activity, activity::getIntentDecorator)
        }else{
            activity.start(activity)
        }

        activity.overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
        activity.finish()
    }



    fun autoDark():Boolean{
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && autoDark
    }

    fun autoDark(boolean: Boolean, activity: Activity){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && autoDark != boolean){
            autoDark = boolean
            auto(activity)
        }
    }

    fun autoApplication(){
        if(!autoDark()){
            return
        }
        when(EasyApplication.INSTANCE.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK){
            Configuration.UI_MODE_NIGHT_YES -> {
                dark(true)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                dark(false)
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {

            }
        }
    }

    fun auto(activity: Activity){
        if(!autoDark()){
            return
        }
        when(activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK){
            Configuration.UI_MODE_NIGHT_YES -> {
                dark(true, activity)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                dark(false, activity)
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {

            }
        }

    }

    fun switch(){
        dark(!isDark)
    }

    fun switch(activity: Activity){
        dark(!isDark, activity)
    }


}