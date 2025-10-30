package org.easybangumi.next.shared.foundation.selection

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlipToBack
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import org.easybangumi.next.shared.foundation.stringRes
import org.easybangumi.next.shared.resources.Res


@Composable
fun SelectionTopAppBar(
    selectionItemsCount: Int,
    onExit: () -> Unit,
    onSelectAll: (() -> Unit)? = null,
    onSelectInvert: (() -> Unit)? = null,
    actions: @Composable (RowScope.()->Unit)? = null,
) {

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
        ),
        navigationIcon = {
            IconButton(onClick = {
                onExit()
            }) {
                Icon(
                    imageVector = Icons.Filled.Close, stringRes(Res.strings.close)
                )
            }
        },
        title = {
            Text(text = selectionItemsCount.toString())
        }, actions = {
            if (onSelectAll != null) {
                IconButton(onClick = {
                    onSelectAll()
                }) {
                    Icon(
                        imageVector = Icons.Filled.SelectAll,
                        stringRes(Res.strings.select_all)
                    )
                }
            }

            if (onSelectInvert != null) {

                IconButton(onClick = {
                    onSelectInvert()
                }) {
                    Icon(
                        imageVector = Icons.Filled.FlipToBack,
                        stringRes(Res.strings.select_invert)
                    )
                }
            }

            actions?.invoke(this)

        })
}