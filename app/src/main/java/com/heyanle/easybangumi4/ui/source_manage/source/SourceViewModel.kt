package com.heyanle.easybangumi4.ui.source_manage.source

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.easybangumi4.source.ConfigSource
import com.heyanle.easybangumi4.source.SourceConfig
import com.heyanle.easybangumi4.source.SourceController
import com.heyanle.easybangumi4.source.SourcePreferences
import com.heyanle.easybangumi4.utils.loge
import com.heyanle.easybangumi4.utils.toJson
import com.heyanle.injekt.core.Injekt
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/4/1 21:34.
 * https://github.com/heyanLE
 */
class SourceViewModel : ViewModel() {

    var configSourceList by mutableStateOf<List<ConfigSource>>(emptyList())
        private set

    private val sourceController: SourceController by Injekt.injectLazy()
    private val sourcePreferences: SourcePreferences by Injekt.injectLazy()

    init {
        viewModelScope.launch {
            sourceController.configSource.collectLatest { libs ->
                libs.loge("SourceViewModel")
                configSourceList = libs.sortedBy { it.config.order }
            }

        }
    }

    fun move(from: Int, to: Int) {

        configSourceList = configSourceList.toMutableList().apply {
            add(to, removeAt(from))

        }
    }

    fun onDragEnd() {
        val map = hashMapOf<String, SourceConfig>()
        configSourceList.forEachIndexed { index, configSource ->
            map[configSource.sourceInfo.source.key] = configSource.config.copy(order = index)
        }
        sourcePreferences.configs.set(map)
    }


    fun enable(sourceConfig: ConfigSource) {
        val map = sourcePreferences.configs.get().toMutableMap()
        val config = map[sourceConfig.sourceInfo.source.key]?.copy(enable = true) ?: SourceConfig(
            sourceConfig.sourceInfo.source.key,
            Int.MAX_VALUE,
            true
        )
        map[sourceConfig.sourceInfo.source.key] = config
        sourcePreferences.configs.set(map)
    }

    fun disable(sourceConfig: ConfigSource) {
        val map = sourcePreferences.configs.get().toMutableMap()
        val config = map[sourceConfig.sourceInfo.source.key]?.copy(enable = false) ?: SourceConfig(
            sourceConfig.sourceInfo.source.key,
            Int.MAX_VALUE,
            false
        )
        map[sourceConfig.sourceInfo.source.key] = config
        sourcePreferences.configs.set(map)
    }

}