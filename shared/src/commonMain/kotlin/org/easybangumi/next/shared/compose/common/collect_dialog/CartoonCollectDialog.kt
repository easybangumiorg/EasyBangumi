package org.easybangumi.next.shared.compose.common.collect_dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.room.util.TableInfo
import org.easybangumi.next.shared.data.cartoon.CartoonCover
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
            Column {
                // bangumi 收藏板块
                if (sta.bangumiState != null) {
                    CartoonCollectDialogBangumiCard(
                        vm = vm,
                        sta = sta.bangumiState,
                    )
                }
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
    Column {
        Row(
            modifier = Modifier.clickable {
                // 教程 Dialog
            }
        ) {
            Text("Bangumi 收藏",)
            Icon(Icons.Filled.QuestionMark, null)
        }

        FlowRow {
            sta.typeList.forEach { type ->
                FilterChip(
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
                            CircularProgressIndicator()
                        } else if (sta.updatingSelection == null && sta.selection == type) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null
                            )
                        }
                    },
                    label = {
                        Text(type.label)
                    }
                )

            }
        }
    }
}