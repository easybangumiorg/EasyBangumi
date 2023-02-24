package com.heyanle.easybangumi4

import android.app.Application
import android.os.Build
import android.os.Looper
import android.util.Log
import com.heyanle.easy_crasher.CrashHandler
import com.heyanle.easybangumi4.source.ExtensionSource
import com.heyanle.extension_load.ExtensionInit
import com.heyanle.extension_load.IconFactoryImpl
import com.heyanle.lib_anim.utils.WebViewUtil
import com.heyanle.easybangumi4.utils.exo_ssl.CropUtil
import com.heyanle.easybangumi4.utils.exo_ssl.TrustAllHostnameVerifier
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
class App: Application() {


    override fun onCreate() {
        super.onCreate()

        APP = this

        initOkkv()

        initCrasher()

        HttpsURLConnection.setDefaultSSLSocketFactory(CropUtil.getUnsafeSslSocketFactory())
        HttpsURLConnection.setDefaultHostnameVerifier(TrustAllHostnameVerifier())

        initAppCenter()

        initExtension()

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

}