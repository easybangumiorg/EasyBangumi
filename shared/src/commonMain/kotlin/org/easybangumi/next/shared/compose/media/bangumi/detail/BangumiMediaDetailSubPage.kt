package org.easybangumi.next.shared.compose.media.bangumi.detail

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.easybangumi.next.shared.compose.media.bangumi.BangumiMediaActions
import org.easybangumi.next.shared.compose.media.bangumi.BangumiMediaCommonVM
import org.easybangumi.next.shared.compose.media.bangumi.BangumiMediaPageParam
import org.easybangumi.next.shared.compose.media.bangumi.detail.action.BangumiAction
import org.easybangumi.next.shared.compose.media.bangumi.detail.preview.BangumiDetailPreview
import org.easybangumi.next.shared.compose.media.mediaPlayLineIndex
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
    param: BangumiMediaPageParam
) {
    val commonVM = param.commonVM
    val state = commonVM.state.collectAsState()
    val sta = state.value
    val playLineIndexState = commonVM.playIndexState.collectAsState()
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(0.dp, 8.dp),
        ) {
            // Bangumi 详情卡片
            bangumiDetailCard(commonVM, sta)
            space(Modifier.height(8.dp))
            // 操作按钮
//            playerAction(param)
//            space(Modifier.height(8.dp))
            // 播放源卡片
            playerSourceCard(commonVM, sta)
            space(Modifier.height(8.dp))
            divider()
            // 播放线路和集数选择
            mediaPlayLineIndex(commonVM.playLineIndexVM, commonVM.playLineIndexVM.ui.value, 2)
        }

    }
}

fun LazyListScope.space(
    modifier: Modifier = Modifier,
){
    item {
        Spacer(modifier = modifier)
    }
}

fun LazyListScope.divider() {
    item{
        HorizontalDivider()
    }
}

fun LazyListScope.bangumiDetailCard(
    vm: BangumiMediaCommonVM,
    sta: BangumiMediaCommonVM.State,
) {

    item {
        BangumiDetailPreview( modifier = Modifier.padding(8.dp, 0.dp), vm)
    }

}

fun LazyListScope.playerAction(
    param: BangumiMediaPageParam
) {
    item {
        BangumiAction(param)
    }

}

fun LazyListScope.playerSourceCard(
    vm: BangumiMediaCommonVM,
    sta: BangumiMediaCommonVM.State,
) {
    item {
        val radarRes = sta.radarResult
        if (radarRes == null) {
            val finderState = sta.silentFindingState
            if (finderState != null && finderState.silentNotResult && !finderState.silentFinding) {
                ListItem(
                    modifier = Modifier
                        .padding(8.dp, 0.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            vm.showMediaRadar()
                        }
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(8.dp))

                    ,
                    colors = ListItemDefaults.colors(
                        containerColor = Color.Transparent
                    ),
                    headlineContent = {
                        Text(stringRes(Res.strings.play_from))
                    },
                    supportingContent = {
                        Text(stringRes(Res.strings.no_media_res_tips))
                    },
                    leadingContent = {
                        AnimationImage(
                            modifier = Modifier.size(36.dp),
                            model = Res.assets.error_tomorin_gif,
                            contentDescription = "no play source"
                        )
                    },
                    trailingContent = {
                        TextButton(
                            onClick = {
                                vm.showMediaRadar()
                            }
                        ) {
                            Text(stringRes(Res.strings.click_to_change_search_from_user))
                        }
                    },
                )
            } else if (finderState != null && finderState.silentFinding && finderState.radarUIState != null ) {
                ListItem(
                    modifier = Modifier
                        .padding(8.dp, 0.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            vm.showMediaRadar()
                        }
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(8.dp))

                    ,
                    colors = ListItemDefaults.colors(
                        containerColor = Color.Transparent
                    ),
                    headlineContent = {
                        Text(stringRes(Res.strings.media_finding))
                    },
                    supportingContent = {
                        Row {
                            finderState.radarUIState.resultTab.forEach { tab ->
                                Crossfade(tab) {
                                    AsyncImage(
                                        model = tab.sourceManifest.icon,
                                        contentDescription = stringRes(it.sourceManifest.label),
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                        LinearProgressIndicator()
                    },
                    leadingContent = {
                        AnimationImage(
                            modifier = Modifier.size(36.dp),
                            model = Res.assets.loading_anon_gif,
                            contentDescription = "no play source"
                        )

                    },
                    trailingContent = {
                        TextButton(
                            onClick = {
                                vm.showMediaRadar()
                            }
                        ) {
                            Text(stringRes(Res.strings.play_from))
                        }
                    },
                )
            } else {
                ListItem(
                    modifier = Modifier
                        .padding(8.dp, 0.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            vm.showMediaRadar()
                        }
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(8.dp))

                    ,
                    colors = ListItemDefaults.colors(
                        containerColor = Color.Transparent
                    ),
                    headlineContent = {
                        Text(stringRes(Res.strings.play_from))
                    },
                    supportingContent = {
                        Text(stringRes(Res.strings.no_play_from_tips))
                    },
                    leadingContent = {
                        AnimationImage(
                            modifier = Modifier.size(36.dp),
                            model = Res.assets.error_tomorin_gif,
                            contentDescription = "no play source"
                        )
                    },
                    trailingContent = {
                        TextButton(
                            onClick = {
                                vm.showMediaRadar()
                            }
                        ) {
                            Text(stringRes(Res.strings.click_to_search_play_from))
                        }
                    },
                )
            }

        } else {

            ListItem(
                modifier = Modifier
                    .padding(8.dp, 0.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        vm.showMediaRadar()
                    }
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(8.dp))
                    ,
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent
                ),
                headlineContent = {
                    Text(stringRes(Res.strings.play_from))
                },
                supportingContent = {
                    Text(
                        stringRes(radarRes.manifest.label),
                    )
                },
                leadingContent = {
                    AsyncImage(
                        model = radarRes.manifest.icon,
                        contentDescription = stringRes(radarRes.manifest.label),
                        modifier = Modifier.size(36.dp)
                    )
                },
                trailingContent = {
                    TextButton(
                        onClick = {
                            vm.showMediaRadar()
                        }
                    ) {
                        Text(stringRes(Res.strings.click_to_change_play_from))
                    }
                },
            )
        }

    }
}