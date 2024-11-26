package com.heyanle.easybangumi4.plugin.source

import com.heyanle.easybangumi4.plugin.source.bundle.ComponentBundle
import com.heyanle.easybangumi4.plugin.source.bundle.SimpleComponentBundle
import com.heyanle.easybangumi4.source_api.Source

/**
 * Created by heyanlin on 2023/10/27.
 */

sealed class SourceInfo {
    abstract val source: Source

//    // 数据迁移中
//    class Migrating(
//        override val source: Source,
//        val componentBundle: ComponentBundle,
//    ): SourceInfo()

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

class ConfigSource(
    val sourceInfo: SourceInfo,
    val config: SourceConfig,
){
    val source: Source
        get() = sourceInfo.source
}

data class SourceConfig(
    val key: String,
    val order: Int,
    val enable: Boolean,
)