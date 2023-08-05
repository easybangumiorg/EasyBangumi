package com.heyanle.easybangumi4.source.utils

import android.content.Context
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.BuildConfig
import com.heyanle.easybangumi4.compose.common.moeSnackBar
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.injekt.api.get
import com.heyanle.injekt.core.Injekt
import com.heyanle.lib_anim.utils.AppHelper
import com.heyanle.lib_anim.utils.FileHelper
import com.heyanle.lib_anim.utils.fileHelper
import com.heyanle.lib_anim.utils.network.NetworkHelper
import com.heyanle.lib_anim.utils.network.networkHelper
import com.heyanle.lib_anim.utils.network.webViewUserHelper
import com.heyanle.lib_anim.utils.network.webview_helper.WebViewHelperImpl
import com.heyanle.lib_anim.utils.network.webview_helper.webViewHelper
import com.heyanle.lib_anim.utils.provider.sourceProviderController
import com.heyanle.lib_anim.utils.stringHelper
import java.util.concurrent.atomic.AtomicBoolean


/**
 * Created by HeYanLe on 2023/2/1 17:49.
 * https://github.com/heyanLE
 */


private val sourceUtilsInitLock = AtomicBoolean(false)
fun initUtils(context: Context) {
    if(sourceUtilsInitLock.compareAndSet(false, true)){
        AppHelper.context = APP
        fileHelper = FileHelper(context)
        networkHelper = NetworkHelper(context, BuildConfig.DEBUG)
        networkHelper.onWebViewError = {
            stringRes(com.heyanle.easy_i18n.R.string.web_view_init_error_msg).moeSnackBar()
        }
        networkHelper.defaultUA
        stringHelper = StringHelperImpl()
        webViewHelper = WebViewHelperImpl(context)
        webViewUserHelper = WebViewUserHelperImpl

        sourceProviderController = Injekt.get()
    }

}