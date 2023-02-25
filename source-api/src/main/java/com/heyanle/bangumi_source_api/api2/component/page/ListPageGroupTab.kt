package com.heyanle.bangumi_source_api.api2.component.page

import com.heyanle.bangumi_source_api.api2.Source
import com.heyanle.bangumi_source_api.api2.component.ComponentBuilderScope

/**
 * Created by HeYanLe on 2023/2/20 16:08.
 * https://github.com/heyanLE
 */
class ListPageGroupTab(
    override val label: String,
    override val source: Source,
    override val newScreen: Boolean = true,
    val listPage: suspend () -> Source.SourceResult<List<ListPage>>
) : CartoonPage
