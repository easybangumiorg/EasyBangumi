package com.heyanle.easy_bangumi_cm.common.plugin.core.source.loader

import com.heyanle.easy_bangumi_cm.common.plugin.core.entity.SourceConfig
import com.heyanle.easy_bangumi_cm.common.plugin.core.entity.SourceInfo
import com.heyanle.easy_bangumi_cm.common.plugin.core.inner.InnerComponentBundle
import com.heyanle.easy_bangumi_cm.common.plugin.core.inner.InnerSource

/**
 * Created by heyanlin on 2025/1/29.
 */
class InnerSourceLoader {

    private val cache = mutableMapOf<String, SourceInfo>()

    fun load(innerSource: InnerSource, sourceConfig: SourceConfig): SourceInfo {
        // 缓存
        val ca = cache[innerSource.key]
        if (ca != null && innerSource.manifest.lastModified == ca.manifest.lastModified){
            return ca
        }

        // 关闭
        if (!sourceConfig.enable) {
            return SourceInfo.Unable(innerSource.manifest, sourceConfig)
        }

        // 加载
        val bundle = InnerComponentBundle(innerSource)
        try {
            bundle.load()
            val info = SourceInfo.Loaded(innerSource.manifest, sourceConfig, bundle)
            cache[innerSource.key] = info
            return info
        }catch (e: Exception){
            return SourceInfo.Error(innerSource.manifest, sourceConfig, e.message ?: "Unknown error", e)
        }

    }

    fun removeCache(key: String){
        cache.remove(key)
    }

}