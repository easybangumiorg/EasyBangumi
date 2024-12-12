package com.heyanle.easy_bangumi_cm.plugin.core.source.entity

import com.heyanle.easy_bangumi_cm.plugin.api.component.ComponentBundle
import com.heyanle.easy_bangumi_cm.plugin.api.source.Source

/**
 * Created by heyanlin on 2024/12/9.
 */
sealed class SourceInfo {
    abstract val source: Source

    // 加载成功
    class Loaded(
        override val source: Source,
        val componentBundle: ComponentBundle,
    ): SourceInfo()

    // 加载失败
    class Error(
        override val source: Source,
        val msg: String,
        val exception: Exception? = null,
    ): SourceInfo()
}

class ConfigSourceInfo(
    val config: SourceConfig,
    val sourceInfo: SourceInfo
) {
    val source: Source
        get() = sourceInfo.source
}


data class SourceConfig(
    val key: String,
    val order: Int,
    val enable: Boolean,
)