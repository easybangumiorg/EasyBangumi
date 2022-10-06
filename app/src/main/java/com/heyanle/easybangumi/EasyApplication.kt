package com.heyanle.easybangumi

import android.app.Application
import android.content.Context
import com.heyanle.easy_crasher.CrashActivity
import com.heyanle.easy_crasher.CrashHandler
import com.heyanle.easy_daynight.ThemeManager
import com.heyanle.okkv2.MMKVStore
import com.heyanle.okkv2.core.Okkv

/**
 * Created by HeYanLe on 2022/9/28 15:25.
 * https://github.com/heyanLE
 */
class EasyApplication: Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        base?.setTheme(com.heyanle.easy_daynight.R.style.Theme_EasyBangumi)
    }

    companion object {
        lateinit var INSTANCE: EasyApplication
    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this

        initOkkv()
        initLightDark()
        initCrasher()

    }

    private fun initOkkv(){
        Okkv.Builder().store(MMKVStore(this)).cache().build().init().default()
        // 如果不使用缓存，请手动指定 key
        Okkv.Builder().store(MMKVStore(this)).build().init().default("no_cache")
    }

    private fun initLightDark(){
        ThemeManager.whiteList.add(CrashActivity::class.java)
        ThemeManager.init(this)
    }

    private fun initCrasher(){
        Thread.setDefaultUncaughtExceptionHandler(CrashHandler(this))
    }
}