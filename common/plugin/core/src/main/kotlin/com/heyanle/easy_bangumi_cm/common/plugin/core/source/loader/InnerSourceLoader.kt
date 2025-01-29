package com.heyanle.easy_bangumi_cm.common.plugin.core.source.loader

import com.heyanle.easy_bangumi_cm.common.plugin.core.entity.SourceConfig
import com.heyanle.easy_bangumi_cm.common.plugin.core.entity.SourceInfo
import com.heyanle.easy_bangumi_cm.common.plugin.core.inner.InnerSource

/**
 * Created by heyanlin on 2025/1/29.
 */
class InnerSourceLoader {

    private val cache = mutableMapOf<String, SourceInfo>()

    fun load(innerSource: InnerSource, sourceConfig: SourceConfig): SourceInfo {
        val ca = cache[innerSource.key]
        if (ca != null && innerSource.manifest.lastModified == ca.manifest.lastModified){
            return ca
        }
        TODO()
    }

    fun removeCache(key: String){
        cache.remove(key)
    }

}