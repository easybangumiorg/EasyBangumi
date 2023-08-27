package com.heyanle.easybangumi4.source

import com.heyanle.bangumi_source_api.api.Source
import com.heyanle.easybangumi4.preferences.SourcePreferences
import com.heyanle.easybangumi4.utils.loge
import com.heyanle.extension_load.ExtensionController
import com.heyanle.extension_load.model.Extension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

/**
 * Created by HeYanLe on 2023/8/27 15:35.
 * https://github.com/heyanLE
 */
class SourceController(
    private val sourcePreferences: SourcePreferences,
    private val migrationController: SourceMigrationController,
) {

    companion object {
        private const val TAG = "SourceController"
    }


    private val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    private val configFlow = sourcePreferences.configs.stateIn(scope)

    private val _sourceLibraryFlow =
        MutableStateFlow<List<Pair<Source, SourcePreferences.LocalSourceConfig>>>(emptyList())
    val sourceLibraryFlow = _sourceLibraryFlow.asStateFlow()

    sealed class SourceState {
        data object None : SourceState()

        data object Loading : SourceState()

        data class Migrating(
            val source: List<Source>,
        ) : SourceState()

        data class Completely(
            val sourceBundle: SourceBundle,
        ) : SourceState()
    }

    private val _sourceState = MutableStateFlow<SourceState>(SourceState.None)
    val sourceState = _sourceState.asStateFlow()

    init {
        scope.launch {
            combine(
                migrationController.migratingSource.stateIn(scope),
                sourcePreferences.configs.stateIn(scope = scope),
                ExtensionController.installedExtensionsFlow
            ) { migrating, configs, extensionState ->
                if (migrating.isNotEmpty()) {
                    return@combine SourceState.Migrating(migrating.toList())
                }
                when (
                    extensionState
                ) {
                    is ExtensionController.ExtensionState.Extensions -> {
                        val sources =
                            extensionState.extensions.filterIsInstance<Extension.Installed>()
                                .flatMap {
                                    it.sources
                                }
                        migrationController.migration(sources)
                        val migrationSources = sources.filter {
                            migrationController.needMigrate(it)
                        }

                        val rc = realConfig(sources, configs)
                        val l = sources.flatMap {
                            val config =
                                rc[it.key]
                                    ?: return@flatMap emptyList<Pair<Source, SourcePreferences.LocalSourceConfig>>()
                            listOf(it to config)
                        }.sortedBy { it.second.order }
                        _sourceLibraryFlow.update {
                            l
                        }


                        if (migrationSources.isNotEmpty()) {
                            migrationController.migration(migrationSources)
                            SourceState.Migrating(migrationSources)
                        } else {
                            SourceState.Completely(SourceBundle(sources.filter {
                                rc[it.key]?.enable ?: true
                            }.sortedBy {
                                rc[it.key]?.order ?: Int.MAX_VALUE
                            }))
                        }

                    }

                    is ExtensionController.ExtensionState.Loading -> {
                        SourceState.Loading
                    }

                    else -> {
                        SourceState.None
                    }
                }
            }.collectLatest { state ->
                state.loge(TAG)
                _sourceState.update {
                    state
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

    fun bundleIfEmpty(): SourceBundle {
        return ((_sourceState.value as? SourceState.Completely)?.sourceBundle) ?: SourceBundle(
            emptyList()
        )
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