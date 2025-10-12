package org.easybangumi.next.shared.compose.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import org.easybangumi.next.shared.compose.search.simple.SimpleSearch
import org.easybangumi.next.shared.foundation.view_model.vm


/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 */

@Composable
fun Search(
    defSearchKeyword: String = "",
    defSourceKey: String? = null,
    onBack: () -> Unit = {}
) {
    val vm = vm(::SearchViewModel, defSearchKeyword, defSourceKey)
    val search = vm.searchFlow.collectAsState()
    val searchHistory = vm.searchHistory.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // TopAppBar
        SearchTopAppBar(
            modifier = Modifier.fillMaxWidth(),
            searchText = vm.searchBarText.value,
            onSearchTextChange = { vm.searchBarText.value = it },
            onBack = onBack,
            onSearch = { keyword ->
                vm.search(keyword)
            },
            onClear = {
                vm.searchBarText.value = ""
            }
        )

        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            // 内容区域
            if (search.value.isBlank()) {
                // 显示搜索历史
                SearchHistoryContent(
                    modifier = Modifier,
                    searchHistory = searchHistory.value,
                    onHistoryClick = { historyItem ->
                        vm.searchBarText.value = historyItem.key
                        vm.search(historyItem.key)
                    }
                )
            } else {
                // 显示搜索结果
                SimpleSearch(vm = vm.simpleVM)
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopAppBar(
    modifier: Modifier = Modifier,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onBack: () -> Unit,
    onSearch: (String) -> Unit,
    onClear: () -> Unit
) {
    TopAppBar(
        modifier = modifier,
        title = {
            TextField(
                value = searchText,
                onValueChange = onSearchTextChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                ),
                placeholder = { },
                trailingIcon = {
                    if (searchText.isNotEmpty()) {
                        IconButton(onClick = onClear) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "清空"
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = { onSearch(searchText) }
                )
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "返回"
                )
            }
        },
        actions = {
            IconButton(onClick = { onSearch(searchText) }) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "搜索"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
private fun SearchHistoryContent(
    modifier: Modifier = Modifier,
    searchHistory: List<org.easybangumi.next.shared.data.store.StoreProvider.SearchHistory>,
    onHistoryClick: (org.easybangumi.next.shared.data.store.StoreProvider.SearchHistory) -> Unit
) {
    Column(
        modifier = modifier.padding(16.dp)
    ) {
        Text(
            text = "搜索历史",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        if (searchHistory.isNotEmpty()) {
            FlowRow(
                maxLines = 2
            ) {
                searchHistory.forEach {
                    FilterChip(
                        selected = false,
                        onClick = {
                            onHistoryClick(it)
                        },
                        label = {
                            Text(it.key)
                        },
                        modifier = Modifier
                    )
                }
            }
        }
    }
}
