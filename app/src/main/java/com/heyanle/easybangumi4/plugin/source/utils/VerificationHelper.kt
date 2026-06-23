package com.heyanle.easybangumi4.plugin.source.utils

import com.heyanle.easybangumi4.plugin.api.component.VerificationParam
import com.heyanle.easybangumi4.plugin.api.component.VerificationResult
import com.heyanle.easybangumi4.plugin.source.utils.network.WebViewHelperV2Impl
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object VerificationHelper {

    suspend fun start(
        param: VerificationParam,
        webViewHelper: WebViewHelperV2Impl,
    ): VerificationResult {
        return when (param) {
            is VerificationParam.ImageCaptcha -> VerificationResult.ImageCaptcha(
                input = CaptchaHelperImpl.start(
                    image = param.image,
                    text = param.text,
                    title = param.title,
                    hint = param.hint,
                )
            )

            is VerificationParam.WebView -> {
                val webView = param.iWebProxy.getWebView()
                    ?: return VerificationResult.WebView(param.iWebProxy)
                suspendCoroutine { continuation ->
                    webViewHelper.openWebPage(
                        webView = webView,
                        tips = param.tips ?: "",
                        onCheck = { false },
                        onStop = {
                            continuation.resume(VerificationResult.WebView(param.iWebProxy))
                        },
                    )
                }
            }
        }
    }
}
