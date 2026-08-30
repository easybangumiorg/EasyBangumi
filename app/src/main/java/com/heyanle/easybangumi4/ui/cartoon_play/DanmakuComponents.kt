package com.heyanle.easybangumi4.ui.cartoon_play

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SubtitlesOff
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.heyanle.easybangumi4.danmaku.DanmakuManualMatchState
import com.heyanle.easybangumi4.danmaku.DanmakuMatchPage
import com.heyanle.easybangumi4.danmaku.DanmakuPlaybackState
import com.heyanle.easybangumi4.danmaku.DanmakuPlaybackStatus
import com.heyanle.easybangumi4.danmaku.DanmakuBangumi
import com.heyanle.easybangumi4.danmaku.DanmakuEpisode
import com.heyanle.easybangumi4.ui.common.OkImage

@Composable
fun DanmakuSection(
    modifier: Modifier = Modifier,
    state: DanmakuPlaybackState,
    onManualMatch: () -> Unit,
    onRetry: () -> Unit,
    onOpenDisplaySettings: () -> Unit,
) {
    val sourceName = state.source?.displayName
    val cardAction = when (state.status) {
        is DanmakuPlaybackStatus.Matched,
        is DanmakuPlaybackStatus.Empty,
        is DanmakuPlaybackStatus.Unmatched,
        -> DanmakuCardAction(label = "选择弹幕", onClick = onManualMatch)

        is DanmakuPlaybackStatus.Unavailable -> DanmakuCardAction(label = "重试", onClick = onRetry)
        DanmakuPlaybackStatus.Disabled,
        DanmakuPlaybackStatus.MatchingBangumi,
        DanmakuPlaybackStatus.MatchingEpisode,
        DanmakuPlaybackStatus.LoadingComments,
        -> null
    }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "弹幕",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.weight(1f))
            IconButton(
                modifier = Modifier.testTag(PlayerPlaybackSettingsTestTags.OPEN_DANMAKU_SETTINGS),
                onClick = onOpenDisplaySettings,
            ) {
                Icon(
                    imageVector = Icons.Filled.Tune,
                    contentDescription = "弹幕显示设置",
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag(DanmakuComponentTestTags.MATCH_CARD)
                .then(
                    if (cardAction == null) {
                        Modifier
                    } else {
                        Modifier.clickable(
                            role = Role.Button,
                            onClickLabel = cardAction.label,
                            onClick = cardAction.onClick,
                        )
                    },
                ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
        ) {
            when (val status = state.status) {
                DanmakuPlaybackStatus.Disabled -> DanmakuCardContent(
                    icon = { Icon(Icons.Filled.SubtitlesOff, contentDescription = null) },
                    title = "弹幕未启用",
                    bodyLines = listOf(
                        DanmakuCardSupportingLine("可在设置中启用内置弹幕源后使用。"),
                    ),
                    showAction = false,
                )

                DanmakuPlaybackStatus.MatchingBangumi -> DanmakuLoadingContent("正在自动匹配番剧")
                DanmakuPlaybackStatus.MatchingEpisode -> DanmakuLoadingContent("正在自动匹配选集")
                DanmakuPlaybackStatus.LoadingComments -> DanmakuLoadingContent("正在加载弹幕")

                is DanmakuPlaybackStatus.Matched -> DanmakuCardContent(
                    icon = { Icon(Icons.Filled.AutoAwesome, contentDescription = null) },
                    title = "已匹配 · ${status.binding.bangumiTitle}",
                    bodyLines = listOf(
                        DanmakuCardSupportingLine(
                            text = listOfNotNull(
                                sourceName,
                                status.binding.episodeTitle,
                                "已缓存".takeIf { status.fromCache },
                            ).joinToString(" · "),
                            testTag = DanmakuComponentTestTags.MATCH_SUMMARY,
                        ),
                        DanmakuCardSupportingLine(
                            text = "${status.comments.size} 条弹幕",
                            testTag = DanmakuComponentTestTags.MATCH_COMMENT_COUNT,
                        ),
                    ),
                    showAction = true,
                )

                is DanmakuPlaybackStatus.Empty -> DanmakuCardContent(
                    icon = { Icon(Icons.Filled.ClosedCaption, contentDescription = null) },
                    title = "已匹配，但暂无弹幕",
                    bodyLines = listOf(
                        DanmakuCardSupportingLine(
                            listOfNotNull(
                                sourceName,
                                status.binding.bangumiTitle,
                                status.binding.episodeTitle,
                            ).joinToString(" · "),
                        ),
                    ),
                    showAction = true,
                )

                is DanmakuPlaybackStatus.Unmatched -> DanmakuCardContent(
                    icon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    title = "尚未匹配弹幕",
                    bodyLines = listOf(
                        DanmakuCardSupportingLine(
                            listOfNotNull(sourceName, status.message).joinToString(" · "),
                        ),
                    ),
                    showAction = true,
                )

                is DanmakuPlaybackStatus.Unavailable -> DanmakuCardContent(
                    icon = { Icon(Icons.Filled.ErrorOutline, contentDescription = null) },
                    title = "弹幕暂不可用",
                    bodyLines = listOf(DanmakuCardSupportingLine(status.message)),
                    showAction = true,
                )
            }
        }
    }
}

