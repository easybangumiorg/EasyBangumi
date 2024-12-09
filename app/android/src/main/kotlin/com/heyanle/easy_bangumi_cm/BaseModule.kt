package com.heyanle.easy_bangumi_cm

import android.app.Application
import com.heyanle.easy_bangumi_cm.base.AndroidLogger
import com.heyanle.easy_bangumi_cm.base.AndroidPathProvider
import com.heyanle.easy_bangumi_cm.base.AndroidPlatform
import com.heyanle.easy_bangumi_cm.base.Logger
import com.heyanle.easy_bangumi_cm.base.PathProvider
import com.heyanle.easy_bangumi_cm.base.Platform
import com.heyanle.easy_bangumi_cm.base.logger
import com.heyanle.easy_bangumi_cm.base.utils.MoshiArrayListJsonAdapter
import com.heyanle.inject.api.InjectModule
import com.heyanle.inject.api.InjectScope
import com.heyanle.inject.api.addAlias
import com.heyanle.inject.api.addSingletonFactory

/**
 * Created by heyanlin on 2023/10/30.
 */
class BaseModule(
    private val application: Application,
): InjectModule {
    override fun InjectScope.registerInjectables() {

        addSingletonFactory<Platform> {
            AndroidPlatform()
        }

        addSingletonFactory<PathProvider> {
            AndroidPathProvider(application)
        }

        addSingletonFactory<Logger> {
            logger ?: AndroidLogger()
        }
    }
}