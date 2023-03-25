package com.heyanle.easybangumi4.ui.main.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.navigationSearch
import com.heyanle.easybangumi4.source.LocalSourceBundleController
import com.heyanle.easybangumi4.ui.common.EasyBottomSheetDialog
import com.heyanle.easybangumi4.ui.common.MD3BottomSheet
import com.heyanle.easybangumi4.ui.common.OkImage
import com.heyanle.easybangumi4.ui.common.page.CartoonPageListTab
import com.heyanle.easybangumi4.ui.common.page.CartoonPageUI
import com.heyanle.easybangumi4.ui.main.LocalMainViewModel

/**
 * Created by HeYanLe on 2023/3/25 15:47.
 * https://github.com/heyanLE
 */
@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
@Composable
fun Home() {

    val vm = viewModel<HomeViewModel>()
    val nav = LocalNavController.current

    val state by vm.stateFlow.collectAsState()

//    val sheetState: ModalBottomSheetState =
//        rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)

    var isSheetShow by remember {
        mutableStateOf(false)
    }

    val mainViewModel = LocalMainViewModel.current

    val scope = rememberCoroutineScope()

    Column {
        HomeTopAppBar(
            title = state.topAppBarTitle,
            onChangeClick = {
                isSheetShow = true

            },
            onSearchClick = { nav.navigationSearch(state.selectionKey) }
        )

        CartoonPageListTab(
            state.pages,
            selectionIndex = state.selectionIndex,
            onPageClick = {
                vm.changeSelectionPage(it)
            }
        )

        Divider()


        AnimatedContent(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            targetState = kotlin.runCatching { state.pages[state.selectionIndex] }.getOrNull(),
            transitionSpec = {
                fadeIn(animationSpec = tween(300, delayMillis = 300)) with
                        fadeOut(animationSpec = tween(300, delayMillis = 0))
            }, label = ""
        ) {
            it?.let {
                val listVmOwner = vm.getViewModelStoreOwner(it)
                CompositionLocalProvider(
                    LocalViewModelStoreOwner provides listVmOwner
                ) {
                    CartoonPageUI(cartoonPage = it)
                }
            }
        }

    }
    val animSources = LocalSourceBundleController.current
    if(isSheetShow){
        EasyBottomSheetDialog(onDismissRequest = { isSheetShow = false }) {
            ListItem(headlineContent = { Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.choose_source)) })
            Divider()
            repeat(10){
                for (page in animSources.pages()) {
                    ListItem(
                        headlineContent = { Text(text = page.source.label) },
                        leadingContent = {
                            val icon = remember {
                                animSources.icon(page.source.key)
                            }
                            OkImage(
                                modifier = Modifier.size(32.dp),
                                image = icon?.getIconFactory()?.invoke(),
                                contentDescription = page.source.label
                            )
                        },
                        trailingContent = {
                            RadioButton(
                                selected = state.selectionKey == page.source.key,
                                onClick = {
                                    isSheetShow = false
                                    vm.changeSelectionSource(page.source.key)
                                })
                        }
                    )
                }
            }


        }
    }

//    HomeBottomSheet(sheetState = sheetState, defSourceKey = state.selectionKey, onSourceClick = {
//        vm.changeSelectionSource(it)
//    })


}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeBottomSheet(
    sheetState: ModalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden),
    defSourceKey: String,
    onSourceClick: (String) -> Unit,
) {
    MD3BottomSheet(
        sheetState = sheetState
    ){
        val animSources = LocalSourceBundleController.current
        ListItem(headlineContent = { Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.choose_source)) })
        Divider()
        for (page in animSources.pages()) {
            ListItem(
                headlineContent = { Text(text = page.source.label) },
                leadingContent = {
                    val icon = remember {
                        animSources.icon(page.source.key)
                    }
                    OkImage(
                        modifier = Modifier.size(32.dp),
                        image = icon?.getIconFactory()?.invoke(),
                        contentDescription = page.source.label
                    )
                },
                trailingContent = {
                    RadioButton(
                        selected = defSourceKey == page.source.key,
                        onClick = { onSourceClick(page.source.key) })
                }
            )
        }
    }


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopAppBar(
    title: String,
    onChangeClick: () -> Unit,
    onSearchClick: () -> Unit,
) {

    TopAppBar(
        navigationIcon = {
            IconButton(onClick = { onChangeClick() }) {
                Icon(
                    Icons.Filled.Sync,
                    stringResource(id = com.heyanle.easy_i18n.R.string.source)
                )
            }
        },
        title = { Text(text = title) },
        actions = {
            IconButton(onClick = { onSearchClick() }) {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = stringResource(id = com.heyanle.easy_i18n.R.string.search)
                )
            }
        }
    )
}