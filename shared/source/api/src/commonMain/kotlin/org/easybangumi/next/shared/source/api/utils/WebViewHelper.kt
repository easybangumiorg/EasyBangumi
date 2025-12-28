package org.easybangumi.next.shared.source.api.utils

import org.easybangumi.next.lib.webview.IWebView


/**
 * Created by HeYanLe on 2024/12/8 23:03.
 * https://github.com/heyanLE
 */

interface WebViewHelper {

    suspend fun <R> use(block: suspend IWebView.()->R): R?

    // 记得关闭
    suspend fun newWebView(): IWebView

}