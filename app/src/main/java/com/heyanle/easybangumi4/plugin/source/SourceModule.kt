package com.heyanle.easybangumi4.plugin.source

import android.app.Application
import android.webkit.CookieManager
import com.heyanle.easybangumi4.base.hekv.HeKV
import com.heyanle.easybangumi4.plugin.source.utils.CaptchaHelperImpl
import com.heyanle.easybangumi4.plugin.source.utils.PreferenceHelperImpl
import com.heyanle.easybangumi4.plugin.source.utils.StringHelperImpl
import com.heyanle.easybangumi4.plugin.source.utils.network.NetworkHelperImpl
import com.heyanle.easybangumi4.plugin.source.utils.network.OkhttpHelperImpl
import com.heyanle.easybangumi4.plugin.source.utils.network.RenderHelperImpl
import com.heyanle.easybangumi4.plugin.source.utils.network.WebViewHelperV2Impl
import com.heyanle.easybangumi4.plugin.source.utils.network.web.WebProxyManager
import com.heyanle.easybangumi4.plugin.source.utils.network.web.WebProxyProvider
import com.heyanle.easybangumi4.plugin.source.push.SourcePushController
import com.heyanle.easybangumi4.plugin.api.utils.api.CaptchaHelper
import com.heyanle.easybangumi4.plugin.api.utils.api.NetworkHelper
import com.heyanle.easybangumi4.plugin.api.utils.api.OkhttpHelper
import com.heyanle.easybangumi4.plugin.api.utils.api.PreferenceHelper
import com.heyanle.easybangumi4.plugin.api.utils.api.RenderHelper
import com.heyanle.easybangumi4.plugin.api.utils.api.StringHelper
import com.heyanle.easybangumi4.plugin.api.utils.api.WebViewHelperV2
import com.heyanle.easybangumi4.utils.WebViewManager
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



        addSingletonFactory<ISourceController> {
            get<SourceController>()
        }

        addSingletonFactory {
            SourceController(
                sourceFolder = File(application.getFilePath("source_v3")),
                sourcePreferences = get(),
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
            OkhttpHelperImpl(application, get(it), get())
        }
        addAlias<OkhttpHelperImpl, OkhttpHelper>()

        // PreferenceHelper
        addScopedPerKeyFactory<PreferenceHelperImpl, String> {
            PreferenceHelperImpl(HeKV(application.getFilePath("source_preference"), it))
        }
        addAlias<PreferenceHelperImpl, PreferenceHelper>()

        addSingletonFactory {
            WebViewManager(CookieManager.getInstance())
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


    }
}

