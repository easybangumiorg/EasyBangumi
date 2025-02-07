package com.heyanle.easy_bangumi_cm.common.plugin.inner.debug.component

import com.heyanle.easy_bangumi_cm.model.cartoon.CartoonCover
import com.heyanle.easy_bangumi_cm.plugin.api.base.SourceResult
import com.heyanle.easy_bangumi_cm.plugin.api.component.ComponentWrapper
import com.heyanle.easy_bangumi_cm.plugin.api.component.media.SearchComponent

/**
 * Created by heyanlin on 2025/2/7.
 */
class DebugSearchComponent: SearchComponent, ComponentWrapper() {
    override suspend fun firstKey(keyword: String): String {
        return "1"
    }

    override suspend fun search(keyword: String, searchKey: String): SourceResult<Pair<String?, List<CartoonCover>>> {
        TODO("Not yet implemented")
    }
}