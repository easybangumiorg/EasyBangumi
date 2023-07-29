package com.heyanle.easybangumi4.ui.history

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.base.entity.CartoonHistory
import com.heyanle.easybangumi4.navigationDetailed
import com.heyanle.easybangumi4.source.LocalSourceBundleController
import com.heyanle.easybangumi4.ui.common.CartoonCard
import com.heyanle.easybangumi4.ui.common.EasyClearDialog
import com.heyanle.easybangumi4.ui.common.EasyDeleteDialog
import com.heyanle.easybangumi4.ui.common.FastScrollToTopFab
import com.heyanle.easybangumi4.ui.common.PagingCommon
import com.heyanle.easybangumi4.ui.common.SelectionTopAppBar
import com.heyanle.easybangumi4.ui.common.pagingCommon
import loli.ball.easyplayer2.utils.TimeUtils

/**
 * Created by HeYanLe on 2023/3/16 22:11.
 * https://github.com/heyanLE
 */

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun History() {
    val vm = viewModel<HistoryViewModel>()

    val nav = LocalNavController.current

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val state by vm.stateFlow.collectAsState()

    val focusRequester = remember {
        FocusRequester()
    }
//    LaunchedEffect(key1 = Unit){
//        scrollBehavior.state.contentOffset = 0F
//    }

    BackHandler(
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
                                Icons.Filled.Delete, contentDescription = stringResource(
                                    id = R.string.delete
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
                    onBack = {
                        nav.popBackStack()
                    },
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
                nav.navigationDetailed(
                    it.id,
                    it.url,
                    it.source,
                    it.lastLinesIndex,
                    it.lastEpisodeIndex,
                    it.lastProcessTime
                )
            },

            onItemDelete = {
                vm.dialogDeleteOne(it)
            }
        )
    }


    val deleteDialog = state.dialog as? HistoryViewModel.Dialog.Delete
    EasyDeleteDialog(
        show = deleteDialog != null,
        onDelete = {
            deleteDialog
                ?.let {
                    vm.delete(it.selection.toList())
                }
            vm.dialogDismiss()
        },
        onDismissRequest = {
            vm.dialogDismiss()
        }
    )

    val clearDialog = state.dialog as? HistoryViewModel.Dialog.Clear
    EasyClearDialog(
        show = clearDialog != null,
        onDelete = {
            vm.clear()
            vm.dialogDismiss()
        },
        onDismissRequest = { vm.dialogDismiss() }
    )


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryList(
    scrollBehavior: TopAppBarScrollBehavior? = null,
    vm: HistoryViewModel = viewModel<HistoryViewModel>(),
    state: HistoryViewModel.HistoryState,
    onItemClick: (CartoonHistory) -> Unit,
    onItemDelete: (CartoonHistory) -> Unit,
) {

    val lazyListState = rememberLazyListState()


    val flow = state.pager
    val lazyPagingItems = flow.collectAsLazyPagingItems()

    val haptic = LocalHapticFeedback.current

    Box(modifier = Modifier.fillMaxSize()) {

        if (lazyPagingItems.itemCount > 0) {
            LazyColumn(

                modifier = Modifier.fillMaxSize().run {
                    if (scrollBehavior != null) {
                        nestedScroll(scrollBehavior.nestedScrollConnection)
                    } else {
                        this
                    }
                },
                state = lazyListState,
                contentPadding = PaddingValues(0.dp, 0.dp, 0.dp, 96.dp)
            ) {

                items(lazyPagingItems.itemCount){
                    lazyPagingItems[it]?.let { history ->
                        HistoryItem(
                            cartoonHistory = history,
                            onClick = {
                                if (state.selection.isEmpty()) {
                                    onItemClick(it)
                                } else {
                                    vm.onSelectionChange(it)
                                }
                            },
                            isSelect = state.selection.contains(history),
                            onDelete = onItemDelete,
                            onLongPress = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                vm.onSelectionChange(it)
                            },
                            isShowDelete = state.selection.isEmpty()
                        )
                    }

                }
                pagingCommon(lazyPagingItems)


            }
        }
        PagingCommon(items = lazyPagingItems)


        FastScrollToTopFab(listState = lazyListState)
    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryItem(
    modifier: Modifier = Modifier,
    cartoonHistory: CartoonHistory,
    isSelect: Boolean,
    isShowDelete: Boolean,
    onClick: (CartoonHistory) -> Unit,
    onLongPress: (CartoonHistory) -> Unit,
    onDelete: (CartoonHistory) -> Unit,
) {
    val sourceBundle = LocalSourceBundleController.current
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

            CartoonCard(
                cover = cartoonHistory.cover,
                name = cartoonHistory.name,
                source = sourceBundle.source(cartoonHistory.source)?.label
                    ?: cartoonHistory.source
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
                    text = stringResource(
                        id = R.string.last_episode_title,
                        cartoonHistory.lastEpisodeTitle
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
                    contentDescription = stringResource(id = R.string.delete)
                )
            }
        }

    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior? = null,
    isSearch: Boolean,
    focusRequester: FocusRequester,
    text: String,
    onBack: () -> Unit,
    onSearchClick: () -> Unit,
    onClear: () -> Unit,
    onSearch: (String) -> Unit,
    onTextChange: (String) -> Unit,
    onSearchExit: () -> Unit,
) {
    TopAppBar(
        scrollBehavior = scrollBehavior, navigationIcon = {
            IconButton(onClick = {
                if (isSearch) {
                    onSearchExit()
                } else {
                    onBack()
                }

            }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack, stringResource(id = R.string.back)
                )
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
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.Transparent,
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
                            text = stringResource(id = R.string.please_input_keyword_to_search)
                        )
                    })
            } else {
                Text(text = stringResource(id = R.string.history))
            }
        }, actions = {
            if (!isSearch) {
                IconButton(onClick = {
                    onSearchClick()
                }) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        stringResource(id = com.heyanle.easy_i18n.R.string.search)
                    )
                }
            } else if (text.isNotEmpty()) {
                IconButton(onClick = {
                    onTextChange("")
                }) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        stringResource(id = com.heyanle.easy_i18n.R.string.clear)
                    )
                }
            }

            IconButton(onClick = {
                onClear()
            }) {
                Icon(
                    imageVector = Icons.Filled.DeleteSweep,
                    stringResource(id = com.heyanle.easy_i18n.R.string.clear)
                )
            }

        })
}