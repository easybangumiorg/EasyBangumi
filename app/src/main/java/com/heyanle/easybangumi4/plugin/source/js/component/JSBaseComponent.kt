package com.heyanle.easybangumi4.plugin.source.js.component

import com.heyanle.easybangumi4.plugin.source.utils.network.web.WebProxyManager
import com.heyanle.easybangumi4.plugin.source.utils.network.web.WebProxyProvider
import com.heyanle.easybangumi4.plugin.api.component.Component

interface JSBaseComponent: Component {
    suspend fun init() {}

    // 绛夊悗缁噸鏋勬妸
    fun setWebProxyManager(
        webProxyManager: WebProxyManager
    )
}