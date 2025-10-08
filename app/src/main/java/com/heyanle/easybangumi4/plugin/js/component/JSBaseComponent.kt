package com.heyanle.easybangumi4.plugin.js.component

import com.heyanle.easybangumi4.plugin.source.utils.network.web.WebProxyManager
import com.heyanle.easybangumi4.plugin.source.utils.network.web.WebProxyProvider
import com.heyanle.easybangumi4.source_api.component.Component

interface JSBaseComponent: Component {
    suspend fun init() {}

    // 等后续重构把
    fun setWebProxyManager(
        webProxyManager: WebProxyManager
    )
}