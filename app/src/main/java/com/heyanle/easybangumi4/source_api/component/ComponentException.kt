package com.heyanle.easybangumi4.source_api.component

import com.heyanle.easybangumi4.plugin.source.utils.network.web.IWebProxy
import com.heyanle.easybangumi4.source_api.entity.CartoonSummary
import com.heyanle.easybangumi4.source_api.entity.Episode
import com.heyanle.easybangumi4.source_api.entity.PlayLine

/**
 * Created by heyanlin on 2026/2/24.
 */
open class ComponentException(msg: String): RuntimeException(msg)

// 插件抛出的异常，后续会封装成 SearchNeedWebViewCheckBusinessException 或 PlayInfoNeedWebViewCheckBusinessException
class NeedWebViewCheckExceptionInner(
    val iWebProxy: IWebProxy,
    val tips: String? = null,
): ComponentException("需要启动网页效验")

data class SearchWebViewCheckParam(
    val key: Int,
    val keyword: String,
    val source: String,
    val iWebProxy: IWebProxy,
    val tips: String? = null,
)

// 分页组件抛出的异常，直接给业务
class SearchNeedWebViewCheckBusinessException(
    val param: SearchWebViewCheckParam
): ComponentException("需要启动网页效验")




data class PlayInfoWebViewCheckParam(
    val summary: CartoonSummary,
    val playLine: PlayLine,
    val episode: Episode,
    val iWebProxy: IWebProxy,
    val tips: String? = null,
)

class PlayInfoNeedWebViewCheckBusinessException(
    val param: PlayInfoWebViewCheckParam
): ComponentException("需要启动网页效验")
