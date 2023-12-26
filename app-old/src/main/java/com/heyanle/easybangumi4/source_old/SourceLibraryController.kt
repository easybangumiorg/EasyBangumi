package com.heyanle.easybangumi4.source_old

import com.heyanle.bangumi_source_api.api.Source
import com.heyanle.easybangumi4.preferences.SourcePreferences
import com.heyanle.extension_load.ExtensionController
import com.heyanle.extension_load.model.Extension
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
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
    private val sourcePreferences: SourcePreferences,
    private val migrationController: SourceMigrationController,
) {

    val scope = MainScope()

    private val configFlow = sourcePreferences.configs.stateIn(scope)

    val isLoading = MutableStateFlow(true)

    private val _sourceLibraryFlow =
        MutableStateFlow<List<Pair<Source, SourcePreferences.LocalSourceConfig>>>(emptyList())
    val sourceLibraryFlow = _sourceLibraryFlow.asStateFlow()

    private val _sourceBundleFlow: MutableStateFlow<SourceBundle> =
        MutableStateFlow(SourceBundle(emptyList()))
    val sourceBundleFlow = _sourceBundleFlow.asStateFlow()


    init {
        scope.launch {
            combine(
                migrationController.migratingSource.stateIn(scope),
                sourcePreferences.configs.flow().stateIn(scope),
                ExtensionController.installedExtensionsFlow
                    .stateIn(scope)
            )
            { migratingSet, configMap, extensionState ->
                if(migratingSet.isNotEmpty()){
                    return@combine
                }
                when (extensionState) {
                    is ExtensionController.ExtensionState.Extensions -> {
                        val sources =
                            extensionState.extensions.filterIsInstance<Extension.Installed>()
                                .flatMap {
                                    it.sources
                                }
                        migrationController.migration(sources)
                        val rc = realConfig(sources, configMap)
                        val l = sources.flatMap {
                            val config =
                                rc[it.key]
                                    ?: return@flatMap emptyList<Pair<Source, SourcePreferences.LocalSourceConfig>>()
                            listOf(it to config)
                        }.sortedBy { it.second.order }
                        _sourceLibraryFlow.update {
                            l
                        }
                        isLoading.update {
                            false
                        }
                    }

                    is ExtensionController.ExtensionState.Loading -> {
                        isLoading.update {
                            true
                        }
                    }

                    else -> {
                    }
                }
            }.collect()
        }

        // 迁移中的源暂时关闭
        scope.launch {
            combine(
                sourceLibraryFlow,
                migrationController.migratingSource
            ) { sl, mi ->
                SourceBundle(sl.filter { it.second.enable && !mi.contains(it.first) }
                    .sortedBy { it.second.order }.map { it.first })

            }.collectLatest { bundle ->
                _sourceBundleFlow.update {
                    bundle
                }
            }
        }

    }

    private fun realConfig(
        list: List<Source>,
        current: Map<String, SourcePreferences.LocalSourceConfig>,
    ): Map<String, SourcePreferences.LocalSourceConfig> {
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
                configs[it.key] = SourcePreferences.LocalSourceConfig(it.key, true, Int.MAX_VALUE)
            }
        }
        return configs
    }

    fun newConfig(map: Map<String, SourcePreferences.LocalSourceConfig>) {
        sourcePreferences.configs.set(map)
    }

    fun newConfig(config: SourcePreferences.LocalSourceConfig){
        val map = sourcePreferences.configs.get().toMutableMap()
        map[config.key] = config
        sourcePreferences.configs.set(map)
    }

    fun enable(sourceKey: String) {
        val map = configFlow.value.toMutableMap()
        val old = map[sourceKey]
        if (old != null) {
            map[sourceKey] = map[sourceKey]!!.copy(enable = true)
        }else{
            map[sourceKey] = SourcePreferences.LocalSourceConfig(key = sourceKey, enable = true, order = Int.MAX_VALUE)
        }
        sourcePreferences.configs.set(map)
    }

    fun disable(sourceKey: String) {
        val map = configFlow.value.toMutableMap()
        val old = map[sourceKey]
        if (old != null) {
            map[sourceKey] = map[sourceKey]!!.copy(enable = false)
        }else{
            map[sourceKey] = SourcePreferences.LocalSourceConfig(key = sourceKey, enable = false, order = Int.MAX_VALUE)
        }
        sourcePreferences.configs.set(map)
    }


}