package com.heyanle.easybangumi4.ui.common.page.listgroup

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.bangumi_source_api.api.component.page.SourcePage
import com.heyanle.easybangumi4.ui.common.ErrorPage
import com.heyanle.easybangumi4.ui.common.LoadingPage
import com.heyanle.easybangumi4.ui.common.page.list.SourceListPage

/**
 * Created by HeYanLe on 2023/2/25 21:18.
 * https://github.com/heyanLE
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SourceListPageGroup(
    listPageGroup: SourcePage.Group
) {
    val vm =
        viewModel<SourceListGroupViewModel>(factory = SourceListGroupViewModelFactory(listPageGroup))
    LaunchedEffect(key1 = Unit) {
        if (vm.groupState is SourceListGroupViewModel.GroupState.None) {
            vm.refresh()
        }
    }

    vm.groupState.let{
        when (it) {
            SourceListGroupViewModel.GroupState.None -> {}
            SourceListGroupViewModel.GroupState.Loading -> {
                LoadingPage(
                    modifier = Modifier.fillMaxSize()
                )
            }
            is SourceListGroupViewModel.GroupState.Group -> {
                SourceListWithGroup(vm, it)
            }
            is SourceListGroupViewModel.GroupState.Error -> {
                ErrorPage(
                    modifier = Modifier.fillMaxSize(),
                    errorMsg = it.errorMsg,
                    clickEnable = true,
                    onClick = {
                        vm.refresh()
                    },
                    other = {
                        Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.click_to_retry))
                    }
                )
            }
        }
    }
}

@Composable
fun SourceListWithGroup(
    groupVM: SourceListGroupViewModel,
    groupState: SourceListGroupViewModel.GroupState.Group,
){
    LaunchedEffect(key1 = Unit){
        if(groupVM.curListState is SourceListGroupViewModel.CurListPageState.None){
            if(groupState.list.isNotEmpty()){
                groupVM.changeListPage(groupState.list[0])
            }
        }
    }
    groupVM.curListState.let {
        when(it){
            SourceListGroupViewModel.CurListPageState.None -> {}
            is SourceListGroupViewModel.CurListPageState.Page -> {
                val listVmOwner = groupVM.getViewModelStoreOwner(it.pageListPage)
                CompositionLocalProvider(
                    LocalViewModelStoreOwner provides listVmOwner
                ) {
                    SourceListPage(
                        listPage = it.pageListPage,
                        header = {
                            SourceListGroupTab(
                                list = groupState.list,
                                curPage = it.pageListPage,
                                onClick = {
                                    groupVM.changeListPage(it)
                                })
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SourceListGroupTab(
    list: List<SourcePage.SingleCartoonPage>,
    curPage: SourcePage.SingleCartoonPage,
    onClick: (SourcePage.SingleCartoonPage)->Unit,
){
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ){
        items(list) {
            val selected = it == curPage
            Surface(
                shape = CircleShape,
                modifier =
                Modifier
                    .padding(2.dp, 8.dp),
                color = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
            ) {
                Text(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable {
                            onClick(it)
                        }
                        .padding(8.dp, 4.dp),
                    color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.W900,
                    text = it.label,
                    fontSize = 12.sp,
                )
            }
        }
    }
}