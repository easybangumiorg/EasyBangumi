package com.heyanle.easy_bangumi_cm.base

import android.app.Application
import com.heyanle.easy_bangumi_cm.base.model.provider.AndroidPathProvider
import com.heyanle.easy_bangumi_cm.base.model.provider.IPathProvider
import com.heyanle.easy_bangumi_cm.base.model.system.AndroidLogger
import com.heyanle.easy_bangumi_cm.base.model.system.AndroidPlatformInformation
import com.heyanle.easy_bangumi_cm.base.model.system.ILogger
import com.heyanle.easy_bangumi_cm.base.model.system.IPlatformInformation
import com.heyanle.lib.inject.api.InjectModule
import com.heyanle.lib.inject.api.InjectScope
import com.heyanle.lib.inject.api.addAlias
import com.heyanle.lib.inject.api.addSingletonFactory

/**
 * Created by heyanlin on 2023/10/30.
 */
class BaseModule(
    private val application: Application,
): InjectModule {
    override fun InjectScope.registerInjectables() {

        addSingletonFactory {
            application
        }

        addSingletonFactory {
            AndroidPathProvider(application)
        }
        addAlias<AndroidPathProvider, IPathProvider>()

        addSingletonFactory {
            AndroidLogger()
        }
        addAlias<AndroidLogger, ILogger>()

        addSingletonFactory {
            AndroidPlatformInformation(application)
        }
        addAlias<AndroidPlatformInformation, IPlatformInformation>()

    }
}