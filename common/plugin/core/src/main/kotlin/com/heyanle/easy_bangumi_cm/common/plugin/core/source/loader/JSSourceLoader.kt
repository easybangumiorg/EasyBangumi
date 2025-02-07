package com.heyanle.easy_bangumi_cm.common.plugin.core.source.loader

import com.heyanle.easy_bangumi_cm.common.plugin.core.entity.SourceConfig
import com.heyanle.easy_bangumi_cm.common.plugin.core.entity.SourceInfo
import com.heyanle.easy_bangumi_cm.plugin.api.source.SourceManifest

/**
 * Created by heyanlin on 2024/12/20.
 */
class JSSourceLoader: SourceLoader {
    override fun loadType(): Int {
        return SourceManifest.LOAD_TYPE_JS
    }

    override suspend fun load(sourceManifest: SourceManifest, sourceConfig: SourceConfig): SourceInfo {
        TODO()
    }

    override fun removeCache(key: String) {
        TODO("Not yet implemented")
    }
}