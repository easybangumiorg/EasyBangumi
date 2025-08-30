package org.easybangumi.next.shared.compose.discover.bangumi

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.easybangumi.next.shared.foundation.stringRes
import org.easybangumi.next.shared.resources.Res

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