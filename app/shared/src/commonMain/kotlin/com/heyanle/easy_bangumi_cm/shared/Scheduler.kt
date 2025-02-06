package com.heyanle.easy_bangumi_cm.shared


import com.heyanle.easy_bangumi_cm.common.plugin.PluginModule
import com.heyanle.easy_bangumi_cm.database.DatabaseModule
import com.heyanle.easy_bangumi_cm.shared.plugin.PluginConfigProvider
import com.heyanle.lib.inject.api.addSingletonFactory
import com.heyanle.lib.inject.core.Inject

/**
 * 初始化时点分发
 * Created by heyanlin on 2024/12/3.
 */

object Scheduler {

    fun onInit() {
        SharedModule().registerWith(Inject)
        DatabaseModule().registerWith(Inject)

        Inject.addSingletonFactory {
            PluginConfigProvider()
        }
        PluginModule(true)
    }

    fun onSplashPageLaunch(){

    }

    fun onHomePageLaunch(){

    }


}

