package com.heyanle.easybangumi4

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Looper
import android.os.Process
import android.util.Log
import com.arialyy.aria.core.Aria
import com.heyanle.easy_crasher.CrashHandler
import com.heyanle.easybangumi4.cartoon.CartoonModule
import com.heyanle.easybangumi4.download.DownloadModule
import com.heyanle.easybangumi4.exo.MediaModule
import com.heyanle.easybangumi4.getter.GetterModule
import com.heyanle.easybangumi4.preferences.SettingMMKVPreferences
import com.heyanle.easybangumi4.utils.AppCenterManager
import com.heyanle.easybangumi4.utils.exo_ssl.CropUtil
import com.heyanle.easybangumi4.utils.exo_ssl.TrustAllHostnameVerifier
import com.heyanle.extension_load.ExtensionInit
import com.heyanle.extension_load.IconFactoryImpl
import org.koin.mp.KoinPlatform.getKoin
import com.heyanle.lib_anim.utils.WebViewUtil
import com.heyanle.okkv2.MMKVStore
import com.heyanle.okkv2.core.Okkv
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import com.microsoft.appcenter.distribute.Distribute
import com.microsoft.appcenter.distribute.DistributeListener
import com.microsoft.appcenter.distribute.ReleaseDetails
import javax.net.ssl.HttpsURLConnection


/**
 * Created by HeYanLe on 2023/2/18 22:47.
 * https://github.com/heyanLE
 */
lateinit var APP: App

class App : Application() {

    companion object {
        init {
            RootModule.registerWith(Injekt)
        }
    }


    override fun onCreate() {
        super.onCreate()
        APP = this
        if (isMainProcess()) {

            Aria.init(this)
            Aria.get(this).downloadConfig.isConvertSpeed = true

            initCrasher()

            initOkkv()


            HttpsURLConnection.setDefaultSSLSocketFactory(CropUtil.getUnsafeSslSocketFactory())
            HttpsURLConnection.setDefaultHostnameVerifier(TrustAllHostnameVerifier())

            initAppCenter()

            MediaModule(this).registerWith(Injekt)
            DatabaseModule(this).registerWith(Injekt)
            PreferencesModule(this).registerWith(Injekt)
            ControllerModule(this).registerWith(Injekt)
            CartoonModule(this).registerWith(Injekt)
            DownloadModule(this).registerWith(Injekt)
            GetterModule(this).registerWith(Injekt)

        }
    }

    override fun getPackageName(): String {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
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
                    val settingPreferences: SettingMMKVPreferences by getKoin().inject()
                    if (settingPreferences.webViewCompatible.get()) {
                        // 兼容模式不改写
                        return super.getPackageName()
                    }
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

                    Distribute.setListener(object : DistributeListener {
                        override fun onReleaseAvailable(
                            activity: Activity?,
                            releaseDetails: ReleaseDetails?
                        ): Boolean {
                            releaseDetails?.let {
                                AppCenterManager.releaseDetail.value = it
                                AppCenterManager.showReleaseDialog.value = true
                            }
                            return true
                        }

                        override fun onNoReleaseAvailable(activity: Activity?) {

                        }
                    })
                }
            }.onFailure {
                it.printStackTrace()
            }
        }
    }


    private fun isMainProcess(): Boolean {
        return packageName == if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getProcessName()
        } else {
            getProcessName(this) ?: packageName
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