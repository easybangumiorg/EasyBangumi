package com.heyanle.easy_bangumi_cm.plugin.api.component.media

import com.heyanle.easy_bangumi_cm.plugin.api.base.SourceResult
import com.heyanle.easy_bangumi_cm.plugin.api.component.ComponentContainer
import com.heyanle.easy_bangumi_cm.plugin.api.component.MediaComponent
import com.heyanle.easy_bangumi_cm.repository.cartoon.CartoonCover


/**
 * Created by HeYanLe on 2024/12/8 21:38.
 * https://github.com/heyanLE
 */

interface SearchComponent : MediaComponent {

    suspend fun firstKey(keyword: String): String

    suspend fun search(keyword: String, searchKey: String): SourceResult<Pair<String?, List<CartoonCover>>>
}

fun ComponentContainer.searchComponent(): SearchComponent? {
    return this.getComponent(SearchComponent::class)
}