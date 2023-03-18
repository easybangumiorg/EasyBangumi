package com.heyanle.easybangumi4.ui.home.star

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
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
import androidx.compose.ui.Modifier
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
import com.heyanle.easybangumi4.db.entity.CartoonStar
import com.heyanle.easybangumi4.navigationDetailed
import com.heyanle.easybangumi4.ui.common.CartoonStarCardWithCover
import com.heyanle.easybangumi4.ui.common.EasyDeleteDialog
import com.heyanle.easybangumi4.ui.common.FastScrollToTopFab
import com.heyanle.easybangumi4.ui.common.pagingCommon

/**
 * Created by HeYanLe on 2023/3/18 17:04.
 * https://github.com/heyanLE
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Star() {
    val vm = viewModel<StarViewModel>()

    var isSearch by remember {
        mutableStateOf(false)
    }


    var deleteStar by remember {
        mutableStateOf<CartoonStar?>(null)
    }

    val nav = LocalNavController.current

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    LaunchedEffect(key1 = Unit) {
        vm.refreshNum()
        scrollBehavior.state.contentOffset = 0F
    }


    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            StarTopAppBar(
                scrollBehavior = scrollBehavior,
                isSearch = isSearch,
                starNum = vm.starNum,
                onSearchClick = { isSearch = true },
                onFilter = { com.heyanle.easybangumi4.utils.TODO("过滤器") },
                onSearch = {
                    vm.search(it)
                },
                onSearchExit = {
                    isSearch = false
                }
            )
        }
    ) {
        Box(modifier = Modifier.padding(it)) {
            StarList(scrollBehavior, vm = vm, onItemClick = {
                nav.navigationDetailed(
                    it.id,
                    it.url,
                    it.source,
                )
            }, onItemLongPress = {
                deleteStar = it
            })

        }
    }

    EasyDeleteDialog(
        show = deleteStar != null,
        onDelete = { deleteStar?.let(vm::delete) },
        onDismissRequest = {
            deleteStar = null
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StarList(
    scrollBehavior: TopAppBarScrollBehavior? = null,
    vm: StarViewModel,
    onItemClick: (CartoonStar) -> Unit,
    onItemLongPress: (CartoonStar) -> Unit,
) {
    val lazyListState = rememberLazyListState()

    val flow = remember(vm.searchPager.value, vm.curPager.value) {
        vm.searchPager.value ?: vm.curPager.value
    }

    val lazyPagingItems = flow.collectAsLazyPagingItems()

    val haptic = LocalHapticFeedback.current


    Box(modifier = Modifier.fillMaxSize()) {

        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize().run {
                if (scrollBehavior != null) {
                    nestedScroll(scrollBehavior.nestedScrollConnection)
                } else {
                    this
                }
            },
            columns = GridCells.Adaptive(150.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(4.dp, 4.dp, 4.dp, 88.dp)
        ) {
            items(lazyPagingItems.itemCount) {
                lazyPagingItems[it]?.let {
                    CartoonStarCardWithCover(
                        modifier = Modifier.fillMaxSize(),
                        cartoon = it,
                        onClick = {
                            onItemClick(it)
                        },
                        onLongPress = {
                            onItemLongPress(it)
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                    )
                }

            }
            pagingCommon(lazyPagingItems)
        }

        FastScrollToTopFab(listState = lazyListState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StarTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior? = null,
    isSearch: Boolean,
    starNum: Int,
    onSearchClick: () -> Unit,
    onFilter: () -> Unit,
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
                Row {
                    Text(text = stringResource(id = R.string.my_anim))
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ) {
                        Text(text = if (starNum <= 999) "$starNum" else "999+")
                    }
                }

            }
        }, actions = {
            if (!isSearch) {
                IconButton(onClick = {
                    onSearchClick()
                }) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        stringResource(id = R.string.search)
                    )
                }
            } else if (text.isNotEmpty()) {
                IconButton(onClick = {
                    text = ""
                    onSearch("")
                }) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        stringResource(id = R.string.clear)
                    )
                }
            }

            IconButton(onClick = {
                onFilter()
            }) {
                Icon(
                    imageVector = Icons.Filled.FilterList,
                    stringResource(id = R.string.filter)
                )
            }

        })
}