package com.heyanle.easybangumi4.ui.home.star

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Badge
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.db.entity.CartoonStar
import com.heyanle.easybangumi4.navigationDetailed
import com.heyanle.easybangumi4.ui.common.CartoonStarCardWithCover
import com.heyanle.easybangumi4.ui.common.EasyDeleteDialog
import com.heyanle.easybangumi4.ui.common.FastScrollToTopFab
import com.heyanle.easybangumi4.ui.common.LoadingPage
import com.heyanle.easybangumi4.ui.common.PagingCommon
import com.heyanle.easybangumi4.ui.common.SelectionTopAppBar
import com.heyanle.easybangumi4.ui.common.pagingCommon
import com.heyanle.easybangumi4.ui.home.LocalHomeViewModel

/**
 * Created by HeYanLe on 2023/3/18 17:04.
 * https://github.com/heyanLE
 */


@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun Star() {

    val homeViewModel = LocalHomeViewModel.current
    val vm = viewModel<StarViewModel>()

    val selectionBottomBar = remember<@Composable () -> Unit> {
        {
            StarSelectionBottomBar(
                onDelete = { vm.dialogDeleteSelection() },
                onChangeUpdateStrategy = { vm.dialogChangeUpdate() },
                onUpdate = { vm.onUpdate() })

        }
    }
    val nav = LocalNavController.current

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()


    val state by vm.stateFlow.collectAsState()

    val focusRequester = remember {
        FocusRequester()
    }

    LaunchedEffect(key1 = state.selection.isEmpty()) {
        if (state.selection.isEmpty()) {
            homeViewModel.customBottomBar = null
        } else {
            homeViewModel.customBottomBar = selectionBottomBar
        }
    }

    DisposableEffect(key1 = Unit) {
        onDispose {
            vm.onSelectionExit()
            homeViewModel.customBottomBar = null
        }
    }

    BackHandler(
        enabled = state.selection.isNotEmpty()
    ) {
        vm.onSelectionExit()
    }

    Column {
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
                )
            } else {
                StarTopAppBar(
                    scrollBehavior = scrollBehavior,
                    focusRequester = focusRequester,
                    isSearch = state.searchQuery != null,
                    text = state.searchQuery ?: "",
                    onTextChange = {
                        vm.onSearch(it)
                    },
                    starNum = state.starCount,
                    onSearchClick = {
                        vm.onSearch("")
                    },
                    onFilter = {
                        com.heyanle.easybangumi4.utils.TODO("过滤器")
                    },
                    onSearch = {
                        vm.onSearch(it)
                    },
                    onSearchExit = {
                        vm.onSearch(null)
                    }
                )
            }
        }


        StarList(
            isLoading = state.isLoading,
            nestedScrollConnection = scrollBehavior.nestedScrollConnection,
            starCartoonList = state.pager.collectAsLazyPagingItems(),
            selectionSet = state.selection,
            onStarClick = {
                if (state.selection.isEmpty()) {
                    nav.navigationDetailed(it.id, it.url, it.source)
                } else {
                    vm.onSelectionChange(it)
                }
            },
            onStarLongPress = {
                vm.onSelectionChange(it)
            })

    }

    val deleteDialog = state.dialog as? StarViewModel.DialogState.Delete
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


}

@Composable
fun StarSelectionBottomBar(
    onDelete: () -> Unit,
    onChangeUpdateStrategy: () -> Unit,
    onUpdate: () -> Unit,
) {

    BottomAppBar(
        actions = {
            IconButton(onClick = {
                onChangeUpdateStrategy()
            }) {
                Icon(
                    Icons.Filled.Settings,
                    contentDescription = stringResource(id = R.string.setting)
                )
            }

            IconButton(onClick = {
                onUpdate()
            }) {
                Icon(Icons.Filled.Update, contentDescription = stringResource(id = R.string.update))
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onDelete() },
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ) {
                Icon(Icons.Filled.Delete, contentDescription = stringResource(id = R.string.delete))
            }
        }
    )


}

@Composable
fun StarList(
    isLoading: Boolean,
    nestedScrollConnection: NestedScrollConnection? = null,
    starCartoonList: LazyPagingItems<CartoonStar>,
    selectionSet: Set<CartoonStar>,
    isHapticFeedback: Boolean = true,
    onStarClick: (CartoonStar) -> Unit,
    onStarLongPress: (CartoonStar) -> Unit,

    ) {
    val lazyGridState = rememberLazyGridState()
    val haptic = LocalHapticFeedback.current

    if (isLoading) {
        LoadingPage(modifier = Modifier.fillMaxSize())
    } else {
        if (starCartoonList.itemCount > 0) {

            LazyVerticalGrid(
                modifier = Modifier
                    .fillMaxSize()
                    .run {
                        if (nestedScrollConnection != null) {
                            nestedScroll(nestedScrollConnection)
                        } else {
                            this
                        }
                    },
                state = lazyGridState,
                columns = GridCells.Adaptive(150.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(4.dp, 4.dp, 4.dp, 88.dp)
            ) {
                items(starCartoonList.itemCount) { int ->
                    val star = starCartoonList[int]
                    if (star != null) {
                        CartoonStarCardWithCover(
                            selected = selectionSet.contains(star),
                            cartoon = star,
                            showSourceLabel = true,
                            onClick = {
                                onStarClick(it)
                            },
                            onLongPress = {
                                if (isHapticFeedback) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                onStarLongPress(it)
                            },
                        )
                    }

                }
                pagingCommon(starCartoonList)
            }
        }
        PagingCommon(items = starCartoonList)
        FastScrollToTopFab(listState = lazyGridState)

    }


}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StarTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior? = null,
    focusRequester: FocusRequester,
    text: String,
    onTextChange: (String) -> Unit,
    isSearch: Boolean,
    starNum: Int,
    onSearchClick: () -> Unit,
    onFilter: () -> Unit,
    onSearch: (String) -> Unit,
    onSearchExit: () -> Unit,
) {

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
            LaunchedEffect(key1 = isSearch) {
                if (isSearch) {
                    focusRequester.requestFocus()
                }
            }
            if (isSearch) {
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
                    onTextChange("")
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
