package com.heyanle.easy_bangumi_cm.plugin.core.source.loader

import com.heyanle.easy_bangumi_cm.plugin.core.entity.SourceInfo
import com.heyanle.easy_bangumi_cm.plugin.entity.SourceManifest

/**
 * Created by heyanlin on 2024/12/20.
 */
class JSSourceLoader: SourceLoader {
    override fun loadType(): Int {
        return SourceManifest.LOAD_TYPE_JS
    }

    override suspend fun load(sourceManifest: SourceManifest): List<SourceInfo> {

    }
}