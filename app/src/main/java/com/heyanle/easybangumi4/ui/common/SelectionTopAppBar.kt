package com.heyanle.easybangumi4.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlipToBack
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.heyanle.easy_i18n.R

/**
 * Created by HeYanLe on 2023/3/20 15:53.
 * https://github.com/heyanLE
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionTopAppBar(
    selectionItemsCount: Int,
    onExit: () -> Unit,
    onSelectAll: () -> Unit,
    onSelectInvert: () -> Unit,
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
                    imageVector = Icons.Filled.Close, stringResource(id = R.string.close)
                )
            }
        }, title = {
            Text(text = selectionItemsCount.toString())
        }, actions = {
            IconButton(onClick = {
                onSelectAll()
            }) {
                Icon(
                    imageVector = Icons.Filled.SelectAll,
                    stringResource(id = R.string.select_all)
                )
            }
            IconButton(onClick = {
                onSelectInvert()
            }) {
                Icon(
                    imageVector = Icons.Filled.FlipToBack,
                    stringResource(id = R.string.select_invert)
                )
            }



        })
}