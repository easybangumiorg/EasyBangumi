package com.heyanle.easybangumi4.v2.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.ui.search_migrate.search.SearchMode
import com.heyanle.easybangumi4.ui.search_migrate.search.SearchViewModelFactory
import com.heyanle.easybangumi4.ui.search_migrate.search.gather.GatherSearch
import com.heyanle.easybangumi4.ui.search_migrate.search.normal.NormalSearch
import com.heyanle.easybangumi4.ui.search_migrate.search.overview.OverviewSearch
import com.heyanle.easybangumi4.v2.theme.V2Tokens
import com.heyanle.easybangumi4.v2.theme.V2Theme

/** V2 search route. Paging and per-source ViewModel ownership remain in the legacy search pages. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SearchV2(
    initialKeyword: String,
    initialSourceKey: String,
) {
    val navController = LocalNavController.current
    val viewModel = SearchViewModelFactory.newViewModel(initialKeyword)
    val submittedKeyword by viewModel.searchFlow.collectAsState()
    val history by viewModel.searchHistory.collectAsState(initial = emptyList())
    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(V2Tokens.WarmBackground),
    ) {
        SearchHeaderV2(
            text = viewModel.searchBarText.value,
            mode = viewModel.searchMode.value,
            landing = submittedKeyword.isEmpty(),
            focusRequester = focusRequester,
            onBack = navController::popBackStack,
            onTextChange = { text ->
                viewModel.searchBarText.value = text
                if (text.isEmpty()) viewModel.search(text)
            },
            onSearch = viewModel::search,
            onModeChange = viewModel::onSearchModeChange,
        )

        if (submittedKeyword.isEmpty()) {
            SearchHistoryV2(
                history = history,
                onSelect = { keyword -> viewModel.search(keyword) },
                onClear = viewModel::clearHistory,
            )
        } else {
            when (viewModel.searchMode.value) {
                SearchMode.SINGLE_SOURCE -> NormalSearch(
                    defSourceKey = initialSourceKey,
                    searchViewModel = viewModel,
                )
                SearchMode.BY_SOURCE -> GatherSearch(searchViewModel = viewModel)
                SearchMode.OVERVIEW -> OverviewSearch(searchViewModel = viewModel)
            }
        }
    }
}

@Composable
private fun SearchHeaderV2(
    text: String,
    mode: SearchMode,
    landing: Boolean,
    focusRequester: FocusRequester,
    onBack: () -> Unit,
    onTextChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onModeChange: (SearchMode) -> Unit,
) {
    var showModeMenu by remember { mutableStateOf(false) }
    if (landing) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = V2Tokens.ScreenHorizontalPadding),
        ) {
            Row(
                modifier = Modifier.height(64.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = V2Tokens.TextPrimary)
                }
                Text(
                    text = "搜索与找番",
                    modifier = Modifier.padding(start = 4.dp),
                    color = V2Tokens.TextPrimary,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            TextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, V2Theme.colors.accent, RoundedCornerShape(10.dp)),
                placeholder = { Text("搜索番剧") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (text.isNotEmpty()) {
                        IconButton(onClick = { onTextChange("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = stringResource(R.string.clear))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch(text) }),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = V2Tokens.Surface,
                    unfocusedContainerColor = V2Tokens.Surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
            )
            Box(modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)) {
                Surface(
                    modifier = Modifier.clickable { showModeMenu = true },
                    color = V2Tokens.SurfaceMuted,
                    contentColor = V2Tokens.TextPrimary,
                    shape = RoundedCornerShape(9.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(searchModeIcon(mode), contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(searchModeTitle(mode), modifier = Modifier.padding(start = 7.dp), fontSize = 13.sp)
                    }
                }
                DropdownMenu(
                    expanded = showModeMenu,
                    onDismissRequest = { showModeMenu = false },
                    containerColor = V2Tokens.Surface,
                ) {
                    SearchMode.entries.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(searchModeTitle(item)) },
                            leadingIcon = { Icon(searchModeIcon(item), contentDescription = null) },
                            trailingIcon = {
                                if (mode == item) Icon(Icons.Filled.Check, contentDescription = null, tint = V2Theme.colors.accent)
                            },
                            onClick = {
                                onModeChange(item)
                                showModeMenu = false
                            },
                        )
                    }
                }
            }
        }
        return
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 4.dp, top = 8.dp, end = 6.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = V2Tokens.TextPrimary,
            )
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
        IconButton(onClick = { onSearch(text) }) {
            Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.search))
        }
        Box {
            IconButton(onClick = { showModeMenu = true }) {
                Icon(searchModeIcon(mode), contentDescription = stringResource(R.string.search_mode))
            }
            DropdownMenu(
                expanded = showModeMenu,
                onDismissRequest = { showModeMenu = false },
                containerColor = V2Tokens.Surface,
            ) {
                SearchMode.entries.forEach { item ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(searchModeTitle(item), color = V2Tokens.TextPrimary)
                                Text(
                                    searchModeDescription(item),
                                    color = V2Tokens.TextSecondary,
                                    fontSize = 11.sp,
                                )
                            }
                        },
                        leadingIcon = { Icon(searchModeIcon(item), contentDescription = null) },
                        trailingIcon = {
                            if (mode == item) {
                                Icon(Icons.Filled.Check, contentDescription = null, tint = V2Theme.colors.accent)
                            }
                        },
                        onClick = {
                            onModeChange(item)
                            showModeMenu = false
                        },
                    )
                }
            }
        }
    }
    HorizontalDivider(color = V2Tokens.Divider)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchHistoryV2(
    history: List<String>,
    onSelect: (String) -> Unit,
    onClear: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = V2Tokens.ScreenHorizontalPadding, vertical = 20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "搜索历史",
                    color = V2Tokens.TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "输入关键词，或继续最近的搜索",
                    modifier = Modifier.padding(top = 4.dp),
                    color = V2Tokens.TextSecondary,
                    fontSize = 12.sp,
                )
            }
            if (history.isNotEmpty()) {
                Text(
                    text = "清空",
                    modifier = Modifier.clickable(onClick = onClear).padding(horizontal = 8.dp, vertical = 6.dp),
                    color = V2Tokens.TextSecondary,
                    fontSize = 13.sp,
                )
            }
        }
        if (history.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("暂无搜索记录", color = V2Tokens.TextSecondary, fontSize = 14.sp)
            }
        } else {
            FlowRow(
                modifier = Modifier.padding(top = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                history.forEach { keyword ->
                    Surface(
                        color = V2Tokens.Surface,
                        contentColor = V2Tokens.TextPrimary,
                        shape = RoundedCornerShape(999.dp),
                    ) {
                        Text(
                            text = keyword,
                            modifier = Modifier
                                .clickable { onSelect(keyword) }
                                .padding(horizontal = 13.dp, vertical = 8.dp),
                            fontSize = 13.sp,
                        )
                    }
                }
            }
        }
    }
}

private fun searchModeIcon(mode: SearchMode): ImageVector = when (mode) {
    SearchMode.SINGLE_SOURCE -> Icons.AutoMirrored.Filled.ViewList
    SearchMode.BY_SOURCE -> Icons.Filled.ViewAgenda
    SearchMode.OVERVIEW -> Icons.Filled.GridView
}

@Composable
private fun searchModeTitle(mode: SearchMode): String = stringResource(
    when (mode) {
        SearchMode.SINGLE_SOURCE -> R.string.search_mode_single_source
        SearchMode.BY_SOURCE -> R.string.search_mode_by_source
        SearchMode.OVERVIEW -> R.string.search_mode_overview
    }
)

@Composable
private fun searchModeDescription(mode: SearchMode): String = stringResource(
    when (mode) {
        SearchMode.SINGLE_SOURCE -> R.string.search_mode_single_source_desc
        SearchMode.BY_SOURCE -> R.string.search_mode_by_source_desc
        SearchMode.OVERVIEW -> R.string.search_mode_overview_desc
    }
)
