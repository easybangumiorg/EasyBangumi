package com.heyanle.easybangumi4.source.utils

import android.content.Context
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.compose.common.moeSnackBar
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.lib_anim.utils.SourceContext
import com.heyanle.lib_anim.utils.network.networkHelper
import com.heyanle.lib_anim.utils.network.webViewUserHelper
import com.heyanle.lib_anim.utils.network.webview_helper.webViewHelper
import com.heyanle.lib_anim.utils.provider.SourceProvider
import com.heyanle.lib_anim.utils.provider.SourceProviderController
import com.heyanle.lib_anim.utils.stringHelper
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by HeYanLe on 2023/8/5 15:29.
 * https://github.com/heyanLE
 */
class SourceProviderControllerImpl(
    private val context: Context
) : SourceProviderController {

    private val map: ConcurrentHashMap<SourceContext, SourceProvider> =
        ConcurrentHashMap()

    override fun getProvider(sourceContext: SourceContext): SourceProvider {
        val provider = map[sourceContext] ?: SourceProvider(
            stringHelper,
            networkHelper,
            webViewUserHelper,
            webViewHelper,
            SourcePreferenceHelper.of(context, sourceContext)
        )
        provider.networkHelper.onWebViewError = {
            stringRes(R.string.web_view_init_error_msg).moeSnackBar()
        }
        map[sourceContext] = provider
        return provider
    }
}