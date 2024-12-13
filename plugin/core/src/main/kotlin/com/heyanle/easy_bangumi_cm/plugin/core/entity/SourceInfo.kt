package com.heyanle.easy_bangumi_cm.plugin.core.entity

import com.heyanle.easy_bangumi_cm.plugin.api.component.ComponentBundle
import com.heyanle.easy_bangumi_cm.plugin.api.source.Source
import com.heyanle.easy_bangumi_cm.plugin.entity.ExtensionManifest
import com.heyanle.easy_bangumi_cm.plugin.entity.SourceManifest

/**
 * Created by heyanlin on 2024/12/13.
 */
sealed class SourceInfo {

    abstract val manifest: SourceManifest
    abstract val extensionManifest: ExtensionManifest

    // 加载成功
    class Loaded(
        override val manifest: SourceManifest,
        override val extensionManifest: ExtensionManifest,
        val componentBundle: ComponentBundle,
    ) : SourceInfo()

    // 加载失败
    class Error(
        override val manifest: SourceManifest,
        override val extensionManifest: ExtensionManifest,
        val msg: String,
        val exception: Exception? = null,
    ) : SourceInfo()
}