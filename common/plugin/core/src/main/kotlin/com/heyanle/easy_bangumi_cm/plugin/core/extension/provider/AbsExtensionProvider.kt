package com.heyanle.easy_bangumi_cm.plugin.core.extension.provider

import com.heyanle.easy_bangumi_cm.plugin.entity.ExtensionManifest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * Created by heyanlin on 2024/12/17.
 */
abstract class AbsExtensionProvider: ExtensionProvider {

    protected val innerFlow =
        MutableStateFlow<ExtensionProvider.ExtensionProviderState>(ExtensionProvider.ExtensionProviderState())

    override val flow: StateFlow<ExtensionProvider.ExtensionProviderState>
        get() = innerFlow

    fun fireLoading(loading: Boolean = true){
        innerFlow.update {
            it.copy(loading = loading)
        }
    }

}