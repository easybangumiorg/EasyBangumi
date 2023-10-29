package com.heyanle.easybangumi4

import android.app.Application
import com.arialyy.aria.core.Aria
import com.heyanle.easy_crasher.CrashHandler
import com.heyanle.easybangumi4.base.preferences.PreferenceStore
import com.heyanle.easybangumi4.base.preferences.android.AndroidPreferenceStore
import com.heyanle.easybangumi4.base.preferences.mmkv.MMKVPreferenceStore
import com.heyanle.easybangumi4.utils.exo_ssl.CropUtil
import com.heyanle.easybangumi4.base.utils.exo_ssl.TrustAllHostnameVerifier
import com.heyanle.easybangumi4.exo.mediaModule
import com.heyanle.easybangumi4.setting.SettingMMKVPreferences
import com.heyanle.easybangumi4.theme.EasyThemeController
import com.heyanle.okkv2.MMKVStore
import com.heyanle.okkv2.core.Okkv
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.binds
import org.koin.dsl.module
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
        initKoin(application)
    }

    /**
     * application#onCreate
     */
    fun runOnAppCreate(application: Application) {
        initCrasher(application)
        loadSettingModule(application)
        loadKoinModules(mediaModule)
        initAppCenter(application)
        initOkkv(application)
        initAria(application)
        initTrustAllHost()
    }

    /**
     * BootActivity#onCreate
     */
    fun runOnBootActivityCreate() {}

    /**
     * MainActivity#onCreate
     */
    fun runOnMainActivityCreate() {}

    /**
     * 初始化 koin
     */
    private fun initKoin(application: Application){
        startKoin {
            androidLogger(Level.INFO)
            androidContext(application)
            module {
                single {
                    application
                }

                // 以下配置需要提前
                single {
                    MMKVPreferenceStore(application)
                }

                single {
                    SettingMMKVPreferences(get())
                }
            }
        }
    }

    /**
     * 装置设置相关 module
     */
    private fun loadSettingModule(application: Application){
        loadKoinModules(module {
            single {
                AndroidPreferenceStore(application)
            } binds arrayOf(AndroidPreferenceStore::class, PreferenceStore::class)
        })
    }

    /**
     * 装配各种 controller （一些属于比较大型业务的 controller 会装配在对应业务的 module 里）
     */
    private fun loadControllerModule(application: Application){
        loadKoinModules(module {
            single {
                EasyThemeController(get())
            }
        })
    }

    /**
     * 全局异常捕获 + crash 界面
     */
    private fun initCrasher(application: Application){
        Thread.setDefaultUncaughtExceptionHandler(CrashHandler(application))
    }

    /**
     * 允许 http 链接
     */
    private fun initTrustAllHost(){
        HttpsURLConnection.setDefaultSSLSocketFactory(com.heyanle.easybangumi4.utils.exo_ssl.CropUtil.getUnsafeSslSocketFactory())
        HttpsURLConnection.setDefaultHostnameVerifier(TrustAllHostnameVerifier())
    }


    /**
     * 初始化 App Center
     */
    private fun initAppCenter(application: Application){

    }

    /**
     * 初始化 okkv
     */
    private fun initOkkv(application: Application){
        Okkv.Builder(MMKVStore(application)).cache().build().init().default()
        // 如果不使用缓存，请手动指定 key
        Okkv.Builder(MMKVStore(application)).build().init().default("no_cache")
    }

    /**
     * 初始化 aria
     */
    private fun initAria(application: Application){
        Aria.init(application)
        Aria.get(application).downloadConfig.isConvertSpeed = true
    }
}