package com.heyanle.easybangumi4.plugin.source

import com.heyanle.easybangumi4.case.ExtensionCase
import com.heyanle.easybangumi4.plugin.extension.ExtensionInfo
import com.heyanle.easybangumi4.plugin.js.source.JSComponentBundle
import com.heyanle.easybangumi4.plugin.js.source.JsSource
import com.heyanle.easybangumi4.plugin.source.bundle.ComponentBundle
import com.heyanle.easybangumi4.plugin.source.bundle.SimpleComponentBundle
import com.heyanle.easybangumi4.plugin.source.bundle.SourceBundle
import com.heyanle.easybangumi4.source_api.Source
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

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
 *  1. 新增 Disable 态，关闭的番源直接不加载，而不是加载完再判断是否关闭
 *  2. 新增 ComponentBundle 缓存，防止重复加载
 */
class SourceControllerV2(
    private val extensionCase: ExtensionCase,
    private val sourcePreferences: SourcePreferences,
): ISourceController {

    private val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    val scope = CoroutineScope(SupervisorJob() + dispatcher)


    private val _sourceInfo = MutableStateFlow<ISourceController.SourceInfoState>(ISourceController.SourceInfoState.Loading)
    override val sourceInfo: StateFlow<ISourceController.SourceInfoState>
        get() = _sourceInfo

    private val _configSource = MutableStateFlow<List<ConfigSource>>(emptyList())
    override val configSource: StateFlow<List<ConfigSource>>
        get() = _configSource

    private val _sourceBundle = MutableStateFlow<SourceBundle?>(null)
    override val sourceBundle: StateFlow<SourceBundle?>
        get() = _sourceBundle



    init {

        scope.launch {
            combine(
                extensionCase.flowExtensionState(),
                sourcePreferences.configs.requestFlow.distinctUntilChanged(),
            ) { extensionState, configs ->

                if (extensionState.loading) {
                    _sourceInfo.update {
                        ISourceController.SourceInfoState.Loading
                    }
                } else {
                    val infoList = extensionState.extensionInfoMap.values.filterIsInstance<ExtensionInfo.Installed>()
                        .flatMap {
                            it.sources
                        }.map {
                            val config = configs[it.key]
                            innerLoad(it, config)
                        }.toMutableList()
                    infoList.add(
                        InnerSourceMaster.localConfigSource
                    )
                    infoList.sortBy { it.config.order }

                    // 历史遗留，其实可以不用更新的
                    _sourceInfo.update {
                        ISourceController.SourceInfoState.Info(
                            infoList.map { it.sourceInfo }
                        )
                    }
                    _configSource.update {
                        infoList
                    }
                }

            }.collect()
        }
        scope.launch {
            _configSource.collectLatest {
                val bundle = SourceBundle(it)
                _sourceBundle.update {
                    bundle
                }
            }
        }


    }

    private val componentBundleTemp = hashMapOf<Source, ComponentBundle>()

    private suspend fun innerLoad(
        source: Source,
        config: SourceConfig?,
    ): ConfigSource {
        if (config?.enable == false) {
            return ConfigSource(
                SourceInfo.Disabled(source),
                config
            )
        } else {
            val temp = componentBundleTemp[source]
            if (temp != null) {
                return ConfigSource(
                    SourceInfo.Loaded(source, temp),
                    config ?: SourceConfig(source.key, Int.MAX_VALUE, true)
                )
            }
            val info = try {
                val bundle =
                    if (source is JsSource) JSComponentBundle(source) else SimpleComponentBundle(source)
                bundle.init()
                SourceInfo.Loaded(source, bundle)
            } catch (e: SourceException) {
                SourceInfo.Error(source,  e.msg, e)
            } catch (e: Exception) {
                e.printStackTrace()
                SourceInfo.Error(source, "加载错误：${e.message}", e)
            }
            if (info is SourceInfo.Loaded) {
                componentBundleTemp[source] = info.componentBundle
            }
            return ConfigSource(
                info,
                config ?: SourceConfig(source.key, Int.MAX_VALUE, true)
            )
        }
    }

}