package org.easybangumi.next.shared.foundation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.stringResource
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
inline fun <reified T> EasyMutiSelectionDialog(
    show: Boolean,
    items: List<T>,
    initSelection: List<T>,
    noinline title: @Composable () -> Unit = {},
    crossinline message: @Composable () -> Unit = {},
    crossinline action: @Composable RowScope.() -> Unit = {},
    crossinline selectLabel: @Composable (T) -> Unit = {},
    confirmText: String = stringRes(Res.strings.confirm),
    noinline onConfirm: (List<T>) -> Unit,
    noinline onDismissRequest: () -> Unit,
) {
    val selectList = remember {
        mutableStateListOf(*initSelection.toTypedArray())
    }

    if (show) {
        AlertDialog(
            title = title,
            text = {
                Column {
                    message()
                    items.forEach { key ->
                        val select = selectList.contains(key)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (!select) {
                                        selectList.add(key)
                                    } else {
                                        selectList.remove(key)
                                    }
                                }
                                .padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(checked = select, onCheckedChange = {
                                if (it) {
                                    selectList.add(key)
                                } else {
                                    selectList.remove(key)
                                }
                            })
                            selectLabel(key)

                        }
                    }
                }
            },
            onDismissRequest = onDismissRequest,
            confirmButton = {

                Row (
                    modifier = Modifier.fillMaxWidth()
                ){
                    action()

                    Spacer(modifier = Modifier.weight(1f))

                    TextButton(
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.onBackground
                        ),
                        onClick = {
                            onDismissRequest()
                        }
                    ) {
                        Text(text = stringRes(Res.strings.cancel))
                    }

                    TextButton(
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.onBackground
                        ),
                        onClick = {
                            onConfirm(selectList)
                            onDismissRequest()
                        }) {
                        Text(text = confirmText)
                    }
                }

            },
        )
    }

}