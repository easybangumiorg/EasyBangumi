package com.heyanle.easybangumi4

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Looper
import android.os.Process
import com.heyanle.easybangumi4.crash.SourceCrashController
import com.heyanle.easybangumi4.setting.SettingMMKVPreferences
import com.heyanle.easybangumi4.source.SourceController
import com.heyanle.easybangumi4.source_api.utils.core.WebViewUtil
import com.heyanle.injekt.core.Injekt
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView
import kotlin.reflect.jvm.internal.impl.metadata.ProtoBuf

/**
 * Created by HeYanLe on 2023/2/18 22:47.
 * https://github.com/heyanLE
 */
lateinit var APP: App

class App : Application() {

    init {
        Scheduler.runOnAppInit(this)

        val view = GeckoView(this)

        val session = GeckoSession()
        session.contentBlockingDelegate

        val runtime = GeckoRuntime.create(this)

        runtime.serviceWorkerDelegate
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
                    val settingPreferences: SettingMMKVPreferences by Injekt.injectLazy()
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


    override fun onCreate() {

        super.onCreate()
        APP = this
        if (isMainProcess()) {
            Scheduler.runOnAppCreate(this)
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