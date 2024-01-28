package com.heyanle.easybangumi4.ui.search_migrate.migrate

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Badge
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.source.LocalSourceBundleController
import com.heyanle.easybangumi4.source_api.entity.CartoonSummary
import com.heyanle.easybangumi4.ui.common.EasyDeleteDialog
import com.heyanle.easybangumi4.ui.common.ErrorPage
import com.heyanle.easybangumi4.ui.common.LoadingImage
import com.heyanle.easybangumi4.ui.common.LoadingPage
import com.heyanle.easybangumi4.ui.common.MoeDialog
import com.heyanle.easybangumi4.ui.common.MoeDialogData
import com.heyanle.easybangumi4.ui.common.OkImage
import com.heyanle.easybangumi4.ui.common.SelectionTopAppBar
import com.heyanle.easybangumi4.ui.common.moeDialog
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.ui.common.show
import com.heyanle.easybangumi4.utils.stringRes

/**
 * Created by heyanle on 2023/12/23.
 * https://github.com/heyanLE
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Migrate(
    summaries: List<CartoonSummary>,
    sources: List<String>,
) {

    val vm = viewModel<MigrateViewModel>(factory = MigrateViewModelFactory(summaries, sources))
    val state = vm.infoListFlow.collectAsState()
    val sta = state.value
    val behavior = TopAppBarDefaults.pinnedScrollBehavior()
    val nav = LocalNavController.current

    val custom = vm.customSearchCartoonInfo
    val cus = custom.value

    val scrollState = rememberLazyListState()

    var deleteCartoonInfo: List<CartoonInfo> by remember {
        mutableStateOf(emptyList())
    }

    if (cus != null) {

        val v = viewModel<MigrateItemViewModel>(
            viewModelStoreOwner = vm.getOwner(cus),
            factory = MigrateItemViewModelFactory(cus, sources)
        )

        MigrateGather(
            cus,
            sourceKeys = sources,
            onBack = {
                custom.value = null
            },
            onClick = {
                v.changeCover(it)
                custom.value = null
            }
        )
    } else {
        Column {
            if (sta.selection.isEmpty()) {
                MigrateTopAppBar(
                    behavior,
                    sta.infoList.size,
                    onExit = {
                        nav.popBackStack()
                    },
                    onHelp = {}
                )
            } else {
                SelectionTopAppBar(
                    selectionItemsCount = sta.selection.size,
                    onExit = { vm.selectExit() },
                    onSelectAll = { vm.selectAll() },
                    onSelectInvert = { vm.selectInvert() },
                )
            }

            MigrateContent(vm = vm, sta = sta, scrollState = scrollState,topAppBarScrollBehavior = behavior, onDelete = {
                deleteCartoonInfo = it
            })

            if(!sta.isLoading && sta.selection.isNotEmpty()){
                BottomAppBar(actions = {
                    IconButton(onClick = {
                        deleteCartoonInfo = sta.infoList
                    }) {
                        Icon(
                            Icons.Filled.Delete, contentDescription = stringResource(id = R.string.delete)
                        )
                    }



                }, floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            vm.migrate(sta.infoList)
                        },
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = stringResource(id = R.string.migrate_start))
                    }
                })
            }

        }

        if(!sta.isLoading){
            if(sta.selection.isEmpty()){
                Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.fillMaxSize()) {

                    ExtendedFloatingActionButton(
                        modifier = Modifier
                            .padding(16.dp, 40.dp),
                        text = {
                            Text(text = stringResource(id = R.string.migrate_start))
                        },
                        icon = {
                            Icon(
                                Icons.Filled.PlayArrow,
                                contentDescription = stringResource(id = R.string.migrate_start)
                            )
                        },
                        onClick = {
                            vm.migrate()
                        }
                    )
                }
            }
        }


    }


    EasyDeleteDialog(show = deleteCartoonInfo.isNotEmpty(), onDelete = {
        deleteCartoonInfo?.let {
            vm.remove(it)
        }
        deleteCartoonInfo = emptyList()
    }) {
        deleteCartoonInfo = emptyList()
    }


}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColumnScope.MigrateContent(
    vm: MigrateViewModel,
    sta: MigrateViewModel.MigrateState,
    scrollState: LazyListState,
    topAppBarScrollBehavior: TopAppBarScrollBehavior,
    onDelete: (List<CartoonInfo>) -> Unit
) {



    BackHandler(
        enabled = sta.selection.isNotEmpty()
    ) {
        vm.selectExit()
    }

    if (sta.isLoading) {
        LoadingPage(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
            state = scrollState,
            contentPadding = PaddingValues(0.dp, 0.dp, 0.dp, 100.dp)
        ) {
            items(sta.infoList) {
                val itemVM = viewModel<MigrateItemViewModel>(
                    viewModelStoreOwner = vm.getOwner(it),
                    factory = vm.getItemViewModelFactory(it)
                )
                MigrateItem(
                    cartoonInfo = it,
                    isSelect = sta.selection.contains(it),
                    itemVM = itemVM,
                    onChangeCover = {
                        vm.customSearchCartoonInfo.value = it
                    },
                    onClick = { info ->
                        if (sta.selection.isNotEmpty()) {
                            vm.selectChange(info)
                        }
                    },
                    onLongPress = { info ->
                        vm.selectLongPress(info)
                    },
                    onDelete = { info ->
                        onDelete(listOf(info))
                    },
                    onMigrateSus = {
                        vm.remove(listOf(it.cartoonInfo))
                        stringRes(R.string.migrate_sus).moeSnackBar()
                    }
                )
            }
        }





    }


}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MigrateItem(
    cartoonInfo: CartoonInfo,
    isSelect: Boolean,
    itemVM: MigrateItemViewModel,
    onChangeCover: (CartoonInfo) -> Unit,
    onClick: (CartoonInfo) -> Unit,
    onLongPress: (CartoonInfo) -> Unit,
    onDelete: (CartoonInfo) -> Unit,
    onMigrateSus: (MigrateItemViewModel.MigrateItemState) -> Unit,
) {
    val state = itemVM.flow.collectAsState()
    val sta = state

    val bundle = LocalSourceBundleController.current

    var showMenu by remember {
        mutableStateOf(false)
    }

    var showPlayLineMenu by remember {
        mutableStateOf(false)
    }

    var showEpisodeMenu by remember {
        mutableStateOf(false)
    }

    val playLine = sta.value.playLine
    val episode = sta.value.episode

    Row(
        modifier = Modifier
            .height(250.dp)
            .combinedClickable(
                onClick = {
                    onClick(cartoonInfo)
                },
                onLongClick = {
                    onLongPress(cartoonInfo)
                }
            )
            .run {
                if (isSelect) {
                    background(MaterialTheme.colorScheme.primary)
                } else {
                    this
                }
            }
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        val process = if (cartoonInfo.lastHistoryTime != 0L) {
            cartoonInfo.matchHistoryEpisode?.let { last ->
                val index = last.first.sortedEpisodeList.indexOf(last.second)
                "${index + 1}/${last.first.sortedEpisodeList.size}"
            }
        } else ""
        MigrateItemCover(
            modifier = Modifier.weight(2f),
            name = cartoonInfo.name,
            cover = cartoonInfo.coverUrl,
            source = bundle.source(cartoonInfo.source)?.label ?: cartoonInfo.sourceName,
            process = process ?: "",
            isSelect = isSelect
        )
        Spacer(modifier = Modifier.size(8.dp))
        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = stringResource(id = R.string.cartoon_migrate)
        )
        Spacer(modifier = Modifier.size(8.dp))
        val targetCover = sta.value.cartoonCover
        if (sta.value.isLoadingCover || sta.value.isLoadingPlay) {
            Box(modifier = Modifier.weight(2f)) {
                LoadingImage(
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.Center)
                )
            }

            Box(modifier = Modifier.size(24.dp))
        } else if (sta.value.isMigrating) {
            Column(modifier = Modifier.weight(2f), verticalArrangement = Arrangement.Center) {
                LoadingImage(
                    modifier = Modifier
                        .size(36.dp)
                )
                Text(text = stringResource(id = R.string.migrating))
            }

            Box(modifier = Modifier.size(24.dp))
        } else if (targetCover == null) {
            Column(
                modifier = Modifier
                    .weight(2f)
                    .clickable {
                        onChangeCover(cartoonInfo)
                    }, verticalArrangement = Arrangement.Center
            ) {
                OkImage(
                    image = com.heyanle.easybangumi4.R.drawable.error_ikuyo,
                    contentDescription = stringRes(R.string.cartoon_migrate)
                )
                Text(text = stringResource(id = R.string.migrate_cover_failed_click_to_search))
            }

        } else {
            val targetProcess = sta.value.playLineWrapper?.let {
                val epi = sta.value.episode ?: return@let null
                val index = it.sortedEpisodeList.indexOf(epi)
                "${index + 1}/${it.sortedEpisodeList.size}"
            } ?: ""
            MigrateItemCover(
                modifier = Modifier.weight(2f),
                name = targetCover.title,
                cover = targetCover.coverUrl ?: "",
                source = bundle.source(targetCover.source)?.label ?: targetCover.source,
                process = targetProcess,
                isSelect = isSelect,
            )

            IconButton(
                onClick = {
                    showMenu = !showMenu
                }
            ) {
                Icon(Icons.Filled.MoreVert, contentDescription = stringResource(id = R.string.more))
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }) {

                    DropdownMenuItem(text = {
                        Text(text = stringResource(id = R.string.research))
                    }, onClick = {
                        onChangeCover(cartoonInfo)
                        showMenu = false
                    })

                    DropdownMenuItem(text = {
                        Text(text = stringResource(id = R.string.migrate_now))
                    }, onClick = {

                        MoeDialogData(
                            text = stringRes(R.string.make_sure_to_migrate),
                            onConfirm = {
                                itemVM.migrate {
                                    onMigrateSus(sta.value)
                                }
                            },
                            onDismiss = {

                            }
                        ).show()

                        showMenu = false
                    })

                    DropdownMenuItem(text = {
                        Text(text = stringResource(id = R.string.delete))
                    }, onClick = {
                        onDelete(cartoonInfo)
                        showMenu = false
                    })



                    if (playLine != null && episode != null) {


                        DropdownMenuItem(
                            onClick = {
                                showPlayLineMenu = true
                            },
                            text = {

                                Text(
                                    text = stringRes(R.string.play_line) + ": " + playLine.label,
                                    maxLines = 1,
                                    textAlign = TextAlign.Start,
                                    overflow = TextOverflow.Ellipsis,
                                    color = if (isSelect) MaterialTheme.colorScheme.onPrimary else Color.Unspecified
                                )

                            }
                        )


                        DropdownMenuItem(
                            onClick = { showEpisodeMenu = true },
                            text = {
                                Text(
                                    text = stringRes(R.string.episode) + ": " + episode.label,
                                    maxLines = 1,
                                    textAlign = TextAlign.Start,
                                    overflow = TextOverflow.Ellipsis,
                                    color = if (isSelect) MaterialTheme.colorScheme.onPrimary else Color.Unspecified
                                )

                            }
                        )
                    }

                }

                if (playLine != null && episode != null) {


                    DropdownMenu(expanded = showPlayLineMenu, onDismissRequest = {
                        showPlayLineMenu = false
                    }) {
                        sta.value.playLineList.forEach {
                            DropdownMenuItem(text = {
                                Text(text = it.label)
                            }, onClick = {
                                showPlayLineMenu = false
                                showMenu = false
                                itemVM.changeEpisode(
                                    sta.value.sortKey,
                                    it,
                                    it.episode.getOrNull(0)
                                )
                            })
                        }
                    }

                    DropdownMenu(expanded = showEpisodeMenu, onDismissRequest = {
                        showEpisodeMenu = false
                    }) {
                        sta.value.playLineWrapper?.let { playLineWrapper ->
                            playLineWrapper.sortedEpisodeList.forEach {
                                DropdownMenuItem(text = {
                                    Text(text = it.label)
                                }, onClick = {
                                    showEpisodeMenu = false
                                    showMenu = false
                                    itemVM.changeEpisode(sta.value.sortKey, playLine, it)
                                })
                            }
                        }
                    }
                }


            }
        }
    }


}

@Composable
fun MigrateItemCover(
    modifier: Modifier,
    name: String,
    cover: String,
    source: String,
    process: String,
    isSelect: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(4.dp))
            .padding(4.dp)
            .then(modifier),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(19 / 27F)
                .clip(RoundedCornerShape(4.dp)),
        ) {
            OkImage(
                modifier = Modifier.fillMaxSize(),
                image = cover,
                contentDescription = name,
                errorRes = com.heyanle.easybangumi4.R.drawable.placeholder,
            )
            Text(
                fontSize = 13.sp,
                text = source,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(0.dp, 4.dp, 0.dp, 0.dp)
                    )
                    .padding(4.dp, 0.dp)
            )
            if (process.isNotEmpty()) {
                Text(
                    fontSize = 13.sp,
                    text = process,
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
        }


        Spacer(modifier = Modifier.size(4.dp))
        Text(
            style = MaterialTheme.typography.bodySmall,
            text = name,
            maxLines = 2,
            textAlign = TextAlign.Start,
            overflow = TextOverflow.Ellipsis,
            color = if (isSelect) MaterialTheme.colorScheme.onPrimary else Color.Unspecified
        )
        Spacer(modifier = Modifier.size(4.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MigrateTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    count: Int,
    onExit: () -> Unit,
    onHelp: () -> Unit,
) {

    TopAppBar(
        title = {
            Row {
                Text(text = stringResource(id = R.string.cartoon_migrate))
                Badge(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ) {
                    Text(text = if (count <= 999) "$count" else "999+")
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = {
                onExit()
            }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack, stringResource(id = R.string.back)
                )
            }
        },
        actions = {
            IconButton(onClick = {
                onHelp()
            }) {
                Icon(
                    imageVector = Icons.Filled.Help, stringResource(id = R.string.help)
                )
            }
        },
        scrollBehavior = scrollBehavior
    )

}