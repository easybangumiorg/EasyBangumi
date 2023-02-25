package com.heyanle.bangumi_source_api.api2.component.search

import com.heyanle.bangumi_source_api.api2.Source
import com.heyanle.bangumi_source_api.api2.component.Component
import com.heyanle.bangumi_source_api.api2.entity.CartoonCover

/**
 * Created by HeYanLe on 2023/2/25 14:35.
 * https://github.com/heyanLE
 */
class SearchComponent(
    override val source: Source,
    val firstKey: () -> Int,
    val search: suspend (
        keyword: String,
        pageKey: Int
    ) -> Source.SourceResult<Pair<Int?, List<CartoonCover>>>
) : Component