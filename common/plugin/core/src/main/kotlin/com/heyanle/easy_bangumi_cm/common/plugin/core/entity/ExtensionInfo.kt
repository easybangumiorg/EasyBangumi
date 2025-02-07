package com.heyanle.easy_bangumi_cm.common.plugin.core.entity

import com.heyanle.easy_bangumi_cm.plugin.api.extension.ExtensionManifest
import com.heyanle.easy_bangumi_cm.plugin.api.source.SourceManifest

/**
 * Created by heyanlin on 2024/12/13.
 */
sealed class ExtensionInfo {

    data class Loaded(
        val manifest: ExtensionManifest,
        val sources: List<SourceManifest>,
    ) : ExtensionInfo()

    data class LoadedError(
        val manifest: ExtensionManifest,
        val errMsg: String,
        val exception: Throwable? = null,
    ) : ExtensionInfo()

}