package com.heyanle.easybangumi4.ui.common.page

import androidx.compose.runtime.Composable
import com.heyanle.bangumi_source_api.api2.component.page.CartoonPage
import com.heyanle.bangumi_source_api.api2.component.page.ListPage
import com.heyanle.bangumi_source_api.api2.component.page.ListPageGroup
import com.heyanle.easybangumi4.ui.common.page.list.SourceListPage
import com.heyanle.easybangumi4.ui.common.page.listgroup.SourceListPageGroup

/**
 * Created by HeYanLe on 2023/2/25 21:54.
 * https://github.com/heyanLE
 */

@Composable
fun CartoonPageUI(
    cartoonPage: CartoonPage
){
    when(cartoonPage){
        is ListPage -> {
            SourceListPage(listPage = cartoonPage)
        }
        is ListPageGroup -> {
            SourceListPageGroup(listPageGroup = cartoonPage)
        }
    }
}