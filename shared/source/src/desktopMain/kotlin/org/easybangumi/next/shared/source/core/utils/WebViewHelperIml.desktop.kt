﻿package org.easybangumi.next.shared.source.core.utils

import org.easybangumi.next.jcef.JcefWebViewProxy
import org.easybangumi.next.lib.webview.IWebView
import org.easybangumi.next.shared.source.api.utils.WebViewHelper

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

class WebViewHelperImpl: WebViewHelper {
    override suspend fun <R> use(block: suspend IWebView.() -> R): R? {
        val wb = JcefWebViewProxy()
        return wb.use {
            block.invoke(it)
        }
    }
}