package com.heyanle.easybangumi.ui.search

import android.animation.ValueAnimator
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.heyanle.bangumi_source_api.api.ISearchParser
import com.heyanle.easybangumi.LocalNavController
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.ui.common.*
import com.heyanle.okkv2.core.okkv
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

/**
 * Created by HeYanLe on 2023/1/10 16:34.
 * https://github.com/heyanLE
 */

var topAppBarExpendAnimator: WeakReference<ValueAnimator>? = null
fun onNewTopAppBarExpendAnim(valueAnimator: ValueAnimator?) {
    topAppBarExpendAnimator?.get()?.cancel()
    topAppBarExpendAnimator = valueAnimator?.let {
        WeakReference(it)
    }
}

@Composable
fun Search(
    default: String,
    source: String,
) {
    SearchSourceContainer(
        errorContainerColor = MaterialTheme.colorScheme.background,
    ) {
        val vm = viewModel<SearchViewModel>(factory = SearchViewModelFactory(default))
        SearchPage(vm = vm, it, source)
    }
}

var animSearchInitialPage by okkv("animSearchInitialPage", 0)

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalPagerApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun SearchPage(
    vm: SearchViewModel,
    searchParsers: List<ISearchParser>,
    source: String,
) {

    val scope = rememberCoroutineScope()

    val nav = LocalNavController.current

    val uiController = rememberSystemUiController()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()


    // 饱和 cancel
    DisposableEffect(key1 = Unit) {
        onNewTopAppBarExpendAnim(null)
        onDispose {
            onNewTopAppBarExpendAnim(null)
        }
    }

    val isHeaderShowForever = remember {
        mutableStateOf(false)
    }

    val focusRequester = remember {
        FocusRequester()
    }

    val pagerState = rememberPagerState(initialPage = Unit.let {
        var index = -1
        searchParsers.forEachIndexed { i, iSearchParser ->
            if (iSearchParser.getKey() == source) {
                index = i
            }
        }
        if (index == -1) animSearchInitialPage else index
    })
    LaunchedEffect(key1 = searchParsers.size) {
        if (animSearchInitialPage >= searchParsers.size) {
            animSearchInitialPage = 0
            //pagerState.scrollToPage(0)
        }
        if (pagerState.currentPage >= searchParsers.size) {
            pagerState.scrollToPage(0)
        }
    }

    LaunchedEffect(key1 = Unit) {
        if (vm.searchEventState.value.isEmpty()) {
            kotlin.runCatching {
                focusRequester.requestFocus()
            }
            scrollBehavior.state.heightOffset = 0F
            scrollBehavior.state.contentOffset = 0F

        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            Surface(
                shadowElevation = 4.dp,
                color = MaterialTheme.colorScheme.primary
            ) {
                Column() {
                    SearchTopBar(
                        modifier = Modifier.statusBarsPadding(),
                        placeholder = {
                            Text(
                                modifier = Modifier,
                                textAlign = TextAlign.Start,
                                text = stringResource(id = R.string.anim_search)
                            )
                        },
                        text = vm.keywordState,
                        onBack = {
                            nav.popBackStack()
                        },
                        onSearch = {
                            vm.search(it)

                        },
                        onValueChange = {
                            vm.keywordState.value = it
                            if (it.isEmpty()) {
                                vm.search(it)
                            }
                        },
                        scrollBehavior = scrollBehavior,
                        containerColor = Color.Transparent,
                        focusRequester = focusRequester
                    )
                    HomeTabRow(
                        containerColor = MaterialTheme.colorScheme.primary,
                        selectedTabIndex = pagerState.currentPage
                    ) {

                        for (i in searchParsers.indices) {
                            HomeTabItem(
                                selected = i == pagerState.currentPage,
                                text = {
                                    Text(text = searchParsers[i].getLabel())
                                },
                                onClick = {
                                    scope.launch {
                                        pagerState.animateScrollToPage(i)
                                    }
                                }
                            )
                        }
                    }
                }
            }

        },
        content = { padding ->
            val keyboard = LocalSoftwareKeyboardController.current
            LaunchedEffect(pagerState.currentPage) {
                animSearchInitialPage = pagerState.currentPage
            }
            HorizontalPager(
                modifier = Modifier
                    .fillMaxHeight()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .nestedScroll(object : NestedScrollConnection {
                        override fun onPreScroll(
                            available: Offset,
                            source: NestedScrollSource
                        ): Offset {
                            keyboard?.hide()
                            return super.onPreScroll(available, source)
                        }
                    })
                    .background(MaterialTheme.colorScheme.background)
                    .padding(padding),
                count = searchParsers.size,
                state = pagerState,
                key = { it }
            ) {
                CompositionLocalProvider(
                    LocalViewModelStoreOwner provides vm.getViewModelStoreOwner(searchParsers[it])
                ) {
                    vm.searchHistory
                    val lazyListState = rememberLazyListState()
                    Box(modifier = Modifier.fillMaxSize()) {
                        SearchPage(
                            isShowTabForever = isHeaderShowForever,
                            searchEventState = vm.searchEventState,
                            historyKey = vm.searchHistory,
                            searchParser = searchParsers[it],
                            isEnable = pagerState.currentPage == it,
                            onHistoryKeyClick = {
                                vm.search(it)
                            },
                            lazyListState = lazyListState,
                            onHistoryDelete = {
                                vm.clearHistory()
                            }
                        )
                        FastScrollToTopFab(listState = lazyListState) {
                            val anim = ValueAnimator.ofFloat(0F, 1F)
                            val sourceHeightOffset = scrollBehavior.state.heightOffset
                            val sourceContentOffset = scrollBehavior.state.contentOffset
                            anim.addUpdateListener {
                                runCatching {
                                    val float = it.animatedValue as Float
                                    val targetHeightOffset =
                                        sourceHeightOffset + (0F - sourceHeightOffset) * float
                                    val targetContentOffset =
                                        sourceContentOffset + (0F - sourceContentOffset) * float
                                    scrollBehavior.state.heightOffset = targetHeightOffset
                                    scrollBehavior.state.contentOffset = targetContentOffset
                                }.onFailure {
                                    it.printStackTrace()
                                }
                            }
                            anim.duration = 200
                            onNewTopAppBarExpendAnim(anim)
                            anim.start()
                        }
                    }
                }

            }

        }
    )
}