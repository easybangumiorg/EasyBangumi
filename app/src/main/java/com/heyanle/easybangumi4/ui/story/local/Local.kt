package com.heyanle.easybangumi4.ui.story.local

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.cartoon.entity.CartoonStoryItem
import com.heyanle.easybangumi4.cartoon.story.local.source.LocalSource
import com.heyanle.easybangumi4.navigationDetailed
import com.heyanle.easybangumi4.source_api.entity.toIdentify
import com.heyanle.easybangumi4.ui.common.EasyDeleteDialog
import com.heyanle.easybangumi4.ui.common.FastScrollToTopFab
import com.heyanle.easybangumi4.ui.common.LoadingPage
import com.heyanle.easybangumi4.ui.common.OkImage
import com.heyanle.easybangumi4.ui.common.SelectionTopAppBar
import com.heyanle.easybangumi4.ui.main.star.CoverStarViewModel

/**
 * Created by heyanle on 2024/7/15.
 * https://github.com/heyanLE
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalTopAppBar(
    label: String? = null,
) {
    val nav = LocalNavController.current
    val vm = viewModel<LocalViewModel>()
    val state = vm.state.collectAsState()
    val sta = state.value
    val coverStarViewModel = viewModel<CoverStarViewModel>()
    val focusRequester = remember { FocusRequester() }
    if (sta.selection.isEmpty())
        TopAppBar(
            title = {
                LaunchedEffect(key1 = sta) {
                    if (sta.searchKey != null) {
                        try {
                            focusRequester.requestFocus()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }
                }

                BackHandler(sta.searchKey != null) {
                    vm.changeKey(null)
                }

                if (sta.searchKey != null) {
                    TextField(keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            vm.changeKey(sta.searchKey)
                        }),
                        maxLines = 1,
                        modifier = Modifier.focusRequester(focusRequester),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                        ),
                        value = sta.searchKey,
                        onValueChange = {
                            vm.changeKey(it)
                        },
                        placeholder = {
                            Text(
                                style = MaterialTheme.typography.titleLarge,
                                text = stringResource(id = R.string.please_input_keyword_to_search)
                            )
                        })
                } else {
                    Text(text = label ?: stringResource(id = R.string.local_download))
                }

            },
            navigationIcon = {
                IconButton(onClick = {
                    if (sta.searchKey == null)
                        nav.popBackStack()
                    else vm.changeKey(null)
                }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        stringResource(id = R.string.back)
                    )
                }
            },
            actions = {
                if (sta.searchKey == null) {
                    IconButton(onClick = {
                        vm.changeKey("")
                    }) {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = stringResource(id = com.heyanle.easy_i18n.R.string.search)
                        )
                    }
                }
                if (sta.searchKey != null) {
                    IconButton(onClick = {
                        vm.changeKey("")
                    }) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = stringResource(id = com.heyanle.easy_i18n.R.string.clear)
                        )
                    }
                }
            }
        )
    else {
        SelectionTopAppBar(
            selectionItemsCount = sta.selection.size,
            onExit = {
                vm.clearSelection()
            },
            actions = {
                IconButton(onClick = {
                    sta.selection.map { it.cartoonLocalItem.cartoonCover }.forEach {
                        coverStarViewModel.star(it)
                    }
                }) {
                    Icon(
                        Icons.Filled.Tag, contentDescription = stringResource(
                            id = R.string.tag_custom
                        )
                    )
                }
                IconButton(onClick = { vm.showDeleteDialog() }) {
                    Icon(
                        Icons.Filled.Delete, contentDescription = stringResource(
                            id = R.string.delete
                        )
                    )
                }
            }
        )
    }
}

@Composable
fun Local() {
    val nav = LocalNavController.current
    val vm = viewModel<LocalViewModel>()
    val state = vm.state.collectAsState()
    val sta = state.value
    val haptic = LocalHapticFeedback.current
    val lazyGridState = rememberLazyGridState()
    val coverStarViewModel = viewModel<CoverStarViewModel>()
    val star = coverStarViewModel.setFlow.collectAsState(initial = setOf<String>())
    Box {
        if (sta.loading) {
            LoadingPage(
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyVerticalGrid(
                modifier = Modifier
                    .fillMaxSize(),
                state = lazyGridState,
                columns = GridCells.Adaptive(100.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(4.dp, 4.dp, 4.dp, 88.dp)
            ) {
                items(sta.storyList) {

                    StoryItemCard(
                        storyItem = it,
                        isSelect = sta.selection.contains(it),
                        isStar = star.value.contains(it.cartoonLocalItem.cartoonCover.toIdentify()),
                        onClick = {
                            if (sta.selection.isEmpty()) {
                                nav.navigationDetailed(
                                    it.cartoonLocalItem.cartoonCover
                                )
                            } else {
                                vm.selectDownloadInfo(it)
                            }

                        },
                        onLongPress = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            vm.onSelectionLongPress(it)
                        }
                    )
                }
            }

            FastScrollToTopFab(listState = lazyGridState)
        }

    }

    val dialog = sta.dialog
    when(dialog){
        is LocalViewModel.Dialog.DeleteSelection -> {
            EasyDeleteDialog(show = true, onDelete = {
                vm.deleteDownload(dialog.selection)
                vm.dismissDialog()
            }) {
                vm.dismissDialog()
            }
        }
        else -> {

        }
    }


}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StoryItemCard(
    storyItem: CartoonStoryItem,
    isSelect: Boolean,
    isStar: Boolean,
    onClick: (CartoonStoryItem) -> Unit,
    onLongPress: ((CartoonStoryItem) -> Unit)? = null,
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .run {
                if (isSelect) {
                    background(MaterialTheme.colorScheme.primary)
                } else {
                    this
                }
            }
            .combinedClickable(
                onClick = {
                    onClick(storyItem)
                },
                onLongClick = {
                    onLongPress?.invoke(storyItem)
                }
            )
            .padding(4.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(19 / 27F)
                .clip(RoundedCornerShape(4.dp)),
        ) {
            OkImage(
                modifier = Modifier.fillMaxSize(),
                image = storyItem.cartoonLocalItem.cartoonCover.coverUrl ?: "",
                contentDescription = storyItem.cartoonLocalItem.title,
                errorRes = com.heyanle.easybangumi4.R.drawable.placeholder,
            )
            if (isStar) {
                Text(
                    fontSize = 13.sp,
                    text = stringResource(id = com.heyanle.easy_i18n.R.string.stared_min),
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(0.dp, 0.dp, 4.dp, 0.dp)
                        )
                        .padding(4.dp, 0.dp)
                )
            }
            Text(
                fontSize = 13.sp,
                text = LocalSource.label,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(0.dp, 4.dp, 0.dp, 0.dp)
                    )
                    .padding(4.dp, 0.dp)
            )
            Text(
                fontSize = 13.sp,
                text = "${storyItem.cartoonLocalItem.episodes.size}",
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(0.dp, 0.dp, 0.dp, 4.dp)
                    )
                    .padding(4.dp, 0.dp)
            )

        }


        Spacer(modifier = Modifier.size(4.dp))
        Text(
            style = MaterialTheme.typography.bodySmall,
            text = storyItem.cartoonLocalItem.title,
            maxLines = 2,
            textAlign = TextAlign.Start,
            overflow = TextOverflow.Ellipsis,
            color = if (isSelect) MaterialTheme.colorScheme.onPrimary else Color.Unspecified
        )
        Spacer(modifier = Modifier.size(4.dp))
    }

}