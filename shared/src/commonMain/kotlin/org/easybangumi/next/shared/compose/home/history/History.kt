package org.easybangumi.next.shared.compose.home.history

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import org.easybangumi.next.shared.LocalNavController
import org.easybangumi.next.shared.data.cartoon.CartoonInfo
import org.easybangumi.next.shared.foundation.FastScrollToTopFab
import org.easybangumi.next.shared.foundation.InnerBackHandler
import org.easybangumi.next.shared.foundation.cartoon.CartoonCoverCard
import org.easybangumi.next.shared.foundation.elements.EmptyElements
import org.easybangumi.next.shared.foundation.elements.LoadingElements
import org.easybangumi.next.shared.foundation.selection.SelectionTopAppBar
import org.easybangumi.next.shared.foundation.stringRes
import org.easybangumi.next.shared.foundation.view_model.vm
import org.easybangumi.next.shared.navigateToDetailOrMedia
import org.easybangumi.next.shared.playcon.TimeUtils
import org.easybangumi.next.shared.resources.Res

/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 */
@Composable
fun History(
    showBack: Boolean = false
) {

    val vm = vm(::HistoryVM)

    val nav = LocalNavController.current

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val state by vm.ui


    val focusRequester = remember {
        FocusRequester()
    }

    InnerBackHandler(
        enabled = state.selection.isNotEmpty()
    ) {
        vm.onSelectionExit()
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        AnimatedContent(targetState = state.selection.isNotEmpty(), label = "") { isSelectionMode ->
            if (isSelectionMode) {
                LaunchedEffect(key1 = Unit) {
                    kotlin.runCatching {
                        focusRequester.freeFocus()
                    }
                }

                SelectionTopAppBar(
                    selectionItemsCount = state.selection.size,
                    onExit = {
                        vm.onSelectionExit()
                    },
                    actions = {
                        IconButton(onClick = { vm.dialogDeleteSelection() }) {
                            Icon(
                                Icons.Filled.Delete, contentDescription = stringRes(
                                    Res.strings.delete
                                )
                            )
                        }
                    }
                )
            } else {
                HistoryTopAppBar(
                    scrollBehavior = scrollBehavior,
                    isSearch = state.searchKey != null,
                    focusRequester = focusRequester,
                    onSearchClick = {
                        vm.search("")
                    },
                    onClear = { vm.clearDialog() },
                    onSearch = {
                        vm.search(it)
                    },
                    text = state.searchKey ?: "",
                    onTextChange = {
                        vm.search(it)
                    },
                    onBack = if (showBack) {
                        {
                            nav.popBackStack()
                        }
                    } else null,
                    onSearchExit = {
                        vm.exitSearch()
                    }
                )
            }
        }

        HistoryList(
            scrollBehavior,
            vm = vm,
            state = state,
            onItemClick = {
                nav.navigateToDetailOrMedia(it)
            },

            onItemDelete = {
                vm.dialogDeleteOne(it)
            }
        )
    }
}

@Composable
fun HistoryList(
    scrollBehavior: TopAppBarScrollBehavior? = null,
    vm: HistoryVM,
    state: HistoryVM.HistoryState,
    onItemClick: (CartoonInfo) -> Unit,
    onItemDelete: (CartoonInfo) -> Unit,
) {

    val lazyListState = rememberLazyListState()


    val haptic = LocalHapticFeedback.current

    Box(modifier = Modifier.fillMaxSize()) {

        LazyColumn(

            modifier = Modifier
                .fillMaxSize()
                .run {
                    if (scrollBehavior != null) {
                        nestedScroll(scrollBehavior.nestedScrollConnection)
                    } else {
                        this
                    }
                },
            state = lazyListState,
            contentPadding = PaddingValues(0.dp, 0.dp, 0.dp, 96.dp)
        ) {
            if (state.isLoading) {
                item {
                    LoadingElements(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    )
                }
            } else if (state.history.isEmpty()) {
                item {
                    EmptyElements(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    )
                }
            } else {
                if (state.isInPrivate) {
                    item {
                        Text(
                            modifier = Modifier.padding(16.dp, 4.dp),
                            text = stringRes(Res.strings.now_in_private),
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic,
                        )
                    }
                }
                items(state.history) {
                    HistoryItem(
                        cartoonHistory = it,
                        onClick = {
                            if (state.selection.isEmpty()) {
                                onItemClick(it)
                            } else {
                                vm.onSelectionChange(it)
                            }
                        },
                        isSelect = state.selection.contains(it),
                        onDelete = onItemDelete,
                        onLongPress = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            vm.onSelectionLongPress(it)
                        },
                        isShowDelete = state.selection.isEmpty()
                    )
                }
            }


        }


        FastScrollToTopFab(listState = lazyListState)
    }

}

