package org.easybangumi.next.shared.compose.media

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.ArrowRightAlt
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayForWork
import androidx.compose.material.icons.filled.PlayLesson
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.room.util.TableInfo
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.shared.compose.media.bangumi.detail.divider
import org.easybangumi.next.shared.compose.media.bangumi.detail.space
import org.easybangumi.next.shared.foundation.elements.ErrorElements
import org.easybangumi.next.shared.foundation.elements.LoadingElements
import org.easybangumi.next.shared.foundation.lazy.itemsFromGrid

fun LazyListScope.mediaPlayLineIndexDesktop(
    vm: PlayLineIndexVM,
    state: PlayLineIndexVM.State,
    gridCount: Int = 2,
    width: Int,
){
    val data = state.playerLineList.okOrNull()
    if (state.playerLineList.isLoading()) {
        item {
            LoadingElements(
                modifier = Modifier.fillMaxWidth().height(200.dp).padding(8.dp, 0.dp),
                isRow = true
            )
        }
    } else if (state.playerLineList is DataState.Error) {
        val errorMsg = state.playerLineList.errorMsg
        item{
            ErrorElements(
                modifier = Modifier.fillMaxWidth().height(200.dp).padding(8.dp, 0.dp),
                errorMsg = errorMsg,
                isRow = true
            )
        }
    } else if(data != null) {
        item {
            Column(
                modifier = Modifier.padding(8.dp, 0.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(8.dp))

            ) {
                val scope = rememberCoroutineScope()
                val showAll = remember {
                    mutableStateOf(false)
                }
                val lazyListState = rememberLazyListState()
                Spacer(Modifier.size(16.dp))
                Row (
                    verticalAlignment = Alignment.CenterVertically,
                ){
                    Spacer(Modifier.size(16.dp))
                    Icon(Icons.Filled.PlaylistPlay, contentDescription = "", modifier = Modifier.size(20.dp))
                    Spacer(Modifier.size(8.dp))
                    Text(
                        "播放线路",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.weight(1f))
                    if ((data.size ?: 0) > 1) {
                        Icon(
                            Icons.Default.KeyboardArrowLeft,
                            contentDescription = "",
                            modifier = Modifier.size(24.dp).clip(CircleShape).clickable {
                                scope.launch {
                                    runCatching {
                                        logger().info("scroll back, width: $width")
                                        if ((width - 64) > 0) {
                                            lazyListState.animateScrollBy(-(width - 64).toFloat())
                                        } else {
                                            lazyListState.animateScrollToItem(lazyListState.firstVisibleItemIndex - 1)
                                        }
                                    }.onFailure {
                                        it.printStackTrace()
                                    }

                                }
                            }
                        )
                        Icon(
                            Icons.Default.KeyboardArrowRight,
                            contentDescription = "",
                            modifier = Modifier.size(24.dp).clip(CircleShape).clickable {
                                scope.launch {
                                    runCatching {
                                        if ((width - 64) > 0) {
                                            lazyListState.animateScrollBy((width - 64).toFloat())
                                        } else {
                                            lazyListState.animateScrollToItem(lazyListState.firstVisibleItemIndex + 1)
                                        }
                                    }.onFailure {
                                        it.printStackTrace()
                                    }

                                }

                            }
                        )
                        Icon(
                            if (showAll.value) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "",
                            modifier = Modifier.size(24.dp).clip(CircleShape).clickable {
                                showAll.value = !showAll.value
                            }
                        )
                        Spacer(Modifier.size(12.dp))
                    }

                }
                Spacer(Modifier.size(8.dp))
                val list = data
                AnimatedContent(showAll.value && list.size > 1) {
                    if (it) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(8.dp, 0.dp)
                        ) {
                            list.forEachIndexed  { it, item ->
                                val isSelected =  it == state.currentShowingPlayerLine
                                Row(modifier = Modifier
//                                .widthIn(max = 120.dp)
                                    .run {
                                        if (isSelected) {
                                            background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(6.dp))
                                        } else {
                                            border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
                                        }
                                    }
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable {
                                        vm.onShowingPlayLineSelected(it)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        if (item == state.playLineOrNull) {
                                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                                            Spacer(Modifier.size(4.dp))
                                        }
                                        Text(item.label,
                                            color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else Color.Unspecified,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        LazyRow(
                            modifier = Modifier,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            state = lazyListState,
                            contentPadding = PaddingValues(8.dp, 0.dp)
                        ) {

                            items(list.size) {
                                val item = list[it]
                                val isSelected =  it == state.currentShowingPlayerLine
                                Row(modifier = Modifier
//                                .widthIn(max = 120.dp)
                                    .run {
                                        if (isSelected) {
                                            background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(6.dp))
                                        } else {
                                            border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
                                        }
                                    }
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable {
                                        vm.onShowingPlayLineSelected(it)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        if (item == state.playLineOrNull) {
                                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                                            Spacer(Modifier.size(4.dp))
                                        }
                                        Text(item.label,
                                            color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else Color.Unspecified,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.size(16.dp))
            }

            Spacer(Modifier.size(8.dp))




        }
        divider()
        space(Modifier.height(8.dp))
//        divider()
        item {
            Row (
                verticalAlignment = Alignment.CenterVertically,
            ){
                Spacer(Modifier.size(8.dp))
                Text(
                    "选集",
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.weight(1f))
                Icon(
                    Icons.Default.Sort,
                    contentDescription = "",
                    modifier = Modifier.size(24.dp).clip(CircleShape).clickable {
//                        showAll.value = !showAll.value
                    }
                )
                Spacer(Modifier.size(8.dp))
            }
        }
        space(Modifier.height(8.dp))

        val currentPlayLine = state.showingPlayerLine
        if (currentPlayLine != null) {
            itemsFromGrid(
                itemsCount = currentPlayLine.episodeList.size,
                girdCount = gridCount,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                rowModifier = Modifier.padding(8.dp, 0.dp)
            ) {
                val item = currentPlayLine.episodeList[it]
                Card(
                    modifier = Modifier.fillMaxWidth().padding(0.dp, 4.dp).clip(CardDefaults.shape).clickable {
                        vm.onEpisodeSelected(state.currentShowingPlayerLine, it)
                    },
                    colors = CardDefaults.cardColors().copy(
                        containerColor = if(item == state.currentEpisodeOrNull) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = if(item == state.currentEpisodeOrNull) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    ),
                ) {
                    Text(text = item.label, modifier = Modifier.padding(8.dp, 8.dp, 0.dp, 24.dp))
                }
            }
        }
    }
}