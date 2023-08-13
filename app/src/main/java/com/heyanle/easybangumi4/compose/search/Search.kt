package com.heyanle.easybangumi4.compose.search

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.compose.common.EmptyPage
import com.heyanle.easybangumi4.compose.common.TabIndicator
import com.heyanle.easybangumi4.compose.search.searchpage.SearchPage
import com.heyanle.easybangumi4.source.LocalSourceBundleController
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/3/27 22:54.
 * https://github.com/heyanLE
 */

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Search(
    defSearchKey: String,
    defSourceKey: String,
) {

    val nav = LocalNavController.current

    val vm = SearchViewModelFactory.newViewModel(defSearchKey = defSearchKey)

    val searchComponents = LocalSourceBundleController.current.searches()

    val scope = rememberCoroutineScope()

    val pagerState =
        rememberPagerState(searchComponents.indexOfFirst { it.source.key == defSourceKey }
            .coerceIn(0, searchComponents.size - 1), 0f) {
            searchComponents.size
        }

    val focusRequester = remember {
        FocusRequester()
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        SearchTopAppBar(
            text = vm.searchBarText.value,
            focusRequester = focusRequester,
            onBack = {
                nav.popBackStack()
            },
            onSearch = {
                vm.search(it)
            },
            onTextChange = {
                vm.searchBarText.value = it
                if (it.isEmpty()) {
                    vm.search(it)
                }
            }
        )

        if (searchComponents.isEmpty()) {
            EmptyPage(
                modifier = Modifier
                    .fillMaxSize(),
                emptyMsg = stringResource(id = R.string.no_source)
            )
        } else {
            ScrollableTabRow(
                edgePadding = 0.dp,
                selectedTabIndex = pagerState.currentPage,
                divider = {},
                indicator = {
                    TabIndicator(
                        currentTabPosition = it[0.coerceAtLeast(
                            pagerState.currentPage
                        )]
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {


                searchComponents.forEachIndexed { index, searchComponent ->
                    Tab(
                        selected = index == pagerState.currentPage,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = {
                            Text(text = searchComponent.source.label)

                        },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Divider()

            HorizontalPager(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                state = pagerState
            ) {

                CompositionLocalProvider(
                    LocalViewModelStoreOwner provides vm.getViewModel(searchComponents[it])
                ) {
                    SearchPage(
                        isShow = pagerState.currentPage == it,
                        searchComponent = searchComponents[it],
                        searchViewModel = vm,
                    )

                }

            }
        }


    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopAppBar(
    text: String,
    focusRequester: FocusRequester,
    onBack: () -> Unit,
    onSearch: (String) -> Unit,
    onTextChange: (String) -> Unit,
) {

    TopAppBar(
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
                        stringResource(id = com.heyanle.easy_i18n.R.string.clear)
                    )
                }
            }
            IconButton(onClick = {
                onSearch(text)
            }) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    stringResource(id = com.heyanle.easy_i18n.R.string.search)
                )
            }
        }
    )

}