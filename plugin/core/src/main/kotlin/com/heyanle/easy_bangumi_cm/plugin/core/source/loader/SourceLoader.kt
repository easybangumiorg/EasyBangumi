package com.heyanle.easy_bangumi_cm.plugin.core.source.loader


import com.heyanle.easy_bangumi_cm.plugin.core.entity.SourceInfo
import com.heyanle.easy_bangumi_cm.plugin.entity.SourceManifest

/**
 * Created by heyanlin on 2024/12/11.
 */
interface SourceLoader {

    fun canLoad(sourceManifest: SourceManifest): Boolean

    suspend fun load(sourceManifest: SourceManifest): List<SourceInfo>

}