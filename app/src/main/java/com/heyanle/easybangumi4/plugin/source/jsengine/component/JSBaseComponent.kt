package com.heyanle.easybangumi4.plugin.source.jsengine.component

import com.heyanle.easybangumi4.plugin.source.utils.network.web.WebProxyManager
import com.heyanle.easybangumi4.plugin.source.utils.network.web.WebProxyProvider
import com.heyanle.easybangumi4.plugin.api.component.Component

interface JSBaseComponent: Component {
    suspend fun init() {}

    // 等后续重构把
    fun setWebProxyManager(
        webProxyManager: WebProxyManager
    )
}