package org.easybangumi.next.shared.compose.web

import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable
import org.easybangumi.next.lib.utils.WeakRef
import org.easybangumi.next.lib.utils.safeMutableMapOf
import org.easybangumi.next.lib.webview.IWebView
import org.easybangumi.next.shared.source.api.component.NeedWebViewCheckException
import org.easybangumi.next.shared.source.api.component.WebViewCheckParam

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
val needWebViewCheckParamMap = safeMutableMapOf<String, WeakRef<WebViewCheckParam>>()
@Serializable
data class WebPageParam(
    val needWebViewCheckKey: String? = null,
)

@Composable
expect fun WebPage(param: WebPageParam)