@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.heyanle.easybangumi4.v2.ui.star

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
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
import com.heyanle.easybangumi4.cartoon.star.isInner
import com.heyanle.easybangumi4.navigationCartoonTag
import com.heyanle.easybangumi4.navigationDetailed
import com.heyanle.easybangumi4.navigationMigrate
import com.heyanle.easybangumi4.plugin.source.LocalSourceBundleController
import com.heyanle.easybangumi4.ui.common.EasyDeleteDialog
import com.heyanle.easybangumi4.ui.common.EasyMutiSelectionDialog
import com.heyanle.easybangumi4.ui.common.EasyMutiSelectionDialogStar
import com.heyanle.easybangumi4.ui.common.LoadingPage
import com.heyanle.easybangumi4.ui.main.MainViewModel
import com.heyanle.easybangumi4.ui.main.star.CartoonStarProcBottomSheet
import com.heyanle.easybangumi4.ui.main.star.StarViewModel
import com.heyanle.easybangumi4.v2.theme.V2Tokens
import com.heyanle.easybangumi4.v2.theme.V2Theme
import com.heyanle.easybangumi4.v2.ui.component.V2ScrollableTabs
import com.heyanle.easybangumi4.v2.ui.component.V2TabStyle
import com.heyanle.easybangumi4.v2.ui.component.V2CountBadge

/**
 * V2 presentation for the followed-cartoon page.
 *
 * Selection, filtering, updates, migration and persistence remain owned by [StarViewModel].
 */
@Composable
internal fun StarV2() {
    val mainViewModel = viewModel<MainViewModel>()
    val starViewModel = viewModel<StarViewModel>()
    val state by starViewModel.stateFlow.collectAsState()
    val navController = LocalNavController.current
    val focusRequester = remember { FocusRequester() }
    val currentSelectionCount = rememberUpdatedState(state.selection.size)
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val selectionBottomBar = remember<@Composable () -> Unit>(starViewModel) {
        {
            StarSelectionActionBarV2(
                selectedCount = currentSelectionCount.value,
                onChangeTag = starViewModel::dialogChangeTag,
                onUpdate = starViewModel::onUpdateSelection,
                onMigrate = starViewModel::dialogMigrateSelect,
                onPin = starViewModel::onUpSelection,
                onDelete = starViewModel::dialogDeleteSelection,
            )
        }
    }

    LaunchedEffect(state.selection.isEmpty()) {
        mainViewModel.customBottomBar = if (state.selection.isEmpty()) null else selectionBottomBar
    }
    DisposableEffect(Unit) {
        onDispose {
            starViewModel.onSelectionExit()
            mainViewModel.customBottomBar = null
        }
    }
    BackHandler(enabled = state.selection.isNotEmpty()) {
        starViewModel.onSelectionExit()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(V2Tokens.WarmBackground),
    ) {
        when {
            state.selection.isNotEmpty() -> StarSelectionHeaderV2(
                selectedCount = state.selection.size,
                onExit = starViewModel::onSelectionExit,
                onSelectAll = starViewModel::onSelectAll,
                onInvert = starViewModel::onSelectInvert,
            )

            state.searchQuery != null -> StarSearchHeaderV2(
                text = state.searchQuery.orEmpty(),
                focusRequester = focusRequester,
                onTextChange = starViewModel::onSearch,
                onSearch = starViewModel::onSearch,
                onExit = { starViewModel.onSearch(null) },
            )

            else -> StarHeaderV2(
                count = state.starCount,
                scrollBehavior = scrollBehavior,
                filterActive = state.isFilter,
                onSearch = { starViewModel.onSearch("") },
                onFilter = starViewModel::dialogProc,
                onUpdate = starViewModel::onUpdate,
            )
        }

        if (state.isLoading) {
            LoadingPage(
                modifier = Modifier.fillMaxSize(),
                loadingMsg = "正在加载追番",
            )
        } else {
            if (state.tagList.size > 1) {
                V2ScrollableTabs(
                    labels = state.tagList.map { it.display },
                    selectedIndex = state.tagList.indexOf(state.curTab).coerceAtLeast(0),
                    onSelected = { index ->
                        state.tagList.getOrNull(index)?.let(starViewModel::changeTab)
                    },
                    style = V2TabStyle.SecondaryDot,
                    badges = state.tagList.map { tag ->
                        state.data[tag.label]?.size ?: 0
                    },
                )
                HorizontalDivider(color = V2Tokens.Divider)
            }

            val list = state.data[state.curTab?.label].orEmpty()
            StarListV1CopyV2(
                cartoons = list,
                selection = state.selection,
                tabKey = state.curTab?.label,
                onRefresh = starViewModel::onUpdate,
                onClick = { cartoon ->
                    if (state.selection.isEmpty()) {
                        navController.navigationDetailed(cartoon.id, cartoon.url, cartoon.source)
                    } else {
                        starViewModel.onSelectionChange(cartoon)
                    }
                },
                onLongPress = starViewModel::onSelectionLongPress,
                nestedScrollConnection = scrollBehavior.nestedScrollConnection.takeIf { state.tagList.size <= 1 },
            )
        }
    }

    when (val dialog = state.dialog) {
        is StarViewModel.DialogState.ChangeTag -> {
            val tags = state.tagList.filter { !it.isInner }
            if (tags.isEmpty()) {
                AlertDialog(
                    onDismissRequest = starViewModel::dialogDismiss,
                    containerColor = V2Tokens.Surface,
                    title = { Text(stringResource(R.string.no_tag)) },
                    text = { Text(stringResource(R.string.click_to_manage_tag)) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                starViewModel.dialogDismiss()
                                navController.navigationCartoonTag()
                            },
                        ) { Text(stringResource(R.string.confirm)) }
                    },
                    dismissButton = {
                        TextButton(onClick = starViewModel::dialogDismiss) {
                            Text(stringResource(R.string.cancel))
                        }
                    },
                )
            } else {
                EasyMutiSelectionDialogStar(
                    show = true,
                    title = { Text(stringResource(R.string.change_tag)) },
                    items = tags,
                    initSelection = dialog.getTags(),
                    onConfirm = { selectedTags ->
                        starViewModel.changeTagSelection(dialog.selection, selectedTags)
                        starViewModel.onSelectionExit()
                    },
                    onManage = navController::navigationCartoonTag,
                    onDismissRequest = starViewModel::dialogDismiss,
                )
            }
        }

        is StarViewModel.DialogState.Proc -> CartoonStarProcBottomSheet(starViewModel, state)

        is StarViewModel.DialogState.Delete -> EasyDeleteDialog(
            show = true,
            onDelete = { starViewModel.deleteSelection(dialog.selection) },
            onDismissRequest = starViewModel::dialogDismiss,
        )

        is StarViewModel.DialogState.MigrateSource -> {
            val sources = LocalSourceBundleController.current.sources()
            EasyMutiSelectionDialog(
                show = true,
                title = { Text(stringResource(R.string.choose_source_to_migrate)) },
                items = sources,
                initSelection = emptyList(),
                onConfirm = { selectedSources ->
                    navController.navigationMigrate(
                        dialog.selection.map { it.toSummary() },
                        selectedSources.map { it.key },
                    )
                    starViewModel.onSelectionExit()
                },
                onDismissRequest = starViewModel::dialogDismiss,
            )
        }

        null -> Unit
    }
}

