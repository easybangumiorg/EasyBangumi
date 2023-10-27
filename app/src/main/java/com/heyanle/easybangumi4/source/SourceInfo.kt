package com.heyanle.easybangumi4.source

import com.heyanle.easybangumi4.source_api.Source
import org.koin.core.scope.Scope

/**
 * Created by heyanlin on 2023/10/27.
 */
class SourceInfo(
    val source: Source,
    val scope: Scope,
    val config: SourceConfig,
)

data class SourceConfig(
    val key: String,
    val order: Int,
    val enable: Boolean,
)