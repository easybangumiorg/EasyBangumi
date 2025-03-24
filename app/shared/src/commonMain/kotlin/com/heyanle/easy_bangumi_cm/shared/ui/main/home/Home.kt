package com.heyanle.easy_bangumi_cm.shared.ui.main.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.heyanle.easy_bangumi_cm.base.utils.DataState
import com.heyanle.easy_bangumi_cm.common.foundation.elements.LoadScaffold
import com.heyanle.easy_bangumi_cm.common.foundation.image.AsyncImage
import com.heyanle.easy_bangumi_cm.common.foundation.plugin.home.HomeComponentContent
import com.heyanle.easy_bangumi_cm.common.foundation.stringRes
import com.heyanle.easy_bangumi_cm.common.foundation.view_model.easyVM
import com.heyanle.easy_bangumi_cm.common.resources.Res
import com.heyanle.easy_bangumi_cm.shared.LocalNavController
import kotlinx.coroutines.launch


/**
 * Created by HeYanLe on 2025/1/5 23:27.
 * https://github.com/heyanLE
 */


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home() {
    val homeViewModel = easyVM<HomeViewModel>()
    val state = homeViewModel.uiState.value

    val navController = LocalNavController.current
    val behavior = TopAppBarDefaults.pinnedScrollBehavior()

    HomeContent(homeViewModel, state, behavior)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    homeViewModel: HomeViewModel,
    state: HomeViewModel.UIState,
    scrollBehavior: TopAppBarScrollBehavior,
) {

    LoadScaffold(modifier = Modifier.fillMaxSize(), data = state.sourceUIState) { sourceUIState ->
        // success
        HomeContent(
            homeViewModel = homeViewModel,
            sourceState = sourceUIState.data,
            contentDataState = state.homeContentUIState,
            scrollBehavior = scrollBehavior
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    homeViewModel: HomeViewModel,
    sourceState: HomeViewModel.SourceUIState,
    contentDataState: DataState<HomeViewModel.HomeContentUIState>,
    scrollBehavior: TopAppBarScrollBehavior,
) {

    val lazyGridState = rememberLazyGridState()

    Column {
        TopAppBar(
            title = { Text(text = stringRes(sourceState.topAppLabel)) },
            navigationIcon = {
                IconButton(onClick = {
                    homeViewModel.showSourcePanel()
                }) {
                    Icon(Icons.Default.Refresh, contentDescription = "showSourcePanel")

                    if (sourceState.isSourcePanelShow) {
                        Popup(
                            alignment = Alignment.BottomStart,
                            onDismissRequest = {
                                homeViewModel.hideSourcePanel()
                            }
                        ) {
                            Column {
                                ListItem(
                                    headlineContent = {
                                        Text(stringRes(Res.strings.choose_source))
                                    }
                                )
                                HorizontalDivider()
                                sourceState.sourceHomeList.forEach {
                                    ListItem(
                                        leadingContent = {
                                            AsyncImage(model = it.icon, contentDescription = stringRes(it.label))
                                        },
                                        headlineContent = {
                                            Text(stringRes(it.label))
                                        },
                                        trailingContent = {
                                            RadioButton(
                                                selected = it.key == sourceState.selectionKey,
                                                onClick = {
                                                    homeViewModel.changeSelectionKey(it.key)
                                                })
                                        }
                                    )
                                }

                            }
                        }
                    }
                }
            },
            scrollBehavior = scrollBehavior
        )

        LoadScaffold(
            modifier = Modifier.fillMaxWidth().weight(1f),
            data = contentDataState,
            errorRetry = {
                homeViewModel.refreshHomeContent()
            }
        ) {
            // success
            HomeComponentContent(
                content = it.data.homeContent,
                nestedScrollConnection = scrollBehavior.nestedScrollConnection,
                columns = GridCells.Adaptive(100.dp)
            )
        }
    }

}

