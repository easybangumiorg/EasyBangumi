package com.heyanle.bangumi_source_api.api2.page

import com.heyanle.bangumi_source_api.api2.Source

/**
 * Created by HeYanLe on 2023/2/20 16:08.
 * https://github.com/heyanLE
 */
interface ListPageGroupTab: CartoonPage {

    override val newScreen: Boolean
        get() = true

    suspend fun listPage(): Source.SourceResult<List<ListPage>>

}