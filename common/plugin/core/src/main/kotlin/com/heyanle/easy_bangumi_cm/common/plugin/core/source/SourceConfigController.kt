package com.heyanle.easy_bangumi_cm.common.plugin.core.source


import com.heyanle.easy_bangumi_cm.base.service.provider.IPathProvider
import com.heyanle.easy_bangumi_cm.base.utils.CoroutineProvider
import com.heyanle.easy_bangumi_cm.base.utils.file_helper.JsonlFileHelper
import com.heyanle.easy_bangumi_cm.common.plugin.core.entity.SourceConfig
import com.heyanle.lib.unifile.UniFile
import com.heyanle.lib.unifile.UniFileFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

/**
 * Created by heyanlin on 2024/12/9.
 */
class SourceConfigController(
    private val pathProvider: IPathProvider,
) {

    data class SourceConfigState(
        val loading: Boolean,
        val sourceConfig: Map<String, SourceConfig>
    )
    private val _sourceConfigFlow = MutableStateFlow(SourceConfigState(true, emptyMap()))
    val sourceConfigFlow = _sourceConfigFlow.asSharedFlow()

    private val dispatcher = CoroutineProvider.io
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    private val configFileHelper = JsonlFileHelper<SourceConfig>(
        UniFileFactory.fromFile(File(pathProvider.getFilePath("source"))),
        "source_config",
        scope,
        SourceConfig::class.java
    )

    init {
        scope.launch {
            configFileHelper.flow().stateIn(scope).map {
                SourceConfigState(false, it.associateBy { it.key })
            }.collect { n ->
                _sourceConfigFlow.update { n }
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