package com.heyanle.easybangumi4.ui.cartoon_play

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.ScreenShare
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heyanle.easybangumi4.R
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.entity.PlayLineWrapper
import com.heyanle.easybangumi4.cartoon.story.download_v1.req.CartoonDownloadReqController
import com.heyanle.easybangumi4.source_api.entity.Episode
import com.heyanle.easybangumi4.ui.common.Action
import com.heyanle.easybangumi4.ui.common.ActionRow
import com.heyanle.easybangumi4.ui.common.EmptyPage
import com.heyanle.easybangumi4.ui.common.OkImage
import com.heyanle.easybangumi4.ui.common.TabIndicator
import com.heyanle.easybangumi4.ui.common.TabPage
import com.heyanle.easybangumi4.ui.common.proc.SortColumn
import com.heyanle.easybangumi4.ui.common.proc.SortState
import com.heyanle.easybangumi4.utils.toast
import com.heyanle.inject.core.Inject
import kotlin.math.max

/**
 * Created by heyanle on 2023/12/17.
 * https://github.com/heyanLE
 */

@Composable
fun CartoonPlayDetailed(
    cartoon: CartoonInfo,


    playLines: List<PlayLineWrapper>,
    selectLineIndex: Int,

    playingPlayLine: PlayLineWrapper?,
    playingEpisode: Episode?,

    listState: LazyGridState = rememberLazyGridState(),

    onLineSelect: (Int) -> Unit,
    onEpisodeClick: (PlayLineWrapper, Episode) -> Unit,

    showPlayLine: Boolean = true,

    isStar: Boolean,
    onStar: (Boolean) -> Unit,

    gridCount: Int,
    onGridChange: (Int) -> Unit,

    sortState: SortState<Episode>,

    onSearch: () -> Unit,
    onWeb: () -> Unit,
    onExtPlayer: () -> Unit,
    onDownload: (PlayLineWrapper, List<Episode>) -> Unit,
    onDelete: (PlayLineWrapper, List<Episode>) -> Unit,
    onSave: (PlayLineWrapper, List<Episode>) -> Unit,
    onSortChange: (String, Boolean) -> Unit,
) {

    // 0-> download 1->delete 2->saving
    val selectionMode = remember {
        mutableStateOf<Pair<Int, PlayLineWrapper>?>(null)
    }
    val selection = remember {
        mutableStateOf(setOf<Int>())
    }


    if (selectionMode.value != null) {
        BackHandler {
            selectionMode.value = null
        }
    }


    LazyVerticalGrid(
        modifier = Modifier.fillMaxSize(),
        columns = GridCells.Fixed(gridCount),
        state = listState,
        contentPadding = PaddingValues(0.dp, 0.dp, 0.dp, 96.dp)
    ) {
        // 番剧信息
        cartoonMessage(cartoon)

        // action
        item(
            span = {
                // LazyGridItemSpanScope:
                // maxLineSpan
                GridItemSpan(maxLineSpan)
            }
        ) {
            Column {
                CartoonActions(
                    isStar = isStar,
                    isDownloading = selectionMode.value?.first == 0,
                    isDeleting = selectionMode.value?.first == 1,
                    isSaving = selectionMode.value?.first == 2,
                    showWeb = cartoon.url.isNotEmpty(),
                    isFromRemote = !cartoon.isLocal,
                    onStar = onStar,
                    onSearch = onSearch,
                    onWeb = onWeb,
                    onExtPlayer = onExtPlayer,
                    onDownload = {

                        if (selectionMode.value == null && selectLineIndex in playLines.indices) {
                            selectionMode.value = 0 to playLines[selectLineIndex]
                            selection.value = emptySet()
                        } else {
                            selectionMode.value = null
                        }
                        val cartoonDownloadReqController: CartoonDownloadReqController by Inject.injectLazy()
                        //cartoonDownloadController.tryShowFirstDownloadDialog()
                    },
                    onDelete = {
                        if (selectionMode.value == null && selectLineIndex in playLines.indices) {
                            selectionMode.value = 1 to playLines[selectLineIndex]
                            selection.value = emptySet()
                        } else {
                            selectionMode.value = null
                        }
                    },
                    onSave = {
                        if (selectionMode.value == null && selectLineIndex in playLines.indices) {
                            selectionMode.value = 2 to playLines[selectLineIndex]
                            selection.value = emptySet()
                        } else {
                            selectionMode.value = null
                        }
                    }
                )
                Spacer(modifier = Modifier.size(8.dp))
                Divider()
            }
        }

        // 播放线路
        cartoonPlayLines(
            playLines = playLines,

            selectionMode = selectionMode,
            selection = selection,
            showPlayLine = showPlayLine,
            selectLineIndex = selectLineIndex,
            sortState = sortState,
            playingPlayLine = playingPlayLine,
            onLineSelect = onLineSelect,
            onSortChange = onSortChange,
            gridCount = gridCount,
            onGridChange = onGridChange
        )

        // 集数
        cartoonEpisodeList(
            playLines = playLines,
            selectLineIndex = selectLineIndex,
            playingPlayLine = playingPlayLine,
            playingEpisode = playingEpisode,

            selectionMode = selectionMode,
            selection = selection,
            onEpisodeClick = onEpisodeClick
        )

    }

    selectionMode.value?.let { (mode, line) ->
        Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.fillMaxSize()) {
            val up = remember { derivedStateOf { listState.firstVisibleItemIndex > 10 } }
            val downPadding by animateDpAsState(if (up.value) 80.dp else 40.dp, label = "")
            ExtendedFloatingActionButton(
                modifier = Modifier
                    .padding(16.dp, downPadding),
                text = {
                    when (mode) {
                        0 -> Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.start_download))
                        1 -> Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.delete))
                        else -> Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.save_media))
                    }
                },
                icon = {
                    when (mode) {
                        0 -> Icon(
                            Icons.Filled.Download,
                            contentDescription = stringResource(id = com.heyanle.easy_i18n.R.string.start_download)
                        )
                        1 -> Icon(
                            Icons.Filled.Delete,
                            contentDescription = stringResource(id = com.heyanle.easy_i18n.R.string.delete)
                        )
                        else -> {
                            Icon(
                                Icons.Filled.Save,
                                contentDescription = stringResource(id = com.heyanle.easy_i18n.R.string.save_media)
                            )
                        }
                    }
                },
                onClick = {
                    selectionMode.value = null
                    if (mode == 0) {

                        onDownload(
                            line,
                            selection.value.flatMap {
                                val epi = line.sortedEpisodeList.getOrNull(it)
                                if (epi == null) {
                                    listOf()
                                } else {
                                    listOf(epi)
                                }
                            }
                        )
                    } else if (mode == 1){
                        onDelete(
                            line,
                            selection.value.flatMap {
                                val epi = line.sortedEpisodeList.getOrNull(it)
                                if (epi == null) {
                                    listOf()
                                } else {
                                    listOf(epi)
                                }
                            }
                        )
                    } else {
                        onSave(
                            line,
                            selection.value.flatMap {
                                val epi = line.sortedEpisodeList.getOrNull(it)
                                if (epi == null) {
                                    listOf()
                                } else {
                                    listOf(epi)
                                }
                            }
                        )
                    }
                }
            )
        }

    }


}


