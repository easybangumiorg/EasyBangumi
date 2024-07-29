package com.heyanle.easybangumi4.plugin.js.component

import com.heyanle.easybangumi4.source_api.component.Component

interface JSBaseComponent: Component {
    suspend fun init() {}
}