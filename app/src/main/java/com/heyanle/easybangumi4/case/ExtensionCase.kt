package com.heyanle.easybangumi4.case


import com.heyanle.easybangumi4.plugin.extension.ExtensionInfo
import com.heyanle.easybangumi4.plugin.extension.IExtensionController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Created by heyanlin on 2023/10/25.
 */
class ExtensionCase(
    private val extensionController: IExtensionController
) {

    fun flowExtensionState(): StateFlow<IExtensionController.ExtensionState> {
        return extensionController.state
    }

    fun flowExtension(): Flow<Collection<ExtensionInfo>> {
        return extensionController.state
            .filter {
                !it.loading
            }
            .map {
                it.extensionInfoMap.values
            }
    }

    suspend fun awaitExtension(): Collection<ExtensionInfo> {
        return flowExtension().first()
    }

}