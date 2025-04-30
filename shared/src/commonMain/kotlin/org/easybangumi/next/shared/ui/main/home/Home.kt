package org.easybangumi.next.shared.ui.main.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.easybangumi.next.shared.foundation.TabPage
import org.easybangumi.next.shared.foundation.plugin.SourceBundleContainer
import org.easybangumi.next.shared.foundation.stringRes
import org.easybangumi.next.shared.foundation.view_model.vm
import org.easybangumi.next.shared.plugin.core.source.SourceBundle
import org.easybangumi.next.shared.resources.Res
import org.easybangumi.next.shared.ui.shared.discover.Discover

/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 */
@Composable
fun Home() {

    SourceBundleContainer(Modifier.fillMaxSize()) {
        val viewModel = vm(::HomeViewModel, it)

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            HomeTopAppBar(
                modifier = Modifier.fillMaxWidth(),
                state = viewModel.ui.value,
                viewModel = viewModel
            )
            HomeContent(
                modifier = Modifier.fillMaxWidth().weight(1f),
                state = viewModel.ui.value,
                viewModel = viewModel
            )
        }

    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopAppBar(
    modifier: Modifier = Modifier,
    state: HomeViewModel.State,
    viewModel: HomeViewModel,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            val label = state.sourceInfo?.manifest?.label
            if (label != null) {
                Text(stringRes(label))
            }
        },
        actions = {
            TextButton(
                onClick = {

                }
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = stringRes(Res.strings.search)
                )
                Text(
                    stringRes(Res.strings.search),
                )
            }
        },
    )


}

@Composable
fun HomeContent(
    modifier: Modifier = Modifier,
    state: HomeViewModel.State,
    viewModel: HomeViewModel,
) {
    val pageState = state.pageState
    val pagerState = rememberPagerState(0) { pageState.showTabList.size }
    Column {
        TabPage(
            pagerModifier = Modifier.fillMaxWidth().weight(1f),
            pagerState = pagerState,
            onTabSelect = {},
            tabs = @Composable { index, selected ->
                val tab = pageState.showTabList.getOrNull(index)
                tab?.let {
                    Text(stringRes(it.label))
                }
            },
            contents = @Composable { index ->
                val tab = pageState.showTabList.getOrNull(index)
                when(tab) {
                    is HomeViewModel.TabState.Discover -> {
                        Discover(
                            tab.discoverBusiness,
                            onJumpDetail = {

                            },
                            onJumpRouter = {

                            }
                        )
                    }
                    is HomeViewModel.TabState.Page -> {

                    }
                    else -> { }
                }
            }
        )
    }
}