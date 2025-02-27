package com.heyanle.easy_bangumi_cm.common.theme

import com.heyanle.lib.inject.api.InjectModule
import com.heyanle.lib.inject.api.InjectScope
import com.heyanle.lib.inject.api.addSingletonFactory
import com.heyanle.lib.inject.api.get

/**
 * Created by heyanlin on 2025/2/27.
 */
class ThemeModule: InjectModule {
    override fun InjectScope.registerInjectables() {
        addSingletonFactory {
            EasyThemeController(get(), get())
        }
    }
}