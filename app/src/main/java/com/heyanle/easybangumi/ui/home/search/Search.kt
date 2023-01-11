package com.heyanle.easybangumi.ui.home.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.insets.statusBarsHeight
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.heyanle.easybangumi.LocalNavController
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.ui.common.HomeTabItem
import com.heyanle.easybangumi.ui.common.HomeTabRow
import com.heyanle.easybangumi.ui.common.LoadingPage
import com.heyanle.easybangumi.ui.common.ScrollHeaderBox
import com.heyanle.easybangumi.ui.common.SearchTopBar
import com.heyanle.easybangumi.ui.home.animInitialPage
import com.heyanle.okkv2.core.okkv
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/1/10 16:34.
 * https://github.com/heyanLE
 */

var animSearchInitialPage by okkv("animSearchInitialPage", 0)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun Search(
    defKeyword: String = ""
){

    val keyword = remember {
        mutableStateOf(defKeyword)
    }

    val pagerState = rememberPagerState(
        initialPage = animSearchInitialPage
    )



    val scope = rememberCoroutineScope()

    val nav = LocalNavController.current

    val vm = viewModel<SearchViewModel>()

    LaunchedEffect(key1 = Unit){
        pagerState.scrollToPage(animInitialPage)
    }

    val isHeaderShowForever = remember {
        mutableStateOf(false)
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            SearchTopBar(
                modifier = Modifier.systemBarsPadding(),
                placeholder = {
                    Text(text = stringResource(id = R.string.anim_search))
                },
                text = keyword,
                onBack = {
                    nav.popBackStack()
                },
                onSearch = {
                    vm.keywordState.value = it
                })
        },
        content = { padding ->
            SideEffect {
                animSearchInitialPage = pagerState.currentPage
            }
            Column(
                modifier = Modifier.padding(padding)
            ) {
                ScrollHeaderBox(
                    canScroll = {
                        if(isHeaderShowForever.value){
                            false
                        }else{
                            val controller = vm.controllerList[pagerState.currentPage]
                            !(it.y < 0 && controller.isCurLast)
                        }
                    },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background),
                    header = {
                        HomeTabRow(selectedTabIndex = pagerState.currentPage) {
                            for(i in vm.searchTitle.indices){
                                HomeTabItem(
                                    selected = i == pagerState.currentPage,
                                    text = {
                                        Text(text = vm.searchTitle[i])
                                    },
                                    onClick = {
                                        scope.launch {
                                            pagerState.animateScrollToPage(i)
                                        }
                                    }
                                )
                            }
                        }
                    }) { padding ->
                    HorizontalPager(
                        modifier = Modifier.fillMaxHeight(),
                        count =  vm.searchTitle.size,
                        state = pagerState,
                        key = {it}
                    ) {

                        val lazyListState = rememberLazyListState()

                        val controller = remember {
                            vm.controllerList[it]
                        }
                        SearchPage(isHeaderShowForever ,padding ,vm = vm, controller = controller, isEnable = pagerState.currentPage == it, lazyListState = lazyListState)


                    }
                }
            }
        }
    )





}