package com.heyanle.easy_bangumi_cm.plugin.core.source


import com.heyanle.easy_bangumi_cm.base.data.DataState
import com.heyanle.easy_bangumi_cm.base.utils.CoroutineProvider
import com.heyanle.easy_bangumi_cm.base.utils.file_helper.JsonlFileHelper
import com.heyanle.easy_bangumi_cm.plugin.core.entity.SourceConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by heyanlin on 2024/12/9.
 */
class SourceConfigController(
    private val configFileHelper: JsonlFileHelper<SourceConfig>
) {


    data class SourceConfigState(
        val loading: Boolean,
        val sourceConfig: Map<String, SourceConfig>
    )
    private val _sourceConfigFlow = MutableStateFlow(SourceConfigState(true, emptyMap()))
    val sourceConfigFlow = _sourceConfigFlow.asSharedFlow()

    private val dispatcher = CoroutineProvider.io
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    init {
        scope.launch {
            configFileHelper.flow.map {
                if (it is DataState.Ok) {
                    SourceConfigState(false, it.data.associateBy { it.key })
                } else {
                    SourceConfigState(true, emptyMap())
                }
            }.collect { n ->
                _sourceConfigFlow.update {
                    n
                }
            }
        }
    }

    fun setSourceConfig(sourceConfig: SourceConfig){
        scope.launch {
            configFileHelper.update {
                var needAdd = true
                val new = it.map {
                    if(it.key == sourceConfig.key){
                        needAdd = false
                        sourceConfig
                    }else{
                        it
                    }
                }
                if (needAdd) {
                    it + sourceConfig
                } else {
                    it
                }
            }
        }
    }

    fun setSourceConfigList(sourceConfigList: List<SourceConfig>){
        scope.launch {
            configFileHelper.update {
                sourceConfigList
            }
        }
    }



}