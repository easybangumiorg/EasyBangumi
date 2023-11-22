package com.heyanle.easybangumi4.ui.search_migrate.migrate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.navigationDetailed
import com.heyanle.easybangumi4.ui.common.CartoonCardWithCover
import com.heyanle.easybangumi4.ui.common.EmptyPage
import com.heyanle.easybangumi4.ui.common.LoadingPage
import com.heyanle.easybangumi4.ui.common.PagingCommon
import com.heyanle.easybangumi4.ui.common.pagingCommonHor
import com.heyanle.easybangumi4.ui.main.star.CoverStarViewModel
import com.heyanle.easybangumi4.ui.search_migrate.search.searchpage.CartoonSearchItem

/**
 * Created by heyanlin on 2023/11/22.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartoonMigrate(
    def: String,
    defSourceKey: List<String>,
) {

    val nav = LocalNavController.current

    val focusRequester = remember {
        FocusRequester()
    }

    val vm = CartoonMigrateViewModelFactory.newViewModel(def, defSourceKey)

    val state = vm.migrateStateFlow.collectAsState()

    val starVm = viewModel<CoverStarViewModel>()

    val listState = rememberLazyListState()

    val behavior = TopAppBarDefaults.pinnedScrollBehavior()


    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        MigrateTopAppBar(
            behavior = behavior,
            text = vm.searchBarText.value,
            focusRequester = focusRequester,
            onBack = {
                nav.popBackStack()
            },
            onChangeSource = {},
            onSearch = {
                vm.search()
            },
            onTextChange = {
                vm.searchBarText.value = it
                if (it.isEmpty()) {
                    vm.search()
                }
            }
        )

        when (val sta = state.value) {
            is CartoonMigrateViewModel.MigrateState.Empty -> {
                EmptyPage(
                    modifier = Modifier
                        .fillMaxSize(),
                    emptyMsg = stringResource(id = R.string.please_input_keyword_to_search)
                )
            }

            is CartoonMigrateViewModel.MigrateState.Loading -> {
                LoadingPage(
                    modifier = Modifier
                        .fillMaxSize(),
                )
            }

            is CartoonMigrateViewModel.MigrateState.Info -> {
                LazyColumn(modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .nestedScroll(behavior.nestedScrollConnection), state = listState){
                    items(sta.items){
                        MigrateSourceItem(sourceItem = it, starVm = starVm)
                    }
                }
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MigrateTopAppBar(
    text: String,
    behavior: TopAppBarScrollBehavior,
    focusRequester: FocusRequester,
    onBack: () -> Unit,
    onChangeSource: () -> Unit,
    onSearch: (String) -> Unit,
    onTextChange: (String) -> Unit,
) {

    TopAppBar(
        scrollBehavior = behavior,
        navigationIcon = {
            IconButton(onClick = {
                onBack()

            }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack, stringResource(id = R.string.back)
                )
            }
        },
        title = {
            LaunchedEffect(key1 = Unit) {
                focusRequester.requestFocus()
            }
            TextField(keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    onSearch(text)
                }),
                maxLines = 1,
                modifier = Modifier.focusRequester(focusRequester),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                ),
                value = text,
                onValueChange = {
                    onTextChange(it)
                },
                placeholder = {
                    Text(
                        style = MaterialTheme.typography.titleLarge,
                        text = stringResource(id = R.string.please_input_keyword_to_search)
                    )
                })
        },
        actions = {
            if (text.isNotEmpty()) {
                IconButton(onClick = {
                    onTextChange("")
                }) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        stringResource(id = R.string.clear)
                    )
                }
            }
            IconButton(onClick = {
                onSearch(text)
            }) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    stringResource(id = R.string.search)
                )
            }
        }
    )

}


@Composable
fun MigrateSourceItem(
    sourceItem: CartoonMigrateViewModel.MigrateItem,
    starVm: CoverStarViewModel,
) {
    val page = sourceItem.flow.collectAsLazyPagingItems()
    val nav = LocalNavController.current
    val haptic = LocalHapticFeedback.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    ) {

        ListItem(
            headlineContent = { Text(text = sourceItem.searchComponent.source.label) },
            trailingContent = { Text(text = stringResource(id = R.string.long_press_to_star))},
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent
            ),
        )
        if (page.itemCount > 0) {
            LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(4.dp)
            ) {
                items(page.itemCount) {
                    page[it]?.let {
                        CartoonCardWithCover(
                            modifier = Modifier.width(100.dp),
                            star = starVm.isCoverStarted(it) ,
                            cartoonCover = it,
                            onClick = {
                                nav.navigationDetailed(it)
                            },
                            onLongPress = {
                                starVm.star(it)
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                        )
                    }

                }
                pagingCommonHor(page)
            }
        }
        PagingCommon(items = page)

    }
}