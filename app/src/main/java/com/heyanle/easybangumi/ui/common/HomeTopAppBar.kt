package com.heyanle.easybangumi.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.heyanle.easybangumi.R

/**
 * Created by HeYanLe on 2023/1/7 21:12.
 * https://github.com/heyanLE
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopAppBar(
    scrollBehavior : TopAppBarScrollBehavior,
    label: @Composable ()->Unit,
    onSearch: (()->Unit)? = null,
    isShowSearch: Boolean = false,
){
    TopAppBar(
        scrollBehavior = scrollBehavior,
        title = label,
        colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        actions = {
            if(isShowSearch){
                IconButton(onClick = {
                    onSearch?.invoke()
                }) {
                    Icon(Icons.Filled.Search, contentDescription = stringResource(id = R.string.search))
                }
            }
        }
    )


}