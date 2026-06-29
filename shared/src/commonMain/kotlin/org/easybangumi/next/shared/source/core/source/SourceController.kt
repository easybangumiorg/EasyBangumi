package org.easybangumi.next.shared.source.core.source

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.shared.source.api.SourceProvider
import org.easybangumi.next.shared.source.api.source.SourceInfo
import org.easybangumi.next.shared.source.api.source.SourceType
import org.easybangumi.next.shared.source.core.inner.InnerSourceProvider
import org.easybangumi.next.shared.source.plugin.PluginSourceController
import kotlin.collections.filterIsInstance

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

class SourceController(
    private val innerSourceController: InnerSourceProvider,
    private val pluginSourceController: PluginSourceController,
){

    data class State(
        val sourceInfoList: List<SourceInfo>,
        val isLoading: Boolean = true,
    )

    val flow: Flow<State> = combine(
        innerSourceController.flow,
        pluginSourceController.flow
    ) {
        val res = mutableListOf<SourceInfo>()
        var isLoading = false
        it.forEach {
            val d = it.okOrNull()
            if (d != null) {
                res.addAll(d.filterIsInstance<SourceInfo>())
            }
            if (it.isLoading()) {
                isLoading = true
            }
        }
        State(
            sourceInfoList = res,
            isLoading = isLoading
        )
    }
}