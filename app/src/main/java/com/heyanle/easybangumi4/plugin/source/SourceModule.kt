package com.heyanle.easybangumi4.plugin.source

import android.app.Application
import com.heyanle.easybangumi4.base.hekv.HeKV
import com.heyanle.easybangumi4.plugin.source.repository.RepositoryController
import com.heyanle.easybangumi4.plugin.source.repository.RepositoryPreferences
import com.heyanle.easybangumi4.plugin.source.utils.CaptchaHelperImpl
import com.heyanle.easybangumi4.plugin.source.utils.PreferenceHelperImpl
import com.heyanle.easybangumi4.plugin.source.utils.StringHelperImpl
import com.heyanle.easybangumi4.plugin.source.utils.network.NetworkHelperImpl
import com.heyanle.easybangumi4.plugin.source.utils.network.OkhttpHelperImpl
import com.heyanle.easybangumi4.plugin.source.utils.network.RenderHelperImpl
import com.heyanle.easybangumi4.plugin.source.utils.network.WebViewHelperV2Impl
import com.heyanle.easybangumi4.plugin.source.utils.network.web.WebProxyManager
import com.heyanle.easybangumi4.plugin.source.utils.network.web.WebProxyProvider
import com.heyanle.easybangumi4.setting.SettingMMKVPreferences
import com.heyanle.easybangumi4.plugin.source.push.SourcePushController
import com.heyanle.easybangumi4.plugin.api.utils.api.CaptchaHelper
import com.heyanle.easybangumi4.plugin.api.utils.api.NetworkHelper
import com.heyanle.easybangumi4.plugin.api.utils.api.OkhttpHelper
import com.heyanle.easybangumi4.plugin.api.utils.api.PreferenceHelper
import com.heyanle.easybangumi4.plugin.api.utils.api.RenderHelper
import com.heyanle.easybangumi4.plugin.api.utils.api.StringHelper
import com.heyanle.easybangumi4.plugin.api.utils.api.WebViewHelperV2
import com.heyanle.easybangumi4.utils.WebViewManager
import com.heyanle.easybangumi4.utils.WebViewRuntime
import com.heyanle.easybangumi4.utils.getFilePath
import com.heyanle.inject.api.InjectModule
import com.heyanle.inject.api.InjectScope
import com.heyanle.inject.api.addAlias
import com.heyanle.inject.api.addSingletonFactory
import com.heyanle.inject.api.get
import java.io.File

/**
 * Created by heyanlin on 2023/11/1.
 */
class SourceModule(
    private val application: Application
) : InjectModule {

    override fun InjectScope.registerInjectables() {

        // Create this eagerly while modules are registered on the application main thread.
        // DefaultInjectScope's first singleton lookup can race across source-loading threads,
        // which would otherwise create multiple WebView providers with independent locks.
        val webViewRuntime = WebViewRuntime(
            application = application,
            // SettingMMKVPreferences cannot be constructed until Scheduler initializes Okkv.
            // Resolve it only when WebView is actually used, after module registration ends.
            shouldSpoofPackageName = {
                !get<SettingMMKVPreferences>().webViewCompatible.get()
            },
        )


        addSingletonFactory<ISourceController> {
            get<SourceController>()
        }

        addSingletonFactory {
            SourceController(
                sourceFolder = File(application.getFilePath("source_v3")),
                sourcePreferences = get(),
                innerSourceFileProvider = get(),
            )
        }
        addSingletonFactory {
            InnerSourceFileProvider(
                application = application,
                cacheFolder = File(application.getFilePath("inner_source")),
            )
        }
        addSingletonFactory {
            SourcePushController(application, get())
        }


        // StringHelper
        addScopedPerKeyFactory<StringHelper, String> {
            StringHelperImpl
        }

        addScopedPerKeyFactory<CaptchaHelper, String> {
            CaptchaHelperImpl
        }

        addSingletonFactory {
            webViewRuntime
        }

        // NetworkHelper
        addSingletonFactory<NetworkHelperImpl> {
            NetworkHelperImpl(webViewRuntime)
        }
        addScopedPerKeyFactory<NetworkHelper, String> {
            get<NetworkHelperImpl>()
        }

        // OkHttpHelper
        addScopedPerKeyFactory<OkhttpHelperImpl, String> {
            OkhttpHelperImpl(application, get(it), get())
        }
        addAlias<OkhttpHelperImpl, OkhttpHelper>()

        // PreferenceHelper
        addScopedPerKeyFactory<PreferenceHelperImpl, String> {
            PreferenceHelperImpl(HeKV(application.getFilePath("source_preference"), it))
        }
        addAlias<PreferenceHelperImpl, PreferenceHelper>()

        addSingletonFactory {
            WebViewManager(webViewRuntime)
        }

        addSingletonFactory<RenderHelperImpl> {
            RenderHelperImpl(get())
        }
        addScopedPerKeyFactory<RenderHelper, String> {
            get<RenderHelperImpl>()
        }

        // WebViewHelperV2
        addSingletonFactory<WebViewHelperV2Impl> {
            WebViewHelperV2Impl(get())
        }
        addScopedPerKeyFactory<WebViewHelperV2, String> {
            get<WebViewHelperV2Impl>()
        }

        // webProxy
        addScopedPerKeyFactory<WebProxyProvider, WebProxyManager> {
            WebProxyProvider(it, get())
        }

        addScopedPerKeyFactory<WebProxyManager, String> {
            WebProxyManager()
        }

        addSingletonFactory {
            RepositoryPreferences()
        }
        addSingletonFactory {
            RepositoryController(
                sourceController = get(),
                repositoryPreferences = get(),
            )
        }

    }
}
