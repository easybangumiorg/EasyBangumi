package com.heyanle.easy_bangumi_cm.common.plugin

import com.heyanle.easy_bangumi_cm.common.plugin.core.EasyPluginConfigProvider
import com.heyanle.easy_bangumi_cm.common.plugin.core.extension.ExtensionController
import com.heyanle.easy_bangumi_cm.common.plugin.core.source.SourceConfigController
import com.heyanle.easy_bangumi_cm.common.plugin.core.source.SourceController
import com.heyanle.lib.inject.api.InjectModule
import com.heyanle.lib.inject.api.InjectScope
import com.heyanle.lib.inject.api.addSingletonFactory
import com.heyanle.lib.inject.api.get

/**
 * Created by heyanlin on 2024/12/5.
 */
class PluginModule(
    private val easyPluginConfigProvider: EasyPluginConfigProvider?
): InjectModule {

    override fun InjectScope.registerInjectables() {
        addSingletonFactory {
            ExtensionController(get())
        }

        addSingletonFactory {
            SourceConfigController(get())
        }

        addSingletonFactory {
            SourceController(get(), get(), get())
        }

        if (easyPluginConfigProvider != null) {
            addSingletonFactory<EasyPluginConfigProvider> {
                easyPluginConfigProvider
            }
        } else {
            addSingletonFactory {
                EasyPluginConfigProvider.Default
            }
        }
    }
}