fun LazyGridScope.cartoonMessage(
    cartoon: CartoonInfo
) {
    item(
        span = {
            // LazyGridItemSpanScope:
            // maxLineSpan
            GridItemSpan(maxLineSpan)
        }
    ) {
        var isExpended by remember {
            mutableStateOf(false)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    indication = null,
                    interactionSource = remember {
                        MutableInteractionSource()
                    }
                ) {
                    isExpended = !isExpended
                }
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedContent(
                modifier = Modifier,
                targetState = isExpended,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300, delayMillis = 300)) togetherWith
                            fadeOut(animationSpec = tween(300, delayMillis = 0))
                }, label = ""
            ) {
                if (it) {
                    CartoonTitleCard(cartoon)
                } else {
                    Row(
                        modifier = Modifier
                    ) {
                        OkImage(
                            modifier = Modifier
                                .width(95.dp)
                                .aspectRatio(19 / 13.5F)
                                .clip(RoundedCornerShape(4.dp)),
                            image = cartoon.coverUrl,
                            crossFade = false,
                            errorRes = R.drawable.placeholder,
                            contentDescription = cartoon.name
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                modifier = Modifier,
                                text = (cartoon.name),
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(modifier = Modifier.size(4.dp))
                            Text(
                                modifier = Modifier,
                                text = (cartoon.description ?: cartoon.intro ?: ""),
                                maxLines = 2,
                                style = MaterialTheme.typography.bodySmall,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }

                    }
                }
            }


            // 箭头
            Box(
                modifier = Modifier
                    .fillMaxWidth(), contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isExpended) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = cartoon.name
                )
            }
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CartoonTitleCard(
    cartoon: CartoonInfo
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Top,
        ) {
            OkImage(
                modifier = Modifier
                    .width(95.dp)
                    .aspectRatio(19 / 27F)
                    .clip(RoundedCornerShape(4.dp)),
                image = cartoon.coverUrl,
                crossFade = false,
                errorRes = R.drawable.placeholder,
                contentDescription = cartoon.name
            )

            Spacer(modifier = Modifier.size(8.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    modifier = Modifier,
                    text = cartoon.name,
                    style = MaterialTheme.typography.titleLarge,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(Modifier.size(16.dp))
                val list = cartoon.genres
                if (list.isNotEmpty() == true) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        list.forEach {
                            Text(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                                    .clickable {
                                    }
                                    .padding(8.dp, 0.dp),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.W900,
                                text = it,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
        Text(modifier = Modifier.padding(8.dp), text = cartoon.description)
    }
}


@Composable
fun CartoonActions(
    isStar: Boolean,
    isDownloading: Boolean,
    isDeleting: Boolean,
    isSaving: Boolean,
    showWeb: Boolean,
    isFromRemote: Boolean,
    onStar: (Boolean) -> Unit,
    onSearch: () -> Unit,
    onWeb: () -> Unit,
    onExtPlayer: () -> Unit,
    onDownload: () -> Unit,
    onDelete: () -> Unit,
    onSave: () -> Unit,
) {
    ActionRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        val starIcon =
            if (isStar) Icons.Filled.Star else Icons.Filled.StarOutline
        val starTextId =
            if (isStar) com.heyanle.easy_i18n.R.string.started_miro else com.heyanle.easy_i18n.R.string.star
        // 点击追番
        Action(
            icon = {
                Icon(
                    starIcon,
                    stringResource(id = starTextId),
                    tint = if (isStar) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                )
            },
            msg = {
                Text(
                    text = stringResource(id = starTextId),
                    color = if (isStar) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                    fontSize = 12.sp
                )
            },
            onClick = {
                onStar(!isStar)
            }
        )

        // 搜索同名番
        Action(
            icon = {
                Icon(
                    Icons.Filled.Search,
                    stringResource(id = com.heyanle.easy_i18n.R.string.search)
                )
            },
            msg = {
                Text(
                    text = stringResource(id = com.heyanle.easy_i18n.R.string.search),
                    fontSize = 12.sp
                )
            },
            onClick = onSearch
        )

        if (showWeb) {
            // 打开原网站
            Action(
                icon = {
                    Icon(
                        painterResource(id = R.drawable.ic_webview_24dp),
                        stringResource(id = com.heyanle.easy_i18n.R.string.open_source_url)
                    )
                },
                msg = {
                    Text(
                        text = stringResource(id = com.heyanle.easy_i18n.R.string.open_source_url),
                        fontSize = 12.sp
                    )
                },
                onClick = onWeb
            )
        }

        if (isFromRemote) {
            // 下载
            Action(
                icon = {
                    Icon(
                        Icons.Filled.Download,
                        stringResource(id = com.heyanle.easy_i18n.R.string.download),
                        tint = if (isDownloading) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                    )
                },
                msg = {
                    Text(
                        text = stringResource(id = com.heyanle.easy_i18n.R.string.download),
                        color = if (isDownloading) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                        fontSize = 12.sp
                    )
                },
                onClick = onDownload
            )
        } else {

//            // 保存到本地
//            Action(
//                icon = {
//                    Icon(
//                        Icons.Filled.Save,
//                        stringResource(id = com.heyanle.easy_i18n.R.string.save),
//                        tint = if (isSaving) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
//                    )
//                },
//                msg = {
//                    Text(
//                        text = stringResource(id = com.heyanle.easy_i18n.R.string.save),
//                        color = if (isDeleting) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
//                        fontSize = 12.sp
//                    )
//                },
//                onClick = onSave
//            )
            // 删除
            Action(
                icon = {
                    Icon(
                        Icons.Filled.Delete,
                        stringResource(id = com.heyanle.easy_i18n.R.string.delete),
                        tint = if (isDeleting) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                    )
                },
                msg = {
                    Text(
                        text = stringResource(id = com.heyanle.easy_i18n.R.string.delete),
                        color = if (isDeleting) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                        fontSize = 12.sp
                    )
                },
                onClick = onDelete
            )
        }

        // 外部播放
        Action(
            icon = {
                Icon(
                    Icons.Filled.ScreenShare,
                    stringResource(id = com.heyanle.easy_i18n.R.string.ext_player)
                )
            },
            msg = {
                Text(
                    text = stringResource(id = com.heyanle.easy_i18n.R.string.ext_player),
                    fontSize = 12.sp
                )
            },
            onClick = onExtPlayer
        )
    }


}

fun LazyGridScope.cartoonPlayLines(
    playLines: List<PlayLineWrapper>,

    selectionMode: MutableState<Pair<Int, PlayLineWrapper>?>,
    selection: MutableState<Set<Int>>,

    showPlayLine: Boolean,
    selectLineIndex: Int,
    sortState: SortState<Episode>,
    gridCount: Int,
    onGridChange: (Int) -> Unit,
    playingPlayLine: PlayLineWrapper?,

    onLineSelect: (Int) -> Unit,
    onSortChange: (String, Boolean) -> Unit,
) {

    // 播放线路
    if (playLines.isEmpty()) {
        item(
            span = {
                // LazyGridItemSpanScope:
                // maxLineSpan
                GridItemSpan(maxLineSpan)
            }
        ) {
            EmptyPage(
                modifier = Modifier.fillMaxWidth(),
                emptyMsg = stringResource(id = com.heyanle.easy_i18n.R.string.no_play_line)
            )
        }
    } else {
        item(
            span = {
                // LazyGridItemSpanScope:
                // maxLineSpan
                GridItemSpan(maxLineSpan)
            }
        ) {
            var isSortShow by remember {
                mutableStateOf(false)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selectionMode.value == null) {
                    if (showPlayLine) {
                        ScrollableTabRow(
                            modifier = Modifier
                                .weight(1f)
                                .padding(0.dp, 8.dp),
                            selectedTabIndex = 0.coerceAtLeast(selectLineIndex),
                            edgePadding = 0.dp,
                            indicator = {
                                TabIndicator(
                                    currentTabPosition = it[0.coerceAtLeast(selectLineIndex)]
                                )
                            },
                            divider = {}
                        ) {
                            playLines.forEachIndexed { index, playLine ->
                                Tab(
                                    selected = index == selectLineIndex,
                                    onClick = {
                                        onLineSelect(index)
                                    },
                                    unselectedContentColor = MaterialTheme.colorScheme.primary.copy(
                                        0.4f
                                    ),
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = playLine.playLine.label)

                                            if (playLine == playingPlayLine) {
                                                Box(
                                                    modifier = Modifier
                                                        .padding(2.dp, 0.dp, 0.dp, 0.dp)
                                                        .size(8.dp)
                                                        .background(
                                                            MaterialTheme.colorScheme.primary,
                                                            CircleShape
                                                        )
                                                )
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    } else {
                        Text(
                            modifier = Modifier
                                .weight(1f)
                                .padding(16.dp, 0.dp),
                            text = stringResource(id = com.heyanle.easy_i18n.R.string.play_list)
                        )
                    }
                } else {
                    // 下载模式的播放线路
                    Text(
                        modifier = Modifier
                            .weight(1f)
                            .padding(16.dp, 0.dp),
                        text = if (selectionMode.value?.first == 0)
                            stringResource(id = com.heyanle.easy_i18n.R.string.select_to_download)
                        else stringResource(id = com.heyanle.easy_i18n.R.string.select_to_delete)
                    )
                    IconButton(onClick = {


                        selectionMode.value?.second?.let {
                            val set = hashSetOf<Int>()
                            it.sortedEpisodeList.forEachIndexed { index, _ ->
                                set.add(index)
                            }
                            selection.value = set
                        }

                    }) {
                        Icon(
                            Icons.Filled.SelectAll,
                            stringResource(id = com.heyanle.easy_i18n.R.string.select_all),
                        )
                    }
                }
                if (isSortShow) {
                    PlayDetailedBottomSheet(
                        sortState = sortState,
                        onGridChange = onGridChange,
                        gridCount = gridCount,
                        onSortChange = onSortChange,
                        onDismissRequest = {
                            isSortShow = false
                        }
                    )
                }
                IconButton(onClick = {

                    isSortShow = true

                }) {
                    val curKey = sortState.current
                    Icon(
                        Icons.Filled.Sort,
                        stringResource(id = com.heyanle.easy_i18n.R.string.sort),
                        tint = if (curKey != PlayLineWrapper.SORT_DEFAULT_KEY) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                    )
                }
            }

        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
fun LazyGridScope.cartoonEpisodeList(
    playLines: List<PlayLineWrapper>,
    selectLineIndex: Int,
    playingPlayLine: PlayLineWrapper?,
    playingEpisode: Episode?,

    selection: MutableState<Set<Int>>,
    selectionMode: MutableState<Pair<Int, PlayLineWrapper>?>,

    onEpisodeClick: (PlayLineWrapper, Episode) -> Unit,
) {
    playLines.getOrNull(selectLineIndex)?.let { playLine ->
        val episode = playLine.sortedEpisodeList
        items(episode.size) {
            val index = it
            episode.getOrNull(index)?.let { item ->
                val select =
                    playLine.playLine == playingPlayLine?.playLine && item == playingEpisode

                Row(
                    modifier = Modifier
                        .padding(4.dp)
                        .fillMaxWidth()
                        //.then(modifier)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (select && selectionMode.value == null) MaterialTheme.colorScheme.secondary else Color.Transparent)
                        .run {
                            if (select && selectionMode.value == null) {
                                this
                            } else {
                                border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline.copy(0.6f),
                                    RoundedCornerShape(4.dp)
                                )
                            }
                        }
                        .combinedClickable(
                            enabled = true,
                            onLongClick = {
                                item.label.toast()
                            }, onClick = {
                                if (selectionMode.value == null) {
                                    onEpisodeClick(
                                        playLines[selectLineIndex],
                                        item
                                    )
                                } else {
                                    val check = selection.value.contains(index)
                                    if (!check) {
                                        selection.value += index
                                    } else {
                                        selection.value -= index
                                        if (selection.value.isEmpty()) {
                                            selectionMode.value = null
                                        }
                                    }
                                }
                            })

                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (selectionMode.value != null) {
                        Checkbox(
                            checked = selection.value.contains(index),
                            onCheckedChange = {
                                if (it) {
                                    selection.value += index
                                } else {
                                    selection.value -= index
                                }
                            }
                        )
                    }
                    Text(
                        color = if (select && selectionMode.value == null) MaterialTheme.colorScheme.onSecondary else Color.Unspecified,
                        text = item.label,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )

                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PlayDetailedBottomSheet(
    sortState: SortState<Episode>,
    gridCount: Int,
    onGridChange: (Int) -> Unit,
    onSortChange: (String, Boolean) -> Unit,
    onDismissRequest: () -> Unit,
) {
    var currentSelect by remember {
        mutableStateOf(0)
    }
    ModalBottomSheet(
        scrimColor = Color.Black.copy(alpha = 0.32f),
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
        contentColor = MaterialTheme.colorScheme.onSurface,
        content = {
            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.colorScheme.onSurface
            ) {
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
                            text = if (index == 0) stringResource(id = com.heyanle.easy_i18n.R.string.sort) else stringResource(
                                id = com.heyanle.easy_i18n.R.string.show
                            )
                        )
                    }) {
                    if (it == 0) {

                        SortColumn(
                            modifier = Modifier,
                            sortState = sortState, onClick = { item, state ->
                                when (state) {
                                    SortState.STATUS_OFF -> {
                                        onSortChange(item.id, false)
                                    }

                                    SortState.STATUS_ON -> {
                                        onSortChange(item.id, true)
                                    }

                                    else -> {
                                        onSortChange(item.id, false)
                                    }
                                }
                            })
                    } else if (it == 1) {
                        Column(
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        ) {
                            ListItem(
                                colors = ListItemDefaults.colors(
                                    containerColor = Color.Transparent
                                ),
                                modifier = Modifier,
                                headlineContent = {
                                    Row {
                                        Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.grid_count))
                                        Spacer(modifier = Modifier.weight(1f))
                                        Text(text = gridCount.toString())
                                    }

                                },

                                supportingContent = {
                                    Slider(
                                        value = max(
                                            gridCount,
                                            1
                                        ).toFloat(),
                                        onValueChange = {
                                            onGridChange(it.toInt())
                                        },
                                        steps = 3,
                                        valueRange = 1F..5F
                                    )
                                }
                            )
                        }

                    }
                }

            }

        })
}
