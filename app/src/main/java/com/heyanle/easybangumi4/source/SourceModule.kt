package com.heyanle.easybangumi4.source

import android.app.Application
import com.heyanle.easybangumi4.BuildConfig
import com.heyanle.easybangumi4.base.hekv.HeKV
import com.heyanle.easybangumi4.source.utils.CaptchaHelperImpl
import com.heyanle.easybangumi4.source.utils.PreferenceHelperImpl
import com.heyanle.easybangumi4.source.utils.StringHelperImpl
import com.heyanle.easybangumi4.source.utils.WebViewHelperImpl
import com.heyanle.easybangumi4.source_api.utils.api.CaptchaHelper
import com.heyanle.easybangumi4.source_api.utils.api.NetworkHelper
import com.heyanle.easybangumi4.source_api.utils.api.OkhttpHelper
import com.heyanle.easybangumi4.source_api.utils.api.PreferenceHelper
import com.heyanle.easybangumi4.source_api.utils.api.StringHelper
import com.heyanle.easybangumi4.source_api.utils.api.WebViewHelper
import com.heyanle.easybangumi4.source_api.utils.core.network.NetworkHelperImpl
import com.heyanle.easybangumi4.source_api.utils.core.network.OkhttpHelperImpl
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.utils.getFilePath
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.injekt.api.InjektModule
import com.heyanle.injekt.api.InjektScope
import com.heyanle.injekt.api.addAlias
import com.heyanle.injekt.api.addSingletonFactory
import com.heyanle.injekt.api.get

/**
 * Created by heyanlin on 2023/11/1.
 */
class SourceModule(
    private val application: Application
) : InjektModule {

    override fun InjektScope.registerInjectables() {
        addSingletonFactory {
            SourceController(get(), get(), get())
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
            ) {
                stringRes(com.heyanle.easy_i18n.R.string.web_view_init_error_msg).moeSnackBar()
            }
        }
        addScopedPerKeyFactory<NetworkHelper, String> {
            get<NetworkHelperImpl>()
        }

        // OkHttpHelper
        addScopedPerKeyFactory<OkhttpHelperImpl, String> {
            OkhttpHelperImpl(application, get(it), get(it), get(it), BuildConfig.DEBUG)
        }
        addAlias<OkhttpHelperImpl, OkhttpHelper>()

        // PreferenceHelper
        addScopedPerKeyFactory<PreferenceHelperImpl, String> {
            PreferenceHelperImpl(HeKV(application.getFilePath("source_preference"), it))
        }
        addAlias<PreferenceHelperImpl, PreferenceHelper>()

        // WebViewHelper
        addScopedPerKeyFactory<WebViewHelper, String> {
            WebViewHelperImpl
        }

    }
}