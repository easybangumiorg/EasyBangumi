package com.heyanle.easybangumi4.plugin.api.component

import com.heyanle.easybangumi4.plugin.api.entity.CartoonSummary
import com.heyanle.easybangumi4.plugin.api.entity.Episode
import com.heyanle.easybangumi4.plugin.api.entity.PlayLine
import com.heyanle.easybangumi4.plugin.source.utils.network.web.IWebProxy

/**
 * Created by heyanlin on 2026/2/24.
 */
open class ComponentException(msg: String) : RuntimeException(msg)

enum class BusinessActionType {
    WEB_VIEW,
    DIALOG_CAPTCHA,
}

sealed class VerificationParam {
    data class WebView(
        val iWebProxy: IWebProxy,
        val tips: String? = null,
    ) : VerificationParam()

    data class ImageCaptcha(
        val image: Any,
        val text: String? = null,
        val title: String? = null,
        val hint: String? = null,
    ) : VerificationParam()
}

sealed class VerificationResult {
    data class WebView(
        val iWebProxy: IWebProxy,
    ) : VerificationResult()

    data class ImageCaptcha(
        val input: String,
    ) : VerificationResult()
}

class NeedWebViewCheckExceptionInner(
    val iWebProxy: IWebProxy,
    val tips: String? = null,
) : ComponentException("need web view check")

data class DialogCaptchaParam(
    val image: Any,
    val text: String? = null,
    val title: String? = null,
    val hint: String? = null,
)

data class SearchVerificationRequest(
    val key: Int,
    val keyword: String,
    val source: String,
)

class SearchNeedVerificationBusinessException(
    val request: SearchVerificationRequest,
    val verificationParam: VerificationParam,
) : ComponentException("need user check") {
    constructor(
        request: SearchVerificationRequest,
        iWebProxy: IWebProxy,
        tips: String? = null,
    ) : this(request, VerificationParam.WebView(iWebProxy, tips))

    val actionType: BusinessActionType
        get() = when (verificationParam) {
            is VerificationParam.WebView -> BusinessActionType.WEB_VIEW
            is VerificationParam.ImageCaptcha -> BusinessActionType.DIALOG_CAPTCHA
        }

    val dialogCaptchaParam: DialogCaptchaParam?
        get() = (verificationParam as? VerificationParam.ImageCaptcha)?.let {
            DialogCaptchaParam(
                image = it.image,
                text = it.text,
                title = it.title,
                hint = it.hint,
            )
        }
}

typealias SearchNeedWebViewCheckBusinessException = SearchNeedVerificationBusinessException

data class PlayInfoVerificationRequest(
    val summary: CartoonSummary,
    val playLine: PlayLine,
    val episode: Episode,
)

class PlayInfoNeedVerificationBusinessException(
    val request: PlayInfoVerificationRequest?,
    val verificationParam: VerificationParam,
) : ComponentException("need user check") {
    constructor(
        request: PlayInfoVerificationRequest,
        iWebProxy: IWebProxy,
        tips: String? = null,
    ) : this(request, VerificationParam.WebView(iWebProxy, tips))

    val actionType: BusinessActionType
        get() = when (verificationParam) {
            is VerificationParam.WebView -> BusinessActionType.WEB_VIEW
            is VerificationParam.ImageCaptcha -> BusinessActionType.DIALOG_CAPTCHA
        }

    val dialogCaptchaParam: DialogCaptchaParam?
        get() = (verificationParam as? VerificationParam.ImageCaptcha)?.let {
            DialogCaptchaParam(
                image = it.image,
                text = it.text,
                title = it.title,
                hint = it.hint,
            )
        }
}

typealias PlayInfoNeedWebViewCheckBusinessException = PlayInfoNeedVerificationBusinessException
