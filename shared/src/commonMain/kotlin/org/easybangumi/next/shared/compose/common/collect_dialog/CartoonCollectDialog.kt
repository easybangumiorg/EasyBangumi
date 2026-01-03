package org.easybangumi.next.shared.compose.common.collect_dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.room.util.TableInfo
import org.easybangumi.next.shared.cartoon.displayName
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.foundation.stringRes
import org.easybangumi.next.shared.foundation.view_model.vm

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
fun CartoonCollectDialog(
    cartoonCover: CartoonCover,
    onDismissRequest: () -> Unit,
) {

    val vm = vm(::CartoonCollectVM, cartoonCover, key = cartoonCover.toIdentify())
    val state = vm.ui
    val sta = state.value

    AlertDialog(
        onDismissRequest = onDismissRequest,
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                // bangumi 收藏板块
                if (sta.bangumiState != null) {
                    CartoonCollectDialogBangumiCard(
                        vm = vm,
                        sta = sta.bangumiState,
                    )
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider()
                }

                // 本地收藏板块
                CartoonCollectDialogLocal(
                    vm = vm,
                    sta = sta.localState,
                )
            }
        },
        confirmButton = {

        }
    )

}

@Composable
fun CartoonCollectDialogBangumiCard(
    vm: CartoonCollectVM,
    sta: CartoonCollectVM.BangumiState,
) {
    Column (
        modifier = Modifier
    ){
        ListItem(
            modifier = Modifier.fillMaxWidth(),
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent,
            ),
            headlineContent = {
                Text(text ="Bangumi 收藏", style = MaterialTheme.typography.titleMedium)
            },
            trailingContent = {
                IconButton(onClick = {}) {
                    Icon(Icons.Filled.Help, null)
                }
            }
        )

        FlowRow(
            modifier = Modifier.padding(PaddingValues(horizontal = 16.dp, vertical = 0.dp)),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
        ) {
            sta.typeList.forEach {  type ->
                FilterChip(
                    modifier = Modifier.height(34.dp).widthIn(min = 84.dp),
                    elevation = null,
                    selected = sta.updatingSelection == type ||
                            (sta.updatingSelection == null && sta.selection == type),
                    onClick = {
                        // 防抖
                        if (sta.updatingSelection == null) {
                            vm.onBangumiTypeChange(type)
                        }
                    },
                    trailingIcon = {
                        if (sta.updatingSelection == type) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else if (sta.updatingSelection == null && sta.selection == type) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null
                            )
                        }
                    },
                    label = {
                        Text(
                            stringRes(type.label),
                        )
                    }
                )
            }
        }
//
//        LazyVerticalGrid(
//            GridCells.Adaptive(minSize = 84.dp),
//            userScrollEnabled = false,
//            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
//            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
//            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
//        ) {
//            items(sta.typeList) {type ->
//                FilterChip(
//                    modifier = Modifier.height(34.dp),
//                    elevation = null,
//                    selected = sta.updatingSelection == type ||
//                            (sta.updatingSelection == null && sta.selection == type),
//                    onClick = {
//                        // 防抖
//                        if (sta.updatingSelection == null) {
//                            vm.onBangumiTypeChange(type)
//                        }
//                    },
//                    trailingIcon = {
//                        if (sta.updatingSelection == type) {
//                            CircularProgressIndicator(
//                                modifier = Modifier.size(24.dp),
//                                strokeWidth = 2.dp
//                            )
//                        } else if (sta.updatingSelection == null && sta.selection == type) {
//                            Icon(
//                                Icons.Filled.Check,
//                                contentDescription = null
//                            )
//                        }
//                    },
//                    label = {
//                        Text(
//                            type.label,
//                        )
//                    }
//                )
//            }
//        }

    }
}

@Composable
fun CartoonCollectDialogLocal(
    vm: CartoonCollectVM,
    sta: CartoonCollectVM.LocalState,
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        ListItem(
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent,
            ),
            headlineContent = {
                Text(text ="本地收藏", style = MaterialTheme.typography.titleMedium)
            },
            trailingContent = {
                IconButton(onClick = {}) {
                    Icon(Icons.Filled.Help, null)
                }
            }
        )
//        Row(
//            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable {
//                // 教程 Dialog
//            }.padding(16.dp, 4.dp),
//            verticalAlignment = Alignment.CenterVertically,
//        ) {
//            Text(text ="本地收藏", style = MaterialTheme.typography.titleMedium)
//            Icon(Icons.Filled.Help, null)
//        }
        sta.tagList.forEach { tag ->
            Row(
                modifier = Modifier
                    .fillMaxWidth().clip(RoundedCornerShape(8.dp))
                    .clickable {
                        vm.onLocalTagChange(tag)
                    },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = sta.selection.contains(tag),
                    onCheckedChange = {
                        vm.onLocalTagChange(tag)
                    }
                )
                Text(stringRes( tag.displayName()))
            }
        }

    }


}