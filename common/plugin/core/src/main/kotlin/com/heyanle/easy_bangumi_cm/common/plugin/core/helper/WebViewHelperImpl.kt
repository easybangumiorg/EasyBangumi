package com.heyanle.easy_bangumi_cm.common.plugin.core.helper

import com.heyanle.easy_bangumi_cm.plugin.utils.WebViewHelper


/**
 * Created by HeYanLe on 2025/2/5 22:20.
 * https://github.com/heyanLE
 */

class WebViewHelperImpl: WebViewHelper {

    // TODO
    override fun renderedHtml(strategy: WebViewHelper.RenderedStrategy): WebViewHelper.RenderedResult {
        return WebViewHelper.RenderedResult(
            strategy,
            strategy.url,
            true,
            "",
            ""
        )
    }
}