package com.heyanle.easybangumi4

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Looper
import android.os.Process
import android.util.Log
import android.widget.Toast
import com.heyanle.easy_crasher.CrashHandler
import com.heyanle.easybangumi4.db.AppDatabase
import com.heyanle.easybangumi4.source.ExtensionSource
import com.heyanle.easybangumi4.source.utils.initUtils
import com.heyanle.easybangumi4.utils.exo_ssl.CropUtil
import com.heyanle.easybangumi4.utils.exo_ssl.TrustAllHostnameVerifier
import com.heyanle.extension_load.ExtensionInit
import com.heyanle.extension_load.IconFactoryImpl
import com.heyanle.lib_anim.utils.WebViewUtil
import com.heyanle.okkv2.MMKVStore
import com.heyanle.okkv2.core.Okkv
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import com.microsoft.appcenter.distribute.Distribute
import javax.net.ssl.HttpsURLConnection


/**
 * Created by HeYanLe on 2023/2/18 22:47.
 * https://github.com/heyanLE
 */
lateinit var APP: App
lateinit var DB: AppDatabase

class App: Application() {


    override fun onCreate() {
        super.onCreate()
        APP = this
        if (isMainProcess()){
            initOkkv()

            initCrasher()

            HttpsURLConnection.setDefaultSSLSocketFactory(CropUtil.getUnsafeSslSocketFactory())
            HttpsURLConnection.setDefaultHostnameVerifier(TrustAllHostnameVerifier())

            initAppCenter()

            initExtension()

            initDataBase()
            kotlin.runCatching {
                initUtils(this)
            }.onFailure {
                it.printStackTrace()
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getPackageName(): String {
        // This causes freezes in Android 6/7 for some reason
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                // Override the value passed as X-Requested-With in WebView requests
                val stackTrace = Looper.getMainLooper().thread.stackTrace
                val chromiumElement = stackTrace.find {
                    it.className.equals(
                        "org.chromium.base.BuildInfo",
                        ignoreCase = true,
                    )
                }
                if (chromiumElement?.methodName.equals("getAll", ignoreCase = true)) {
                    return WebViewUtil.SPOOF_PACKAGE_NAME
                }
            } catch (e: Exception) {
            }
        }
        return super.getPackageName()
    }

    private fun initOkkv() {
        Okkv.Builder(MMKVStore(this)).cache().build().init().default()
        // 如果不使用缓存，请手动指定 key
        Okkv.Builder(MMKVStore(this)).build().init().default("no_cache")
    }

    private fun initCrasher() {
        Thread.setDefaultUncaughtExceptionHandler(CrashHandler(this))
    }

    private fun initAppCenter() {
        if (!BuildConfig.DEBUG) {
            kotlin.runCatching {
                // https://appcenter.ms
                val sc = BuildConfig.APP_CENTER_SECRET
                Log.d("BangumiApp", "app center secret -> $sc")
                if (sc.isNotEmpty()) {
                    AppCenter.start(
                        this, sc,
                        Analytics::class.java, Crashes::class.java, Distribute::class.java
                    )
                    // 禁用自动更新 使用手动更新
                    Distribute.disableAutomaticCheckForUpdate()
                }
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    private fun initExtension(){
        ExtensionSource.init()
        ExtensionInit.init(this, IconFactoryImpl())
    }

    private fun initDataBase(){

        AppDatabase.init(this)
    }

    private fun isMainProcess(): Boolean{
        return packageName == if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getProcessName()
        }else{
            getProcessName(this)
        }

    }

    private fun getProcessName(cxt: Context): String? {
        val pid = Process.myPid()
        val am = cxt.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val runningApps = am.runningAppProcesses ?: return null
        for (procInfo in runningApps) {
            if (procInfo.pid == pid) {
                return procInfo.processName
            }
        }
        return null
    }

}