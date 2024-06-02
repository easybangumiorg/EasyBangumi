package com.heyanle.easybangumi4.case

import com.heyanle.easybangumi4.extension.ExtensionInfo
import com.heyanle.easybangumi4.extension.ExtensionController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Created by heyanlin on 2023/10/25.
 */
class ExtensionCase(
    private val extensionController: ExtensionController
) {

    fun flowExtensionState(): StateFlow<ExtensionController.ExtensionLoaderState> {
        return extensionController.state
    }

    fun flowExtension(): Flow<Collection<ExtensionInfo>> {
        return extensionController.state
            .filter {
                !it.isLoading
            }
            .map {
                it.appExtensions.values + it.fileExtensionInfo.values
            }
    }

    suspend fun awaitExtension(): Collection<ExtensionInfo> {
        return flowExtension().first()
    }
}