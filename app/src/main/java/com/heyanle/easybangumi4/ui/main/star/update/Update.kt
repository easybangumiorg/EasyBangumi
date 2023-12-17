package com.heyanle.easybangumi4.ui.main.star.update

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.ListItem
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.old.entity.CartoonStar
import com.heyanle.easybangumi4.ui.common.EmptyPage
import com.heyanle.easybangumi4.ui.common.LoadingImage
import com.heyanle.easybangumi4.ui.common.LoadingPage
import com.heyanle.easybangumi4.ui.common.OkImage
import com.heyanle.easybangumi4.navigationDetailed
import com.heyanle.easybangumi4.source.LocalSourceBundleController
import java.text.DateFormat

/**
 * Created by HeYanLe on 2023/3/19 16:43.
 * https://github.com/heyanLE
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Update(
    vm: UpdateViewModel = viewModel<UpdateViewModel>()
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val state by vm.stateFlow.collectAsState()

    val nav = LocalNavController.current

    Column {

        if (state.isLoading) {
            LoadingPage(
                modifier = Modifier.weight(1f)
            )
        } else {

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
            ) {
                item {
                    UpdateStateCard(state = state)
                }

                item {
                    UpdateStateTime(state = state)
                }

                items(state.updateCartoonList) {
                    when (it) {
                        is UpdateViewModel.UpdateItem.Cartoon -> {
                            UpdateCartoonCard(cartoonStar = it.star, onClick = {
                                nav.navigationDetailed(it.id, it.url, it.source)
                            })
                        }

                        is UpdateViewModel.UpdateItem.Header -> {
                            ListItem(
                                headlineContent = {
                                    Text(text = it.header)
                                }
                            )

                        }
                    }

                }

                if(state.updateCartoonList.isEmpty()){
                    item {
                        EmptyPage(
                            modifier = Modifier.fillMaxWidth().padding(top = 128.dp),
                            emptyMsg = stringResource(id = R.string.empty_update)
                        )
                    }

                }
            }
        }


    }
}

@Composable
fun UpdateCartoonCard(
    cartoonStar: CartoonInfo,
    onClick: (CartoonInfo) -> Unit,
) {

    val sourceBundle = LocalSourceBundleController.current

    ListItem(
        modifier = Modifier.clickable {
            onClick(cartoonStar)
        },
        headlineContent = {
            Text(text = cartoonStar.name, maxLines = 2, overflow = TextOverflow.Ellipsis)
        },
        trailingContent = {
            Text(
                fontSize = 13.sp,
                text = sourceBundle.source(cartoonStar.source)?.label
                    ?: cartoonStar.source,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(4.dp)
                    )
                    .padding(4.dp),
            )
        },
        leadingContent = {
            OkImage(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(4.dp)),
                image = cartoonStar.coverUrl,
                contentDescription = cartoonStar.name
            )
        }
    )

}

@Composable
fun UpdateStateCard(
    state: UpdateViewModel.State
) {

    var showErrorDialog by remember {
        mutableStateOf(false)
    }

    if (state.isUpdating) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 0.dp)
                .clip(RoundedCornerShape(8.dp))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LoadingImage(modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.size(8.dp))
            Text(text = stringResource(id = R.string.doing_update),)
        }
    } else if (state.lastUpdateError != null) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 0.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.errorContainer)

                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Error,
                contentDescription = stringResource(id = R.string.update_error)
            )
            Spacer(modifier = Modifier.size(4.dp))
            Text(text = stringResource(id = R.string.update_error), color = MaterialTheme.colorScheme.onErrorContainer)
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

@Composable
fun UpdateStateTime(
    state: UpdateViewModel.State
) {
    if (state.lastUpdateTime > 0 && ! state.isUpdating) {
        Text(
            modifier = Modifier.padding(16.dp, 4.dp),
            text = stringResource(
                id = R.string.last_update_at,
                DateFormat.getDateInstance(
                    DateFormat.MEDIUM
                ).format(state.lastUpdateTime)
            ),
            style = MaterialTheme.typography.bodySmall,
            fontStyle = FontStyle.Italic,
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
                    isMenuShow = false
                })
            }


        }
    )

}