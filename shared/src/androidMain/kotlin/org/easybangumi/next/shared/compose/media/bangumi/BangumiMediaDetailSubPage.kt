package org.easybangumi.next.shared.compose.media.bangumi

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.easybangumi.next.shared.foundation.image.AnimationImage
import org.easybangumi.next.shared.foundation.image.AsyncImage
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
fun BangumiMediaDetailSubPage(
    vm: AndroidBangumiMediaViewModel
) {
    val state = vm.state.collectAsState()
    val sta = state.value
    Box(modifier = Modifier.fillMaxSize()) {

        LazyVerticalGrid(
            GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
        ) {
            bangumiDetailCard(vm, sta)
            playerSourceCard(vm, sta)
        }

    }
}

fun LazyGridScope.bangumiDetailCard (
    vm: AndroidBangumiMediaViewModel,
    sta: AndroidBangumiMediaViewModel.State,
) {

    item {
        Row(
            modifier = Modifier.fillMaxWidth().clickable {
                vm.showBangumiDetailPanel()
            }
        ) {
            Text(
                sta.detailNamePreview,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            IconButton(onClick = {
                vm.showBangumiDetailPanel()
            }) {
                Icon(Icons.Default.MoreVert, contentDescription = "")
            }
        }
    }

}

fun LazyGridScope.playerSourceCard (
    vm: AndroidBangumiMediaViewModel,
    sta: AndroidBangumiMediaViewModel.State,
) {
    item {
        val radarRes = sta.radarResult
        if (radarRes == null) {
            Row(
                modifier = Modifier.padding(8.dp, 4.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(8.dp))
                    .padding(16.dp, 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimationImage(
                    modifier = Modifier.size(24.dp),
                    model = Res.assets.error_tomorin_gif,
                    contentDescription = "no play source"
                )

                Spacer(modifier = Modifier.size(4.dp))

                Column {
                    Text(stringRes(Res.strings.play_from))
                    Text(stringRes(Res.strings.no_play_from_tips), style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.weight(1f))

                OutlinedButton(
                    onClick = {
                        vm.showMediaRadar()
                    }
                ) {
                    Text(stringRes(Res.strings.click_to_search_play_from))
                }
            }
        } else {
            Row(
                modifier = Modifier.padding(8.dp, 4.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(8.dp))
                    .padding(16.dp, 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = radarRes.playBusiness.source.manifest.icon,
                    contentDescription = stringRes(radarRes.playBusiness.source.manifest.label),
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.size(4.dp))

                Column {
                    Text(stringRes(Res.strings.play_from))
                    Text(stringRes(radarRes.playBusiness.source.manifest.label), style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.weight(1f))

                OutlinedButton(
                    onClick = {
                        vm.showMediaRadar()
                    }
                ) {
                    Text(stringRes(Res.strings.click_to_change_play_from))
                }
            }
        }

    }
}