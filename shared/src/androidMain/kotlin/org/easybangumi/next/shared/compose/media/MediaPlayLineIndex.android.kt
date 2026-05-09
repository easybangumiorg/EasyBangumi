package org.easybangumi.next.shared.compose.media

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayLesson
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.shared.foundation.elements.ErrorElements
import org.easybangumi.next.shared.foundation.elements.LoadingElements

/**
 * 剧集优先模式的 Android UI 组件
 */
@OptIn(ExperimentalMaterial3Api::class)
fun LazyListScope.mediaEpisodeFirstIndexAndroid(
    vm: PlayLineIndexVM,
    state: PlayLineIndexVM.State,
) {
    // 剧集选择区域
    val episodeData = state.episodeList.okOrNull()
    if (state.episodeList.isLoading()) {
        item {
            LoadingElements(
                modifier = Modifier.fillMaxWidth().height(100.dp).padding(8.dp, 0.dp),
                isRow = true
            )
        }
    } else if (state.episodeList is DataState.Error) {
        val errorMsg = state.episodeList.errorMsg
        item {
            ErrorElements(
                modifier = Modifier.fillMaxWidth().height(100.dp).padding(8.dp, 0.dp),
                errorMsg = errorMsg,
                isRow = true
            )
        }
    } else if (episodeData != null) {
        item {
            Column {
                val showEpisodeSheet = remember { mutableStateOf(false) }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp, 0.dp)
                ) {
                    Icon(
                        Icons.Filled.PlayLesson,
                        contentDescription = "",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        "选集",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = { showEpisodeSheet.value = true }) {
                        Text("更多")
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(8.dp, 0.dp)
                ) {
                    items(episodeData.size) { index ->
                        val item = episodeData[index]
                        FilterChip(
                            elevation = null,
                            selected = index == state.currentShowingEpisode,
                            onClick = {
                                vm.onShowingEpisodeSelected(index)
                                vm.onEpisodeSimpleSelected(index)
                            },
                            label = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (index == state.currentEpisodeIndex) {
                                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                                        Spacer(Modifier.size(4.dp))
                                    }
                                    Text(item.label)
                                }
                            },
                            modifier = Modifier
                        )
                    }
                }

                // BottomSheet for episode selection
                if (showEpisodeSheet.value) {
                    ModalBottomSheet(
                        onDismissRequest = { showEpisodeSheet.value = false },
                        sheetState = rememberModalBottomSheetState()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                "全部剧集",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            // Grid-like layout for episodes
                            val chunkedEpisodes = episodeData.chunked(4)
                            chunkedEpisodes.forEach { rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowItems.forEach { item ->
                                        val index = episodeData.indexOf(item)
                                        val isSelected = index == state.currentEpisodeIndex
                                        FilterChip(
                                            elevation = null,
                                            selected = isSelected,
                                            onClick = {
                                                vm.onShowingEpisodeSelected(index)
                                                vm.onEpisodeSimpleSelected(index)
                                                showEpisodeSheet.value = false
                                            },
                                            label = { Text(item.label) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    // Fill remaining space if row is not full
                                    repeat(4 - rowItems.size) {
                                        Spacer(Modifier.weight(1f))
                                    }
                                }
                                Spacer(Modifier.size(8.dp))
                            }

                            Spacer(Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }

    // 播放线路选择区域
    val playLineData = state.episodePlayLineList.okOrNull()
    if (state.episodePlayLineList.isLoading()) {
        item {
            LoadingElements(
                modifier = Modifier.fillMaxWidth().height(100.dp).padding(8.dp, 0.dp),
                isRow = true
            )
        }
    } else if (state.episodePlayLineList is DataState.Error) {
        val errorMsg = state.episodePlayLineList.errorMsg
        item {
            ErrorElements(
                modifier = Modifier.fillMaxWidth().height(100.dp).padding(8.dp, 0.dp),
                errorMsg = errorMsg,
                isRow = true
            )
        }
    } else if (playLineData != null) {
        item {
            Column {
                val showPlayLineSheet = remember { mutableStateOf(false) }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp, 0.dp)
                ) {
                    Icon(
                        Icons.Filled.PlaylistPlay,
                        contentDescription = "",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        "播放源",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = { showPlayLineSheet.value = true }) {
                        Text("更多")
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(8.dp, 0.dp)
                ) {
                    items(playLineData.size) { index ->
                        val item = playLineData[index]
                        FilterChip(
                            elevation = null,
                            selected = index == state.currentShowingEpisodePlayLine,
                            onClick = {
                                vm.onShowingEpisodePlayLineSelected(index)
                                vm.onPlayLineSimpleSelected(index)
                            },
                            label = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (index == state.currentEpisodePlayLine) {
                                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                                        Spacer(Modifier.size(4.dp))
                                    }
                                    Text(item.label)
                                }
                            },
                            modifier = Modifier
                        )
                    }
                }

                // BottomSheet for play line selection
                if (showPlayLineSheet.value) {
                    ModalBottomSheet(
                        onDismissRequest = { showPlayLineSheet.value = false },
                        sheetState = rememberModalBottomSheetState()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                "全部播放源",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            // Grid-like layout for play lines
                            val chunkedPlayLines = playLineData.chunked(3)
                            chunkedPlayLines.forEach { rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowItems.forEach { item ->
                                        val index = playLineData.indexOf(item)
                                        val isSelected = index == state.currentEpisodePlayLine
                                        FilterChip(
                                            elevation = null,
                                            selected = isSelected,
                                            onClick = {
                                                vm.onShowingEpisodePlayLineSelected(index)
                                                vm.onPlayLineSimpleSelected(index)
                                                showPlayLineSheet.value = false
                                            },
                                            label = { Text(item.label) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    // Fill remaining space if row is not full
                                    repeat(3 - rowItems.size) {
                                        Spacer(Modifier.weight(1f))
                                    }
                                }
                                Spacer(Modifier.size(8.dp))
                            }

                            Spacer(Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
