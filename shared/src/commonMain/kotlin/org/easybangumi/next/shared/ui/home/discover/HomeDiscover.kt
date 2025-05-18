package org.easybangumi.next.shared.ui.home.discover

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.easybangumi.next.shared.foundation.plugin.SourceBundleContainer
import org.easybangumi.next.shared.foundation.stringRes
import org.easybangumi.next.shared.foundation.view_model.vm
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
fun HomeDiscover() {

    SourceBundleContainer(Modifier.fillMaxSize()) {
        val viewModel = vm(::HomeDiscoverViewModel, it)

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            DiscoverTopAppBar(
                modifier = Modifier.fillMaxWidth(),
                state = viewModel.ui.value,
                viewModel = viewModel
            )
            HomeDiscoverContent(
                modifier = Modifier.fillMaxWidth().weight(1f),
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
fun HomeDiscoverContent(
    modifier: Modifier = Modifier,
    state: HomeDiscoverViewModel.State,
    viewModel: HomeDiscoverViewModel,
) {
    val discoverBusiness = state.discoverBusiness
    if (discoverBusiness != null) {
        Discover(
            modifier,
            discoverBusiness,
            onJumpDetail = {

            }
        )
    }
}