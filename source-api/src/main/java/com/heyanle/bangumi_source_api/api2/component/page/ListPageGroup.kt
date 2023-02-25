package com.heyanle.bangumi_source_api.api2.component.page

import com.heyanle.bangumi_source_api.api2.Source
import com.heyanle.bangumi_source_api.api2.component.ComponentBuilderScope
import com.heyanle.bangumi_source_api.api2.entity.CartoonCover

/**
 * Created by HeYanLe on 2023/2/20 16:05.
 * https://github.com/heyanLE
 */
class ListPageGroup(
    override val label: String,
    override val source: Source,
    override val newScreen: Boolean = false,
    val listPage: suspend () -> Source.SourceResult<List<ListPage>>
) : CartoonPage





