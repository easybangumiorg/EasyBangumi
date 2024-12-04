package com.heyanle.easy_bangumi_cm

import com.heyanle.easy_bangumi_cm.base.DesktopLogger
import com.heyanle.easy_bangumi_cm.base.DesktopPathProvider
import com.heyanle.easy_bangumi_cm.base.DesktopPlatform
import com.heyanle.easy_bangumi_cm.shared.base.Logger
import com.heyanle.easy_bangumi_cm.shared.base.PathProvider
import com.heyanle.easy_bangumi_cm.shared.base.Platform
import com.heyanle.easy_bangumi_cm.shared.base.logger
import com.heyanle.inject.api.InjectModule
import com.heyanle.inject.api.InjectScope
import com.heyanle.inject.api.addSingletonFactory

/**
 * Created by heyanlin on 2024/12/3.
 */
class BaseModule: InjectModule {
    override fun InjectScope.registerInjectables() {

        addSingletonFactory<Platform> {
            DesktopPlatform()
        }

        addSingletonFactory<PathProvider> {
            DesktopPathProvider()
        }

        addSingletonFactory<Logger> {
            logger ?: DesktopLogger()
        }

    }
}