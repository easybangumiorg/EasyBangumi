package org.easybangumi.next.shared.compose.media_finder

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.cash.paging.compose.collectAsLazyPagingItems
import org.easybangumi.next.shared.foundation.cartoon.CartoonCoverCard
import org.easybangumi.next.shared.foundation.lazy.pagingCommon
import org.easybangumi.next.shared.foundation.stringRes
import org.easybangumi.next.shared.resources.Res
import org.easybangumi.next.shared.scheme.EasyScheme
import org.easybangumi.next.shared.source.api.component.getManifest

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
fun Search(
    vm: MediaFinderVM,
    modifier: Modifier = Modifier,
) {

    val state = vm.ui.value
    val searchState = state.searchState

    LaunchedEffect(state.keyword) {
        vm.searchVM.changeKeyword(state.keyword)
    }


    LazyColumn(
        modifier = Modifier.fillMaxWidth().then(modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {


        items(searchState.searchLineStateList) { line ->

            Column {
                ListItem(
                    headlineContent = {
                        Text(stringRes(line.sourceManifest.label))
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = Color.Transparent
                    )
                )
                val height = EasyScheme.size.cartoonCoverSmallHeight

                val paging = line.flow.collectAsLazyPagingItems()

                LazyRow(
                    modifier = Modifier.fillMaxWidth()
                        .height(height),
                    contentPadding = PaddingValues(8.dp, 0.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (paging.itemCount > 0) {
                        items(paging.itemCount) {
                            val item = paging[it]
                            if (item != null) {
                                CartoonCoverCard(
                                    modifier = Modifier,
                                    model = item.coverUrl,
                                    name = item.name,
                                    nameShowOutside = true,
                                    itemSize = height,
                                    itemIsWidth = false,
                                    coverAspectRatio = EasyScheme.size.cartoonCoverSmallAspectRatio,
                                    onClick = {
                                        vm.onUserResultSelect(
                                            MediaFinderVM.SelectionResult(
                                                playCover = item,
                                                manifest = line.sourceManifest,
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }
                    pagingCommon(height, paging)
                }
            }
        }


    }


}