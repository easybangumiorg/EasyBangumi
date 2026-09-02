@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.heyanle.easybangumi4.v2.ui.history

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.navigationDetailed
import com.heyanle.easybangumi4.plugin.source.LocalSourceBundleController
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.CartoonPlayViewModel
import com.heyanle.easybangumi4.ui.common.LoadingPage
import com.heyanle.easybangumi4.ui.common.OkImage
import com.heyanle.easybangumi4.ui.main.history.HistoryViewModel
import com.heyanle.easybangumi4.v2.theme.V2Tokens
import com.heyanle.easybangumi4.v2.theme.V2Theme
import com.heyanle.easybangumi4.v2.ui.component.V2CountBadge
import loli.ball.easyplayer2.utils.TimeUtils
import java.util.Calendar

/** V2 history presentation backed entirely by the existing [HistoryViewModel]. */
@Composable
internal fun HistoryV2() {
    val viewModel = viewModel<HistoryViewModel>()
    val state by viewModel.stateFlow.collectAsState()
    val navController = LocalNavController.current
    val focusRequester = remember { FocusRequester() }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    BackHandler(enabled = state.selection.isNotEmpty()) {
        viewModel.onSelectionExit()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(V2Tokens.WarmBackground),
    ) {
        when {
            state.selection.isNotEmpty() -> HistorySelectionHeaderV2(
                selectedCount = state.selection.size,
                onExit = viewModel::onSelectionExit,
                onSelectAll = viewModel::onSelectAll,
                onInvert = viewModel::onSelectInvert,
                onDelete = viewModel::dialogDeleteSelection,
            )

            state.searchKey != null -> HistorySearchHeaderV2(
                text = state.searchKey.orEmpty(),
                focusRequester = focusRequester,
                onTextChange = viewModel::search,
                onSearch = viewModel::search,
                onExit = viewModel::exitSearch,
            )

            else -> HistoryHeaderV2(
                count = state.history.size,
                scrollBehavior = scrollBehavior,
                onSearch = { viewModel.search("") },
                onClear = viewModel::clearDialog,
            )
        }

        when {
            state.isLoading -> LoadingPage(
                modifier = Modifier.fillMaxSize(),
                loadingMsg = "正在加载历史记录",
            )

            else -> HistoryListV2(
                state = state,
                scrollBehavior = scrollBehavior,
                onItemClick = { cartoon ->
                    val enterData = CartoonPlayViewModel.EnterData(
                        playLineId = cartoon.lastLineId,
                        playLineLabel = cartoon.lastLineLabel,
                        playLineIndex = cartoon.lastLinesIndex,
                        episodeId = cartoon.lastEpisodeId,
                        episodeLabel = cartoon.lastEpisodeLabel,
                        episodeOrder = cartoon.lastEpisodeOrder,
                        episodeIndex = cartoon.lastEpisodeIndex,
                        adviceProgress = cartoon.lastProcessTime,
                    )
                    navController.navigationDetailed(cartoon.id, cartoon.source, enterData)
                },
                onToggleSelection = viewModel::onSelectionChange,
                onLongPress = viewModel::onSelectionLongPress,
                onDeleteOne = { cartoon ->
                    viewModel.onSelectionExit()
                    viewModel.dialogDeleteOne(cartoon)
                },
            )
        }
    }

    when (val dialog = state.dialog) {
        is HistoryViewModel.Dialog.Delete -> HistoryConfirmDialogV2(
            title = "删除历史记录",
            message = "确定删除选中的 ${dialog.selection.size} 条历史记录吗？",
            confirmLabel = "删除",
            onConfirm = {
                viewModel.delete(dialog.selection.toList())
                viewModel.dialogDismiss()
                viewModel.onSelectionExit()
            },
            onDismiss = viewModel::dialogDismiss,
        )

        is HistoryViewModel.Dialog.Clear -> HistoryConfirmDialogV2(
            title = "清空历史记录",
            message = "此操作会删除全部播放历史，且无法撤销。",
            confirmLabel = "清空",
            onConfirm = {
                viewModel.clear()
                viewModel.dialogDismiss()
                viewModel.onSelectionExit()
            },
            onDismiss = viewModel::dialogDismiss,
        )

        null -> Unit
    }
}

