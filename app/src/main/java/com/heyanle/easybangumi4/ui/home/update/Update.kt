package com.heyanle.easybangumi4.ui.home.update

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.db.entity.CartoonStar
import com.heyanle.easybangumi4.ui.common.LoadingImage
import com.heyanle.easybangumi4.ui.common.LoadingPage

/**
 * Created by HeYanLe on 2023/3/19 16:43.
 * https://github.com/heyanLE
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Update() {

    val vm = viewModel<UpdateViewModel>()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val state by vm.stateFlow.collectAsState()

    Column {
        UpdateTopAppBar(
            scrollBehavior = scrollBehavior,
            onUpdate = { vm.update(it) }
        )

        if(state.isLoading){
            LoadingPage(
                modifier = Modifier.fillMaxSize()
            )
        }else{

            LazyColumn(){
                item {
                    UpdateStateCard(state = state)
                }

                items(state.updateCartoonList) {

                }
            }
        }


    }
}

@Composable
fun UpdateCartoonCard(
    cartoonStar: CartoonStar,
    onClick: ()->Unit,
){
    Row {

    }

}

@Composable
fun UpdateStateCard(
    state: UpdateViewModel.State
) {

    var showErrorDialog by remember {
        mutableStateOf(false)
    }

    if (state.isLoading) {
        Row(
            modifier = Modifier
                .padding(4.dp)
                .background(MaterialTheme.colorScheme.primary)
                .clip(RoundedCornerShape(4.dp))
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LoadingImage()
            Spacer(modifier = Modifier.size(4.dp))
            Text(text = stringResource(id = R.string.doing_update))
        }
    } else if (state.lastUpdateError != null) {
        Row(
            modifier = Modifier
                .padding(4.dp)
                .background(MaterialTheme.colorScheme.errorContainer)
                .clip(RoundedCornerShape(4.dp))
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Error,
                contentDescription = stringResource(id = R.string.update_error)
            )
            Spacer(modifier = Modifier.size(4.dp))
            Text(text = stringResource(id = R.string.update_error))
        }
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text(text = stringResource(id = R.string.update_error)) },
            confirmButton = {
                TextButton(onClick = {
                    showErrorDialog = false
                }) {
                    Text(text = stringResource(id = R.string.confirm))
                }
            },
            text = {
                Text(text = state.lastUpdateError ?: "")
            }

        )
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior? = null,
    onUpdate: (Boolean) -> Unit,
) {

    var isMenuShow by remember {
        mutableStateOf(false)
    }

    TopAppBar(
        scrollBehavior = scrollBehavior,
        title = {
            Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.update))
        },
        actions = {
            IconButton(onClick = {
                onUpdate(false)
            }) {
                Icon(
                    imageVector = Icons.Filled.Update, stringResource(id = R.string.update)
                )
            }
            IconButton(onClick = {
                isMenuShow = true
            }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert, stringResource(id = R.string.more)
                )
            }


            DropdownMenu(expanded = isMenuShow, onDismissRequest = { isMenuShow = false }) {
                DropdownMenuItem(text = {
                    Text(text = stringResource(id = R.string.update_strict))
                }, onClick = {
                    onUpdate(true)
                })
            }


        }
    )

}