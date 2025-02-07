package com.heyanle.easy_bangumi_cm.common.plugin.core.entity

import com.heyanle.easy_bangumi_cm.plugin.api.source.SourceManifest

/**
 * Created by heyanlin on 2024/12/13.
 */
class ConfigSourceInfo(
    val config: SourceConfig,
    val sourceInfo: SourceInfo
) {
    val sourceManifest: SourceManifest
        get() = sourceInfo.manifest
}


data class SourceConfig(
    val key: String,
    val order: Long,
    val enable: Boolean,
){

}