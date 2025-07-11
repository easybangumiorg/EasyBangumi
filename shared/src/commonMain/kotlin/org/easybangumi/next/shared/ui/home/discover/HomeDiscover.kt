package org.easybangumi.next.shared.ui.home.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import org.easybangumi.next.shared.LocalNavController
import org.easybangumi.next.shared.RouterPage
import org.easybangumi.next.shared.foundation.plugin.SourceBundleContainer
import org.easybangumi.next.shared.foundation.stringRes
import org.easybangumi.next.shared.foundation.view_model.vm
import org.easybangumi.next.shared.resources.Res
import org.easybangumi.next.shared.ui.discover.Discover

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
fun HomeDiscover() {

    SourceBundleContainer(Modifier.fillMaxSize()) {
        val viewModel = vm(::HomeDiscoverViewModel, it)

        val behavior = TopAppBarDefaults.pinnedScrollBehavior()
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            DiscoverTopAppBar(
                modifier = Modifier.fillMaxWidth(),
                state = viewModel.ui.value,
                viewModel = viewModel,
                behavior
            )
            HomeDiscoverContent(
                modifier = Modifier.nestedScroll(behavior.nestedScrollConnection).fillMaxWidth().weight(1f),
                state = viewModel.ui.value,
                viewModel = viewModel
            )
        }

    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverTopAppBar(
    modifier: Modifier = Modifier,
    state: HomeDiscoverViewModel.State,
    viewModel: HomeDiscoverViewModel,
    behavior: TopAppBarScrollBehavior? = null,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            val label = state.sourceInfo?.manifest?.label
            if (label != null) {
                Text(stringRes(label))
            }
        },
        colors = TopAppBarDefaults.topAppBarColors().copy(
            containerColor = Color.Transparent,
        ),
//        scrollBehavior = behavior,
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
fun HomeDiscoverContent(
    modifier: Modifier = Modifier,
    state: HomeDiscoverViewModel.State,
    viewModel: HomeDiscoverViewModel,
) {
    val navController = LocalNavController.current
    val discoverBusiness = state.discoverBusiness
    if (discoverBusiness != null) {
        Discover(
            modifier.background(MaterialTheme.colorScheme.surfaceContainerLowest),
            discoverBusiness,
            onJumpDetail = {
                navController.navigate(RouterPage.Detail(it.id, it.source, it.ext))
            }
        )
    }
}