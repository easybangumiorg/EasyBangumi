package com.heyanle.easybangumi4.ui.main.source_manage.source

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.bangumi_source_api.api.Source
import com.heyanle.easybangumi4.source.SourceLibraryMaster
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/4/1 21:34.
 * https://github.com/heyanLE
 */
class SourceViewModel : ViewModel() {


    val sourceMapState =
        mutableStateOf<Map<String, Source>>(mapOf())

    val sourceLibraryState =
        mutableStateOf<List<String>>(emptyList())

    val configState =
        mutableStateOf<Map<String, SourceLibraryMaster.SourceConfig>>(mapOf())


    init {
        viewModelScope.launch {
            SourceLibraryMaster.sourceLibraryFlow.collectLatest {

                if (it.size != sourceLibraryState.value.size) {
                    sourceLibraryState.value = it.sortedBy {
                        it.second.order
                    }.map { it.first.key }

                    val map = hashMapOf<String, SourceLibraryMaster.SourceConfig>()
                    val sourceMap = hashMapOf<String, Source>()
                    it.forEach {
                        map[it.second.key] = it.second
                        sourceMap[it.second.key] = it.first
                    }
                    configState.value = map
                    sourceMapState.value = sourceMap
                    return@collectLatest
                }
                var isUpdate = false
                val oldSet = sourceLibraryState.value.toSet()
                it.forEach {
                    if (!oldSet.contains(it.first.key)) {
                        isUpdate = true
                    }
                }
                if (isUpdate) {
                    sourceLibraryState.value = it.sortedBy {
                        it.second.order
                    }.map { it.first.key }

                    val map = hashMapOf<String, SourceLibraryMaster.SourceConfig>()
                    val sourceMap = hashMapOf<String, Source>()
                    it.forEach {
                        map[it.second.key] = it.second
                        sourceMap[it.second.key] = it.first
                    }
                    configState.value = map
                    sourceMapState.value = sourceMap
                }


            }
        }
    }

    fun move(from: Int, to: Int) {
        sourceLibraryState.value = sourceLibraryState.value.toMutableList().apply {
            add(to, removeAt(from))

        }
    }

    fun onDragEnd() {
        val config = configState.value.toMutableMap()
        sourceLibraryState.value.forEachIndexed { i, it ->
            if (config.containsKey(it)) {
                config[it] = config[it]!!.copy(order = i)
            }
        }
        SourceLibraryMaster.newOkkvConfig(config)

    }

    fun enable(sourceKey: String) {
        SourceLibraryMaster.enable(sourceKey)
        val map = configState.value.toMutableMap()
        map[sourceKey]?.let {
            map[sourceKey] = it.copy(enable = true)
        }
        configState.value = map
    }

    fun disable(sourceKey: String) {
        SourceLibraryMaster.disable(sourceKey)
        val map = configState.value.toMutableMap()
        map[sourceKey]?.let {
            map[sourceKey] = it.copy(enable = false)
        }
        configState.value = map
    }

}