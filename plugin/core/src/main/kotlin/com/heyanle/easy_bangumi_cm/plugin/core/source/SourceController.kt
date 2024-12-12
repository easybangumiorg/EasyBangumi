package com.heyanle.easy_bangumi_cm.plugin.core.source

import com.heyanle.easy_bangumi_cm.base.utils.CoroutineProvider
import com.heyanle.easy_bangumi_cm.base.utils.file_helper.JsonlFileHelper
import com.heyanle.easy_bangumi_cm.plugin.core.extension.ExtensionController
import com.heyanle.easy_bangumi_cm.plugin.core.source.entity.ConfigSourceInfo
import com.heyanle.easy_bangumi_cm.plugin.core.source.entity.SourceConfig
import com.heyanle.easy_bangumi_cm.plugin.core.source.entity.SourceInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 *  ExtensionInfo 拓展数据
 *             ↓
 *  SourceInfo 源数据          SourceConfig 源配置
 *           ↘                        ↙
 *           ConfigSourceInfo 带配置的源列表
 *                          ↓  SourceLoader
 *           SourceBundle 所有源的 ComponentBundle
 *
 * Created by heyanlin on 2024/12/9.
 */
class SourceController(
    private val extensionController: ExtensionController,
    private val sourceConfigController: SourceConfigController,
) {

    // ============== flow 定义 ==============

    // 源
    data class SourceInfoState(
        val loading: Boolean = true,
        val sourceInfo: List<SourceInfo> = emptyList()
    )
    private val _sourceInfoFlow = MutableStateFlow(SourceInfoState())

    // 带配置的源
    data class ConfigSourceState(
        val loading: Boolean = true,
        val configSourceInfo: List<ConfigSourceInfo> = emptyList()
    ) {
    }
    private val _configSourceFlow = MutableStateFlow(ConfigSourceState())


    // 源中的 Component 包
    data class SourceBundleState(
        val sourceBundle: SourceBundle? = null
    ) {
        val loading = sourceBundle == null
    }
    private val _sourceBundleFlow = MutableStateFlow(SourceBundleState())

    // ============== 协程 定义 ==============

    private val dispatcher = CoroutineProvider.io
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    // ============== 源配置 ==============

    init {



    }



}