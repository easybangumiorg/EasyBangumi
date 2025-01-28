package com.heyanle.easy_bangumi_cm.plugin.core.source.loader


import com.heyanle.easy_bangumi_cm.plugin.core.entity.SourceInfo
import com.heyanle.easy_bangumi_cm.plugin.entity.SourceManifest

/**
 * Created by heyanlin on 2024/12/11.
 */
interface SourceLoader {

    fun loadType(): Int

    fun canLoad(sourceManifest: SourceManifest): Boolean {
        return sourceManifest.loadType == loadType()
    }

    suspend fun load(sourceManifest: SourceManifest): List<SourceInfo>

}