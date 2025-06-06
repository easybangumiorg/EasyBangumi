package org.easybangumi.next.shared.plugin.core.source

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.coroutineProvider
import org.easybangumi.next.lib.utils.mapWithState
import org.easybangumi.next.shared.plugin.api.source.SourceManifest
import org.easybangumi.next.shared.plugin.core.extension.ExtensionController
import org.easybangumi.next.shared.plugin.core.info.ExtensionInfo
import org.easybangumi.next.shared.plugin.core.info.SourceConfig
import org.easybangumi.next.shared.plugin.core.info.SourceInfo
import org.easybangumi.next.shared.plugin.core.inner.InnerSource
import org.easybangumi.next.shared.plugin.core.source.loader.InnerSourceLoader
import org.easybangumi.next.shared.plugin.core.source.loader.SourceLoader
import org.easybangumi.next.shared.plugin.core.source.loader.getSourceLoaderFactory

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
) {

    private val logger = logger()

    // ============== flow 定义 ==============

    // 源
    private val _sourceInfoFlow = MutableStateFlow<DataState<List<SourceInfo>>>(DataState.loading())
    val sourceInfoFlow = _sourceInfoFlow.asStateFlow()

    // 源中的 Component 包
    private val _sourceBundleFlow = MutableStateFlow<DataState<SourceBundle>>(DataState.loading())
    val sourceBundleFlow = _sourceBundleFlow.asStateFlow()


    // ============== 协程 定义 ==============

    private val dispatcher = coroutineProvider.io()
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    // ============== Loader ==============

    private val innerLoader = InnerSourceLoader()

    init {
        // (sourceManifest, sourceConfig) -> SourceInfo
        scope.launch {
            combine(
                extensionController.infoState,
                sourceConfigController.sourceConfigFlow,
            ) { extensionInfo, sourceConfig ->
                logger.info("extension: $extensionInfo source config: $sourceConfig")
                if (extensionInfo.isNone() || extensionInfo.isLoading()) {
                    return@combine DataState.loading<List<SourceInfo>>()
                }

                if (sourceConfig.isLoading() || sourceConfig.isNone()) {
                    return@combine DataState.loading<List<SourceInfo>>()
                }

                if (extensionInfo.isError() && extensionInfo is DataState.Error) {
                    return@combine DataState.error<List<SourceInfo>>(
                        errorMsg = extensionInfo.errorMsg,
                        extensionInfo.throwable
                    )
                }

                if (sourceConfig.isError() && sourceConfig is DataState.Error) {
                    logger.error("source config error: ${sourceConfig.errorMsg}", sourceConfig.throwable)
                }

                // config 错误打印日志，还是可以使用
                val realConfig = sourceConfig.okOrNull() ?: emptyMap()

                val extensionList = extensionInfo.okOrNull()!!.values
                val res = extensionList.filterIsInstance<ExtensionInfo.Loaded>().flatMap {
                    (it.sources).map { sourceManifest ->
                        val config = realConfig[sourceManifest.key] ?: SourceConfig(sourceManifest.key, Clock.System.now().toEpochMilliseconds(), true)
                        async {
                            innerLoad(sourceManifest, config)
                        }
                    }
                }.map {
                    it.await()
                }

                val innerRes = InnerSource.InnerSourceLists.map {
                    val config = realConfig[it.manifest.key] ?: SourceConfig(it.manifest.key, Clock.System.now().toEpochMilliseconds(), true)
                    innerLoadInner(it, config)
                }
                DataState.ok(res + innerRes)
            }.collectLatest { res ->
                _sourceInfoFlow.update {
                    res
                }
            }
        }

        // SourceInfo -> SourceBundle
        scope.launch {
            _sourceInfoFlow.collectLatest { sourceInfoState ->
                _sourceBundleFlow.update {
                    sourceInfoState.mapWithState {
                        val loaded = it.filterIsInstance<SourceInfo.Loaded>()
                        if (loaded.isEmpty()) {
                            return@mapWithState DataState.empty("source is empty")
                        }
                        DataState.ok(
                            SourceBundle(loaded)
                        )
                    }
                }
            }
        }
    }

    private fun getSourceLoader(type: Int): SourceLoader? {
        if (type == SourceManifest.LOAD_TYPE_INNER) {
            return null
        }
        return getSourceLoaderFactory().getLoader(type)
    }


    private suspend fun innerLoad(
        sourceManifest: SourceManifest,
        sourceConfig: SourceConfig
    ): SourceInfo {


        val loader = getSourceLoader(sourceManifest.loadType)
            ?: return SourceInfo.Error(sourceManifest, sourceConfig, "loader not found")

        // 已关闭的源清除一下缓存
        if (!sourceConfig.enable) {
            loader.removeCache(sourceManifest.key)
            return SourceInfo.Unable(sourceManifest, sourceConfig)
        }

        return loader.load(sourceManifest, sourceConfig)
    }

    private suspend fun innerLoadInner(
        innerSource: InnerSource,
        sourceConfig: SourceConfig,
    ): SourceInfo{
        if (!sourceConfig.enable) {
            // 已关闭的源清除一下缓存
            innerLoader.removeCache(innerSource.key)
            return SourceInfo.Unable(innerSource.manifest, sourceConfig)
        }
        return innerLoader.load(innerSource, sourceConfig)
    }

    fun refresh(){
        extensionController.refreshExtension()
    }

}