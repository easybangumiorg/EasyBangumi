package com.heyanle.easybangumi4.ui.common.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
) {
    when (cartoonPage) {
        is SourcePage.SingleCartoonPage -> {
            SourceListPage(listPage = cartoonPage)
        }

        is SourcePage.Group -> {
            SourceListPageGroup(listPageGroup = cartoonPage)
        }

        else -> {}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartoonPageListTab(
    cartoonPage: List<SourcePage>,
    selectionIndex: Int,
    onPageClick: (Int) -> Unit,
) {

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(8.dp, 0.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        itemsIndexed(cartoonPage) { i, page ->
            //val selectPage = (vm.currentSourceState as? SourceHomeViewModel.CurrentSourcePageState.Page )?.cartoonPage
            val select = selectionIndex == i

            FilterChip(
                selected = select,
                onClick = {
                    onPageClick(i)
                },
                label = { Text(page.label) },
                colors = FilterChipDefaults.filterChipColors(),
            )

        }
    }
}