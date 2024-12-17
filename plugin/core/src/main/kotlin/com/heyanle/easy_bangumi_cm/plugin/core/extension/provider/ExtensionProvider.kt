package com.heyanle.easy_bangumi_cm.plugin.core.extension.provider

import com.heyanle.easy_bangumi_cm.base.data.DataState
import com.heyanle.easy_bangumi_cm.plugin.entity.ExtensionManifest
import kotlinx.coroutines.flow.StateFlow
import java.io.File


/**
 * Created by HeYanLe on 2024/12/8 22:49.
 * https://github.com/heyanLE
 */

interface ExtensionProvider {

    data class ExtensionProviderState (
        val loading: Boolean = true,
        val extensionManifestList: List<ExtensionManifest> = emptyList(),
    )

    val flow: StateFlow<ExtensionProviderState>

    fun refresh()

    fun uninstall(extensionManifest: ExtensionManifest)

    fun install(file: File, callback: ((DataState<ExtensionManifest>) -> Unit)? = null)

    fun release()


}