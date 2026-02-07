package org.easybangumi.next.shared.compose.home.collection

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import org.easybangumi.next.shared.LocalNavController
import org.easybangumi.next.shared.cartoon.collection.CartoonCollectionController
import org.easybangumi.next.shared.cartoon.displayName
import org.easybangumi.next.shared.compose.home.HomeVM
import org.easybangumi.next.shared.data.bangumi.BangumiConst
import org.easybangumi.next.shared.foundation.InnerBackHandler
import org.easybangumi.next.shared.foundation.TabPage
import org.easybangumi.next.shared.foundation.selection.SelectionTopAppBar
import org.easybangumi.next.shared.foundation.stringRes
import org.easybangumi.next.shared.foundation.todo.easyTODO
import org.easybangumi.next.shared.foundation.view_model.vm
import org.easybangumi.next.shared.navigateToDetailOrMedia
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

const val TAG_COLLECTION = "collection"
@Composable
fun Collection(
    homeVM: HomeVM? = null,
) {
    val collectionVM = vm(::CollectionVM)

    val nav = LocalNavController.current

    val selectionBottomBar = remember<@Composable () -> Unit> {
        {
            CollectionSelectionBottomBar(
                onDelete = { collectionVM.dialogDeleteSelection() },
                onChangeTag = { collectionVM.dialogChangeTag() },
                onUpdate = { collectionVM.fireUpdateSelection() },
                onMigrate = {
                    collectionVM.dialogMigrateSelect()
                },
                onUp = {
                    collectionVM.fireUpdateSelection()
                }
            )

        }
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val focusRequester = remember {
        FocusRequester()
    }

    val state by collectionVM.ui

    InnerBackHandler(
        enabled = state.selection.isNotEmpty()
    ) {
        collectionVM.onSelectionExit()
    }

    LaunchedEffect(key1 = state.selection.isEmpty()) {
        if (state.selection.isEmpty()) {
            homeVM?.cleanBottomBarCompose(TAG_COLLECTION)
        } else {
            homeVM?.pushBottomBarCompose(TAG_COLLECTION, selectionBottomBar)
        }

        collectionVM.onLaunchEffect()
    }

    DisposableEffect(key1 = Unit) {
        onDispose {
            collectionVM.onSelectionExit()
            homeVM?.cleanBottomBarCompose(TAG_COLLECTION)
        }
    }

    val pagerState = state.pagerState

    Column {
        AnimatedContent(targetState = state.selection.isNotEmpty(), label = "") { isSelectionMode ->

            if (isSelectionMode) {
                LaunchedEffect(key1 = Unit) {
                    kotlin.runCatching {
                        focusRequester.freeFocus()
                    }

                }
                SelectionTopAppBar(selectionItemsCount = state.selection.size, onExit = {
                    collectionVM.onSelectionExit()
                }, onSelectAll = {
                    collectionVM.onSelectAll()
                }, onSelectInvert = {
                    collectionVM.onSelectInvert()
                })
            } else {
                StarTopAppBar(
                    scrollBehavior = scrollBehavior,
                    focusRequester = focusRequester,
                    isSearch = state.searchQuery != null,
                    isFilter = state.isFilter,
                    text = state.searchQuery ?: "",
                    onTextChange = {
                    },
                    starNum = state.starCount,
                    onSearchClick = {
                        easyTODO("搜索")
                    },
                    onUpdate = {
                        easyTODO("更新")
                    },
                    onSearch = {
                        easyTODO("搜索")
                    },
                    onFilterClick = {
                        collectionVM.dialogProc()
                    },
                    onSearchExit = {

                    })
            }
        }

        if (state.tagList.size == 1) {
            val tab = state.tagList.firstOrNull()
            val collectionData = tab?.let { state.data[it] }
            when (collectionData) {
                is CartoonCollectionController.CollectionData.BangumiCollection -> {
                    var bangumiSelectionType by remember(tab, collectionData.typeList) {
                        mutableStateOf(
                            collectionData.typeList.firstOrNull() ?: BangumiConst.collectTypeList.first()
                        )
                    }
                    LaunchedEffect(collectionData.typeList) {
                        if (collectionData.typeList.isNotEmpty() && bangumiSelectionType !in collectionData.typeList) {
                            bangumiSelectionType = collectionData.typeList.first()
                        }
                    }
                    BangumiCollectionList(
                        nestedScrollConnection = scrollBehavior.nestedScrollConnection,
                        data = collectionData,
                        selectionType = bangumiSelectionType,
                        onTypeSelected = {
                            bangumiSelectionType = it
                        },
                        selectionSet = state.selection,
                        onRefresh = {},
                        onClick = {
                            if (state.selection.isEmpty()) {
                                nav.navigateToDetailOrMedia(it.toCartoonIndex(), it.toCartoonCover())
                            } else {
                                collectionVM.onSelectionChange(it)
                            }
                        },
                        onLongPress = {
                            collectionVM.onSelectionLongPress(it)
                        }
                    )
                }
                else -> {
                    val list = collectionData?.localOrNull() ?: emptyList()
                    LocalStarList(
                        nestedScrollConnection = scrollBehavior.nestedScrollConnection,
                        starCartoon = list,
                        selectionSet = state.selection,
                        onClick = {
                            if (state.selection.isEmpty()) {
                                nav.navigateToDetailOrMedia(it.toCartoonIndex(), it.toCartoonCover())
                            } else {
                                collectionVM.onSelectionChange(it)
                            }
                        },
                        onLongPress = {
                            collectionVM.onSelectionLongPress(it)
                        },
                        onRefresh = {
                        }
                    )
                }
            }
        } else {
            TabPage(
                tabScrollable = true,
                pagerState = pagerState,
                onTabSelect = {
//                    runCatching {
//                        collectionVM.changeTab(state.tagList[it])
//                    }.onFailure {
//                        it.printStackTrace()
//                    }
                },
                tabs = { i, _ ->
                    Row {
                        val tab = state.tagList[i]
                        val starNum = state.data[tab]?.localOrNull()?.size
                        Text(text = stringRes(tab.displayName()))
                        if (starNum != null) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ) {
                                Text(text = if (starNum <= 999) "$starNum" else "999+")
                            }
                        }
                    }

                }) {
                val tab = state.tagList[it]
                val collectionData = state.data[tab]
                when (collectionData) {
                    is CartoonCollectionController.CollectionData.BangumiCollection -> {
//                        var bangumiSelectionType by remember(tab, collectionData.typeList) {
//                            mutableStateOf(
//                                collectionData.typeList.firstOrNull() ?: BangumiConst.collectTypeList.first()
//                            )
//                        }
//                        LaunchedEffect(collectionData.typeList) {
//                            if (collectionData.typeList.isNotEmpty() && bangumiSelectionType !in collectionData.typeList) {
//                                bangumiSelectionType = collectionData.typeList.first()
//                            }
//                        }
                        BangumiCollectionList(
                            data = collectionData,
                            selectionType = state.curBangumiType,
                            onTypeSelected = { collectionVM.bangumiChangeType(it) },
                            selectionSet = state.selection,
                            onRefresh = { },
                            onClick = {
                                if (state.selection.isEmpty()) {
                                    nav.navigateToDetailOrMedia(it.toCartoonIndex(), it.toCartoonCover())
                                } else {
                                    collectionVM.onSelectionChange(it)
                                }
                            },
                            onLongPress = {
                                collectionVM.onSelectionLongPress(it)
                            }
                        )
                    }
                    else -> {
                        val list = collectionData?.localOrNull() ?: emptyList()
                        LocalStarList(
                            starCartoon = list,
                            selectionSet = state.selection,
                            onClick = {
                                if (state.selection.isEmpty()) {
                                    nav.navigateToDetailOrMedia(it.toCartoonIndex(), it.toCartoonCover())
                                } else {
                                    collectionVM.onSelectionChange(it)
                                }
                            },
                            onLongPress = {
                                collectionVM.onSelectionLongPress(it)
                            },
                            onRefresh = {

                            }
                        )
                    }
                }
            }
        }


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
    isFilter: Boolean,
    starNum: Int,
    onSearchClick: () -> Unit,
    onUpdate: () -> Unit,
    onSearch: (String) -> Unit,
    onFilterClick: () -> Unit,
    onSearchExit: () -> Unit,
) {

    TopAppBar(
//        colors = TopAppBarDefaults.topAppBarColors(scrolledContainerColor = MaterialTheme.colorScheme.surface),
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            if (isSearch) {
                IconButton(onClick = {
                    onSearchExit()
                }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack, stringRes(Res.strings.back)
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
                Row {
                    Text(text = stringRes(Res.strings.my_anim))
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
                        imageVector = Icons.Filled.Search, stringRes(Res.strings.search)
                    )
                }

                IconButton(onClick = {
                    onFilterClick()
                }) {
                    Icon(
                        imageVector = Icons.Filled.FilterAlt, stringRes(Res.strings.filter),
                        tint = if (isFilter) MaterialTheme.colorScheme.primary else LocalContentColor.current
                    )
                }

                IconButton(onClick = {
                    onUpdate()
                }) {
                    Icon(
                        imageVector = Icons.Filled.Update, stringRes(Res.strings.update)
                    )
                }
            } else if (text.isNotEmpty()) {
                IconButton(onClick = {
                    onTextChange("")
                }) {
                    Icon(
                        imageVector = Icons.Filled.Clear, stringRes(Res.strings.clear)
                    )
                }
            }


        })
}


@Composable
fun CollectionSelectionBottomBar(
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
                Icons.Filled.Tag, contentDescription = stringRes(Res.strings.change_tag)
            )
        }

        IconButton(onClick = {
            onUpdate()
        }) {
            Icon(Icons.Filled.Update, contentDescription = stringRes(Res.strings.update))
        }

        IconButton(onClick = {
            onMigrate()
        }) {
            Icon(Icons.Filled.SyncAlt, contentDescription = stringRes(Res.strings.migrating))
        }

        IconButton(onClick = {
            onUp()
        }) {
            Icon(
                Icons.Filled.PushPin,
                contentDescription = stringRes(Res.strings.filter_with_is_up)
            )
        }

    }, floatingActionButton = {
        FloatingActionButton(
            onClick = { onDelete() },
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ) {
            Icon(Icons.Filled.Delete, contentDescription = stringRes(Res.strings.delete))
        }
    })


}
