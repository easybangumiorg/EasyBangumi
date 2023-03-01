package com.heyanle.easybangumi4.ui.common.page

import androidx.compose.runtime.Composable
import com.heyanle.bangumi_source_api.api.component.page.SourcePage
import com.heyanle.easybangumi4.ui.common.page.list.SourceListPage
import com.heyanle.easybangumi4.ui.common.page.listgroup.SourceListPageGroup

/**
 * Created by HeYanLe on 2023/2/25 21:54.
 * https://github.com/heyanLE
 */

@Composable
fun CartoonPageUI(
    cartoonPage: SourcePage
){
    when(cartoonPage){
        is SourcePage.SingleCartoonPage -> {
            SourceListPage(listPage = cartoonPage)
        }
        is SourcePage.Group -> {
            SourceListPageGroup(listPageGroup = cartoonPage)
        }
        else -> {}
    }
}