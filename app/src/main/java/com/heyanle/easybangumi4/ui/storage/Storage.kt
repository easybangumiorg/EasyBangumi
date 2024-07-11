package com.heyanle.easybangumi4.ui.storage

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.ui.common.TabIndicator
import com.heyanle.easybangumi4.ui.storage.backup.Backup
import com.heyanle.easybangumi4.ui.storage.restore.Restore
import com.heyanle.easybangumi4.ui.storage.restore.RestoreViewModel
import kotlinx.coroutines.launch

/**
 * Created by heyanlin on 2024/4/29.
 */
sealed class StoragePage(
    val tabLabel: @Composable (() -> Unit),
    val topAppBar: @Composable (() -> Unit),
    val content: @Composable (() -> Unit),
) {

    data object Backup :
        StoragePage(tabLabel = { Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.backup)) },
            topAppBar = { BackupTopAppBar() },
            content = { Backup() })

    data object Restore :
        StoragePage(tabLabel = { Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.restore)) },
            topAppBar = { RestoreTopAppBar() },
            content = { Restore() })

}

val StoragePageItems = listOf<StoragePage>(
    StoragePage.Backup,
    StoragePage.Restore
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun Storage() {
    val nav = LocalNavController.current
    val pagerState = rememberPagerState(0) { StoragePageItems.size }
    val scope = rememberCoroutineScope()
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        Column {
            StoragePageItems[pagerState.currentPage].topAppBar()
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                modifier = Modifier.fillMaxWidth(),
                indicator = {
                    TabIndicator(currentTabPosition = it[pagerState.currentPage])
                },
            ) {
                StoragePageItems.forEachIndexed { index, downloadPage ->
                    Tab(selected = index == pagerState.currentPage,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = {
                            downloadPage.tabLabel()
                        })
                }
            }

            HorizontalPager(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                state = pagerState,
            ) {
                val page = StoragePageItems[it]
                page.content()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupTopAppBar() {
    val nav = LocalNavController.current
    TopAppBar(
        title = {
            Text(text = stringResource(id = R.string.backup_and_store))
        },
        navigationIcon = {
            IconButton(onClick = {
                nav.popBackStack()
            }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack, stringResource(id = R.string.back)
                )
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestoreTopAppBar(){
    val restoreViewModel = viewModel<RestoreViewModel>()
    val nav = LocalNavController.current
    val ctx = LocalContext.current


    TopAppBar(
        title = {
            Text(text = stringResource(id = R.string.backup_and_store))
        },
        navigationIcon = {
            IconButton(onClick = {
                nav.popBackStack()
            }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack, stringResource(id = R.string.back)
                )
            }
        },
        actions = {
            IconButton(onClick = {
                restoreViewModel.onAddRestoreFile(ctx)
            }) {
                Icon(
                    imageVector = Icons.Filled.Add, stringResource(id = R.string.click_to_add)
                )
            }
        }
    )
}