package com.heyanle.easybangumi.ui.home.search

import android.animation.ValueAnimator
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.insets.statusBarsHeight
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.heyanle.easybangumi.LocalNavController
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.ui.common.FastScrollToTopFab
import com.heyanle.easybangumi.ui.common.HomeTabItem
import com.heyanle.easybangumi.ui.common.HomeTabRow
import com.heyanle.easybangumi.ui.common.LoadingPage
import com.heyanle.easybangumi.ui.common.ScrollHeaderBox
import com.heyanle.easybangumi.ui.common.SearchTopBar
import com.heyanle.easybangumi.ui.home.animInitialPage
import com.heyanle.okkv2.core.okkv
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

/**
 * Created by HeYanLe on 2023/1/10 16:34.
 * https://github.com/heyanLE
 */

var animSearchInitialPage by okkv("animSearchInitialPage", 0)

var topAppBarExpendAnimator: WeakReference<ValueAnimator>? = null
fun onNewTopAppBarExpendAnim(valueAnimator: ValueAnimator?){
    topAppBarExpendAnimator?.get()?.cancel()
    topAppBarExpendAnimator = valueAnimator?.let {
        WeakReference(it)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class,
    ExperimentalComposeUiApi::class
)
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

    val uiController = rememberSystemUiController()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    LaunchedEffect(key1 = Unit){
        pagerState.scrollToPage(animInitialPage)
    }

    // 饱和 cancel
    DisposableEffect(key1 = Unit){
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

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            Surface(
                shadowElevation = 4.dp,
                color = MaterialTheme.colorScheme.primary
            ){
                Column() {
                    SearchTopBar(
                        modifier = Modifier.statusBarsPadding(),
                        placeholder = {

                            Text(modifier = Modifier, textAlign = TextAlign.Start,text = stringResource(id = R.string.anim_search))
                        },
                        text = keyword,
                        onBack = {
                            nav.popBackStack()
                        },
                        onSearch = {
                            vm.keywordState.value = it
                        },
                        scrollBehavior = scrollBehavior,
                        containerColor = Color.Transparent,
                        focusRequester = focusRequester
                    )
                    HomeTabRow(
                        containerColor = MaterialTheme.colorScheme.primary,
                        selectedTabIndex = pagerState.currentPage
                    ) {
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
                }
            }

        },
        content = { padding ->
            val keyboard = LocalSoftwareKeyboardController.current
            HorizontalPager(
                modifier = Modifier
                    .fillMaxHeight()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .nestedScroll(object: NestedScrollConnection{
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
                count =  vm.searchTitle.size,
                state = pagerState,
                key = {it}
            ) {

                val lazyListState = rememberLazyListState()

                val controller = remember {
                    vm.controllerList[it]
                }
                Box(modifier = Modifier.fillMaxSize()){
                    SearchPage(isHeaderShowForever ,vm = vm, controller = controller, isEnable = pagerState.currentPage == it, lazyListState = lazyListState)

                    FastScrollToTopFab(listState = lazyListState){
                        val anim = ValueAnimator.ofFloat(0F, 1F)
                        val sourceHeightOffset = scrollBehavior.state.heightOffset
                        val sourceContentOffset = scrollBehavior.state.contentOffset
                        anim.addUpdateListener {
                            runCatching {
                                val float = it.animatedValue as Float
                                val targetHeightOffset = sourceHeightOffset + (0F - sourceHeightOffset)*float
                                val targetContentOffset = sourceContentOffset + (0F - sourceContentOffset)*float
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
    )
}