package com.heyanle.easybangumi4.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.heyanle.easybangumi4.cartoon.entity.CartoonTag
import com.heyanle.easybangumi4.cartoon.entity.CartoonTagWrapper
import com.heyanle.easybangumi4.source_api.Source

@Composable
fun EasyDeleteDialog(
    show: Boolean,
    message: @Composable () -> Unit = { Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.delete_confirmation)) },
    onDelete: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    if (show) {
        AlertDialog(
            text = {
                message()
            },
            onDismissRequest = onDismissRequest,
            confirmButton = {
                TextButton(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onError,
                        contentColor = MaterialTheme.colorScheme.error

                    ),
                    onClick = {
                        onDelete()
                        onDismissRequest()
                    }) {
                    Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.delete))
                }
            },
            dismissButton = {
                TextButton(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onBackground

                    ),
                    onClick = {
                        onDismissRequest()
                    }
                ) {
                    Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun EasyClearDialog(
    show: Boolean,
    onDelete: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    if (show) {
        AlertDialog(
            text = {
                Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.clear_confirmation))
            },
            onDismissRequest = onDismissRequest,
            confirmButton = {
                TextButton(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onError,
                        contentColor = MaterialTheme.colorScheme.error

                    ),
                    onClick = {
                        onDelete()
                        onDismissRequest()
                    }) {
                    Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.clear))
                }
            },
            dismissButton = {
                TextButton(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onBackground

                    ),
                    onClick = {
                        onDismissRequest()
                    }
                ) {
                    Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun EasyMutiSelectionDialogStar(
    show: Boolean,
    items: List<CartoonTagWrapper>,
    initSelection: List<CartoonTagWrapper>,
    title: @Composable () -> Unit = {},
    message: @Composable () -> Unit = {},
    confirmText: String = stringResource(id = com.heyanle.easy_i18n.R.string.confirm),
    onConfirm: (List<CartoonTagWrapper>) -> Unit,
    onManage: ()->Unit,
    onDismissRequest: () -> Unit,
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
                            Text(text = key.label)

                        }
                    }
                }
            },
            onDismissRequest = onDismissRequest,
            confirmButton = {

                Row (
                    modifier = Modifier.fillMaxWidth()
                ){
                    TextButton(
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.onBackground
                        ),
                        onClick = {
                            onManage()
                            onDismissRequest()
                        }
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = stringResource(id = com.heyanle.easy_i18n.R.string.edit))
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.edit))
                    }

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
                        Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.cancel))
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

@Composable
fun EasyMutiSelectionDialog(
    show: Boolean,
    items: List<CartoonTag>,
    initSelection: List<CartoonTag>,
    title: @Composable () -> Unit = {},
    message: @Composable () -> Unit = {},
    confirmText: String = stringResource(id = com.heyanle.easy_i18n.R.string.confirm),
    onConfirm: (List<CartoonTag>) -> Unit,
    onManage: ()->Unit,
    onDismissRequest: () -> Unit,
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
                            Text(text = key.label)

                        }
                    }
                }
            },
            onDismissRequest = onDismissRequest,
            confirmButton = {

                Row (
                    modifier = Modifier.fillMaxWidth()
                ){
                    TextButton(
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.onBackground
                        ),
                        onClick = {
                            onManage()
                            onDismissRequest()
                        }
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = stringResource(id = com.heyanle.easy_i18n.R.string.edit))
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.edit))
                    }

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
                        Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.cancel))
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

@Composable
fun EasyMutiSelectionDialog(
    show: Boolean,
    items: List<Source>,
    initSelection: List<Source>,
    title: @Composable () -> Unit = {},
    message: @Composable () -> Unit = {},
    confirmText: String = stringResource(id = com.heyanle.easy_i18n.R.string.confirm),
    onConfirm: (List<Source>) -> Unit,
    onDismissRequest: () -> Unit,
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

                    LazyColumn {
                        items(items){ key ->
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
                                Text(text = key.label)

                            }
                        }
                    }
                }
            },
            onDismissRequest = onDismissRequest,
            confirmButton = {
                TextButton(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    onClick = {
                        onDismissRequest()
                    }
                ) {
                    Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.cancel))
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

            },
        )
    }

}


