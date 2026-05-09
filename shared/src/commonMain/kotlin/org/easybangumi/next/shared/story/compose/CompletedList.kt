package org.easybangumi.next.shared.story.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.easybangumi.next.shared.download.model.DownloadInfo
import org.easybangumi.next.shared.local.LocalCartoonItem

/**
 * 已完成列表
 */
@Composable
fun CompletedList(
    items: List<DownloadInfo>,
    onRemove: (String) -> Unit,
) {
    if (items.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text("没有已完成的下载", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(items, key = { it.req.uuid }) { info ->
            CompletedItem(
                info = info,
                onRemove = { onRemove(info.req.uuid) },
            )
        }
    }
}

/**
 * 已完成列表项
 */
@Composable
private fun CompletedItem(
    info: DownloadInfo,
    onRemove: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = info.req.fromCartoonName,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "第${info.req.toEpisode}集 ${info.req.toEpisodeTitle}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(onClick = onRemove) {
                Text("移除")
            }
        }
    }
}

/**
 * 本地番剧列表
 */
@Composable
fun LocalAnimeList(
    items: List<LocalCartoonItem>,
    onPlay: (LocalCartoonItem) -> Unit,
    onDelete: (LocalCartoonItem) -> Unit,
    onRefresh: () -> Unit,
) {
    if (items.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text("没有本地番剧", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(items, key = { it.itemId }) { item ->
            LocalAnimeItem(
                item = item,
                onPlay = { onPlay(item) },
                onDelete = { onDelete(item) },
            )
        }
    }
}

/**
 * 本地番剧列表项
 */
@Composable
private fun LocalAnimeItem(
    item: LocalCartoonItem,
    onPlay: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        onClick = onPlay,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "${item.episodes.size} 集",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (item.tags.isNotEmpty()) {
                    Text(
                        text = item.tags.joinToString(", "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            TextButton(onClick = onDelete) {
                Text("删除")
            }
        }
    }
}
