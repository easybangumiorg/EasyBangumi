package com.heyanle.easy_bangumi_cm.common.plugin.core.source.loader


import com.heyanle.easy_bangumi_cm.common.plugin.core.entity.SourceConfig
import com.heyanle.easy_bangumi_cm.common.plugin.core.entity.SourceInfo
import com.heyanle.easy_bangumi_cm.plugin.api.source.SourceManifest

/**
 * Created by heyanlin on 2024/12/11.
 */
interface SourceLoader {

    fun loadType(): Int

    fun canLoad(sourceManifest: SourceManifest): Boolean {
        return sourceManifest.loadType == loadType()
    }

    suspend fun load(sourceManifest: SourceManifest, sourceConfig: SourceConfig): SourceInfo

    fun removeCache(key: String)
}