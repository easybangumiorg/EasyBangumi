package com.heyanle.lib_anim.utils.provider

import com.heyanle.lib_anim.utils.SourceContext
import com.heyanle.lib_anim.utils.StringHelper
import com.heyanle.lib_anim.utils.network.NetworkHelper
import com.heyanle.lib_anim.utils.network.WebViewUserHelper
import com.heyanle.lib_anim.utils.network.webview_helper.WebviewHelper
import com.heyanle.lib_anim.utils.preference.PreferenceHelper

/**
 * 工具收归，给源提供自己特定实例
 * SourceProvider.of(Source) 获取实例
 * Created by HeYanLe on 2023/8/4 22:36.
 * https://github.com/heyanLE
 */
class SourceProvider(
    val stringHelper: StringHelper,
    val networkHelper: NetworkHelper,
    val webViewUserHelper: WebViewUserHelper,
    val webViewHelper: WebviewHelper,
    val preferenceHelper: PreferenceHelper,
){
    companion object {
        fun of(sourceContext: SourceContext): SourceProvider {
            return sourceProviderController.getProvider(sourceContext) ?: throw NullPointerException("SourceProvider is null ${sourceContext}")
        }
    }
}