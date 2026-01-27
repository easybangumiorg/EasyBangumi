package org.easybangumi.next.shared.compose.media_finder

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.easybangumi.next.shared.foundation.cartoon.CartoonCoverCard
import org.easybangumi.next.shared.foundation.image.AsyncImage
import org.easybangumi.next.shared.foundation.stringRes
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
fun Radar(
    vm: MediaFinderVM,
    modifier: Modifier = Modifier,
    onPanelHide: () -> Unit,
) {

    val state = vm.ui.value
    val radarState = state.radarUIState

    LaunchedEffect(state.keyword) {
        vm.radarV1VM.changeKeyword(state.keyword)
    }

    LazyColumn(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item {
            Row(
                modifier = Modifier.padding(16.dp, 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    "已搜索 ${state.radarUIState.resultTabCount}/${state.radarUIState.radarSourceTabList.size} 个番源",
                        style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Start,
                )
                Spacer(Modifier.weight(1f))
                IconButton(
                    onClick = {}
                ) {
                    Icon(Icons.Filled.Refresh, null)
                }

            }


        }
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(16.dp, 0.dp),
            ) {

                items(radarState.radarSourceTabList.size) {
                    val tab = radarState.radarSourceTabList[it]
                    val selected = tab == radarState.selectionSource
                    Row(modifier = Modifier
//                                .widthIn(max = 120.dp)

                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
                        .clip(RoundedCornerShape(6.dp))
                        .clickable {
//                                    vm.onTagClick(tag.name)
                        }
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = tab.sourceManifest.icon,
                            contentDescription = stringRes(tab.sourceManifest.label),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        if (tab.error) {
                            Icon(Icons.Filled.Check, tint = MaterialTheme.colorScheme.error, contentDescription = null)
                        } else if (tab.loading) {
                            // 加载中动画
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(tab.count.toString())
                        }
                    }
                }
            }
        }

        item {
            ListItem(
                headlineContent = {
                    Text(
                        "资源搜索结果 ${radarState.result.size}",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Start
                    )
                },
                trailingContent = {
                    Text(
                        "点击选择",
//                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Start
                    )
                },
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent
                )
            )

        }

        items(radarState.result.size) {
            val item = radarState.result[it]
            val selection = state.result == item

            Card(
                modifier = Modifier.padding(16.dp, 0.dp).fillMaxWidth().padding(0.dp, 4.dp).clip(RoundedCornerShape(16.dp)).clickable {
                    vm.onUserResultSelect(
                        MediaFinderVM.SelectionResult(
                            playCover = item.cover,
                            manifest = item.businessPair.getManifest(),
                            suggestPlayerLine = null,
                        )
                    )
                    onPanelHide()
                },
                colors = CardDefaults.cardColors().copy(
                    containerColor = if(selection) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = if(selection) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                ),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp).height(EasyScheme.size.cartoonCoverSmallHeight),
                ) {
                    CartoonCoverCard(
                        modifier = Modifier,
                        model = item.cover.coverUrl,
                        name = null,
                        itemSize = EasyScheme.size.cartoonCoverSmallHeight,
                        itemIsWidth = false,
                        coverAspectRatio = EasyScheme.size.cartoonCoverSmallAspectRatio,
                        onClick = null
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        Text(item.cover.name, maxLines = 2, style = MaterialTheme.typography.titleMedium)

                        Spacer(modifier = Modifier.size(8.dp))

                        Row {
                            AsyncImage(
                                model = item.businessPair.getManifest().icon,
                                contentDescription = stringRes(item.businessPair.getManifest().label),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.size(2.dp))
                            Text(stringRes(item.businessPair.getManifest().label), style = MaterialTheme.typography.bodySmall)
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        item.playerLine?.let { lineList ->
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                items(lineList.size) {
                                    val line = lineList[it]

                                    Row(modifier = Modifier
//                                .widthIn(max = 120.dp)

                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable {
//                                    vm.onTagClick(tag.name)
                                        }
                                        .padding(horizontal = 8.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(line.label, style = MaterialTheme.typography.bodyMedium)
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            "(${(line.episodeList.size.toString())})",
                                            color = MaterialTheme.colorScheme.primary,
                                            style = MaterialTheme.typography.bodyMedium)

                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
    }
}