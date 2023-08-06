package com.heyanle.easybangumi4.compose.source_manage.source

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.bangumi_source_api.api.Source
import com.heyanle.easybangumi4.preferences.SourcePreferences
import com.heyanle.easybangumi4.source.SourceLibraryController
import com.heyanle.easybangumi4.utils.loge
import com.heyanle.injekt.core.Injekt
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/4/1 21:34.
 * https://github.com/heyanLE
 */
class SourceViewModel : ViewModel() {

    var sourceConfigs by mutableStateOf<List<Pair<Source, SourcePreferences.SourceConfig>>>(emptyList())
        private set

    private val sourceController: SourceLibraryController by Injekt.injectLazy()

    init {
        viewModelScope.launch {
            sourceController.sourceLibraryFlow.collectLatest { libs ->
                libs.loge("SourceViewModel")
                sourceConfigs = libs
            }

        }
    }

    fun move(from: Int, to: Int) {

        sourceConfigs = sourceConfigs.toMutableList().apply {
            add(to, removeAt(from))

        }
    }

    fun onDragEnd() {
        val map = hashMapOf<String, SourcePreferences.SourceConfig>()
        sourceConfigs.forEachIndexed { index, pair ->
            map[pair.first.key] = pair.second.copy(order = index)
        }

        sourceController.newConfig(map)

    }


    fun enable(sourceConfig: SourcePreferences.SourceConfig) {
        sourceController.newConfig(sourceConfig.copy(enable = true))
    }

    fun disable(sourceConfig: SourcePreferences.SourceConfig) {
        sourceController.newConfig(sourceConfig.copy(enable = false))
    }

}