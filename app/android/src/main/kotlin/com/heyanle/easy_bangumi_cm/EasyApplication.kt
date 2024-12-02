package com.heyanle.easy_bangumi_cm

import android.app.Application
import com.heyanle.easy_bangumi_cm.base.AndroidLogger
import com.heyanle.easy_bangumi_cm.base.AndroidPathProvider
import com.heyanle.easy_bangumi_cm.base.Logger
import com.heyanle.easy_bangumi_cm.base.path_provider.PathProvider
import com.heyanle.easy_bangumi_cm.base.preference.AndroidPreferenceStore
import com.heyanle.easy_bangumi_cm.base.preference.PreferenceStore
import com.heyanle.easy_bangumi_cm.shared.SharedApp
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.bind
import org.koin.dsl.module


/**
 * Created by HeYanLe on 2024/12/3 0:13.
 * https://github.com/heyanLE
 */

class EasyApplication: Application() {

    companion object {
        lateinit var instance: EasyApplication
    }

    private val baseModule = module {
        single {
            AndroidPreferenceStore(get())
        } bind PreferenceStore::class

        single {
            AndroidLogger()
        } bind Logger::class

        single {
            AndroidPathProvider(get())
        } bind PathProvider::class
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        startKoin {
            androidLogger(Level.INFO)
            androidContext(this@EasyApplication)
            modules(baseModule)
        }
        SharedApp.init()
    }

}