@Composable
private fun DanmakuLoadingContent(title: String) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(modifier = Modifier.width(20.dp), strokeWidth = 2.dp)
            Spacer(Modifier.width(12.dp))
            Text(title, style = MaterialTheme.typography.titleMedium)
        }
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    }
}

private data class DanmakuCardAction(
    val label: String,
    val onClick: () -> Unit,
)

private data class DanmakuCardSupportingLine(
    val text: String,
    val testTag: String? = null,
)

@Composable
private fun DanmakuCardContent(
    icon: @Composable () -> Unit,
    title: String,
    bodyLines: List<DanmakuCardSupportingLine>,
    showAction: Boolean,
) {
    Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon()
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            bodyLines.forEach { line ->
                Text(
                    modifier = line.testTag?.let { Modifier.testTag(it) } ?: Modifier,
                    text = line.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (showAction) {
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

internal object DanmakuComponentTestTags {
    const val MATCH_CARD = "danmaku_match_card"
    const val MATCH_SUMMARY = "danmaku_match_summary"
    const val MATCH_COMMENT_COUNT = "danmaku_match_comment_count"
    const val BANGUMI_STEP = "danmaku_match_bangumi_step"
    const val EPISODE_STEP = "danmaku_match_episode_step"
    const val CHANGE_BANGUMI = "danmaku_change_bangumi"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DanmakuMatchBottomSheet(
    state: DanmakuManualMatchState?,
    isVisible: Boolean,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onBangumiSelect: (DanmakuBangumi) -> Unit,
    onEpisodeSelect: (DanmakuEpisode) -> Unit,
    onBackToBangumiSelection: () -> Unit,
    onDismiss: () -> Unit,
    isFullScreen: Boolean = false,
) {
    if (isFullScreen) {
        // 常驻组合（宿主保证全屏期间不离开组合），visible 驱动右往左滑入/滑出动画；
        // 首次打开前面板已组合，因此滑入动画从第一次就会播放。
        FullscreenPlayerSidePanel(
            visible = isVisible && state != null,
            onDismiss = onDismiss,
        ) {
            if (state != null) {
                DanmakuMatchPanel(
                    state = state,
                    onQueryChange = onQueryChange,
                    onSearch = onSearch,
                    onBangumiSelect = onBangumiSelect,
                    onEpisodeSelect = onEpisodeSelect,
                    onBackToBangumiSelection = onBackToBangumiSelection,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    } else if (state != null && isVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            scrimColor = Color.Black.copy(alpha = 0.32f),
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
        ) {
            DanmakuMatchPanel(
                state = state,
                onQueryChange = onQueryChange,
                onSearch = onSearch,
                onBangumiSelect = onBangumiSelect,
                onEpisodeSelect = onEpisodeSelect,
                onBackToBangumiSelection = onBackToBangumiSelection,
                // 高度随内容增长、内容过长时占满 sheet 上限后内部滚动。
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 360.dp),
            )
        }
    }
}

/**
 * 匹配面板内容：标题固定在顶部，标题以下（副标题/进度/输入/候选列表）
 * 是一整块可滚动区域，切换搜索状态时不再各自跳变高度。
 * 候选列表使用普通 Column 以便与外层滚动协同。
 */
@Composable
private fun DanmakuMatchPanel(
    state: DanmakuManualMatchState,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onBangumiSelect: (DanmakuBangumi) -> Unit,
    onEpisodeSelect: (DanmakuEpisode) -> Unit,
    onBackToBangumiSelection: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "匹配弹幕",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 24.dp),
                text = "先匹配对应番剧，再为当前播放位置匹配选集。",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
            DanmakuMatchProgress(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                page = state.page,
            )

            if (state.page == DanmakuMatchPage.BANGUMI) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = state.query,
                        onValueChange = onQueryChange,
                        label = { Text("番名") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                if (!state.isSearching && state.query.isNotBlank()) onSearch()
                            },
                        ),
                    )
                }
            } else {
                SelectedDanmakuBangumi(
                    bangumi = state.selectedBangumi,
                    onChangeBangumi = onBackToBangumiSelection,
                )
            }

            state.errorMessage?.let {
                Text(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            HorizontalDivider()
            if (state.page == DanmakuMatchPage.BANGUMI) {
                DanmakuBangumiCandidates(
                    candidates = state.candidates,
                    hasSearched = state.hasSearched,
                    isSearching = state.isSearching,
                    onBangumiSelect = onBangumiSelect,
                )
            } else {
                DanmakuEpisodeCandidates(
                    isLoading = state.isLoadingEpisodes,
                    episodes = state.episodes,
                    selectedEpisodeId = state.selectedEpisode?.remoteEpisodeId,
                    onEpisodeSelect = onEpisodeSelect,
                )
            }
        }
    }
}

@Composable
private fun DanmakuMatchProgress(
    modifier: Modifier,
    page: DanmakuMatchPage,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        DanmakuMatchStep(
            modifier = Modifier.testTag(DanmakuComponentTestTags.BANGUMI_STEP),
            index = 1,
            label = "选择番剧",
            active = page == DanmakuMatchPage.BANGUMI,
        )
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        DanmakuMatchStep(
            modifier = Modifier.testTag(DanmakuComponentTestTags.EPISODE_STEP),
            index = 2,
            label = "选择选集",
            active = page == DanmakuMatchPage.EPISODE,
        )
    }
}

