package com.heyanle.easy_bangumi_cm.common.plugin.core.source

import com.heyanle.easy_bangumi_cm.base.utils.CoroutineProvider
import com.heyanle.easy_bangumi_cm.base.utils.DataState
import com.heyanle.easy_bangumi_cm.base.utils.map
import com.heyanle.easy_bangumi_cm.common.plugin.core.EasyPluginConfigProvider
import com.heyanle.easy_bangumi_cm.common.plugin.core.entity.ExtensionInfo
import com.heyanle.easy_bangumi_cm.common.plugin.core.entity.SourceConfig
import com.heyanle.easy_bangumi_cm.common.plugin.core.entity.SourceInfo
import com.heyanle.easy_bangumi_cm.common.plugin.core.extension.ExtensionController
import com.heyanle.easy_bangumi_cm.common.plugin.core.inner.InnerSource
import com.heyanle.easy_bangumi_cm.common.plugin.core.source.loader.InnerSourceLoader
import com.heyanle.easy_bangumi_cm.common.plugin.core.source.loader.JSCryLoader
import com.heyanle.easy_bangumi_cm.common.plugin.core.source.loader.JSSourceLoader
import com.heyanle.easy_bangumi_cm.plugin.api.source.SourceManifest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 *  SourceManifest 源清单           SourceConfig 源配置
 *             ↘                    ↙
 *                  SourceInfo 源的所有信息
 *                          ↓
 *           SourceBundle 所有源的 ComponentBundle
 *
 * Created by heyanlin on 2024/12/9.
 */
class SourceController(
    private val extensionController: ExtensionController,
    private val sourceConfigController: SourceConfigController,
    private val configProvider: EasyPluginConfigProvider,
) {

    // ============== flow 定义 ==============

    // 源
    private val _sourceInfoFlow = MutableStateFlow<DataState<List<SourceInfo>>>(DataState.loading())
    val sourceInfoFlow = _sourceInfoFlow.asStateFlow()

    // 源中的 Component 包
    private val _sourceBundleFlow = MutableStateFlow<DataState<SourceBundle>>(DataState.none())
    val sourceBundleFlow = _sourceBundleFlow.asStateFlow()

    // 本地 Source
    private val innerSourceProvider = configProvider.innerSourceProvider
    // ============== 协程 定义 ==============

    private val dispatcher = CoroutineProvider.io
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    // ============== Loader ==============

    private val innerLoader = InnerSourceLoader()

    private val jsLoader = JSSourceLoader()
    private val jsCryLoader = JSCryLoader(jsLoader)
    private val loaderMap = mapOf(
        SourceManifest.LOAD_TYPE_JS to jsLoader,
        SourceManifest.LOAD_TYPE_CRY_JS to jsCryLoader
    )


    init {
        // (sourceManifest, sourceConfig) -> SourceInfo
        scope.launch {
            combine(
                extensionController.infoState,
                sourceConfigController.sourceConfigFlow,
                innerSourceProvider.flowInnerSource().distinctUntilChanged()
            ) { extensionInfo, sourceConfig, innerSource ->

                if (extensionInfo.loading) {
                    return@combine true to emptyList<SourceInfo>()
                }

                if (sourceConfig.loading) {
                    return@combine true to emptyList<SourceInfo>()
                }

                val res = extensionInfo.extensionInfoInfoMap.values.filterIsInstance<ExtensionInfo.Loaded>().flatMap {
                    (it.sources).map { sourceManifest ->
                        val config = sourceConfig.sourceConfig[sourceManifest.key] ?: SourceConfig(sourceManifest.key, System.currentTimeMillis(), true)
                        async {
                            innerLoad(sourceManifest, config)
                        }
                    }
                }.map {
                    it.await()
                }

                val innerRes = innerSource.map {
                    val config = sourceConfig.sourceConfig[it.manifest.key] ?: SourceConfig(it.manifest.key, System.currentTimeMillis(), true)
                    innerLoadInner(it, config)
                }

                false to (res + innerRes)
            }.collectLatest { res ->
                if (res.first) {
                    _sourceInfoFlow.update {
                        DataState.loading()
                    }
                } else {
                    _sourceInfoFlow.update {
                        DataState.ok(res.second)
                    }
                }
            }
        }

        // SourceInfo -> SourceBundle
        scope.launch {
            _sourceInfoFlow.collectLatest { sourceInfoState ->
                _sourceBundleFlow.update {
                    sourceInfoState.map {
                        SourceBundle(it.filterIsInstance<SourceInfo.Loaded>())
                    }
                }
            }
        }
    }

    private suspend fun innerLoad(
        sourceManifest: SourceManifest,
        sourceConfig: SourceConfig
    ): SourceInfo {

        val loader = loaderMap[sourceManifest.loadType]
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