package com.heyanle.easybangumi4

import com.heyanle.easybangumi4.source.utils.network.WebViewHelperV2Impl
import com.heyanle.easybangumi4.source_api.utils.api.WebViewHelperV2
import com.heyanle.easybangumi4.utils.logi
import com.heyanle.inject.api.get
import com.heyanle.inject.core.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * Created by heyanle on 2024/6/10.
 * https://github.com/heyanLE
 */
object TestMain {


    fun main(){
        val helper = Inject.get<WebViewHelperV2Impl>()
        helper.openWebPage({false}, {})
//helper.getGlobalWebView().loadUrl("https://www.nyafun.net/play/7187-1-1.html")
        MainScope().launch(Dispatchers.IO) {
            try {

                val result = helper.renderedHtml(WebViewHelperV2.RenderedStrategy(
                    url = "https://www.nyafun.net/play/7187-1-1.html",
                    userAgentString = "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; QQDownload 732; .NET4.0C; .NET4.0E; LBBROWSER)",
                    callBackRegex = ".*mp4?verify=.*", //"".*mp4?verify=.*",
                    timeOut = 10000,
                ))
                result.logi("TestMain")
            }catch (e: Throwable){
                e.printStackTrace()
            }

        }
    }

}