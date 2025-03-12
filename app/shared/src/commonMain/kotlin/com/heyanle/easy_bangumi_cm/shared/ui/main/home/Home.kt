package com.heyanle.easy_bangumi_cm.shared.ui.main.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easy_bangumi_cm.common.foundation.elements.EmptyElements
import com.heyanle.easy_bangumi_cm.common.foundation.elements.ErrorElements
import com.heyanle.easy_bangumi_cm.common.foundation.elements.LoadingElements
import com.heyanle.easy_bangumi_cm.common.foundation.plugin.home.HomeComponentContent
import com.heyanle.easy_bangumi_cm.common.foundation.stringRes
import com.heyanle.easy_bangumi_cm.common.foundation.view_model.easyVM
import com.heyanle.easy_bangumi_cm.plugin.api.component.media.home.HomeContent
import com.heyanle.easy_bangumi_cm.shared.LocalNavController


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
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    HomeContent(homeViewModel, state, scrollBehavior)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    homeViewModel: HomeViewModel,
    state: HomeViewModel.UIState,
    scrollBehavior: TopAppBarScrollBehavior,
) {

    when (val sourceState = state.sourceUIState) {
        is HomeViewModel.SourceUIState.Loading -> {
            // loading
            LoadingElements(
                modifier = Modifier.fillMaxSize(),
                isRow = false
            )
        }
        is HomeViewModel.SourceUIState.Empty -> {
            // empty
            EmptyElements(
                modifier = Modifier.fillMaxSize(),
                isRow = false
            )
        }
        is HomeViewModel.SourceUIState.Error -> {
            // error
            ErrorElements(
                modifier = Modifier.fillMaxSize(),
                isRow = false,
                errorMsg = sourceState.errorMsg,
                onClick = {
                    homeViewModel.refreshSource()
                }
            )
        }
        is HomeViewModel.SourceUIState.Success -> {
            // success
            HomeContent(
                homeViewModel = homeViewModel,
                sourceState = sourceState,
                contentState = state.homeContentUIState,
                scrollBehavior = scrollBehavior
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    homeViewModel: HomeViewModel,
    sourceState: HomeViewModel.SourceUIState.Success,
    contentState: HomeViewModel.HomeContentUIState,
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
                }
            },
            scrollBehavior = scrollBehavior
        )
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            when (contentState) {
                is HomeViewModel.HomeContentUIState.Loading -> {
                    // loading
                    LoadingElements(
                        modifier = Modifier.fillMaxSize(),
                        isRow = false
                    )
                }
                is HomeViewModel.HomeContentUIState.Error -> {
                    // error
                    ErrorElements(
                        modifier = Modifier.fillMaxSize(),
                        isRow = false,
                        errorMsg = contentState.errorMsg,
                        onClick = {
                            homeViewModel.refreshHomeContent()
                        }
                    )
                }
                is HomeViewModel.HomeContentUIState.Success -> {
                    // success
                    HomeComponentContent(
                        content = contentState.homeContent,
                        scrollBehavior = scrollBehavior,
                        lazyGridState = lazyGridState,
                        columns = GridCells.Adaptive(100.dp)
                    )
                }
            }
        }
    }

}