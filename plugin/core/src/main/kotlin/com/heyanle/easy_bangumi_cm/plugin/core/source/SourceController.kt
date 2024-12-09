package com.heyanle.easy_bangumi_cm.plugin.core.source

import com.heyanle.easy_bangumi_cm.plugin.core.extension.ExtensionController
import com.heyanle.easy_bangumi_cm.plugin.core.source.entity.ConfigSourceInfo
import com.heyanle.easy_bangumi_cm.plugin.core.source.entity.SourceConfig
import com.heyanle.easy_bangumi_cm.plugin.core.source.entity.SourceInfo
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * SourceInfo   ↘
 * SourceConfig -> ConfigSourceInfo -> SourceBundle
 * Created by heyanlin on 2024/12/9.
 */
class SourceController(
    private val extensionController: ExtensionController
) {


    data class SourceInfoState(
        val loading: Boolean = true,
        val sourceInfo: List<SourceInfo> = emptyList()
    )
    private val _sourceInfoFlow = MutableStateFlow(SourceInfoState())

    data class SourceConfigState(
        val loading: Boolean,
        val sourceConfig: Map<String, SourceConfig>
    )
    private val _sourceConfigFlow = MutableStateFlow(SourceConfigState(true, emptyMap()))


    data class ConfigSourceState(
        val sourceInfoLoading: Boolean = true,
        val sourceConfigLoading: Boolean = true,
        val configSourceInfo: List<ConfigSourceInfo> = emptyList()
    ) {
        val loading = sourceInfoLoading || sourceConfigLoading
    }
    private val _configSourceFlow = MutableStateFlow(ConfigSourceState())


    data class SourceBundleState(
        val sourceBundle: SourceBundle? = null
    ) {
        val loading = sourceBundle == null
    }
    private val _sourceBundleFlow = MutableStateFlow(SourceBundleState())




}