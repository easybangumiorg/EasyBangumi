package com.heyanle.easybangumi4.source_api.component.page

import com.heyanle.easybangumi4.source_api.component.Component


/**
 * Created by HeYanLe on 2023/10/18 23:25.
 * https://github.com/heyanLE
 */
interface PageComponent: Component {

    class NonLabelSinglePage(
        cartoonPage: SourcePage
    ) : List<SourcePage> by listOf(cartoonPage)

    fun getPages(): List<SourcePage>

}