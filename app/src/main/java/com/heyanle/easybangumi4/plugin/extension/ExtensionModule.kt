package com.heyanle.easybangumi4.plugin.extension

import android.app.Application
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.plugin.extension.push.ExtensionPushController
import com.heyanle.easybangumi4.plugin.extension.remote.ExtensionRemoteController
import com.heyanle.easybangumi4.plugin.extension.remote.ExtensionRepoController
import com.heyanle.easybangumi4.plugin.js.JSDebugPreference
import com.heyanle.easybangumi4.setting.SettingMMKVPreferences
import com.heyanle.easybangumi4.utils.getCachePath
import com.heyanle.easybangumi4.utils.getFilePath
import com.heyanle.easybangumi4.utils.getInnerFilePath
import com.heyanle.extension_api.IconFactory
import com.heyanle.inject.api.InjectModule
import com.heyanle.inject.api.InjectScope
import com.heyanle.inject.api.addAlias
import com.heyanle.inject.api.addSingletonFactory
import com.heyanle.inject.api.get

/**
 * Created by heyanlin on 2023/11/1.
 */
class ExtensionModule(
    private val application: Application
) : InjectModule {

    private val extensionPath = application.getInnerFilePath("extension")
    private val extensionCachePath = application.getCachePath("extension")

    private val extensionJSPath = application.getFilePath("extension-js")

    private val extensionJSPathV2 = application.getFilePath("extension_v2")
    private val extensionCachePathV2 = application.getCachePath("extension_v2")
    override fun InjectScope.registerInjectables() {
        addSingletonFactory<IExtensionController> {
            val mmkvSetting = get<SettingMMKVPreferences>()
            if (mmkvSetting.extensionV2Temp) {
                get<ExtensionControllerV2>()
            } else {
                get<ExtensionController>()
            }
        }
        addSingletonFactory {
            ExtensionController(
                application,
                extensionPath,
                extensionJSPath,
                extensionCachePath
            )
        }
        addSingletonFactory {
            ExtensionControllerV2(
                extensionJSPathV2,
                get()
            )
        }

        addSingletonFactory {
            ExtensionRemoteController(get(), extensionCachePathV2)
        }
        addSingletonFactory {
            ExtensionRepoController(get(), get(), get(), extensionCachePathV2)
        }
        addSingletonFactory {
            ExtensionIconFactoryImpl(get())
        }
        addAlias<ExtensionIconFactoryImpl, IconFactory>()

        addSingletonFactory {
            ExtensionPushController(application, get(), get())
        }

        addSingletonFactory {
            JSDebugPreference(application, get(), get())
        }


    }
}