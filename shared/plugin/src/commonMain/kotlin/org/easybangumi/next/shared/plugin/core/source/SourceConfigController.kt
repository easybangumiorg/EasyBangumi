package org.easybangumi.next.shared.plugin.core.source

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.store.file_helper.json.JsonlFileHelper
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.coroutineProvider
import org.easybangumi.next.lib.utils.pathProvider
import org.easybangumi.next.shared.plugin.info.SourceConfig

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
class SourceConfigController {

    private val _sourceConfigFlow: MutableStateFlow<DataState<Map<String, SourceConfig>>> = MutableStateFlow(DataState.none())
    val sourceConfigFlow = _sourceConfigFlow.asSharedFlow()

    private val singleDispatcher = coroutineProvider.single()
    private val scope = CoroutineScope(SupervisorJob() + coroutineProvider.io())

    val workerFile = pathProvider.getFilePath("source")

    private val configFileHelper = JsonlFileHelper<SourceConfig>(
        workerFile,
        "source_config",
        SourceConfig::class,
        scope
    )

    init {
        scope.launch {
            configFileHelper.flow().stateIn(scope).map {
                DataState.ok(it.associateBy { it.key })
            }.collect { n ->
                _sourceConfigFlow.update { n }
            }
        }
    }

    fun setSourceConfig(sourceConfig: SourceConfig){
        scope.launch {
            configFileHelper.update {
                var needAdd = true
                val new = it.map {
                    if(it.key == sourceConfig.key){
                        needAdd = false
                        sourceConfig
                    }else{
                        it
                    }
                }
                if (needAdd) {
                    new + sourceConfig
                } else {
                    new
                }
            }
        }
    }

    fun setSourceConfigList(sourceConfigList: List<SourceConfig>){
        scope.launch {
            configFileHelper.update {
                sourceConfigList
            }
        }
    }


}