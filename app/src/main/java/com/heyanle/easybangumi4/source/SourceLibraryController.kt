package com.heyanle.easybangumi4.source

import com.heyanle.bangumi_source_api.api.Source
import com.heyanle.extension_load.ExtensionController
import com.heyanle.extension_load.model.Extension
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Flow<List<Extensions.Installed>> --↘
 * Flow<Map<String, SourceConfig>>  --→ Flow<List<Pair<Source, SourceConfig>>> -> Flow<SourceBundle>
 *
 * Created by HeYanLe on 2023/2/22 21:43.
 * https://github.com/heyanLE
 */
@OptIn(FlowPreview::class)
class SourceLibraryController(
    private val sourcePreferences: SourcePreferences
) {

    val scope = MainScope()

    private val configFlow = sourcePreferences.configs.stateIn(scope)

    val sourceLibraryFlow = MutableStateFlow<List<Pair<Source, SourcePreferences.SourceConfig>>>(emptyList())
    val sourceBundleFlow: Flow<SourceBundle>
        get() = sourceLibraryFlow.map { pairs ->
            SourceBundle(pairs.filter { it.second.enable }
                .sortedBy { it.second.order }.map { it.first })
        }


    init {
        scope.launch {
            combine(
                configFlow,
                ExtensionController.installedExtensionsFlow.filterIsInstance<ExtensionController.ExtensionState.Extensions>()
                    .map { it.extensions })
            { configMap, extensions ->


                val sources = extensions.filterIsInstance<Extension.Installed>().flatMap {
                    it.sources
                }
                sources to realConfig(sources, configMap)
            }.collectLatest { p ->
                val l = p.first.flatMap {
                    val config =
                        p.second[it.key] ?: return@flatMap emptyList<Pair<Source, SourcePreferences.SourceConfig>>()
                    listOf(it to config)
                }.sortedBy { it.second.order }
                sourceLibraryFlow.emit(l)
            }
        }

    }

    private fun realConfig(
        list: List<Source>,
        current: Map<String, SourcePreferences.SourceConfig>,
    ): Map<String, SourcePreferences.SourceConfig>{
        val cacheList = hashMapOf<String, Source>()
        list.forEach {
            if ((cacheList[it.key]?.versionCode ?: -1) < it.versionCode) {
                cacheList[it.key] = it
            }
        }
        val configs = current.toMutableMap()
        cacheList.iterator().forEach {
            if (!configs.containsKey(it.key)) {
                // 未排序状态
                configs[it.key] = SourcePreferences.SourceConfig(it.key, true, Int.MAX_VALUE)
            }
        }
        return configs
    }


    fun enable(sourceKey: String) {
        val map = configFlow.value.toMutableMap()
        val old = map[sourceKey]
        if (old != null) {
            map[sourceKey] = map[sourceKey]!!.copy(enable = true)
        }
        sourcePreferences.configs.set(map)
    }

    fun disable(sourceKey: String) {
        val map = configFlow.value.toMutableMap()
        val old = map[sourceKey]
        if (old != null) {
            map[sourceKey] = map[sourceKey]!!.copy(enable = false)
        }
        sourcePreferences.configs.set(map)
    }


}