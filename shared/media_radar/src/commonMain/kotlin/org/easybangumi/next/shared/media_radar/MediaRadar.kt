package org.easybangumi.next.shared.media_radar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.cash.paging.compose.collectAsLazyPagingItems
import org.easybangumi.next.shared.foundation.cartoon.CartoonCoverCard
import org.easybangumi.next.shared.foundation.elements.LoadScaffold
import org.easybangumi.next.shared.foundation.lazy.pagingCommon
import org.easybangumi.next.shared.foundation.stringRes
import org.easybangumi.next.shared.resources.Res
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
fun MediaRadar(
    modifier: Modifier,
    vm: MediaRadarViewModel,
) {

    val state = vm.ui

    Column (
        modifier = Modifier.fillMaxWidth().then(modifier)
    ) {
        MediaRadarHeader(
            Modifier.fillMaxWidth(),
            vm,
            state.value,
        )
        HorizontalDivider()
        MediaRadarList(
            Modifier.fillMaxWidth().weight(1f),
            vm,
            state.value,
        )

    }

}

@Composable
fun MediaRadarHeader(
    modifier: Modifier,
    vm: MediaRadarViewModel,
    state: MediaRadarViewModel.State
) {
    Row(
        modifier
    ) {
        TextField(
            modifier = Modifier.fillMaxHeight().weight(1f),
            value = state.keyword,
            onValueChange = { vm.onFieldChange(it) },
        )
        TextButton(onClick = {
            vm.onSearchKeywordChange()
        }) {
            Text(stringRes(Res.strings.search))
        }
    }
}

@Composable
fun MediaRadarList(
    modifier: Modifier,
    vm: MediaRadarViewModel,
    state: MediaRadarViewModel.State,
){
    val line = state.lineState
    LazyColumn(
        modifier = modifier.fillMaxWidth()
    ) {
        items(line) {
            Column {
                Text(
                    stringRes(it.playBusiness.source.manifest.label),
                    style = MaterialTheme.typography.bodyLarge
                )
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .height(EasyScheme.size.cartoonCoverHeight)
                ) {

                }
                LoadScaffold(
                    modifier = Modifier.fillMaxWidth()
                        .height(EasyScheme.size.cartoonCoverHeight),
                    data = it.pagingFlow
                ) {
                    val paging = it.data.collectAsLazyPagingItems()
                    val height = EasyScheme.size.cartoonCoverHeight
                    LazyRow(
                        modifier = Modifier.fillMaxWidth()
                            .height(EasyScheme.size.cartoonCoverHeight)
                    ) {

                        if (paging.itemCount > 0) {
                            items(paging.itemCount) {
                                val item = paging[it]
                                if (item != null) {
                                    CartoonCoverCard(
                                        modifier = Modifier,
                                        model = item.coverUrl,
                                        name = item.name,
                                        onClick = {
//                                            vm.onCartoonCoverClick(item)
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
}

