package com.heyanle.easy_bangumi_cm.plugin.core.entity

import com.heyanle.easy_bangumi_cm.plugin.entity.ExtensionManifest
import com.heyanle.easy_bangumi_cm.plugin.entity.SourceManifest

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
        val exception: Throwable?,
        val errMsg: String,
    ) : ExtensionInfo()

}