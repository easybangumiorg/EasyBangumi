package com.heyanle.easybangumi4.v2.ui.story

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heyanle.easybangumi4.R
import com.heyanle.easybangumi4.cartoon.entity.CartoonDownloadInfo
import com.heyanle.easybangumi4.cartoon.story.download.runtime.CartoonDownloadRuntime
import com.heyanle.easybangumi4.ui.common.OkImage
import com.heyanle.easybangumi4.ui.story.download.DownloadViewModel
import com.heyanle.easybangumi4.v2.theme.V2Tokens
import com.heyanle.easybangumi4.v2.theme.V2Theme

@Composable
internal fun DownloadTasksV2(
    state: DownloadViewModel.State,
    onItemClick: (CartoonDownloadInfo) -> Unit,
    onItemLongPress: (CartoonDownloadInfo) -> Unit,
) {
    when {
        state.loading -> StoryLoadingV2("正在读取下载任务")
        state.errorMessage != null -> StoryEmptyV2(
            title = "读取下载任务失败",
            subtitle = state.errorMessage,
            titleColor = V2Tokens.Error,
        )
        state.downloadInfo.isEmpty() -> StoryEmptyV2(
            title = "暂无下载任务",
            subtitle = "在番剧详情页选择剧集下载",
        )
        else -> {
            val haptic = LocalHapticFeedback.current
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 0.dp,
                    top = 0.dp,
                    end = 0.dp,
                    bottom = 24.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                items(state.downloadInfo, key = { it.req.uuid }) { item ->
                    DownloadTaskCardV2(
                        item = item,
                        selected = item.req.uuid in state.selectionIds,
                        engineName = state.quickDownloadEngines
                            .firstOrNull { it.id == item.req.quickDownloadEngineId }
                            ?.displayName,
                        onClick = { onItemClick(item) },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onItemLongPress(item)
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DownloadTaskCardV2(
    item: CartoonDownloadInfo,
    selected: Boolean,
    engineName: String?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val shape = RoundedCornerShape(0.dp)
    val runtime = item.runtime
    val progressInfo = runtime?.getDownloadInfo()
    val busStatus = progressInfo?.status?.value.orEmpty()
    val busDetail = progressInfo?.subStatus?.value.orEmpty()
    val rawProgress = progressInfo?.process?.value
    val determinate = rawProgress != null && rawProgress.isFinite() && rawProgress >= 0f
    val progress = rawProgress?.coerceIn(0f, 1f) ?: 0f
    val presentation = downloadPresentation(runtime, busStatus, busDetail)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (selected) 1.5.dp else 0.dp,
                color = if (selected) V2Theme.colors.accent else Color.Transparent,
                shape = shape,
            )
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        color = if (selected) V2Theme.colors.accentContainer else V2Tokens.WarmBackground,
        contentColor = V2Tokens.TextPrimary,
        shape = shape,
        tonalElevation = 0.dp,
    ) {
        Row(modifier = Modifier.padding(horizontal = V2Tokens.ScreenHorizontalPadding, vertical = 10.dp)) {
            Box(
                modifier = Modifier
                    .width(72.dp)
                    .aspectRatio(19f / 13.5f),
            ) {
                OkImage(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(V2Tokens.Divider, RoundedCornerShape(10.dp)),
                    image = item.req.localItem.cartoonCover.coverUrl.orEmpty(),
                    crossFade = false,
                    contentDescription = item.req.localItem.title,
                    errorRes = R.drawable.placeholder,
                )
                if (selected) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(26.dp),
                        color = V2Theme.colors.accent,
                        shape = CircleShape,
                    ) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = "已选择",
                            tint = V2Tokens.Surface,
                            modifier = Modifier.padding(5.dp),
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.req.localItem.title,
                        modifier = Modifier.weight(1f),
                        color = V2Tokens.TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Surface(
                        color = V2Theme.colors.accentContainer,
                        shape = RoundedCornerShape(6.dp),
                    ) {
                        Text(
                            text = if (item.req.quickMode) {
                                "快速 · ${engineName ?: "未知引擎"}"
                            } else {
                                "完整模式"
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            color = V2Tokens.TextPrimary,
                            fontSize = 9.sp,
                            maxLines = 1,
                        )
                    }
                }
                Text(
                    text = "编号 ${item.req.toEpisode} · ${item.req.toEpisodeTitle}",
                    modifier = Modifier.padding(top = 5.dp),
                    color = V2Tokens.TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(9.dp))
                Text(
                    text = presentation.status,
                    color = when (presentation.tone) {
                        DownloadPresentationTone.Normal -> V2Tokens.TextPrimary
                        DownloadPresentationTone.Secondary -> V2Tokens.TextSecondary
                        DownloadPresentationTone.Error -> V2Tokens.Error
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (presentation.detail.isNotBlank()) {
                    Text(
                        text = presentation.detail,
                        modifier = Modifier.padding(top = 2.dp),
                        color = V2Tokens.TextSecondary,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.height(7.dp))
                when {
                    runtime == null || runtime.state == CartoonDownloadRuntime.State.CANCEL -> Unit
                    runtime.state == CartoonDownloadRuntime.State.ERROR -> {
                        LinearProgressIndicator(
                            progress = { 0f },
                            modifier = Modifier.fillMaxWidth(),
                            color = V2Tokens.Divider,
                            trackColor = V2Tokens.Divider,
                        )
                    }
                    determinate -> LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth(),
                        color = V2Theme.colors.accent,
                        trackColor = V2Tokens.Divider,
                    )
                    else -> LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = V2Theme.colors.accent,
                        trackColor = V2Tokens.Divider,
                    )
                }
            }
        }
    }
}

private data class DownloadPresentation(
    val status: String,
    val detail: String,
    val tone: DownloadPresentationTone = DownloadPresentationTone.Normal,
)

private enum class DownloadPresentationTone { Normal, Secondary, Error }

private fun downloadPresentation(
    runtime: CartoonDownloadRuntime?,
    busStatus: String,
    busDetail: String,
): DownloadPresentation = when (runtime?.state) {
    null -> DownloadPresentation("下载已中断", "轻触恢复下载", DownloadPresentationTone.Secondary)
    CartoonDownloadRuntime.State.WAITING -> DownloadPresentation(
        busStatus.ifBlank { "等待中" },
        busDetail.ifBlank { "正在排队" },
    )
    CartoonDownloadRuntime.State.STEP_COMPLETELY -> DownloadPresentation(
        "准备下一步",
        busDetail,
    )
    CartoonDownloadRuntime.State.DOING -> DownloadPresentation(
        busStatus.ifBlank { "处理中" },
        busDetail,
    )
    CartoonDownloadRuntime.State.PAUSED -> DownloadPresentation(
        "已暂停",
        "轻触选择恢复方式",
        DownloadPresentationTone.Secondary,
    )
    CartoonDownloadRuntime.State.ERROR -> DownloadPresentation(
        "下载失败",
        runtime.errorMsg.ifBlank { busStatus.ifBlank { "轻触重试" } },
        DownloadPresentationTone.Error,
    )
    CartoonDownloadRuntime.State.CANCEL -> DownloadPresentation(
        "正在取消",
        "任务即将从列表移除",
        DownloadPresentationTone.Secondary,
    )
    CartoonDownloadRuntime.State.SUCCESS -> DownloadPresentation(
        "已完成",
        "正在写入本地番剧",
    )
}
