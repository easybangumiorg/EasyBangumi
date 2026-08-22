package com.heyanle.easybangumi4.v2.ui.migrate

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.plugin.api.entity.CartoonSummary
import com.heyanle.easybangumi4.ui.search_migrate.migrate.MigrateContent
import com.heyanle.easybangumi4.ui.search_migrate.migrate.MigrateGather
import com.heyanle.easybangumi4.ui.search_migrate.migrate.MigrateItemViewModel
import com.heyanle.easybangumi4.ui.search_migrate.migrate.MigrateItemViewModelFactory
import com.heyanle.easybangumi4.ui.search_migrate.migrate.MigrateViewModel
import com.heyanle.easybangumi4.ui.search_migrate.migrate.MigrateViewModelFactory
import com.heyanle.easybangumi4.v2.theme.V2Tokens
import com.heyanle.easybangumi4.v2.theme.V2Theme
import com.heyanle.easybangumi4.v2.ui.component.V2SecondaryHeader
import com.heyanle.easybangumi4.v2.ui.story.StoryEmptyV2

/** V2 migration shell. Per-item matching, source search and episode mapping reuse legacy VMs. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MigrateV2(
    summaries: List<CartoonSummary>,
    sourceKeys: List<String>,
) {
    val navController = LocalNavController.current
    val viewModel = viewModel<MigrateViewModel>(
        factory = MigrateViewModelFactory(summaries, sourceKeys)
    )
    val state by viewModel.infoListFlow.collectAsState()
    val customSearchItem = viewModel.customSearchCartoonInfo.value
    val scrollState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var pendingRemove by remember { mutableStateOf<List<CartoonInfo>?>(null) }
    var pendingMigrate by remember { mutableStateOf<List<CartoonInfo>?>(null) }

    if (customSearchItem != null) {
        val itemViewModel = viewModel<MigrateItemViewModel>(
            viewModelStoreOwner = viewModel.getOwner(customSearchItem),
            factory = MigrateItemViewModelFactory(customSearchItem, sourceKeys),
        )
        MigrateGather(
            cartoonInfo = customSearchItem,
            sourceKeys = sourceKeys,
            onBack = { viewModel.customSearchCartoonInfo.value = null },
            onClick = { cover ->
                itemViewModel.changeCover(cover)
                viewModel.customSearchCartoonInfo.value = null
            },
        )
    } else {
        BackHandler(enabled = state.selection.isNotEmpty()) { viewModel.selectExit() }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(V2Tokens.WarmBackground),
        ) {
            if (state.selection.isEmpty()) {
                V2SecondaryHeader(
                    title = "${stringResource(R.string.cartoon_migrate)} · ${state.infoList.size}",
                    onBack = navController::popBackStack,
                    largeTitle = true,
                )
            } else {
                MigrateSelectionHeaderV2(
                    count = state.selection.size,
                    onExit = viewModel::selectExit,
                    onSelectAll = viewModel::selectAll,
                    onInvert = viewModel::selectInvert,
                )
            }

            if (!state.isLoading && state.infoList.isEmpty()) {
                StoryEmptyV2(
                    title = "迁移任务已完成",
                    subtitle = "成功迁移或移除的项目不会继续显示",
                )
            } else {
                MigrateContent(
                    vm = viewModel,
                    sta = state,
                    scrollState = scrollState,
                    topAppBarScrollBehavior = scrollBehavior,
                    onDelete = { pendingRemove = it },
                )
            }

            if (!state.isLoading && state.infoList.isNotEmpty()) {
                MigrateBottomBarV2(
                    totalCount = state.infoList.size,
                    selectedCount = state.selection.size,
                    onRemove = {
                        pendingRemove = state.selection.toList()
                    },
                    onMigrate = {
                        pendingMigrate = if (state.selection.isEmpty()) {
                            state.infoList
                        } else {
                            state.selection.toList()
                        }
                    },
                )
            }
        }
    }

    pendingRemove?.let { items ->
        AlertDialog(
            onDismissRequest = { pendingRemove = null },
            containerColor = V2Tokens.Surface,
            title = { Text("从迁移任务中移除？", color = V2Tokens.TextPrimary) },
            text = {
                Text(
                    "将移除 ${items.size} 个待迁移项目，不会取消原追番，也不会删除番剧数据。",
                    color = V2Tokens.TextSecondary,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.remove(items)
                        pendingRemove = null
                    },
                ) { Text("移除", color = V2Tokens.Error) }
            },
            dismissButton = {
                TextButton(onClick = { pendingRemove = null }) {
                    Text(stringResource(R.string.cancel), color = V2Tokens.TextSecondary)
                }
            },
        )
    }

    pendingMigrate?.let { items ->
        AlertDialog(
            onDismissRequest = { pendingMigrate = null },
            containerColor = V2Tokens.Surface,
            title = { Text("确认迁移 ${items.size} 项？", color = V2Tokens.TextPrimary) },
            text = {
                Text(
                    "将转移追番标签与集数记录，单集播放位置会重置；不会下载或删除本地文件。目标已有记录时会停止该项，避免覆盖数据。",
                    color = V2Tokens.TextSecondary,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.migrate(items)
                        pendingMigrate = null
                    },
                ) { Text("开始迁移", color = V2Theme.colors.accent) }
            },
            dismissButton = {
                TextButton(onClick = { pendingMigrate = null }) {
                    Text(stringResource(R.string.cancel), color = V2Tokens.TextSecondary)
                }
            },
        )
    }
}

@Composable
private fun MigrateSelectionHeaderV2(
    count: Int,
    onExit: () -> Unit,
    onSelectAll: () -> Unit,
    onInvert: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(64.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        androidx.compose.material3.IconButton(onClick = onExit) {
            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.cancel))
        }
        Text(
            text = "已选择 $count 项",
            modifier = Modifier.weight(1f),
            color = V2Tokens.TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
        )
        TextButton(onClick = onSelectAll) { Text("全选", color = V2Theme.colors.accent) }
        TextButton(onClick = onInvert) { Text("反选", color = V2Theme.colors.accent) }
    }
    HorizontalDivider(color = V2Tokens.Divider)
}

@Composable
private fun MigrateBottomBarV2(
    totalCount: Int,
    selectedCount: Int,
    onRemove: () -> Unit,
    onMigrate: () -> Unit,
) {
    Surface(
        modifier = Modifier.navigationBarsPadding(),
        color = V2Tokens.Surface,
        tonalElevation = 0.dp,
    ) {
        Column {
            HorizontalDivider(color = V2Tokens.Divider)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = V2Tokens.ScreenHorizontalPadding),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (selectedCount == 0) "待迁移 $totalCount 项" else "已选择 $selectedCount 项",
                    modifier = Modifier.weight(1f),
                    color = V2Tokens.TextSecondary,
                    fontSize = 13.sp,
                )
                if (selectedCount > 0) {
                    Row(
                        modifier = Modifier
                            .clickable(onClick = onRemove)
                            .padding(horizontal = 10.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Filled.DeleteOutline, contentDescription = null, tint = V2Tokens.Error)
                        Spacer(Modifier.size(5.dp))
                        Text("移除", color = V2Tokens.Error, fontSize = 13.sp)
                    }
                }
                Row(
                    modifier = Modifier
                        .clickable(onClick = onMigrate)
                        .padding(horizontal = 10.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = V2Theme.colors.accent)
                    Spacer(Modifier.size(5.dp))
                    Text(
                        if (selectedCount == 0) "迁移全部" else "迁移所选",
                        color = V2Tokens.TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}
