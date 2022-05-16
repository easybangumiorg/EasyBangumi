package com.heyanle.easybangumi

import android.app.Application
import android.util.Log
import cn.jzvd.JzvdStd
import com.heyanle.easybangumi.crash.CrashHandler
import com.heyanle.easybangumi.db.EasyDatabase
import com.heyanle.easybangumi.entity.Bangumi
import com.heyanle.easybangumi.source.IHomeParser
import com.heyanle.easybangumi.source.ISearchParser
import com.heyanle.easybangumi.source.ISourceParser
import com.heyanle.easybangumi.source.SourceParserFactory
import com.heyanle.easybangumi.utils.*
import com.heyanle.okkv.*
import com.heyanle.okkv.core.chain.BaseInterceptorChain
import com.heyanle.okkv.core.chain.SimpleInterceptor
import com.heyanle.okkv.mkkv.LogcatInterceptorChain
import com.heyanle.okkv.mkkv.MMKVStore
import com.heyanle.okkv.sp.SPStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import kotlin.concurrent.thread

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
            .addInterceptorChain(LogcatInterceptorChain())

            .store(MMKVStore(this))
            .build().init().default()

        DarkUtils.autoApplication()
        SourceParserFactory.init()
        EasyDatabase.AppDB
        var s: Boolean by okkv("f", false)
        s = true
        s.toString().logI("Easy")
        //DarkUtils.dark(true)


    }



}