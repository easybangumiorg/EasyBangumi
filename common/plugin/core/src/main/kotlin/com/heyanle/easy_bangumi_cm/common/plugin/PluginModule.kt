package com.heyanle.easy_bangumi_cm.common.plugin

import com.heyanle.easy_bangumi_cm.common.plugin.core.EasyPluginConfigProvider
import com.heyanle.easy_bangumi_cm.common.plugin.core.extension.ExtensionController
import com.heyanle.easy_bangumi_cm.common.plugin.core.helper.PreferenceHelperImpl
import com.heyanle.easy_bangumi_cm.common.plugin.core.helper.StringHelperImpl
import com.heyanle.easy_bangumi_cm.common.plugin.core.helper.WebViewHelperImpl
import com.heyanle.easy_bangumi_cm.common.plugin.core.inner.InnerExtensionManifestProvider
import com.heyanle.easy_bangumi_cm.common.plugin.core.source.SourceConfigController
import com.heyanle.easy_bangumi_cm.common.plugin.core.source.SourceController
import com.heyanle.easy_bangumi_cm.plugin.entity.SourceManifest
import com.heyanle.easy_bangumi_cm.plugin.utils.PreferenceHelper
import com.heyanle.easy_bangumi_cm.plugin.utils.StringHelper
import com.heyanle.easy_bangumi_cm.plugin.utils.WebViewHelper
import com.heyanle.lib.inject.api.*

/**
 * Created by heyanlin on 2024/12/5.
 */
class PluginModule(
    private val customConfig: Boolean = false,
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

        addSingletonFactory {
            InnerExtensionManifestProvider(get())
        }

        if (!customConfig) {
            addSingletonFactory {
                EasyPluginConfigProvider.Default
            }
        }

        // Plugin Utils
        addPerKeyFactory<PreferenceHelper, SourceManifest> {
            PreferenceHelperImpl(it)
        }

        addSingletonFactory<WebViewHelper> {
            WebViewHelperImpl()
        }
        addPerKeyFactory<WebViewHelper, SourceManifest> {
            get()
        }

        addSingletonFactory<StringHelper> {
            StringHelperImpl()
        }
        addPerKeyFactory<StringHelper, SourceManifest> {
            get()
        }
    }
}