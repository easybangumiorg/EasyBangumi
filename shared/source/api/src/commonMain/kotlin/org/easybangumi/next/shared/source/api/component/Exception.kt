package org.easybangumi.next.shared.source.api.component

import org.easybangumi.next.lib.webview.IWebView


/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 */

open class ComponentException(msg: String): RuntimeException(msg)

data class WebViewCheckParam(
    val tips: String? = null,
    val iWebView: IWebView,
    val check: (suspend (IWebView) -> Boolean)? = null,
    val onFinish: (() -> Unit)? = null
)
class NeedWebViewCheckException(
    val param: WebViewCheckParam
): ComponentException("需要启动网页效验")