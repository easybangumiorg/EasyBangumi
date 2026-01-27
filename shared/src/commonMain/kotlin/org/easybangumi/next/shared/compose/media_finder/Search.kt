package org.easybangumi.next.shared.compose.media_finder

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.cash.paging.compose.collectAsLazyPagingItems
import org.easybangumi.next.shared.foundation.cartoon.CartoonCoverCard
import org.easybangumi.next.shared.foundation.elements.EmptyElements
import org.easybangumi.next.shared.foundation.lazy.pagingCommon
import org.easybangumi.next.shared.foundation.paging.isLoading
import org.easybangumi.next.shared.foundation.stringRes
import org.easybangumi.next.shared.scheme.EasyScheme

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
    onPanelHide: () -> Unit = {},
) {

    val state = vm.ui.value
    val searchState = state.searchUIState

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
                    modifier = Modifier.fillMaxWidth().sizeIn(minHeight = height),
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
                                        onPanelHide()
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
                    } else if (!paging.isLoading()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillParentMaxWidth()
                                    .height(height),
                                contentAlignment = Alignment.Center,
                            ) {
                                EmptyElements(
                                    modifier = Modifier.fillMaxWidth().height(height),
                                    isRow = false
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