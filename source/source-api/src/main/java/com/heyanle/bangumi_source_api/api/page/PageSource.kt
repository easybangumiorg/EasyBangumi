package com.heyanle.bangumi_source_api.api.page

import com.heyanle.bangumi_source_api.api.Source

/**
 * Created by HeYanLe on 2023/2/27 22:09.
 * https://github.com/heyanLE
 */
interface PageSource: Source {

    fun getPages(): List<SourcePage>

}