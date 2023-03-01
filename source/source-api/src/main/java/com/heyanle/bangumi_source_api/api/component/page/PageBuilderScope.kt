package com.heyanle.bangumi_source_api.api.component.page

import com.heyanle.bangumi_source_api.api.Source

/**
 * Created by HeYanLe on 2023/2/27 22:06.
 * https://github.com/heyanLE
 */
class PageBuilderScope{

    val pages = arrayListOf<SourcePage>()

}

fun Source.sourcePage(
    block: PageBuilderScope.()->Unit,
): List<SourcePage> {
    return PageBuilderScope().apply(block).pages
}