@Composable
private fun DanmakuMatchStep(
    modifier: Modifier = Modifier,
    index: Int,
    label: String,
    active: Boolean,
) {
    Surface(
        modifier = modifier.semantics { selected = active },
        color = if (active) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            Color.Transparent
        },
        contentColor = if (active) {
            MaterialTheme.colorScheme.onSecondaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        shape = MaterialTheme.shapes.large,
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            text = "$index  $label",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
        )
    }
}

@Composable
private fun SelectedDanmakuBangumi(
    bangumi: DanmakuBangumi?,
    onChangeBangumi: () -> Unit,
) {
    Card(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        ),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            bangumi?.let { DanmakuBangumiCover(it) }
            Spacer(Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = bangumi?.title ?: "尚未选择番剧",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                bangumi?.typeDescription?.takeIf(String::isNotBlank)?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            TextButton(
                modifier = Modifier.testTag(DanmakuComponentTestTags.CHANGE_BANGUMI),
                onClick = onChangeBangumi,
            ) {
                Text("更换番剧")
            }
        }
    }
}

@Composable
private fun DanmakuBangumiCandidates(
    modifier: Modifier = Modifier,
    candidates: List<DanmakuBangumi>,
    hasSearched: Boolean,
    isSearching: Boolean,
    onBangumiSelect: (DanmakuBangumi) -> Unit,
) {
    if (candidates.isEmpty()) {
        Text(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
            text = when {
                isSearching -> "正在搜索番剧…"
                hasSearched -> "没有找到匹配番剧，请尝试其他番名。"
                else -> "输入番名搜索，并从结果中选择对应番剧。"
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    Text(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
        text = "搜索结果 · ${candidates.size}",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
    )
    Column(modifier = modifier) {
        candidates.forEach { bangumi ->
            ListItem(
                modifier = Modifier
                    .clickable(role = Role.Button) { onBangumiSelect(bangumi) }
                    .semantics {
                        contentDescription = "选择番剧：${bangumi.title}"
                    },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                headlineContent = { Text(bangumi.title) },
                supportingContent = { Text(bangumi.typeDescription.orEmpty()) },
                leadingContent = { DanmakuBangumiCover(bangumi) },
                trailingContent = {
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = null,
                    )
                },
            )
        }
    }
}

@Composable
private fun DanmakuBangumiCover(bangumi: DanmakuBangumi) {
    val shape = MaterialTheme.shapes.small
    Box(
        modifier = Modifier
            .size(width = 52.dp, height = 72.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (bangumi.imageUrl.isNullOrBlank()) {
            Icon(
                imageVector = Icons.Filled.ImageNotSupported,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            OkImage(
                modifier = Modifier.fillMaxSize(),
                image = bangumi.imageUrl,
                contentDescription = "${bangumi.title}封面",
                contentScale = ContentScale.Crop,
                errorColor = MaterialTheme.colorScheme.surfaceVariant,
                placeholderColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
}

@Composable
private fun DanmakuEpisodeCandidates(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    episodes: List<DanmakuEpisode>,
    selectedEpisodeId: Long?,
    onEpisodeSelect: (DanmakuEpisode) -> Unit,
) {
    Text(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
        text = "选择选集",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
    )
    if (isLoading) {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp))
    }
    Column(modifier = modifier) {
        episodes.forEach { episode ->
            val isSelected = episode.remoteEpisodeId == selectedEpisodeId
            ListItem(
                modifier = Modifier
                    .selectable(
                        selected = isSelected,
                        role = Role.Button,
                        onClick = { onEpisodeSelect(episode) },
                    )
                    .semantics {
                        stateDescription = if (isSelected) "当前已选" else "未选择"
                    },
                colors = ListItemDefaults.colors(
                    containerColor = if (isSelected) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        Color.Transparent
                    },
                ),
                headlineContent = { Text(episode.episodeTitle) },
                supportingContent = { Text(episode.episodeNumber?.let { "第 $it 集" }.orEmpty()) },
                leadingContent = { Icon(Icons.Filled.ClosedCaption, contentDescription = null) },
                trailingContent = if (isSelected) {
                    {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "当前已选",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                } else {
                    null
                },
            )
        }
    }
}
