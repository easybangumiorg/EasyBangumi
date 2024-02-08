package com.heyanle.easybangumi4.ui.search_migrate.search.gather

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.navigationDetailed
import com.heyanle.easybangumi4.source.LocalSourceBundleController
import com.heyanle.easybangumi4.source_api.entity.CartoonCover
import com.heyanle.easybangumi4.source_api.entity.toIdentify
import com.heyanle.easybangumi4.ui.common.CartoonCardWithCover
import com.heyanle.easybangumi4.ui.common.PagingCommon
import com.heyanle.easybangumi4.ui.common.pagingCommonHor
import com.heyanle.easybangumi4.ui.main.star.CoverStarViewModel
import com.heyanle.easybangumi4.ui.search_migrate.search.SearchViewModel

/**
 * Created by heyanlin on 2023/12/18.
 */
@Composable
fun ColumnScope.GatherSearch(
    searchViewModel: SearchViewModel
) {
    val nav = LocalNavController.current
    val keyboard = LocalSoftwareKeyboardController.current
    val searchComponents = LocalSourceBundleController.current.searches()
    val vm =
        viewModel<GatherSearchViewModel>(factory = GatherSearchViewModelFactory(searchComponents))

    val starVm = viewModel<CoverStarViewModel>()

    val searchKey = searchViewModel.searchFlow.collectAsState()
    LaunchedEffect(key1 = searchKey.value) {
        vm.newSearchKey(searchKey.value)
    }

    val itemList = vm.searchItemList.collectAsState()

    Divider()



    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .nestedScroll(object : NestedScrollConnection {
                override fun onPostScroll(
                    consumed: Offset,
                    available: Offset,
                    source: NestedScrollSource
                ): Offset {
                    keyboard?.hide()
                    return super.onPostScroll(consumed, available, source)
                }
            })
    ) {
        itemList.value?.let {
            items(it) {
                MigrateSourceItem(sourceItem = it, starVm = starVm){
                    nav.navigationDetailed(it)
                }
            }
        }

    }
}

@Composable
fun MigrateSourceItem(
    sourceItem: GatherSearchViewModel.GatherSearchItem,
    starVm: CoverStarViewModel,
    supportLongTouchStart: Boolean = true,
    onClick: (CartoonCover)->Unit,
) {
    val page = sourceItem.flow.collectAsLazyPagingItems()
    val haptic = LocalHapticFeedback.current
    val set = starVm.setFlow.collectAsState(initial = setOf<String>())
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    ) {

        ListItem(
            headlineContent = { Text(text = sourceItem.searchComponent.source.label) },
            trailingContent = { if (supportLongTouchStart) Text(text = stringResource(id = R.string.long_press_to_star)) },
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent
            ),
        )
        if (page.itemCount > 0) {
            LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(4.dp)
            ) {
                items(page.itemCount) {
                    page[it]?.let {
                        CartoonCardWithCover(
                            modifier = Modifier.width(100.dp),
                            star = set.value.contains(it.toIdentify()),
                            cartoonCover = it,
                            onClick = {
                               onClick(it)
                            },
                            onLongPress = if (supportLongTouchStart) {
                                {
                                    starVm.star(it)
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            } else null,
                        )
                    }

                }
                pagingCommonHor(page)
            }
        }
        PagingCommon(items = page)

    }
}