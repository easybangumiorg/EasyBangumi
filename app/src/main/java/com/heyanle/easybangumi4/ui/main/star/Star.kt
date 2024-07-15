package com.heyanle.easybangumi4.ui.main.star

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ListItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.star.CartoonInfoSortFilterConst
import com.heyanle.easybangumi4.cartoon.star.isInner
import com.heyanle.easybangumi4.navigationCartoonTag
import com.heyanle.easybangumi4.navigationDetailed
import com.heyanle.easybangumi4.navigationMigrate
import com.heyanle.easybangumi4.source.LocalSourceBundleController
import com.heyanle.easybangumi4.ui.common.CartoonStarCardWithCover
import com.heyanle.easybangumi4.ui.common.EasyDeleteDialog
import com.heyanle.easybangumi4.ui.common.EasyMutiSelectionDialog
import com.heyanle.easybangumi4.ui.common.EasyMutiSelectionDialogStar
import com.heyanle.easybangumi4.ui.common.EmptyPage
import com.heyanle.easybangumi4.ui.common.FastScrollToTopFab
import com.heyanle.easybangumi4.ui.common.SelectionTopAppBar
import com.heyanle.easybangumi4.ui.common.TabPage
import com.heyanle.easybangumi4.ui.common.proc.FilterColumn
import com.heyanle.easybangumi4.ui.common.proc.FilterState
import com.heyanle.easybangumi4.ui.common.proc.SortColumn
import com.heyanle.easybangumi4.ui.common.proc.SortState
import com.heyanle.easybangumi4.ui.main.MainViewModel
import com.heyanle.easybangumi4.ui.story.local.Local
import com.heyanle.easybangumi4.ui.story.local.LocalTopAppBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/7/29 23:21.
 * https://github.com/heyanLE
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun Star() {

    val homeVM = viewModel<MainViewModel>()
    val starVM = viewModel<StarViewModel>()

    val nav = LocalNavController.current

    val selectionBottomBar = remember<@Composable () -> Unit> {
        {
            StarSelectionBottomBar(
                onDelete = { starVM.dialogDeleteSelection() },
                onChangeTag = { starVM.dialogChangeTag() },
                onUpdate = { starVM.onUpdateSelection() },
                onMigrate = {
                    starVM.dialogMigrateSelect()
                },
                onUp = {
                    starVM.onUpSelection()
                }
            )

        }
    }


    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

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
                    isFilter = state.isFilter,
                    text = state.searchQuery ?: "",
                    onTextChange = {
                        starVM.onSearch(it)

                    },
                    starNum = state.starCount,
                    onSearchClick = {
                        starVM.onSearch("")
                    },
                    onUpdate = {
                        starVM.onUpdate()
                    },
                    onSearch = {
                        starVM.onSearch(it)

                    },
                    onFilterClick = {
                        starVM.dialogProc()
                    },
                    onSearchExit = {
                        starVM.onSearch(null)

                    })
            }
        }

        if (state.tagList.size == 1) {
            val tab = state.tagList.firstOrNull()
            val list = state.data[tab?.label] ?: emptyList()
            StarList(
                nestedScrollConnection = scrollBehavior.nestedScrollConnection,
                starCartoon = list, selectionSet = state.selection, onStarClick = {
                    if (state.selection.isEmpty()) {
                        nav.navigationDetailed(it.id, it.url, it.source)
                    } else {
                        starVM.onSelectionChange(it)
                    }
                }, onStarLongPress = {
                    starVM.onSelectionLongPress(it)
                }, onRefresh = {
                    starVM.onUpdate()
                })
        } else {
            TabPage(initialPage = state.tagList.indexOf(state.curTab).coerceAtLeast(0),
                tabSize = state.tagList.size,
                onTabSelect = {
                    runCatching {
                        starVM.changeTab(state.tagList[it])
                    }.onFailure {
                        it.printStackTrace()
                    }
                },
                tabs = { i, _ ->
                    Row {
                        val tab = state.tagList[i]
                        val starNum = state.data[tab.label]?.size ?: 0
                        Text(text = tab.display)
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ) {
                            Text(text = if (starNum <= 999) "$starNum" else "999+")
                        }
                    }

                }) {
                val tab = state.tagList[it]
                val list = state.data[tab.label] ?: emptyList()
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
                    }, onRefresh = {
                        starVM.onUpdate()
                    })
            }
        }


    }

    when (val sta = state.dialog) {
        is StarViewModel.DialogState.ChangeTag -> {
            val tags =
                state.tagList

            if (tags.isEmpty()) {
                AlertDialog(
                    title = {
                        Text(text = stringResource(id = R.string.no_tag))
                    },
                    text = {
                        Text(text = stringResource(id = R.string.click_to_manage_tag))
                    },
                    confirmButton = {
                        TextButton(
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.onBackground
                            ),
                            onClick = {
                                starVM.dialogDismiss()
                                nav.navigationCartoonTag()
                            }) {
                            Text(text = stringResource(id = R.string.confirm))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.onBackground
                            ),
                            onClick = {
                                starVM.dialogDismiss()
                                //starVM.onSelectionExit()
                            }) {
                            Text(text = stringResource(id = R.string.cancel))
                        }
                    },
                    onDismissRequest = {
                        starVM.dialogDismiss()
                    }
                )
            } else {
                EasyMutiSelectionDialogStar(show = true,
                    title = {
                        Text(text = stringResource(id = R.string.change_tag))
                    },
                    items = tags,
                    initSelection = sta.getTags(),
                    onConfirm = {
                        starVM.changeTagSelection(sta.selection, it)
                        starVM.onSelectionExit()
                    },
                    onManage = {
                        nav.navigationCartoonTag()
                    },
                    onDismissRequest = {
                        starVM.dialogDismiss()
                    })
            }

        }

        is StarViewModel.DialogState.Proc -> {
            CartoonStarProcBottomSheet(vm = starVM, state)
        }

        is StarViewModel.DialogState.Delete -> {
            EasyDeleteDialog(show = true, onDelete = {
                starVM.deleteSelection(sta.selection)
            }) {
                starVM.dialogDismiss()
            }
        }

        is StarViewModel.DialogState.MigrateSource -> {
            val sources = LocalSourceBundleController.current.sources()
            EasyMutiSelectionDialog(show = true,
                title = {
                    Text(text = stringResource(id = R.string.choose_source_to_migrate))
                },
                items = sources,
                initSelection = emptyList(),
                onConfirm = {
                    nav.navigationMigrate(
                        sta.selection.map { it.toSummary() }.toList(),
                        it.map { it.key })
                    starVM.onSelectionExit()
                },
                onDismissRequest = {
                    starVM.dialogDismiss()
                })
        }

        else -> {}
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CartoonStarProcBottomSheet(
    vm: StarViewModel,
    state: StarViewModel.State,
) {
    var currentSelect by remember {
        mutableStateOf(0)
    }
    val currentTab = state.curTab
    if (currentTab != null) {
        ModalBottomSheet(
            modifier = Modifier,
            sheetState = rememberModalBottomSheetState(true),
            scrimColor = Color.Black.copy(alpha = 0.32f),
            onDismissRequest = {
                vm.dialogDismiss()
            },
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                3.dp
            ),
            content = {
                CompositionLocalProvider(
                    LocalContentColor provides MaterialTheme.colorScheme.onSurface
                ) {
                    if (state.tagList.size > 1 && !currentTab.isAll) {
                        ListItem(
                            colors = ListItemDefaults.colors(
                                containerColor = Color.Transparent
                            ),
                            modifier = Modifier.clickable {
                                vm.tagConfigChange(
                                    currentTab,
                                    isCustomSetting = !currentTab.isCustomSetting,
                                )
                            },
                            headlineContent = {
                                Text(text = stringResource(id = R.string.tag_custom))
                            },
                            trailingContent = {
                                Switch(
                                    checked = currentTab.isCustomSetting,
                                    onCheckedChange = {
                                        vm.tagConfigChange(
                                            currentTab,
                                            isCustomSetting = !currentTab.isCustomSetting,
                                        )
                                    }
                                )
                            }

                        )
                    }
                    TabPage(
                        pagerModifier = Modifier,
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        tabSize = 2,
                        beyondBoundsPageCount = 2,
                        onTabSelect = {
                            currentSelect = it
                        },
                        tabs = { index, select ->
                            Text(
                                text = if (index == 0) stringResource(id = R.string.filter) else stringResource(
                                    id = R.string.sort
                                )
                            )
                        }
                    ) {
                        if (it == 0) {
                            FilterColumn(
                                modifier = Modifier,
                                filterState = FilterState(
                                    CartoonInfoSortFilterConst.filterWithList,
                                    currentTab.filterState
                                ),
                                onFilterClick = { filter, sta ->
                                    vm.tagConfigChange(
                                        currentTab,
                                        filterWithId = filter.id,
                                        filterState = when (sta) {
                                            FilterState.STATUS_OFF -> FilterState.STATUS_ON
                                            FilterState.STATUS_ON -> FilterState.STATUS_EXCLUDE
                                            FilterState.STATUS_EXCLUDE -> FilterState.STATUS_OFF
                                            else -> FilterState.STATUS_OFF
                                        }
                                    )
                                }
                            )
                        } else if (it == 1) {
                            SortColumn(
                                modifier = Modifier,
                                sortState = SortState(
                                    CartoonInfoSortFilterConst.sortByList,
                                    currentTab.sortId,
                                    currentTab.isReverse
                                ), onClick = { i, sta ->

                                    vm.tagConfigChange(
                                        currentTab,
                                        sortById = i.id,
                                        isReverse = when (sta) {
                                            SortState.STATUS_OFF -> false
                                            SortState.STATUS_ON -> true
                                            else -> false
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            }
        )
    }


}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun StarList(
    nestedScrollConnection: NestedScrollConnection? = null,
    starCartoon: List<CartoonInfo>,
    selectionSet: Set<CartoonInfo>,
    isHapticFeedback: Boolean = true,
    onRefresh: () -> Unit,
    onStarClick: (CartoonInfo) -> Unit,
    onStarLongPress: (CartoonInfo) -> Unit,

    ) {
    val lazyGridState = rememberLazyGridState()
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val refreshing = remember {
        mutableStateOf(false)
    }
    val state = rememberPullRefreshState(refreshing.value, onRefresh = {
        scope.launch {
            refreshing.value = true
            onRefresh()
            delay(500)
            refreshing.value = false
        }

    })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(state)
    ) {
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
                    showWatchProcess = true,
                    showIsUp = true,
                    showIsUpdate = true,
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
        PullRefreshIndicator(
            refreshing.value,
            state,
            Modifier.align(Alignment.TopCenter),
            backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        FastScrollToTopFab(listState = lazyGridState)
    }


}

@Composable
fun StarSelectionBottomBar(
    onDelete: () -> Unit,
    onChangeTag: () -> Unit,
    onUpdate: () -> Unit,
    onMigrate: () -> Unit,
    onUp: () -> Unit,
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

        IconButton(onClick = {
            onMigrate()
        }) {
            Icon(Icons.Filled.SyncAlt, contentDescription = stringResource(id = R.string.migrating))
        }

        IconButton(onClick = {
            onUp()
        }) {
            Icon(
                Icons.Filled.PushPin,
                contentDescription = stringResource(id = R.string.filter_with_is_up)
            )
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
    isFilter: Boolean,
    starNum: Int,
    onSearchClick: () -> Unit,
    onUpdate: () -> Unit,
    onSearch: (String) -> Unit,
    onFilterClick: () -> Unit,
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
                    onFilterClick()
                }) {
                    Icon(
                        imageVector = Icons.Filled.FilterAlt, stringResource(id = R.string.filter),
                        tint = if (isFilter) MaterialTheme.colorScheme.primary else LocalContentColor.current
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
