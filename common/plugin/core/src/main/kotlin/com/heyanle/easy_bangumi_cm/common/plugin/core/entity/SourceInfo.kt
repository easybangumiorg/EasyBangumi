package com.heyanle.easy_bangumi_cm.common.plugin.core.entity

import com.heyanle.easy_bangumi_cm.plugin.api.component.ComponentBundle
import com.heyanle.easy_bangumi_cm.plugin.api.source.SourceManifest

/**
 * Created by heyanlin on 2024/12/13.
 */
sealed class SourceInfo {

    abstract val manifest: SourceManifest
    abstract val sourceConfig: SourceConfig

    // 加载成功
    class Loaded(
        override val manifest: SourceManifest,
        override val sourceConfig: SourceConfig,
        val componentBundle: ComponentBundle,
    ) : SourceInfo()

    // 关闭
    class Unable(
        override val manifest: SourceManifest,
        override val sourceConfig: SourceConfig,
    ) : SourceInfo()

    // 加载失败
    class Error(
        override val manifest: SourceManifest,
        override val sourceConfig: SourceConfig,
        val msg: String,
        val exception: Exception? = null,
    ) : SourceInfo()


}