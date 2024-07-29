package com.heyanle.easybangumi4.plugin.extension.provider

import com.heyanle.easybangumi4.plugin.extension.ExtensionInfo
import kotlinx.coroutines.flow.StateFlow

/**
 * Created by heyanle on 2024/7/29.
 * https://github.com/heyanLE
 */
interface ExtensionProvider {

    data class ExtensionProviderState (
        val loading: Boolean,
        val extensionMap: Map<String, ExtensionInfo> = emptyMap(),
    )

    val flow: StateFlow<ExtensionProviderState>

    fun init()

    fun release()

}