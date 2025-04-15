package org.easybangumi.next.shared.plugin.core.source

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.coroutineProvider
import org.easybangumi.next.shared.plugin.EasyPluginConfigProvider
import org.easybangumi.next.shared.plugin.extension.ExtensionController
import org.easybangumi.next.shared.plugin.info.SourceInfo

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
 *
 *  SourceManifest 源清单           SourceConfig 源配置
 *             ↘                    ↙
 *                  SourceInfo 源的所有信息
 *                          ↓
 *           SourceBundle 所有源的 ComponentBundle
 *
 */
class SourceController(
    private val sourceConfigController: SourceConfigController,
    private val extensionController: ExtensionController,
    private val configProvider: EasyPluginConfigProvider,
) {

    // ============== flow 定义 ==============

    // 源
    private val _sourceInfoFlow = MutableStateFlow<DataState<List<SourceInfo>>>(DataState.loading())
    val sourceInfoFlow = _sourceInfoFlow.asStateFlow()

    // 源中的 Component 包
    private val _sourceBundleFlow = MutableStateFlow<DataState<SourceBundle>>(DataState.loading())
    val sourceBundleFlow = _sourceBundleFlow.asStateFlow()

    // 内置 Source
    private val innerSourceProvider = configProvider.innerSourceProvider


    // ============== 协程 定义 ==============

    private val dispatcher = coroutineProvider.io()
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

}