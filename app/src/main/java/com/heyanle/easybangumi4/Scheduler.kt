package com.heyanle.easybangumi4

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.app.Application
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.animation.addListener
import com.arialyy.aria.core.Aria
import com.heyanle.easy_crasher.CrashHandler
import com.heyanle.easybangumi4.cartoon.CartoonModule
import com.heyanle.easybangumi4.cartoon_download.CartoonDownloadModule
import com.heyanle.easybangumi4.exo.MediaModule
import com.heyanle.easybangumi4.extension.ExtensionController
import com.heyanle.easybangumi4.extension.ExtensionModule
import com.heyanle.easybangumi4.case.CaseModule
import com.heyanle.easybangumi4.dlna.DlnaModule
import com.heyanle.easybangumi4.setting.SettingModule
import com.heyanle.easybangumi4.source.SourceModule
import com.heyanle.easybangumi4.ui.common.moeDialog
import com.heyanle.easybangumi4.utils.AppCenterManager
import com.heyanle.easybangumi4.utils.UUIDHelper
import com.heyanle.easybangumi4.utils.exo_ssl.CropUtil
import com.heyanle.easybangumi4.utils.exo_ssl.TrustAllHostnameVerifier
import com.heyanle.extension_api.IconFactory
import com.heyanle.extension_api.iconFactory
import com.heyanle.injekt.core.Injekt
import com.heyanle.okkv2.MMKVStore
import com.heyanle.okkv2.core.Okkv
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import com.microsoft.appcenter.distribute.Distribute
import com.microsoft.appcenter.distribute.DistributeListener
import com.microsoft.appcenter.distribute.ReleaseDetails
import com.tencent.bugly.crashreport.CrashReport
import javax.net.ssl.HttpsURLConnection

/**
 * 全局初始化时点分发
 * Created by HeYanLe on 2023/10/29 14:39.
 * https://github.com/heyanLE
 */
object Scheduler {

    /**
     * application#init
     */
    fun runOnAppInit(application: Application) {
        RootModule(application).registerWith(Injekt)
    }

    /**
     * application#onCreate
     */
    fun runOnAppCreate(application: Application) {
        initCrasher(application)

        // 注册各种 Controller
        SettingModule(application).registerWith(Injekt)
        ControllerModule(application).registerWith(Injekt)
        CartoonModule(application).registerWith(Injekt)
        MediaModule(application).registerWith(Injekt)
        CaseModule(application).registerWith(Injekt)
        ExtensionModule(application).registerWith(Injekt)
        SourceModule(application).registerWith(Injekt)
        CartoonDownloadModule(application).registerWith(Injekt)
        DlnaModule(application).registerWith(Injekt)


        initOkkv(application)
        initAppCenter(application)
        initBugly(application)
        initAria(application)
        initTrustAllHost()
    }


    /**
     * MainActivity#onCreate
     */
    fun runOnMainActivityCreate(activity: MainActivity, isFirst: Boolean) {
        Migrate.update(activity)
        val extensionController: ExtensionController by Injekt.injectLazy()
        val extensionIconFactory: IconFactory by Injekt.injectLazy()
        iconFactory = extensionIconFactory
        extensionController.init()
    }


    /**
     * 全局异常捕获 + crash 界面
     */
    private fun initCrasher(application: Application) {
        Thread.setDefaultUncaughtExceptionHandler(CrashHandler(application))
    }

    /**
     * 允许 http 链接
     */
    private fun initTrustAllHost() {
        HttpsURLConnection.setDefaultSSLSocketFactory(CropUtil.getUnsafeSslSocketFactory())
        HttpsURLConnection.setDefaultHostnameVerifier(TrustAllHostnameVerifier())
    }

    private fun initBugly(application: Application) {
        if (!BuildConfig.DEBUG) {
            CrashReport.initCrashReport(application)
            CrashReport.setDeviceModel(application, android.os.Build.MODEL)
            CrashReport.setDeviceId(application, UUIDHelper.getUUID())

        }
    }

    /**
     * 初始化 App Center
     */
    private fun initAppCenter(application: Application) {
        if (!BuildConfig.DEBUG) {
            kotlin.runCatching {
                // https://appcenter.ms
                val sc = BuildConfig.APP_CENTER_SECRET
                Log.i("BangumiApp", "app center secret -> $sc")
                if (sc.isNotEmpty()) {
                    AppCenter.start(
                        application, sc,
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
                                if (it.version != BuildConfig.VERSION_CODE) {
                                    AppCenterManager.releaseDetail.value = it
                                    AppCenterManager.showReleaseDialog.value = true
                                }
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

    /**
     * 初始化 okkv
     */
    private fun initOkkv(application: Application) {
        Okkv.Builder(MMKVStore(application)).cache().build().init().default()
        // 如果不使用缓存，请手动指定 key
        Okkv.Builder(MMKVStore(application)).build().init().default("no_cache")
    }

    /**
     * 初始化 aria
     */
    private fun initAria(application: Application) {
        Aria.init(application)
        Aria.get(application).downloadConfig.isConvertSpeed = true
    }
}