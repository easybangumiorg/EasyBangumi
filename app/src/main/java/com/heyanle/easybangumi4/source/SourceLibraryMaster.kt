package com.heyanle.easybangumi4.source

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.heyanle.bangumi_source_api.api.Source
import com.heyanle.extension_load.ExtensionController
import com.heyanle.extension_load.model.Extension
import com.heyanle.okkv2.core.okkv
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Flow<List<Extensions.Installed>>      --↘
 * Flow<List<SourceConfig>>              --→ Flow<List<Pair<Source, SourceConfig>>> -> SourceMaster : Flow<SourceBundle>
 *
 * Created by HeYanLe on 2023/2/22 21:43.
 * https://github.com/heyanLE
 */
@OptIn(FlowPreview::class)
object SourceLibraryMaster {

    val scope = MainScope()

    data class SourceConfig(
        val key: String,
        val enable: Boolean,
        val order: Int,
    )

    private var configOkkv by okkv("source_config", "[]")
    val configFlow = MutableStateFlow<Map<String, SourceConfig>>(getOkkvConfig())

    val sourceLibraryFlow = MutableStateFlow<List<Pair<Source, SourceConfig>>>(emptyList())


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
                sources to transformConfig(sources, configMap)
            }.collectLatest { p ->
                configOkkv = Gson().toJson(p.second.values.toList())
                val l = p.first.flatMap {
                    val config =
                        p.second[it.key] ?: return@flatMap emptyList<Pair<Source, SourceConfig>>()
                    listOf(it to config)
                }.sortedBy { it.second.order }

                sourceLibraryFlow.emit(l)
            }
        }

    }

    fun newOkkvConfig(config: Map<String, SourceConfig>) {
        configFlow.update {
            it.toMutableMap().apply {
                putAll(config)
            }
        }
    }

    fun enable(sourceKey: String) {
        val map = configFlow.value.toMutableMap()
        val old = map[sourceKey]
        if (old != null) {
            map[sourceKey] = map[sourceKey]!!.copy(enable = true)
        }
        newOkkvConfig(map)
    }

    fun disable(sourceKey: String) {
        val map = configFlow.value.toMutableMap()
        val old = map[sourceKey]
        if (old != null) {
            map[sourceKey] = map[sourceKey]!!.copy(enable = false)
        }
        newOkkvConfig(map)
    }

    private fun transformConfig(
        list: List<Source>,
        oldConfig: Map<String, SourceConfig>,
    ): Map<String, SourceConfig> {
        val cacheList = hashMapOf<String, Source>()
        list.forEach {
            if ((cacheList[it.key]?.versionCode ?: -1) < it.versionCode) {
                cacheList[it.key] = it
            }
        }
        val configs = oldConfig.toMutableMap()
        cacheList.iterator().forEach {
            if (!configs.containsKey(it.key)) {
                // 未排序状态
                configs[it.key] = SourceConfig(it.key, true, Int.MAX_VALUE)
            }
        }
        return configs
    }


    private fun getOkkvConfig(): Map<String, SourceConfig> {
        val configList = Gson().fromJson<List<SourceConfig>>(
            configOkkv,
            object : TypeToken<List<SourceConfig>>() {}.type
        )
        val map = hashMapOf<String, SourceConfig>()
        configList.forEach {
            map[it.key] = it
        }
        return map
    }


}