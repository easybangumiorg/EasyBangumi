package org.easybangumi.next.shared.ui.detail.bangumi

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.easybangumi.ext.shared.plugin.bangumi.plugin.BangumiMetaComponent
import org.easybangumi.next.shared.LocalNavController
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.foundation.view_model.vm
import org.easybangumi.next.shared.plugin.api.component.ComponentBusiness

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
fun BangumiDetailPage(
    cartoonIndex: CartoonIndex,
    metaBusiness: ComponentBusiness<BangumiMetaComponent>,
) {

    val navController = LocalNavController.current
    val vm = vm(
        ::BangumiDetailViewModel, cartoonIndex, metaBusiness
    )
    val appBarBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Box(modifier = Modifier.fillMaxSize()) {

        BangumiDetail(
            modifier = Modifier.fillMaxSize(),
            vm = vm,
            nestedScrollConnection = appBarBehavior.nestedScrollConnection,
            contentPaddingTop = 64.dp,
        )
        BangumiDetailTopBar(vm, navController, appBarBehavior)
    }

}


@Composable
fun BangumiDetailTopBar(
    vm: BangumiDetailViewModel,
    navController: NavController,
    behavior: TopAppBarScrollBehavior? = null,
) {
    TopAppBar(
        title = {},
        navigationIcon = {
            IconButton(onClick = {
                navController.navigateUp()
            }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        scrollBehavior = behavior,
        colors = TopAppBarDefaults.topAppBarColors().copy(containerColor = Color.Transparent)
    )

}