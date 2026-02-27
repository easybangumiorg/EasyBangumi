package org.easybangumi.next.shared.compose.discover.bangumi

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
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
import org.easybangumi.next.shared.LocalNavController
import org.easybangumi.next.shared.RouterPage
import org.easybangumi.next.shared.compose.bangumi.bangumi
import org.easybangumi.next.shared.compose.bangumi.bangumiContainer
import org.easybangumi.next.shared.foundation.stringRes
import org.easybangumi.next.shared.navigate
import org.easybangumi.next.shared.resources.Res
import org.easybangumi.next.shared.source.bangumi.source.BangumiInnerSource

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
fun BangumiDiscoverTopAppBar(
    modifier: Modifier,
    vm: BangumiDiscoverViewModel,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    val nav = LocalNavController.current
    val user = vm.bangumiAccountCardVM.ui.value

    TopAppBar(
        modifier = modifier,
        title = {
            Text(stringRes(Res.strings.discover))
        },
        colors = TopAppBarDefaults.topAppBarColors().copy(
            containerColor = Color.Transparent,
        ),
        scrollBehavior = scrollBehavior,
        actions = {

            if (user.isNone()) {
                TextButton(
                    onClick = {
                        nav.navigate(RouterPage.BangumiLogin, true)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = MaterialTheme.colorScheme.bangumiContainer,
                        contentColor = MaterialTheme.colorScheme.bangumi,
                    )
                ) {
                    Text(
                        text = "绑定 Bangumi",
                    )
                }
            }

            TextButton(
                onClick = {
                    nav.navigate(RouterPage.Search.from("", BangumiInnerSource.SOURCE_KEY))
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