package com.heyanle.easybangumi4.ui.cartoon_play

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.STORY
import com.heyanle.easybangumi4.base.DataResult
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.entity.PlayLineWrapper
import com.heyanle.easybangumi4.source_api.entity.Episode
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.CartoonDownloadReqModel
import com.heyanle.easybangumi4.ui.common.CartoonCardWithCover
import com.heyanle.easybangumi4.ui.common.ErrorPage
import com.heyanle.easybangumi4.ui.common.LoadingImage
import com.heyanle.easybangumi4.ui.common.LoadingPage
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.easybangumi4.utils.toast

/**
 * Created by heyanle on 2024/7/8.
 * https://github.com/heyanLE
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CartoonDownloadDialog(
    cartoonInfo: CartoonInfo,
    playerLineWrapper: PlayLineWrapper,
    episodes: List<Episode>,
    onDismissRequest: () -> Unit,
) {


    val model = remember {
        CartoonDownloadReqModel(
            cartoonInfo = cartoonInfo,
            playerLineWrapper = playerLineWrapper,
            episodes = episodes
        )
    }

    val state = model.state.collectAsState()
    val sta = state.value
    val dialog = sta.dialog

    val focusRequester = remember { FocusRequester() }

    BackHandler {
        if (sta.keyword == null)
            onDismissRequest()
        else
            model.changeKeyword(null)
    }

    Column(
        modifier = Modifier
            .systemBarsPadding()
            .background(Color.Black.copy(0.6f))
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.background)
    ) {

        TopAppBar(
            navigationIcon = {
                IconButton(onClick = {
                    if (sta.keyword == null)
                        if (sta.targetLocalInfo == null)
                            onDismissRequest()
                        else model.targetLocalItem(null)
                    else
                        model.changeKeyword(null)
                }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "back")
                }

            },
            title = {
                LaunchedEffect(key1 = sta) {
                    if (sta.keyword != null && sta.targetLocalInfo == null) {
                        try {
                            focusRequester.requestFocus()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }
                }
                if (sta.keyword != null && sta.targetLocalInfo == null) {
                    TextField(keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            model.changeKeyword(sta.keyword)
                        }),
                        maxLines = 1,
                        modifier = Modifier.focusRequester(focusRequester),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                        ),
                        value = sta.keyword,
                        onValueChange = {
                            model.changeKeyword(it)
                        },
                        placeholder = {
                            Text(
                                style = MaterialTheme.typography.titleLarge,
                                text = stringResource(id = com.heyanle.easy_i18n.R.string.please_input_keyword_to_search)
                            )
                        })
                } else {
                    Text(text = stringResource(id = if (sta.targetLocalInfo == null) com.heyanle.easy_i18n.R.string.download_to else com.heyanle.easy_i18n.R.string.edit_episode_msg))

                }
            },
            actions = {
                if (sta.keyword == null && sta.targetLocalInfo == null) {
                    IconButton(onClick = {
                        model.changeKeyword("")
                    }) {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = stringResource(id = com.heyanle.easy_i18n.R.string.search)
                        )
                    }
                }
                if (sta.keyword != null && sta.targetLocalInfo == null) {
                    IconButton(onClick = {
                        model.changeKeyword("")
                    }) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = stringResource(id = com.heyanle.easy_i18n.R.string.clear)
                        )
                    }
                }
                if (sta.keyword == null) {
                    IconButton(onClick = {
                        // TODO
                    }) {
                        Icon(
                            Icons.Filled.Help,
                            contentDescription = stringResource(id = com.heyanle.easy_i18n.R.string.help)
                        )
                    }

                }
            }
        )

        val sl = sta.storyList

        if (sl is DataResult.Loading) {
            LoadingPage(
                modifier = Modifier.fillMaxSize()
            )
        } else if (sl is DataResult.Error) {
            ErrorPage(
                modifier = Modifier.fillMaxSize(),
                errorMsg = sl.errorMsg,
                other = {
                    Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.click_to_retry))
                },
                clickEnable = true,
                onClick = {
                    model.retry()
                }
            )
        } else if (sta.targetLocalInfo == null || sta.reqList.isEmpty()) {
            LazyVerticalGrid(
                modifier = Modifier
                    .fillMaxSize(),
                columns = GridCells.Adaptive(100.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                contentPadding = PaddingValues(4.dp, 4.dp, 4.dp, 88.dp)
            ) {
                items(sta.localWithKeyword.size + 1) {
                    if (it == 0) {
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable {
                                    model.showNewLocalDialog()
                                }
                                .padding(4.dp),
                            horizontalAlignment = Alignment.Start,
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(19 / 27F)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Add,
                                    contentDescription = stringResource(id = com.heyanle.easy_i18n.R.string.click_to_add)
                                )
                            }

                            Spacer(modifier = Modifier.size(4.dp))
                            Text(
                                style = MaterialTheme.typography.bodySmall,
                                text = stringResource(id = com.heyanle.easy_i18n.R.string.new_local_cartoon),
                                maxLines = 4,
                                textAlign = TextAlign.Start,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(modifier = Modifier.size(4.dp))
                        }

                    } else {
                        val local = sta.localWithKeyword[it - 1]
                        CartoonCardWithCover(
                            cartoonCover = local.cartoonLocalItem.cartoonCover,
                            onClick = {
                                model.targetLocalItem(local)
                            }
                        )
                    }

                }
            }
        } else {
            val nav = LocalNavController.current
            if (sta.isRepeat) {
                ListItem(headlineContent = {
                    Text(
                        text = stringResource(id = R.string.episode_repeat),
                        color = MaterialTheme.colorScheme.error
                    )
                })
            } else {
                ListItem(
                    headlineContent = {
                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    model.changeQuickMode(!sta.isQuickMode)
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                modifier = Modifier.padding(0.dp), checked = sta.isQuickMode, onCheckedChange = {
                                    model.changeQuickMode(it)
                                })
                            Text(text = stringResource(id = R.string.quick_download_mode))
                            Spacer(modifier = Modifier.size(16.dp))
                        }
                    },
                    trailingContent = {
                        FilledTonalButton(
                            modifier = Modifier
                                .padding(16.dp, 0.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            onClick = {
                                onDismissRequest()
                                model.pushReq(sta)
                                stringRes(R.string.add_download_completely).moeSnackBar(
                                    confirmLabel = stringRes(R.string.click_to_view),
                                    onConfirm = {
                                        nav.navigate(STORY)
                                    }
                                )
                            }) {
                            Text(stringResource(id = R.string.next))

                        }
                    }

                )

            }
            Divider()
            LazyColumn(
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(4.dp, 4.dp, 4.dp, 88.dp)
            ) {
                items(
                    sta.reqList
                ) { req ->
                    val repeat = remember(sta) {
                        sta.cantEpisodeSet.contains(req.toEpisode) || sta.reqList.any {
                            it != req && (it.toEpisode == req.toEpisode)
                        }
                    }
                    ListItem(
                        modifier = Modifier.clickable {
                            model.showChangeEpisode(req.uuid, req.toEpisodeTitle, req.toEpisode)

                        },
                        leadingContent = {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (repeat) MaterialTheme.colorScheme.error else
                                            MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable {
                                        model.showChangeEpisode(
                                            req.uuid,
                                            req.toEpisodeTitle,
                                            req.toEpisode
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = req.toEpisode.toString(),
                                    color = if (repeat) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        },
                        headlineContent = {
                            Text(text = req.toEpisodeTitle)
                        }
                    )
                }
            }


        }
    }

    when (dialog) {
        is CartoonDownloadReqModel.Dialog.NewLocalReqWithTitle -> {
            val textLabel = remember {
                mutableStateOf(dialog.localMsg.itemId)
            }
            val fq = remember { FocusRequester() }
            LaunchedEffect(key1 = Unit){
                try {
                    fq.requestFocus()
                }catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            AlertDialog(
                onDismissRequest = { model.dismissDialog() },
                title = {
                    Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.new_local_cartoon))
                },
                text = {
                    TextField(
                        value = textLabel.value,
                        onValueChange = {
                            textLabel.value = it
                        },
                        label = {
                            Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.episode_label))
                        },
                        isError = textLabel.value.isNotEmpty() && sta.storyList.okOrNull()?.any { it.cartoonLocalItem.itemId == textLabel.value } == true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)

                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        val repeat = textLabel.value.isNotEmpty() && sta.storyList.okOrNull()?.any { it.cartoonLocalItem.itemId == textLabel.value } == true
                        if (repeat) {
                            stringRes(R.string.local_cartoon_name_repeat_reenter).toast()
                            return@TextButton
                        }
                        model.addLocalCartoon(dialog.localMsg.copy(itemId = textLabel.value, title = textLabel.value))
                    }) {
                        Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.confirm))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { model.dismissDialog() }) {
                        Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.cancel))
                    }
                }
            )
        }
        is CartoonDownloadReqModel.Dialog.NewLocalReq -> {
            val isRepeatName = sta.storyList.okOrNull()?.any { it.cartoonLocalItem.itemId == dialog.localMsg.itemId } == true
            AlertDialog(
                onDismissRequest = { model.dismissDialog() },
                title = {
                    Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.new_local_cartoon))
                },
                text = {
                    if (!isRepeatName) {
                        Text(text = stringResource(id = R.string.create_local_cartoon_from_current))
                    } else {
                        Text(text = stringResource(id = R.string.create_local_cartoon_from_current_name_repeat))
                    }
                },
                confirmButton = {
                    if (!isRepeatName) {
                        TextButton(onClick = {
                            model.addLocalCartoon(dialog.localMsg)
                        }) {
                            Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.confirm))
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            TextButton(onClick = {
                                model.addLocalCartoon(dialog.localMsg, true)
                            }) {
                                Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.refresh_local_cartoon))
                            }
                            TextButton(onClick = {
                                model.showNewLocalDialogWithTitle()
                            }) {
                                Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.new_local_cartoon_name))
                            }
                            TextButton(onClick = { model.dismissDialog() }) {
                                Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.cancel))
                            }

                        }
                    }
                },
            )
        }
        is CartoonDownloadReqModel.Dialog.LoadingNewLocal -> {
            AlertDialog(
                onDismissRequest = {
                    //cartoonRecordedTaskModel.onDismissRequest()
                },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LoadingImage()
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = stringResource(id = R.string.local_cartoon_creating))
                    }
                },
                confirmButton = {}
            )
        }
        is CartoonDownloadReqModel.Dialog.ChangeEpisode -> {

            val textEpisode = remember {
                mutableStateOf(dialog.episode.toString())
            }
            val textLabel = remember {
                mutableStateOf(dialog.title.toString())
            }
            val fq = remember { FocusRequester() }
            LaunchedEffect(key1 = Unit){
                try {
                    fq.requestFocus()
                }catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            AlertDialog(
                onDismissRequest = { model.dismissDialog() },
                title = {
                    Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.edit_episode_msg))
                },
                text = {

                    Column {
                        TextField(
                            modifier = Modifier.focusRequester(fq),
                            value = textEpisode.value,
                            onValueChange = {
                                textEpisode.value = it
                            },
                            label = {
                                Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.episode))
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)

                        )
                        TextField(
                            value = textLabel.value,
                            onValueChange = {
                                textLabel.value = it
                            },
                            label = {
                                Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.episode_label))
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)

                        )

                    }



                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val i = textEpisode.value.toIntOrNull()
                            if (i == null){
                                stringRes(R.string.please_input_right_speed).toast()
                                return@TextButton
                            }
                            if (sta.cantEpisodeSet.contains(i)) {
                                stringRes(R.string.episode_repeat).toast()
                                return@TextButton
                            }
                            if (textLabel.value.isEmpty()) {
                                stringRes(R.string.label_empty).toast()
                                return@TextButton
                            }
                            model.dismissDialog()
                            model.changeReq(
                                dialog.uuid,
                                textLabel.value,
                                i
                            )
                        }
                    ) {
                        Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.confirm))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { model.dismissDialog() }) {
                        Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.cancel))
                    }
                }
            )
        }

        else -> {}
    }
}