package com.heyanle.easybangumi4.ui.home.history

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.db.entity.CartoonHistory
import com.heyanle.easybangumi4.navigationDetailed
import com.heyanle.easybangumi4.source.SourceMaster
import com.heyanle.easybangumi4.ui.common.CartoonCard
import com.heyanle.easybangumi4.ui.common.EasyClearDialog
import com.heyanle.easybangumi4.ui.common.EasyDeleteDialog
import com.heyanle.easybangumi4.ui.common.FastScrollToTopFab
import com.heyanle.easybangumi4.ui.common.pagingCommon
import com.heyanle.easybangumi4.ui.common.player.utils.TimeUtils

/**
 * Created by HeYanLe on 2023/3/16 22:11.
 * https://github.com/heyanLE
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun History() {

    val vm = viewModel<HistoryViewModel>()

    var isSearch by remember {
        mutableStateOf(false)
    }

    var clearDialog by remember {
        mutableStateOf(false)
    }

    var deleteHistory by remember {
        mutableStateOf<CartoonHistory?>(null)
    }

    val nav = LocalNavController.current

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            HistoryTopAppBar(
                scrollBehavior = scrollBehavior,
                isSearch = isSearch,
                onSearchClick = {
                    isSearch = true
                },
                onClear = { clearDialog = true },
                onSearch = {
                    vm.search(it)
                },
                onSearchExit = {
                    isSearch = false
                    vm.exitSearch()

                }
            )
        }
    ) {
        Box(modifier = Modifier.padding(it)){
            HistoryList(
                scrollBehavior,
                vm = vm,
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
                    deleteHistory = it
                }
            )
        }
    }

//    Column {
//        HistoryTopAppBar(
//            scrollBehavior = scrollBehavior,
//            isSearch = isSearch,
//            onSearchClick = {
//                isSearch = true
//            },
//            onClear = { clearDialog = true },
//            onSearch = {
//                vm.search(it)
//            },
//            onSearchExit = {
//                isSearch = false
//
//            }
//        )
//
//        HistoryList(
//            scrollBehavior,
//            vm = vm,
//            onItemClick = {
//                nav.navigationDetailed(
//                    it.id,
//                    it.url,
//                    it.source,
//                    it.lastLinesIndex,
//                    it.lastEpisodeIndex,
//                    it.lastProcessTime
//                )
//            },
//            onItemDelete = {
//                deleteHistory = it
//            }
//        )
//
//    }

    EasyDeleteDialog(
        show = deleteHistory != null,
        onDelete = { deleteHistory?.let(vm::delete) },
        onDismissRequest = {
            deleteHistory = null
        }
    )
    EasyClearDialog(
        show = clearDialog,
        onDelete = { vm.clear() },
        onDismissRequest = { clearDialog = false }
    )
    vm.curPager.value.collectAsLazyPagingItems()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryList(
    scrollBehavior: TopAppBarScrollBehavior? = null,
    vm: HistoryViewModel = viewModel<HistoryViewModel>(),
    onItemClick: (CartoonHistory) -> Unit,
    onItemDelete: (CartoonHistory) -> Unit,
) {

    val lazyListState = rememberLazyListState()

    val flow = remember(vm.searchPager.value, vm.curPager.value) {
        vm.searchPager.value ?: vm.curPager.value
    }

    val lazyPagingItems = flow.collectAsLazyPagingItems()


    Box(modifier = Modifier.fillMaxSize()) {

        LazyColumn(
            modifier = Modifier.run {
                if (scrollBehavior != null) {
                    nestedScroll(scrollBehavior.nestedScrollConnection)
                } else {
                    this
                }
            },
            contentPadding = PaddingValues(0.dp, 0.dp, 0.dp, 96.dp)
        ) {

            items(lazyPagingItems) {
                it?.let {
                    HistoryItem(cartoonHistory = it, onClick = onItemClick, onDelete = onItemDelete)
                }
            }

            pagingCommon(lazyPagingItems)
        }

        FastScrollToTopFab(listState = lazyListState)
    }

}

@Composable
fun HistoryItem(
    modifier: Modifier = Modifier,
    cartoonHistory: CartoonHistory,
    onClick: (CartoonHistory) -> Unit,
    onDelete: (CartoonHistory) -> Unit,
) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .clickable {
            onClick(cartoonHistory)
        }
        .padding(16.dp, 8.dp)
        .then(modifier)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CartoonCard(
                cover = cartoonHistory.cover,
                name = cartoonHistory.name,
                source = SourceMaster.animSourceFlow.value.source(cartoonHistory.source)?.label
                    ?: cartoonHistory.source
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = cartoonHistory.name,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(
                        id = R.string.last_episode_title,
                        cartoonHistory.lastEpisodeTitle
                    ),
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onBackground.copy(0.6f)
                )
                Text(
                    text = TimeUtils.toString(cartoonHistory.lastProcessTime).toString(),
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onBackground.copy(0.6f)
                )
            }
        }
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior? = null,
    isSearch: Boolean,
    onSearchClick: () -> Unit,
    onClear: () -> Unit,
    onSearch: (String) -> Unit,
    onSearchExit: () -> Unit,
) {

    var text by remember {
        mutableStateOf("")
    }

    val focusRequester = remember {
        FocusRequester()
    }

    TopAppBar(
        scrollBehavior = scrollBehavior, navigationIcon = {
            if (isSearch) {
                IconButton(onClick = {
                    onSearchExit()
                }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack, stringResource(id = R.string.back)
                    )
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
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                    ),
                    value = text,
                    onValueChange = {
                        text = it
                        onSearch(text)
                    },
                    placeholder = {
                        Text(
                            style = MaterialTheme.typography.titleLarge,
                            text = stringResource(id = R.string.please_input_keyword_to_search)
                        )
                    })
            } else {
                Text(text = stringResource(id = R.string.mine_history))
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
                    text = ""
                    onSearch("")
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