@Composable
private fun HistoryHeaderV2(
    count: Int,
    scrollBehavior: TopAppBarScrollBehavior,
    onSearch: () -> Unit,
    onClear: () -> Unit,
) {
    TopAppBar(
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = V2Tokens.WarmBackground,
            scrolledContainerColor = V2Tokens.SurfaceMuted,
        ),
        title = {
            Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            ) {
            Text(
                text = stringResource(R.string.history),
                color = V2Tokens.TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )
            V2CountBadge(count)
            }
        },
        actions = {
            IconButton(onClick = onSearch) {
            Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.search))
            }
            IconButton(onClick = onClear) {
            Icon(Icons.Filled.DeleteSweep, contentDescription = stringResource(R.string.clear))
            }
        },
    )
}

@Composable
private fun HistorySearchHeaderV2(
    text: String,
    focusRequester: FocusRequester,
    onTextChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onExit: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(64.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onExit) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
        }
        TextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            placeholder = { Text(stringResource(R.string.please_input_keyword_to_search)) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch(text) }),
            trailingIcon = {
                if (text.isNotEmpty()) {
                    IconButton(onClick = { onTextChange("") }) {
                        Icon(Icons.Filled.Clear, contentDescription = stringResource(R.string.clear))
                    }
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = V2Tokens.Surface,
                unfocusedContainerColor = V2Tokens.Surface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
        )
    }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
}

@Composable
private fun HistorySelectionHeaderV2(
    selectedCount: Int,
    onExit: () -> Unit,
    onSelectAll: () -> Unit,
    onInvert: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onExit) {
            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.cancel))
        }
        Text(
            text = "已选择 $selectedCount 项",
            modifier = Modifier.weight(1f),
            color = V2Tokens.TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
        )
        TextButton(onClick = onSelectAll) { Text("全选", color = V2Theme.colors.accent) }
        TextButton(onClick = onInvert) { Text("反选", color = V2Theme.colors.accent) }
        IconButton(onClick = onDelete) {
            Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.delete), tint = V2Tokens.Error)
        }
    }
    HorizontalDivider(color = V2Tokens.Divider)
}

