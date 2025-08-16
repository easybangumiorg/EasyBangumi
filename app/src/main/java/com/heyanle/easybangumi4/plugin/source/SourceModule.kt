package com.heyanle.easybangumi4.plugin.source

import android.app.Application
import com.heyanle.easybangumi4.base.hekv.HeKV
import com.heyanle.easybangumi4.plugin.source.utils.CaptchaHelperImpl
import com.heyanle.easybangumi4.plugin.source.utils.NativeHelperImpl
import com.heyanle.easybangumi4.plugin.source.utils.PreferenceHelperImpl
import com.heyanle.easybangumi4.plugin.source.utils.StringHelperImpl
import com.heyanle.easybangumi4.plugin.source.utils.network.NetworkHelperImpl
import com.heyanle.easybangumi4.plugin.source.utils.network.OkhttpHelperImpl
import com.heyanle.easybangumi4.plugin.source.utils.network.WebViewHelperImpl
import com.heyanle.easybangumi4.plugin.source.utils.network.WebViewHelperV2Impl
import com.heyanle.easybangumi4.setting.SettingMMKVPreferences

import com.heyanle.easybangumi4.source_api.utils.api.CaptchaHelper
import com.heyanle.easybangumi4.source_api.utils.api.NetworkHelper
import com.heyanle.easybangumi4.source_api.utils.api.OkhttpHelper
import com.heyanle.easybangumi4.source_api.utils.api.PreferenceHelper
import com.heyanle.easybangumi4.source_api.utils.api.StringHelper
import com.heyanle.easybangumi4.source_api.utils.api.WebViewHelper
import com.heyanle.easybangumi4.source_api.utils.api.WebViewHelperV2
import com.heyanle.easybangumi4.utils.getFilePath
import com.heyanle.inject.api.InjectModule
import com.heyanle.inject.api.InjectScope
import com.heyanle.inject.api.addAlias
import com.heyanle.inject.api.addSingletonFactory
import com.heyanle.inject.api.get

/**
 * Created by heyanlin on 2023/11/1.
 */
class SourceModule(
    private val application: Application
) : InjectModule {

    override fun InjectScope.registerInjectables() {

        addSingletonFactory {
            NativeHelperImpl(application)
        }

        addSingletonFactory<ISourceController> {
            val mmkvSetting = get<SettingMMKVPreferences>()
            if (mmkvSetting.extensionV2Temp) {
                get<SourceControllerV2>()
            } else {
                get<SourceController>()
            }
        }

        addSingletonFactory {
            SourceController(get(), get(), get())
        }
        addSingletonFactory {
            SourceControllerV2(get(), get())
        }


        // StringHelper
        addScopedPerKeyFactory<StringHelper, String> {
            StringHelperImpl
        }

        addScopedPerKeyFactory<CaptchaHelper, String> {
            CaptchaHelperImpl
        }

        // NetworkHelper
        addSingletonFactory<NetworkHelperImpl> {
            NetworkHelperImpl(
                application
            )
        }
        addScopedPerKeyFactory<NetworkHelper, String> {
            get<NetworkHelperImpl>()
        }

        // OkHttpHelper
        addScopedPerKeyFactory<OkhttpHelperImpl, String> {
            OkhttpHelperImpl(application, get(it), get(it), get())
        }
        addAlias<OkhttpHelperImpl, OkhttpHelper>()

        // PreferenceHelper
        addScopedPerKeyFactory<PreferenceHelperImpl, String> {
            PreferenceHelperImpl(HeKV(application.getFilePath("source_preference"), it))
        }
        addAlias<PreferenceHelperImpl, PreferenceHelper>()

        // WebViewHelper
        addScopedPerKeyFactory<WebViewHelper, String> {
            WebViewHelperImpl(get(it))
        }

        // WebViewHelperV2
        addSingletonFactory<WebViewHelperV2Impl> {
            WebViewHelperV2Impl()
        }
        addScopedPerKeyFactory<WebViewHelperV2, String> {
            get<WebViewHelperV2Impl>()
        }

    }
}