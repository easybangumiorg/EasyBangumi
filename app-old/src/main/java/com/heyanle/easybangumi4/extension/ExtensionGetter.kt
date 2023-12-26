package com.heyanle.easybangumi4.extension

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Created by heyanlin on 2023/10/25.
 */
class ExtensionGetter(
    private val extensionController: ExtensionController
) {

    fun flowExtension(): Flow<Collection<Extension>> {
        return extensionController.state
            .filter {
                !it.isLoading
            }
            .map {
                it.appExtensions.values + it.fileExtension.values
            }
    }

    suspend fun awaitExtension(): Collection<Extension> {
        return flowExtension().first()
    }
}