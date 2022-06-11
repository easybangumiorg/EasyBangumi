package com.heyanle.easybangumi

import android.app.Application
import com.heyanle.easybangumi.crash.CrashHandler
import com.heyanle.easybangumi.db.EasyDatabase
import com.heyanle.easybangumi.source.SourceParserFactory
import com.heyanle.easybangumi.utils.DarkUtils
import com.heyanle.okkv2.MMKVStore
import com.heyanle.okkv2.core.Okkv
import java.io.File

/**
 * Created by HeYanLe on 2021/9/8 22:07.
 * https://github.com/heyanLE
 */
class EasyApplication : Application() {

    companion object{
        lateinit var INSTANCE: EasyApplication
    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        Thread.setDefaultUncaughtExceptionHandler(CrashHandler(this))
        Okkv.Builder()
            //.addInterceptorChain(LogcatInterceptorChain())
            .store(MMKVStore(this))
            .build().init().default()

        DarkUtils.autoApplication()
        SourceParserFactory.init()
        EasyDatabase.AppDB
        //DarkUtils.dark(true)


    }



}