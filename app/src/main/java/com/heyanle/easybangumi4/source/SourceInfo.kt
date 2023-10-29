package com.heyanle.easybangumi4.source

import com.heyanle.easybangumi4.source.bundle.ComponentBundle
import com.heyanle.easybangumi4.source_api.Source
import org.koin.core.scope.Scope

/**
 * Created by heyanlin on 2023/10/27.
 */

sealed class SourceInfo {
    abstract val source: Source

    class Migrating(
        override val source: Source,
        val componentBundle: ComponentBundle,
    ): SourceInfo()
    class Loaded(
        override val source: Source,
        val componentBundle: ComponentBundle,
    ): SourceInfo()

    class Error(
        override val source: Source,
        val msg: String,
        val exception: Exception? = null,
    ): SourceInfo()
}

class ConfigSource(
    val source: SourceInfo,
    val config: SourceConfig,
)

data class SourceConfig(
    val key: String,
    val order: Int,
    val enable: Boolean,
)