@Composable
private fun StarHeaderV2(
    count: Int,
    scrollBehavior: TopAppBarScrollBehavior,
    filterActive: Boolean,
    onSearch: () -> Unit,
    onFilter: () -> Unit,
    onUpdate: () -> Unit,
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
                text = stringResource(R.string.my_anim),
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
            IconButton(onClick = onFilter) {
            Icon(
                Icons.Filled.FilterAlt,
                contentDescription = stringResource(R.string.filter),
                tint = if (filterActive) V2Theme.colors.accent else V2Tokens.TextPrimary,
            )
            }
            IconButton(onClick = onUpdate) {
            Icon(Icons.Filled.Update, contentDescription = stringResource(R.string.update))
            }
        },
    )
}

@Composable
private fun StarSearchHeaderV2(
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
private fun StarSelectionHeaderV2(
    selectedCount: Int,
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
    }
    HorizontalDivider(color = V2Tokens.Divider)
}

@Composable
private fun StarSelectionActionBarV2(
    selectedCount: Int,
    onChangeTag: () -> Unit,
    onUpdate: () -> Unit,
    onMigrate: () -> Unit,
    onPin: () -> Unit,
    onDelete: () -> Unit,
) {
    Surface(
        color = V2Tokens.Surface,
        contentColor = V2Tokens.TextPrimary,
        tonalElevation = 0.dp,
    ) {
        Column {
            HorizontalDivider(color = V2Tokens.Divider)
            Text(
                text = "已选择 $selectedCount 项",
                modifier = Modifier.padding(start = 16.dp, top = 8.dp),
                color = V2Tokens.TextSecondary,
                fontSize = 12.sp,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                SelectionActionV2(Icons.Filled.Tag, stringResource(R.string.change_tag), onChangeTag)
                SelectionActionV2(Icons.Filled.Update, stringResource(R.string.update), onUpdate)
                SelectionActionV2(Icons.Filled.SyncAlt, "迁移", onMigrate)
                SelectionActionV2(Icons.Filled.PushPin, "设置置顶", onPin)
                SelectionActionV2(
                    Icons.Filled.Delete,
                    stringResource(R.string.delete),
                    onDelete,
                    color = V2Tokens.Error,
                )
            }
        }
    }
}

@Composable
private fun RowScope.SelectionActionV2(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    color: Color = V2Theme.colors.accent,
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(22.dp))
        Spacer(Modifier.size(4.dp))
        Text(text = label, color = V2Tokens.TextPrimary, fontSize = 11.sp, maxLines = 1)
    }
}
