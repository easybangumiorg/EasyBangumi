package com.heyanle.easybangumi4.ui.search_migrate.migrate

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.source.LocalSourceBundleController
import com.heyanle.easybangumi4.source_api.entity.CartoonCover
import com.heyanle.easybangumi4.ui.main.star.CoverStarViewModel
import com.heyanle.easybangumi4.ui.search_migrate.search.SearchTopAppBar
import com.heyanle.easybangumi4.ui.search_migrate.search.gather.GatherSearchViewModel
import com.heyanle.easybangumi4.ui.search_migrate.search.gather.GatherSearchViewModelFactory
import com.heyanle.easybangumi4.ui.search_migrate.search.gather.MigrateSourceItem

/**
 * Created by heyanle on 2024/1/28.
 * https://github.com/heyanLE
 */
@Composable
fun MigrateGather(
    cartoonInfo: CartoonInfo,
    sourceKeys: List<String>,
    onBack: ()->Unit,
    onClick: (CartoonCover) -> Unit,
) {

    var searchText by remember {
        mutableStateOf<String>(cartoonInfo.name)
    }

    val focusRequester = remember {
        FocusRequester()
    }

    val searchComponents = LocalSourceBundleController.current.searches()
    val curComponents = remember(searchComponents) {
        searchComponents.filter { sourceKeys.contains(it.source.key) }
    }

    val keyboard = LocalSoftwareKeyboardController.current

    val gatherSearchViewModel = viewModel<GatherSearchViewModel>(factory = GatherSearchViewModelFactory(
        curComponents
    ))

    LaunchedEffect(key1 = cartoonInfo){
        gatherSearchViewModel.newSearchKey(cartoonInfo.name)
    }

    val starVm = viewModel<CoverStarViewModel>()

    BackHandler {
        onBack()
    }

    Column {
        SearchTopAppBar(
            text = searchText,
            isGather = true,
            focusRequester = focusRequester,
            showAction = false,
            onBack = {
                onBack()
            },
            onSearch = {
                gatherSearchViewModel.newSearchKey(it)
            },
            onTextChange = {
                searchText = it
                if(it.isEmpty()){
                    gatherSearchViewModel.newSearchKey(it)
                }
            },
            onIsGatherChange = {

            }
        )

        val itemList = gatherSearchViewModel.searchItemList.collectAsState()

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
                    MigrateSourceItem(sourceItem = it, starVm = starVm, supportLongTouchStart = false){
                        onClick(it)
                    }
                }
            }

        }
    }
}