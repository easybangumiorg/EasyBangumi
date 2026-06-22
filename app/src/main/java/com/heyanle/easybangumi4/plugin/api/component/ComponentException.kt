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

class NeedWebViewCheckExceptionInner(
    val iWebProxy: IWebProxy,
    val tips: String? = null,
) : ComponentException("need web view check")

data class DialogCaptchaParam(
    val image: Any,
    val text: String? = null,
    val title: String? = null,
    val hint: String? = null,
    val onInput: (String) -> Unit,
)

data class SearchWebViewCheckParam(
    val key: Int,
    val keyword: String,
    val source: String,
    val iWebProxy: IWebProxy,
    val tips: String? = null,
)

class SearchNeedWebViewCheckBusinessException(
    val param: SearchWebViewCheckParam,
    val actionType: BusinessActionType = BusinessActionType.WEB_VIEW,
    val dialogCaptchaParam: DialogCaptchaParam? = null,
) : ComponentException("need user check")

data class PlayInfoWebViewCheckParam(
    val summary: CartoonSummary,
    val playLine: PlayLine,
    val episode: Episode,
    val iWebProxy: IWebProxy,
    val tips: String? = null,
)

class PlayInfoNeedWebViewCheckBusinessException(
    val param: PlayInfoWebViewCheckParam?,
    val actionType: BusinessActionType = BusinessActionType.WEB_VIEW,
    val dialogCaptchaParam: DialogCaptchaParam? = null,
) : ComponentException("need user check")
