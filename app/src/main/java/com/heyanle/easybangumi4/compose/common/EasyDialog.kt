package com.heyanle.easybangumi4.compose.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource

@Composable
fun EasyDeleteDialog(
    show: Boolean,
    message: @Composable ()->Unit = {Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.delete_confirmation))},
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