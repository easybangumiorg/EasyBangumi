package com.heyanle.easy_bangumi_cm.plugin.core.extension.provider

import com.heyanle.easy_bangumi_cm.plugin.api.ExtensionInfo
import kotlinx.coroutines.flow.StateFlow


/**
 * Created by HeYanLe on 2024/12/8 22:49.
 * https://github.com/heyanLE
 */

interface ExtensionProvider {

    data class ExtensionProviderState (
        val loading: Boolean,
        val extensionInfoMap: Map<String, ExtensionInfo> = emptyMap(),
    )

    val flow: StateFlow<ExtensionProviderState>

    fun init()

    fun release()


}