package com.heyanle.easybangumi4.source

import com.heyanle.easybangumi4.extension.Extension
import com.heyanle.easybangumi4.source.bundle.ComponentBundle
import com.heyanle.easybangumi4.source_api.Source

/**
 * Created by heyanlin on 2023/10/27.
 */

sealed class SourceInfo {
    abstract val source: Source
    abstract val extension: Extension.Installed

//    // 数据迁移中
//    class Migrating(
//        override val source: Source,
//        val componentBundle: ComponentBundle,
//    ): SourceInfo()

    // 加载成功
    class Loaded(
        override val source: Source,
        override val extension: Extension.Installed,
        val componentBundle: ComponentBundle,
    ): SourceInfo()

    // 加载失败
    class Error(
        override val source: Source,
        override val extension: Extension.Installed,
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