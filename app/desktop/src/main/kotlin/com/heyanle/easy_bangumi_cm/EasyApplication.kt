package com.heyanle.easy_bangumi_cm

import com.heyanle.easy_bangumi_cm.base.*
import com.heyanle.easy_bangumi_cm.base.path_provider.PathProvider
import com.heyanle.easy_bangumi_cm.base.preference.HeKVPreferenceStore
import com.heyanle.easy_bangumi_cm.base.preference.PreferenceStore
import com.heyanle.easy_bangumi_cm.shared.SharedApp
import com.heyanle.easy_bangumi_cm.utils.jvm.HeKV
import org.koin.core.context.startKoin
import org.koin.core.definition.Definition

/**
 * Created by heyanlin on 2024/12/3.
 */
class EasyApplication: BaseFactory {

    companion object {
        lateinit var instance: EasyApplication
    }

    init {
        instance = this
        initKoin()
        initBase()
        initShared()
    }

    // ================== InitDesktopKoin ==================

    private fun initKoin() {
        startKoin {
            logger(_logger)
        }
    }


    // ================== InitBase ==================

    private val _logger = DesktopLogger()

    override val makePathProvider: Definition<PathProvider>
        get() = {
            DesktopPathProvider()
        }
    override val makeCoroutineProvider: Definition<CoroutineProvider>
        get() = {
            DesktopCoroutineProvider()
        }
    override val makeLogger: Definition<Logger>
        get() = {
            _logger
        }
    override val makePreferenceStore: Definition<PreferenceStore>
        get() = {
            val pathProvider = get<PathProvider>()
            val path = pathProvider.getFilePath("preference")
            val hekv = HeKV(path, "global")
            HeKVPreferenceStore(hekv)
        }
    override val makePlatform: Definition<Platform>
        get() = {
            DesktopPlatform()
        }

    private fun initBase() {
        BaseApp.init(this)
    }

    // ================== InitShared ==================

    private fun initShared() {
        SharedApp.init()
    }
}