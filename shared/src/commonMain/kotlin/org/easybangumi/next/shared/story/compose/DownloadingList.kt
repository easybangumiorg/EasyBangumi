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
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.easybangumi.next.shared.download.model.DownloadInfo
import org.easybangumi.next.shared.download.model.DownloadState

/**
 * 下载中列表
 */
@Composable
fun DownloadingList(
    items: List<DownloadInfo>,
    onPause: (String) -> Unit,
    onResume: (String) -> Unit,
    onCancel: (String) -> Unit,
) {
    if (items.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text("没有正在下载的任务", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(items, key = { it.req.uuid }) { info ->
            DownloadingItem(
                info = info,
                onPause = { onPause(info.req.uuid) },
                onResume = { onResume(info.req.uuid) },
                onCancel = { onCancel(info.req.uuid) },
            )
        }
    }
}

/**
 * 下载中列表项
 */
@Composable
private fun DownloadingItem(
    info: DownloadInfo,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 番剧名称
            Text(
                text = info.req.fromCartoonName,
                style = MaterialTheme.typography.titleMedium,
            )

            // 剧集信息
            Text(
                text = "第${info.req.toEpisode}集 ${info.req.toEpisodeTitle}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // 进度条
            if (info.runtime != null) {
                val progress = info.runtime.progress
                if (progress >= 0) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    )
                } else {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    )
                }

                // 状态文本
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = info.runtime.statusText,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    if (info.runtime.subStatusText.isNotEmpty()) {
                        Text(
                            text = info.runtime.subStatusText,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }

            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                if (info.runtime?.state == DownloadState.PAUSED) {
                    TextButton(onClick = onResume) {
                        Text("恢复")
                    }
                } else if (info.runtime?.state == DownloadState.DOING) {
                    TextButton(onClick = onPause) {
                        Text("暂停")
                    }
                }
                TextButton(onClick = onCancel) {
                    Text("取消")
                }
            }
        }
    }
}
