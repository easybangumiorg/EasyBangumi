package com.heyanle.bangumi_source_api.api2.page

import com.heyanle.bangumi_source_api.api2.Source

/**
 * Created by HeYanLe on 2023/2/20 16:05.
 * https://github.com/heyanLE
 */
interface ListPageGroup: CartoonPage {
    suspend fun listPage(): Source.SourceResult<List<ListPage>>

}