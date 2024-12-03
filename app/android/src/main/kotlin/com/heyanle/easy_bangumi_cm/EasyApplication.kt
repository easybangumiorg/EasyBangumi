package com.heyanle.easy_bangumi_cm

import android.app.Application
import com.heyanle.easy_bangumi_cm.base.*
import com.heyanle.easy_bangumi_cm.base.path_provider.PathProvider
import com.heyanle.easy_bangumi_cm.base.preference.AndroidPreferenceStore
import com.heyanle.easy_bangumi_cm.base.preference.PreferenceStore
import com.heyanle.easy_bangumi_cm.shared.SharedApp
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.definition.Definition
import org.koin.core.logger.Level
import org.koin.dsl.bind
import org.koin.dsl.module


/**
 * Created by HeYanLe on 2024/12/3 0:13.
 * https://github.com/heyanLE
 */

class EasyApplication: Application(), BaseFactory {

    companion object {
        lateinit var instance: EasyApplication
    }

    // ================== Application ==================

    override fun onCreate() {
        super.onCreate()
        instance = this
        initKoin()
        initBase()
        initShared()
    }

    // ================== InitAndroidKoin ==================

    private fun initKoin() {
        startKoin {
            androidLogger(Level.INFO)
            androidContext(this@EasyApplication)
        }
    }

    // ================== InitBase ==================

    private val _logger = AndroidLogger()

    override val makePathProvider: Definition<PathProvider>
        get() = {
            AndroidPathProvider(get())
        }
    override val makeCoroutineProvider: Definition<CoroutineProvider>
        get() = {
            AndroidCoroutineProvider()
        }
    override val makeLogger: Definition<Logger>
        get() = {
            _logger
        }
    override val makePreferenceStore: Definition<PreferenceStore>
        get() = {
            AndroidPreferenceStore(get())
        }
    override val makePlatform: Definition<Platform>
        get() = {
            AndroidPlatform()
        }

    private fun initBase(){
        BaseApp.init(this)
    }

    // ================== InitShared ==================

    private fun initShared(){
        SharedApp.init()
    }
}