@Composable
fun HistoryItem(
    modifier: Modifier = Modifier,
    cartoonHistory: CartoonInfo,
    isSelect: Boolean,
    isShowDelete: Boolean,
    onClick: (CartoonInfo) -> Unit,
    onLongPress: (CartoonInfo) -> Unit,
    onDelete: (CartoonInfo) -> Unit,
) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp, 4.dp)
        .clip(RoundedCornerShape(8.dp))
        .run {
            if (isSelect) {
                background(MaterialTheme.colorScheme.primary)
            } else {
                this
            }
        }
        .combinedClickable(
            onClick = {
                onClick(cartoonHistory)
            },
            onLongClick = {
                onLongPress(cartoonHistory)
            }
        )

        .padding(8.dp)
        .then(modifier)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CartoonCoverCard(
                modifier = Modifier,
                model = cartoonHistory.coverUrl,
                name = cartoonHistory.name,
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = cartoonHistory.name,
                    maxLines = 2,
                    color = if (isSelect) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringRes(
                        Res.strings.last_episode_title,
                        cartoonHistory.lastEpisodeLabel
                    ),
                    maxLines = 1,
                    color = if (isSelect) MaterialTheme.colorScheme.onPrimary.copy(0.6f) else MaterialTheme.colorScheme.onBackground.copy(
                        0.6f
                    )
                )
                Text(
                    text = TimeUtils.toString(cartoonHistory.lastProcessTime).toString(),
                    maxLines = 1,
                    color = if (isSelect) MaterialTheme.colorScheme.onPrimary.copy(0.6f) else MaterialTheme.colorScheme.onBackground.copy(
                        0.6f
                    )
                )
            }
        }
        if (isShowDelete) {
            IconButton(
                modifier = Modifier.align(Alignment.BottomEnd),
                onClick = {
                    onDelete(cartoonHistory)
                }) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = stringRes(Res.strings.delete)
                )
            }
        }

    }
}


@Composable
fun HistoryTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior? = null,
    isSearch: Boolean,
    focusRequester: FocusRequester,
    text: String,
    onBack: (() -> Unit)? = null,
    onSearchClick: () -> Unit,
    onClear: () -> Unit,
    onSearch: (String) -> Unit,
    onTextChange: (String) -> Unit,
    onSearchExit: () -> Unit,
) {
    TopAppBar(
        scrollBehavior = scrollBehavior, navigationIcon = {
            if (isSearch) {
                IconButton(onClick = {
                    onSearchExit()
                }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack, stringRes(Res.strings.back)
                    )
                }
            } else {
                onBack?.let {
                    IconButton(onClick = {
                        it()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack, stringRes(Res.strings.back)
                        )
                    }
                }
            }

        }, title = {
            if (isSearch) {
                LaunchedEffect(key1 = Unit) {
                    focusRequester.requestFocus()
                }
                TextField(keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        onSearch(text)
                    }),
                    maxLines = 1,
                    modifier = Modifier.focusRequester(focusRequester),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                    ),
                    value = text,
                    onValueChange = {
                        onTextChange(it)
                    },
                    placeholder = {
                        Text(
                            style = MaterialTheme.typography.titleLarge,
                            text = stringRes(Res.strings.please_input_keyword_to_search)
                        )
                    })
            } else {
                Text(text = stringRes(Res.strings.history))
            }
        }, actions = {
            if (!isSearch) {
                IconButton(onClick = {
                    onSearchClick()
                }) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        stringRes(Res.strings.search)
                    )
                }
            } else if (text.isNotEmpty()) {
                IconButton(onClick = {
                    onTextChange("")
                }) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        stringRes(Res.strings.clear)
                    )
                }
            }

            IconButton(onClick = {
                onClear()
            }) {
                Icon(
                    imageVector = Icons.Filled.DeleteSweep,
                    stringRes(Res.strings.clear)
                )
            }

        })
}