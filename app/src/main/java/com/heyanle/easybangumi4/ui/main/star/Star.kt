package com.heyanle.easybangumi4.ui.main.star

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.cartoon.entity.CartoonStar
import com.heyanle.easybangumi4.cartoon.tags.isUpdate
import com.heyanle.easybangumi4.cartoon.tags.tagLabel
import com.heyanle.easybangumi4.ui.common.CartoonStarCardWithCover
import com.heyanle.easybangumi4.ui.common.EasyDeleteDialog
import com.heyanle.easybangumi4.ui.common.EasyMutiSelectionDialog
import com.heyanle.easybangumi4.ui.common.EmptyPage
import com.heyanle.easybangumi4.ui.common.FastScrollToTopFab
import com.heyanle.easybangumi4.ui.common.SelectionTopAppBar
import com.heyanle.easybangumi4.ui.common.TabPage
import com.heyanle.easybangumi4.ui.main.MainViewModel
import com.heyanle.easybangumi4.ui.main.star.update.Update
import com.heyanle.easybangumi4.ui.main.star.update.UpdateViewModel
import com.heyanle.easybangumi4.navigationCartoonTag
import com.heyanle.easybangumi4.navigationDetailed

/**
 * Created by HeYanLe on 2023/7/29 23:21.
 * https://github.com/heyanLE
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun Star() {

    val homeVM = viewModel<MainViewModel>()
    val starVM = viewModel<StarViewModel>()
    val updateVM = viewModel<UpdateViewModel>()

    val selectionBottomBar = remember<@Composable () -> Unit> {
        {
            StarSelectionBottomBar(
                onDelete = { starVM.dialogDeleteSelection() },
                onChangeTag = { starVM.dialogChangeTag() },
                onUpdate = { starVM.onUpdateSelection() }
            )

        }
    }

    val nav = LocalNavController.current

    //val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val state by starVM.stateFlow.collectAsState()

    val focusRequester = remember {
        FocusRequester()
    }

    LaunchedEffect(key1 = state.selection.isEmpty()) {
        if (state.selection.isEmpty()) {
            homeVM.customBottomBar = null
        } else {
            homeVM.customBottomBar = selectionBottomBar
        }
    }

    DisposableEffect(key1 = Unit) {
        onDispose {
            starVM.onSelectionExit()
            homeVM.customBottomBar = null
        }
    }


    BackHandler(
        enabled = state.selection.isNotEmpty()
    ) {
        starVM.onSelectionExit()
    }

    Column {
        AnimatedContent(targetState = state.selection.isNotEmpty(), label = "") { isSelectionMode ->

            if (isSelectionMode) {
                LaunchedEffect(key1 = Unit) {
                    kotlin.runCatching {
                        focusRequester.freeFocus()
                    }

                }
                SelectionTopAppBar(selectionItemsCount = state.selection.size, onExit = {
                    starVM.onSelectionExit()
                }, onSelectAll = {
                    starVM.onSelectAll()
                }, onSelectInvert = {
                    starVM.onSelectInvert()
                })
            } else {
                StarTopAppBar(
                    //scrollBehavior = scrollBehavior,
                    focusRequester = focusRequester,
                    isSearch = state.searchQuery != null,
                    text = state.searchQuery ?: "",
                    onTextChange = {
                        starVM.onSearch(it)
                        updateVM.search(it)
                    },
                    starNum = state.starCount,
                    onSearchClick = {
                        starVM.onSearch("")
                    },
                    onUpdate = {
                        starVM.onUpdateAll()
                    },
                    onSearch = {
                        starVM.onSearch(it)
                        updateVM.search(it)
                    },
                    onSearchExit = {
                        starVM.onSearch(null)
                        updateVM.search("")
                    })
            }
        }


        TabPage(initialPage = state.tabs.indexOf(state.curTab).coerceAtLeast(0),
            tabSize = state.tabs.size,
            onTabSelect = {
                runCatching {
                    starVM.changeTab(state.tabs[it])
                }.onFailure {
                    it.printStackTrace()
                }
            },
            tabs = { i, b ->
                Row {
                    val tab = state.tabs[i]
                    val starNum = state.data[state.tabs[i]]?.size ?: 0
                    Text(text = tab.tagLabel())
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ) {
                        Text(text = if (starNum <= 999) "$starNum" else "999+")
                    }
                }

            }) {
            val tab = state.tabs[it]
            if (tab.isUpdate()) {
                Update(updateVM)
            } else {
                val list = state.data[tab] ?: emptyList()
                StarList(
                    //nestedScrollConnection = scrollBehavior.nestedScrollConnection,
                    starCartoon = list, selectionSet = state.selection, onStarClick = {
                        if (state.selection.isEmpty()) {
                            nav.navigationDetailed(it.id, it.url, it.source)
                        } else {
                            starVM.onSelectionChange(it)
                        }
                    }, onStarLongPress = {
                        starVM.onSelectionLongPress(it)
                    })
            }
        }
    }

    when (val sta = state.dialog) {
//        is StarViewModel.DialogState.ChangeTag -> {
//            val tags =
//                state.tabs.filter { it != StarViewModel.DEFAULT_TAG && it != StarViewModel.UPDATE_TAG }
//            if (tags.isEmpty()) {
//                AlertDialog(
//                    title = {
//                        Text(text = stringResource(id = R.string.no_tag))
//                    },
//                    text = {
//                        Text(text = stringResource(id = R.string.click_to_manage_tag))
//                    },
//                    confirmButton = {
//                        TextButton(
//                            colors = ButtonDefaults.buttonColors(
//                                containerColor = Color.Transparent,
//                                contentColor = MaterialTheme.colorScheme.onBackground
//                            ),
//                            onClick = {
//                                //TODO
//                                starVM.dialogDismiss()
//                                nav.navigationCartoonTag()
//                            }) {
//                            Text(text = stringResource(id = R.string.confirm))
//                        }
//                    },
//                    dismissButton = {
//                        TextButton(
//                            colors = ButtonDefaults.buttonColors(
//                                containerColor = Color.Transparent,
//                                contentColor = MaterialTheme.colorScheme.onBackground
//                            ),
//                            onClick = {
//                                starVM.dialogDismiss()
//                                //starVM.onSelectionExit()
//                            }) {
//                            Text(text = stringResource(id = R.string.cancel))
//                        }
//                    },
//                    onDismissRequest = {
//                        starVM.dialogDismiss()
//                    }
//                )
//            } else {
//                EasyMutiSelectionDialog(show = true,
//                    title = {
//                        Text(text = stringResource(id = R.string.change_tag))
//                    },
//                    items = tags,
//                    initSelection = sta.getTags(),
//                    onConfirm = {
//                        starVM.changeTagSelection(sta.selection, it)
//                        starVM.onSelectionExit()
//                    },
//                    onManage = {
//                        nav.navigationCartoonTag()
//                    },
//                    onDismissRequest = {
//                        starVM.dialogDismiss()
//                    })
//            }
//
//        }

        is StarViewModel.DialogState.Delete -> {
            EasyDeleteDialog(show = true, onDelete = {
                starVM.deleteSelection(sta.selection)
            }) {
                starVM.dialogDismiss()
            }
        }

        else -> {}
    }
}


@Composable
fun StarList(
    nestedScrollConnection: NestedScrollConnection? = null,
    starCartoon: List<CartoonStar>,
    selectionSet: Set<CartoonStar>,
    isHapticFeedback: Boolean = true,
    onStarClick: (CartoonStar) -> Unit,
    onStarLongPress: (CartoonStar) -> Unit,

    ) {
    val lazyGridState = rememberLazyGridState()
    val haptic = LocalHapticFeedback.current

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
        columns = GridCells.Adaptive(100.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(4.dp, 4.dp, 4.dp, 88.dp)
    ) {
        if (starCartoon.isEmpty()) {
            item(span = {
                // LazyGridItemSpanScope:
                // maxLineSpan
                GridItemSpan(maxLineSpan)
            }) {
                EmptyPage(
                    modifier = Modifier.height(256.dp)
                )
            }
        }
        items(starCartoon) { star ->
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
    FastScrollToTopFab(listState = lazyGridState)
}

@Composable
fun StarSelectionBottomBar(
    onDelete: () -> Unit,
    onChangeTag: () -> Unit,
    onUpdate: () -> Unit,
) {

    BottomAppBar(actions = {
        IconButton(onClick = {
            onChangeTag()
        }) {
            Icon(
                Icons.Filled.Tag, contentDescription = stringResource(id = R.string.change_tag)
            )
        }

        IconButton(onClick = {
            onUpdate()
        }) {
            Icon(Icons.Filled.Update, contentDescription = stringResource(id = R.string.update))
        }
    }, floatingActionButton = {
        FloatingActionButton(
            onClick = { onDelete() },
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ) {
            Icon(Icons.Filled.Delete, contentDescription = stringResource(id = R.string.delete))
        }
    })


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
    onUpdate: () -> Unit,
    onSearch: (String) -> Unit,
    onSearchExit: () -> Unit,
) {

    TopAppBar(colors = TopAppBarDefaults.topAppBarColors(scrolledContainerColor = MaterialTheme.colorScheme.surface),
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            if (isSearch) {
                IconButton(onClick = {
                    onSearchExit()
                }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack, stringResource(id = R.string.back)
                    )
                }
            }
        },
        title = {
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
        },
        actions = {
            if (!isSearch) {
                IconButton(onClick = {
                    onSearchClick()
                }) {
                    Icon(
                        imageVector = Icons.Filled.Search, stringResource(id = R.string.search)
                    )
                }

                IconButton(onClick = {
                    onUpdate()
                }) {
                    Icon(
                        imageVector = Icons.Filled.Update, stringResource(id = R.string.update)
                    )
                }
            } else if (text.isNotEmpty()) {
                IconButton(onClick = {
                    onTextChange("")
                }) {
                    Icon(
                        imageVector = Icons.Filled.Clear, stringResource(id = R.string.clear)
                    )
                }
            }


        })
}
