package com.heyanle.easybangumi4

import android.app.Application
import android.util.Base64
import com.heyanle.easy_crasher.CrashHandler
import com.heyanle.easybangumi4.cartoon.CartoonModule
import com.heyanle.easybangumi4.exo.MediaModule
import com.heyanle.easybangumi4.extension.ExtensionController
import com.heyanle.easybangumi4.extension.ExtensionModule
import com.heyanle.easybangumi4.case.CaseModule
import com.heyanle.easybangumi4.crash.SourceCrashController
import com.heyanle.easybangumi4.dlna.DlnaModule
import com.heyanle.easybangumi4.setting.SettingModule
import com.heyanle.easybangumi4.source.SourceModule
import com.heyanle.easybangumi4.source.utils.NativeHelperImpl
import com.heyanle.easybangumi4.splash.SplashActivity
import com.heyanle.easybangumi4.storage.StorageModule
import com.heyanle.easybangumi4.ui.common.dismiss
import com.heyanle.easybangumi4.ui.common.moeDialog
import com.heyanle.easybangumi4.utils.UUIDHelper
import com.heyanle.easybangumi4.utils.exo_ssl.CropUtil
import com.heyanle.easybangumi4.utils.exo_ssl.TrustAllHostnameVerifier
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.extension_api.IconFactory
import com.heyanle.extension_api.iconFactory
import com.heyanle.inject.api.get
import com.heyanle.inject.core.Inject
import com.heyanle.okkv2.MMKVStore
import com.heyanle.okkv2.core.Okkv
import com.heyanle.okkv2.core.okkv
import com.tencent.bugly.crashreport.CrashReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
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
        RootModule(application).registerWith(Inject)
    }

    /**
     * application#onCreate
     */
    fun runOnAppCreate(application: Application) {

        initCrasher(application)

        // 注册各种 Controller
        SettingModule(application).registerWith(Inject)
        ControllerModule(application).registerWith(Inject)
        CartoonModule(application).registerWith(Inject)
        MediaModule(application).registerWith(Inject)
        CaseModule(application).registerWith(Inject)
        ExtensionModule(application).registerWith(Inject)
        SourceModule(application).registerWith(Inject)
        StorageModule(application).registerWith(Inject)
        DlnaModule(application).registerWith(Inject)
        Inject.get<NativeHelperImpl>()
        initOkkv(application)
        initBugly(application)
        initAria(application)

        SourceCrashController.init(application, Inject.get())
        initTrustAllHost()
    }

    var first by okkv("first_visible_version_code", def = 0)

    fun runOnSplashActivityCreate(activity: SplashActivity, isFirst: Boolean) {
        Migrate.update(activity)
    }

    /**
     * MainActivity#onCreate
     */
    fun runOnMainActivityCreate(activity: MainActivity, isFirst: Boolean) {
        val extensionController: ExtensionController by Inject.injectLazy()
        val extensionIconFactory: IconFactory by Inject.injectLazy()
        iconFactory = extensionIconFactory
        extensionController.init()
        if (isFirst) {
            try {
                // 启动须知
                val firstAnnoBase = """
               ICAgICAgICAxLiDnuq=nuq/nnIvnlarmmK/kuLrkuoblrabkuaAgSml0cGFjayBjb21wb3NlIOWSjOmfs+inhumikeebuOWFs+aKgOacr+i=m+ihjOW8gOWPkeeahOS4gOS4qumhueebru+8jOWumOaWueS4jeaPkOS+m+aJk+WMheWSjOS4i+i9ve+8jOWFtua6kOS7o-eggeS7heS+m+S6pOa1geWtpuS5oOOAguWboOWFtuS7luS6uuengeiHquaJk-WMheWPkeihjOWQjumAoOaIkOeahOS4gOWIh+WQjuaenOacrOaWueamguS4jei0n+i0o+OAggogICAgICAgIDIuIOe6r+e6r+eci+eVquaJk+WMheWQjuS4jeaPkOS+m+S7u+S9leinhumikeWGheWuue+8jOmcgOimgeeUqOaIt+iHquW3seaJi+WKqOa3u+WKoOOAgueUqOaIt+iHquihjOWvvOWFpeeahOWGheWuueWSjOacrOi9r+S7tuaXoOWFs+OAggogICAgICAgIDMuIOe6r+e6r+eci+eVqua6kOeggeWujOWFqOWFjei0ue+8jOWcqCBHaXRodWIg5byA5rqQ44CC55So5oi35Y+v6Ieq6KGM5LiL6L295omT5YyF44CC5aaC5p6c5L2g5piv5pS26LS56LSt5Lmw55qE5pys6L2v5Lu277yM5YiZ5pys5pa55qaC5LiN6LSf6LSj44CC
            """.trimIndent().replace("=", "/").replace("-", "+")
                Base64.decode(firstAnnoBase, Base64.DEFAULT).toString(Charsets.UTF_8).moeDialog(
                    stringRes(com.heyanle.easy_i18n.R.string.first_anno),
                    dismissLabel = stringRes(com.heyanle.easy_i18n.R.string.cancel),
                    onDismiss = {
                        it.dismiss()
                    }
                )
            }catch (e: Throwable){
                e.printStackTrace()
            }

        }
    }

    fun runOnComposeLaunch(activity: MainActivity) {
        if (first != BuildConfig.VERSION_CODE) {
            try {
                // 更新日志
                val scope = MainScope()
                scope.launch(Dispatchers.IO) {
                    activity.assets?.open("update_log.txt")?.bufferedReader()?.use {
                        it.readText().moeDialog(
                            stringRes(com.heyanle.easy_i18n.R.string.version) + ": " + BuildConfig.VERSION_NAME,
                            dismissLabel = stringRes(com.heyanle.easy_i18n.R.string.cancel),
                            onDismiss = {
                                it.dismiss()
                            }
                        )
                    }
                }
            }catch (e: Throwable){
                e.printStackTrace()
            }
        }
        first = BuildConfig.VERSION_CODE
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
//        runCatching {
//            Aria.init(application)
//            Aria.get(application).downloadConfig.isConvertSpeed = true
//        }.onFailure {
//            it.printStackTrace()
//        }
    }
}