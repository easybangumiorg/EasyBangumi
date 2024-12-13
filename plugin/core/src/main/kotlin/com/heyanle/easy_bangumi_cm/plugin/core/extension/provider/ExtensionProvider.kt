package com.heyanle.easy_bangumi_cm.plugin.core.extension.provider

import com.heyanle.easy_bangumi_cm.plugin.entity.ExtensionManifest
import kotlinx.coroutines.flow.StateFlow


/**
 * Created by HeYanLe on 2024/12/8 22:49.
 * https://github.com/heyanLE
 */

interface ExtensionProvider {

    data class ExtensionProviderState (
        val loading: Boolean = true,
        val extensionManifestMap: Map<String, ExtensionManifest> = emptyMap(),
    )

    val flow: StateFlow<ExtensionProviderState>

    fun load()

    fun release()


}