package com.heyanle.easybangumi.ui.anim

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.heyanle.easybangumi.ANIM
import com.heyanle.easybangumi.LocalNavController
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.SEARCH
import com.heyanle.easybangumi.ui.anim.history.AnimHistory
import com.heyanle.easybangumi.ui.anim.home.AnimHome
import com.heyanle.easybangumi.ui.anim.my.AnimMy
import com.heyanle.easybangumi.ui.common.HomeTabItem
import com.heyanle.easybangumi.ui.common.HomeTabRow
import com.heyanle.easybangumi.ui.common.HomeTopAppBar
import com.heyanle.easybangumi.ui.common.moeSnackBar
import com.heyanle.okkv2.core.okkv
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/1/7 21:52.
 * https://github.com/heyanLE
 */

// 番剧页面相关的子页面
sealed class AnimSubPage(
    val tabLabel: @Composable (() -> Unit),
    val content: @Composable (()->Unit),
){
    //番剧主页
    object Home: AnimSubPage(
        tabLabel = { Text(text = stringResource(id = R.string.home)) },
        content = { AnimHome() }
    )

    // 我的追番
    object My: AnimSubPage(
        tabLabel = { Text(text = stringResource(id = R.string.my_anim)) },
        content = { AnimMy() }
    )

    // 历史记录
    object History: AnimSubPage(
        tabLabel = { Text(text = stringResource(id = R.string.mine_history)) },
        content = { AnimHistory() }
    )
}

val animSubPageItems = listOf(
    AnimSubPage.Home,
    AnimSubPage.My,
    AnimSubPage.History
)

var animInitialPage by okkv("animInitialPage", 0)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun AnimPage(
){
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val nav = LocalNavController.current
    val scope = rememberCoroutineScope()

    val pagerState = rememberPagerState(
        initialPage = animInitialPage
    )

    LaunchedEffect(key1 = Unit){
        pagerState.scrollToPage(animInitialPage)
    }


    Scaffold(
        topBar = {
            HomeTopAppBar(
                scrollBehavior = scrollBehavior,
                label = {
                    Text(text = stringResource(id = R.string.anim_title))
                },
                isShowSearch = true,
                onSearch = {
                    scope.launch {
                        nav.navigate("${SEARCH}/${ANIM}")
                    }

                }
            )
        },
        content = { padding ->

            SideEffect {
                animInitialPage = pagerState.currentPage
            }

            Column(modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .nestedScroll(scrollBehavior.nestedScrollConnection)

            ) {
                HomeTabRow(selectedTabIndex = pagerState.currentPage) {
                    for(i in animSubPageItems.indices){
                        HomeTabItem(
                            selected = i == pagerState.currentPage,
                            text = animSubPageItems[i].tabLabel,
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(i)
                                }
                            }
                        )
                    }
                }

                HorizontalPager(
                    modifier = Modifier,
                    state = pagerState,
                    count = animSubPageItems.size
                ) {
                    animSubPageItems[it].content()
                }

            }
        }
    )

}