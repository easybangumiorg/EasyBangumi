package com.heyanle.easy_bangumi_cm.base

import com.heyanle.easy_bangumi_cm.base.model.provider.DesktopPathProvider
import com.heyanle.easy_bangumi_cm.base.model.provider.IPathProvider
import com.heyanle.easy_bangumi_cm.base.model.system.DesktopLogger
import com.heyanle.easy_bangumi_cm.base.model.system.DesktopPlatformInformation
import com.heyanle.easy_bangumi_cm.base.model.system.ILogger
import com.heyanle.easy_bangumi_cm.base.model.system.IPlatformInformation
import com.heyanle.lib.inject.api.*

/**
 * Created by heyanlin on 2024/12/3.
 */
class BaseModule: InjectModule {
    override fun InjectScope.registerInjectables() {

        addSingletonFactory {
            DesktopPathProvider(get())
        }
        addAlias<DesktopPathProvider, IPathProvider>()

        addSingletonFactory {
            DesktopLogger()
        }
        addAlias<DesktopLogger, ILogger>()

        addSingletonFactory {
            DesktopPlatformInformation()
        }
        addAlias<DesktopPlatformInformation, IPlatformInformation>()

    }
}