@Composable
private fun HistoryListV2(
    state: HistoryViewModel.HistoryState,
    scrollBehavior: TopAppBarScrollBehavior,
    onItemClick: (CartoonInfo) -> Unit,
    onToggleSelection: (CartoonInfo) -> Unit,
    onLongPress: (CartoonInfo) -> Unit,
    onDeleteOne: (CartoonInfo) -> Unit,
) {
    if (state.history.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = if (state.searchKey.isNullOrEmpty()) "还没有播放历史" else "没有匹配的历史记录",
                color = V2Tokens.TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.size(8.dp))
            Text("播放过的番剧会显示在这里", color = V2Tokens.TextSecondary, fontSize = 14.sp)
        }
        return
    }

    val hapticFeedback = LocalHapticFeedback.current
    LazyColumn(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        contentPadding = PaddingValues(bottom = 96.dp),
    ) {
        if (state.isInPrivate) {
            item {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Filled.Lock,
                        contentDescription = null,
                        tint = V2Theme.colors.accent,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.size(6.dp))
                    Text(stringResource(R.string.now_in_private), color = V2Tokens.TextSecondary, fontSize = 13.sp)
                }
            }
        }
        items(state.history, key = { "${it.source}:${it.id}" }) { cartoon ->
            HistoryItemV2(
                cartoon = cartoon,
                selected = cartoon in state.selection,
                showDelete = state.selection.isEmpty(),
                onClick = {
                    if (state.selection.isEmpty()) onItemClick(cartoon) else onToggleSelection(cartoon)
                },
                onLongPress = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongPress(cartoon)
                },
                onDelete = { onDeleteOne(cartoon) },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HistoryItemV2(
    cartoon: CartoonInfo,
    selected: Boolean,
    showDelete: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    onDelete: () -> Unit,
) {
    val sourceController = LocalSourceBundleController.current
    val sourceLabel = sourceController.source(cartoon.source)?.label ?: cartoon.source
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (selected) V2Theme.colors.accentContainer else V2Tokens.WarmBackground)
            .combinedClickable(onClick = onClick, onLongClick = onLongPress)
            .padding(horizontal = V2Tokens.ScreenHorizontalPadding, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(width = 84.dp, height = 64.dp)
                .clip(RoundedCornerShape(7.dp)),
        ) {
            OkImage(
                image = cartoon.coverUrl,
                contentDescription = cartoon.name,
                modifier = Modifier.fillMaxSize(),
                errorRes = com.heyanle.easybangumi4.R.drawable.placeholder,
            )
            if (selected) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = V2Theme.colors.accent,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp),
                )
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = cartoon.name,
                color = V2Tokens.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "观看至 ${cartoon.lastEpisodeLabel.ifBlank { "当前视频" }}",
                    modifier = Modifier.weight(1f),
                    color = V2Tokens.TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = historyProgressText(
                        positionMs = cartoon.lastProcessTime,
                        durationMs = cartoon.lastTotalTile,
                    ),
                    color = V2Tokens.TextSecondary,
                    fontSize = 12.sp,
                )
            }
            LinearProgressIndicator(
                progress = {
                    historyProgressFraction(
                        positionMs = cartoon.lastProcessTime,
                        durationMs = cartoon.lastTotalTile,
                    )
                },
                modifier = Modifier.fillMaxWidth().height(2.dp),
                color = V2Theme.colors.accent,
                trackColor = V2Tokens.Divider,
            )
        }
        if (showDelete) {
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.delete), tint = V2Tokens.TextSecondary)
            }
        }
    }
    HorizontalDivider(
        modifier = Modifier.padding(start = 116.dp),
        color = V2Tokens.Divider,
    )
}

internal fun historyProgressText(positionMs: Long, durationMs: Long): String {
    val position = positionMs.coerceAtLeast(0L)
    val duration = durationMs.coerceAtLeast(0L)
    val positionText = TimeUtils.toString(position).toString()
    return if (duration > 0L) {
        "$positionText / ${TimeUtils.toString(duration)}"
    } else {
        positionText
    }
}

internal fun historyProgressFraction(positionMs: Long, durationMs: Long): Float {
    if (durationMs <= 0L) return 0f
    return (positionMs.toFloat() / durationMs).coerceIn(0f, 1f)
}

private fun historyDayLabel(timestamp: Long): String {
    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply { timeInMillis = timestamp }
    val sameYear = now.get(Calendar.YEAR) == target.get(Calendar.YEAR)
    val dayDiff = now.get(Calendar.DAY_OF_YEAR) - target.get(Calendar.DAY_OF_YEAR)
    return when {
        sameYear && dayDiff == 0 -> "今天"
        sameYear && dayDiff == 1 -> "昨天"
        sameYear -> "${target.get(Calendar.MONTH) + 1}月${target.get(Calendar.DAY_OF_MONTH)}日"
        else -> "${target.get(Calendar.YEAR)}年${target.get(Calendar.MONTH) + 1}月${target.get(Calendar.DAY_OF_MONTH)}日"
    }
}

@Composable
private fun HistoryConfirmDialogV2(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = V2Tokens.Surface,
        title = { Text(title, color = V2Tokens.TextPrimary) },
        text = { Text(message, color = V2Tokens.TextSecondary) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(confirmLabel, color = V2Tokens.Error) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消", color = V2Tokens.TextPrimary) }
        },
    )
}
