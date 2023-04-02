package com.heyanle.easybangumi4.ui.source_order

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.bangumi_source_api.api.Source
import com.heyanle.easybangumi4.source.SourceLibraryMaster
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/4/1 21:08.
 * https://github.com/heyanLE
 */
class SourceManagerViewModel : ViewModel() {


    val sourceConfig = mutableStateOf(listOf<Pair<Source, SourceLibraryMaster.SourceConfig>>())

    val orderSourceConfig = mutableStateOf(listOf<Pair<Source, SourceLibraryMaster.SourceConfig>>())

    init {
        viewModelScope.launch {
            SourceLibraryMaster.sourceLibraryFlow.collectLatest {
                sourceConfig.value = it.toList()
                orderSourceConfig.value = emptyList()
            }
        }
    }

    fun saveOrder() {
        viewModelScope.launch {
            val map = hashMapOf<String, SourceLibraryMaster.SourceConfig>()
            orderSourceConfig.value.forEach {
                map[it.first.key] = it.second
            }
            SourceLibraryMaster.newOkkvConfig(map)
            orderSourceConfig.value = emptyList()
        }

    }

    fun enable(sourceKey: String) {
        SourceLibraryMaster.enable(sourceKey)
    }

    fun disable(sourceKey: String) {
        SourceLibraryMaster.disable(sourceKey)
    }

    fun order() {
        orderSourceConfig.value = sourceConfig.value.toList()
    }


}