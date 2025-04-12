package org.easybangumi.next.shared.plugin.extension.provider

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.shared.plugin.extension.ExtensionManifest

/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 */

abstract class AbsExtensionProvider: ExtensionProvider {

    protected val innerFlow =
        MutableStateFlow<DataState<List<ExtensionManifest>>>(DataState.none())

    override val flow: StateFlow<DataState<List<ExtensionManifest>>>
        get() = innerFlow

    fun fireLoading(){
        innerFlow.update {
            DataState.loading()
        }
    }

    fun fireData(data: List<ExtensionManifest>) {
        innerFlow.update {
            DataState.ok(data)
        }
    }

    fun fireError(
        errorMsg: String,
        throwable: Throwable?,
        isEmpty: Boolean = false,
    ) {
        innerFlow.update {
            if (isEmpty) {
                DataState.empty(errorMsg,)
            } else {
                DataState.error(errorMsg, throwable)
            }

        }
    }

}