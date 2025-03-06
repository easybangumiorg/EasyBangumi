package com.heyanle.easy_bangumi_cm.shared.ui.main.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easy_bangumi_cm.common.compose.stringRes
import com.heyanle.easy_bangumi_cm.shared.LocalNavController
import dev.icerock.moko.resources.compose.stringResource


/**
 * Created by HeYanLe on 2025/1/5 23:27.
 * https://github.com/heyanLE
 */


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home() {
    val homeViewModel = viewModel<HomeViewModel>()
    val state = homeViewModel.stateFlow.collectAsState().value

    val navController = LocalNavController.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    HomeContent(homeViewModel, state, scrollBehavior)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    homeViewModel: HomeViewModel,
    state: HomeViewModel.State,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    Column {
        TopAppBar(
            navigationIcon = {
                IconButton(onClick = {
                    homeViewModel.toggleSourcePanel()
                }) {
                    Icon(Icons.Filled.Recycling, contentDescription = "change source panel")

                }
            },
            title = {
                val label = state.topAppLabel
                if (label != null) {
                    Text(stringRes(label))
                }
            },
            scrollBehavior = scrollBehavior
        )
        Box(
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {

        }
    }
}