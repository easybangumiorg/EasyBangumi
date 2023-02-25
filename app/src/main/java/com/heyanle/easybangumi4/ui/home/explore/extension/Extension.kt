package com.heyanle.easybangumi4.ui.home.explore.extension

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.heyanle.easybangumi4.ui.common.ExtensionContainer
import com.heyanle.easybangumi4.ui.common.OkImage
import com.heyanle.extension_load.ExtensionController
import com.heyanle.extension_load.model.Extension

/**
 * Created by HeYanLe on 2023/2/21 23:33.
 * https://github.com/heyanLE
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtensionTopAppBar(behavior: TopAppBarScrollBehavior){
    TopAppBar(
        title = { Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.explore)) },
        scrollBehavior = behavior
    )
}
@Composable
fun Extension (){
    ExtensionContainer(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()){
            items(it){ extension ->
                ExtensionItem(extension = extension, onClick = {

                }, onAction = {

                })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtensionItem (
    extension: Extension,
    onClick: (Extension)->Unit,
    onAction: (Extension)->Unit,
){

    ListItem(
        modifier = Modifier.clickable {
            onClick(extension)
        },
        headlineText = {
            Text(text = extension.label)
        },
        supportingText = {
            Text(
                text = extension.versionName,
            )
        },
        trailingContent = {
            when(extension){
                is Extension.Installed -> {
                    TextButton(onClick = {
                        onAction(extension)
                    }) {
                        Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.setting))
                    }
                }
                is Extension.InstallError -> {
                    TextButton(
                        enabled = false,
                        onClick = {

                        }) {
                        Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.loading_error))
                    }
                }
            }
        },
        leadingContent = {
            OkImage(
                modifier = Modifier.size(40.dp),
                image = extension.icon,
                contentDescription = extension.label
            )
        }
    )

}