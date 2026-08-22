package com.heyanle.easybangumi4.v2.ui.story

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.cartoon.entity.CartoonStoryItem
import com.heyanle.easybangumi4.plugin.api.entity.toIdentify
import com.heyanle.easybangumi4.navigationDetailed
import com.heyanle.easybangumi4.ui.common.cover_star.CoverStarViewModel
import com.heyanle.easybangumi4.ui.story.download.DownloadViewModel
import com.heyanle.easybangumi4.ui.story.local.LocalViewModel
import com.heyanle.easybangumi4.v2.theme.V2Tokens
import com.heyanle.easybangumi4.v2.theme.V2Theme
import com.heyanle.easybangumi4.v2.ui.component.V2ScrollableTabs
import com.heyanle.easybangumi4.v2.ui.component.V2TabStyle
import com.heyanle.easybangumi4.v2.ui.component.V2SecondaryHeader
import kotlinx.coroutines.launch

/**
 * V2 container for local media and download tasks.
 *
 * Local and download state remain route-scoped in the original ViewModels. This composable owns
 * only transient presentation state, such as confirmation snapshots, so cancellation never
 * mutates the legacy selection state.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun StoryV2(initialPage: Int = 0) {
    val navController = LocalNavController.current
    val localViewModel = viewModel<LocalViewModel>()
    val downloadViewModel = viewModel<DownloadViewModel>()
    val coverStarViewModel = viewModel<CoverStarViewModel>()
    val localState by localViewModel.state.collectAsState()
    val downloadState by downloadViewModel.state.collectAsState()
    val starState by coverStarViewModel.stateFlow.collectAsState()
    val pagerState = rememberPagerState(initialPage = initialPage.coerceIn(0, 1)) { 2 }
    val scope = rememberCoroutineScope()
    val localSelectionActive = localState.selection.isNotEmpty()
    val downloadSelectionActive = downloadState.selectionIds.isNotEmpty()
    val selectionActive = localSelectionActive || downloadSelectionActive
    val handlesBack = if (pagerState.currentPage == 0) {
        localSelectionActive || localState.searchKey != null
    } else {
        downloadSelectionActive
    }
    val focusRequester = remember { FocusRequester() }
    var pendingLocalDelete by remember { mutableStateOf<Set<CartoonStoryItem>?>(null) }
    var pendingDownloadDelete by remember { mutableStateOf<Set<String>?>(null) }

    BackHandler(enabled = handlesBack) {
        if (pagerState.currentPage == 0) {
            if (localSelectionActive) localViewModel.clearSelection() else localViewModel.changeKey(null)
        } else if (downloadSelectionActive) {
            downloadViewModel.clearSelection()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(V2Tokens.WarmBackground),
    ) {
        when {
            pagerState.currentPage == 0 && localSelectionActive -> StorySelectionHeaderV2(
                selectedCount = localState.selection.size,
                onExit = localViewModel::clearSelection,
                onSelectAll = localViewModel::selectAll,
                onInvert = localViewModel::selectInvert,
            )

            pagerState.currentPage == 1 && downloadSelectionActive -> StorySelectionHeaderV2(
                selectedCount = downloadState.selectionIds.size,
                onExit = downloadViewModel::clearSelection,
                onSelectAll = downloadViewModel::selectAll,
            )

            pagerState.currentPage == 0 && localState.searchKey != null -> StorySearchHeaderV2(
                text = localState.searchKey.orEmpty(),
                focusRequester = focusRequester,
                onTextChange = localViewModel::changeKey,
                onExit = { localViewModel.changeKey(null) },
            )

            else -> V2SecondaryHeader(
                title = "本地与下载",
                onBack = navController::popBackStack,
                largeTitle = true,
                actions = {
                    if (pagerState.currentPage == 0) {
                        IconButton(onClick = { localViewModel.changeKey("") }) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = stringResource(R.string.search),
                                tint = V2Tokens.TextPrimary,
                            )
                        }
                    }
                },
            )
        }

        V2ScrollableTabs(
            labels = listOf("本地番剧", "下载任务"),
            selectedIndex = pagerState.currentPage,
            onSelected = { page ->
                if (!selectionActive) {
                    scope.launch { pagerState.animateScrollToPage(page) }
                }
            },
            style = V2TabStyle.PrimaryUnderline,
        )
        HorizontalDivider(color = V2Tokens.Divider)

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            userScrollEnabled = !selectionActive,
        ) { page ->
            when (page) {
                0 -> LocalLibraryV2(
                    state = localState,
                    starredIdentities = starState.identifySet,
                    onItemClick = { item ->
                        if (localState.selection.isEmpty()) {
                            navController.navigationDetailed(item.cartoonLocalItem.cartoonCover)
                        } else {
                            localViewModel.selectDownloadInfo(item)
                        }
                    },
                    onItemLongPress = localViewModel::onSelectionLongPress,
                )

                else -> DownloadTasksV2(
                    state = downloadState,
                    onItemClick = { item ->
                        if (downloadState.selectionIds.isEmpty()) {
                            downloadViewModel.clickDownloadInfo(item)
                        } else {
                            downloadViewModel.selectDownloadInfo(item)
                        }
                    },
                    onItemLongPress = downloadViewModel::onSelectionLongPress,
                )
            }
        }

        if (pagerState.currentPage == 0 && localSelectionActive) {
            val selectedCovers = localState.selection.map { it.cartoonLocalItem.cartoonCover }
            val selectedIdentities = selectedCovers.map { it.toIdentify() }
            LocalSelectionBarV2(
                canStar = selectedIdentities.any { it !in starState.identifySet },
                canUnstar = selectedIdentities.any { it in starState.identifySet },
                onStar = {
                    coverStarViewModel.starAll(selectedCovers)
                    localViewModel.clearSelection()
                },
                onUnstar = {
                    coverStarViewModel.unstarAll(selectedCovers)
                    localViewModel.clearSelection()
                },
                onDelete = { pendingLocalDelete = localState.selection },
            )
        } else if (pagerState.currentPage == 1 && downloadSelectionActive) {
            DownloadSelectionBarV2(
                selectedCount = downloadState.selectionIds.size,
                onDelete = { pendingDownloadDelete = downloadState.selectionIds },
            )
        }
    }

    pendingLocalDelete?.let { selection ->
        DeleteConfirmationV2(
            title = "删除本地番剧？",
            message = "将删除所选 ${selection.size} 部番剧的本地文件，并移除关联下载任务。此操作无法撤销。",
            onConfirm = {
                localViewModel.deleteDownload(selection)
                localViewModel.clearSelection()
                pendingLocalDelete = null
            },
            onDismiss = { pendingLocalDelete = null },
        )
    }

    pendingDownloadDelete?.let { taskIds ->
        DeleteConfirmationV2(
            title = "删除下载任务？",
            message = "将移除所选 ${taskIds.size} 个下载任务及其临时数据。已完成的本地番剧不会在这里删除。",
            onConfirm = {
                downloadViewModel.deleteDownload(taskIds)
                downloadViewModel.clearSelection()
                pendingDownloadDelete = null
            },
            onDismiss = { pendingDownloadDelete = null },
        )
    }

    when (val dialog = downloadState.dialog) {
        is DownloadViewModel.Dialog.ResumeTask -> ResumeDownloadDialogV2(
            taskId = dialog.taskId,
            state = downloadState,
            viewModel = downloadViewModel,
        )
        else -> Unit
    }
}

@Composable
private fun StorySearchHeaderV2(
    text: String,
    focusRequester: FocusRequester,
    onTextChange: (String) -> Unit,
    onExit: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 10.dp),
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
            placeholder = { Text("搜索本地番剧") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onTextChange(text) }),
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
    HorizontalDivider(color = V2Tokens.Divider)
}

@Composable
private fun StorySelectionHeaderV2(
    selectedCount: Int,
    onExit: () -> Unit,
    onSelectAll: () -> Unit,
    onInvert: (() -> Unit)? = null,
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
        if (onInvert != null) {
            TextButton(onClick = onInvert) { Text("反选", color = V2Theme.colors.accent) }
        }
    }
    HorizontalDivider(color = V2Tokens.Divider)
}

@Composable
private fun LocalSelectionBarV2(
    canStar: Boolean,
    canUnstar: Boolean,
    onStar: () -> Unit,
    onUnstar: () -> Unit,
    onDelete: () -> Unit,
) {
    Surface(color = V2Tokens.Surface, tonalElevation = 0.dp) {
        Column {
            HorizontalDivider(color = V2Tokens.Divider)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StorySelectionActionV2(
                    icon = Icons.Filled.Star,
                    label = "追番",
                    enabled = canStar,
                    onClick = onStar,
                )
                StorySelectionActionV2(
                    icon = Icons.Filled.StarBorder,
                    label = "取消追番",
                    enabled = canUnstar,
                    onClick = onUnstar,
                )
                StorySelectionActionV2(
                    icon = Icons.Filled.Delete,
                    label = stringResource(R.string.delete),
                    color = V2Tokens.Error,
                    onClick = onDelete,
                )
            }
        }
    }
}

@Composable
private fun DownloadSelectionBarV2(
    selectedCount: Int,
    onDelete: () -> Unit,
) {
    Surface(color = V2Tokens.Surface, tonalElevation = 0.dp) {
        Column {
            HorizontalDivider(color = V2Tokens.Divider)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .height(58.dp)
                    .padding(horizontal = V2Tokens.ScreenHorizontalPadding),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "已选择 $selectedCount 个任务",
                    modifier = Modifier.weight(1f),
                    color = V2Tokens.TextSecondary,
                    fontSize = 13.sp,
                )
                TextButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = null, tint = V2Tokens.Error)
                    Spacer(Modifier.size(6.dp))
                    Text(stringResource(R.string.delete), color = V2Tokens.Error)
                }
            }
        }
    }
}

@Composable
private fun RowScope.StorySelectionActionV2(
    icon: ImageVector,
    label: String,
    enabled: Boolean = true,
    color: Color = V2Theme.colors.accent,
    onClick: () -> Unit,
) {
    val actionColor = if (enabled) color else V2Tokens.TextSecondary.copy(alpha = 0.38f)
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 9.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = label, tint = actionColor, modifier = Modifier.size(21.dp))
        Spacer(Modifier.size(3.dp))
        Text(text = label, color = actionColor, fontSize = 11.sp, maxLines = 1)
    }
}

@Composable
private fun DeleteConfirmationV2(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = V2Tokens.Surface,
        title = { Text(title, color = V2Tokens.TextPrimary) },
        text = { Text(message, color = V2Tokens.TextSecondary) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("删除", color = V2Tokens.Error) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}

@Composable
private fun ResumeDownloadDialogV2(
    taskId: String,
    state: DownloadViewModel.State,
    viewModel: DownloadViewModel,
) {
    val item = state.downloadInfo.firstOrNull { it.req.uuid == taskId }
    LaunchedEffect(item) {
        if (item == null) viewModel.dismissDialog()
    }
    if (item == null) return
    AlertDialog(
        onDismissRequest = viewModel::dismissDialog,
        containerColor = V2Tokens.Surface,
        title = { Text("恢复下载任务", color = V2Tokens.TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(item.req.localItem.title, color = V2Tokens.TextPrimary)
                Text(
                    "选择快速下载引擎，或切换到完整下载模式。",
                    color = V2Tokens.TextSecondary,
                )
                state.quickDownloadEngines.forEach { engine ->
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            if (item.req.quickDownloadEngineId == engine.id && item.runtime?.isPaused() == true) {
                                viewModel.resume(taskId)
                            } else if (item.req.quickDownloadEngineId == engine.id) {
                                viewModel.retry(taskId)
                            } else {
                                viewModel.switchEngine(taskId, engine.id)
                            }
                            viewModel.dismissDialog()
                        },
                    ) {
                        val selected = item.req.quickDownloadEngineId == engine.id
                        Text(
                            if (selected) "${engine.displayName}（当前）" else engine.displayName,
                            color = if (selected) V2Theme.colors.accent else V2Tokens.TextPrimary,
                        )
                    }
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    if (item.runtime?.isPaused() == true) viewModel.resume(taskId) else viewModel.retry(taskId)
                    viewModel.dismissDialog()
                },
            ) {
                Text(
                    if (item.runtime?.isPaused() == true) "继续当前任务" else "按当前配置重试",
                    color = V2Tokens.TextPrimary,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    viewModel.retryAsFull(taskId)
                    viewModel.dismissDialog()
                },
            ) { Text("改用完整模式", color = V2Theme.colors.accent) }
        },
    )
}
