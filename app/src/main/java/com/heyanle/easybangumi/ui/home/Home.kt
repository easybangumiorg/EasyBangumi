package com.heyanle.easybangumi.ui.home

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.heyanle.easybangumi.LocalNavController
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.SEARCH
import com.heyanle.easybangumi.ui.common.FastScrollToTopFab
import com.heyanle.easybangumi.ui.home.history.AnimHistory
import com.heyanle.easybangumi.ui.home.home.AnimHome
import com.heyanle.easybangumi.ui.home.my.AnimMy
import com.heyanle.easybangumi.ui.common.HomeTabItem
import com.heyanle.easybangumi.ui.common.HomeTabRow
import com.heyanle.easybangumi.ui.common.HomeTopAppBar
import com.heyanle.easybangumi.ui.setting.SettingPage
import com.heyanle.okkv2.core.okkv
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/1/7 21:52.
 * https://github.com/heyanLE
 */

// 番剧页面相关的子页面
sealed class HomePage(
    val tabLabel: @Composable (() -> Unit),
    val content: @Composable (()->Unit),
){
    //番剧主页
    object Home: HomePage(
        tabLabel = { Text(text = stringResource(id = R.string.home)) },
        content = { AnimHome() }
    )

    // 我的追番
    object My: HomePage(
        tabLabel = { Text(text = stringResource(id = R.string.my_anim)) },
        content = { AnimMy() }
    )

    // 历史记录
    object History: HomePage(
        tabLabel = { Text(text = stringResource(id = R.string.mine_history)) },
        content = { AnimHistory() }
    )

    object Setting: HomePage(
        tabLabel = { Text(text = stringResource(id = R.string.setting)) },
        content = { SettingPage() }
    )
}

val animSubPageItems = listOf(
    HomePage.Home,
    HomePage.My,
    HomePage.History,
    HomePage.Setting
)

var animInitialPage by okkv("animInitialPage", 0)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun Home(
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
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            Column() {
                HomeTopAppBar(
                    scrollBehavior = scrollBehavior,
                    label = {
                        Text(text = stringResource(id = R.string.anim_title))
                    },
                    isShowSearch = true,
                    onSearch = {
                        scope.launch {
                            nav.navigate(SEARCH)
                        }

                    }
                )
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
            }


        },
        content = { padding ->

            Log.d("Home", padding.calculateTopPadding().value.toString())
            SideEffect {
                animInitialPage = pagerState.currentPage
            }

            Column(modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .padding(padding)
                .nestedScroll(scrollBehavior.nestedScrollConnection),

                ) {
//                HomeTabRow(selectedTabIndex = pagerState.currentPage) {
//                    for(i in animSubPageItems.indices){
//                        HomeTabItem(
//                            selected = i == pagerState.currentPage,
//                            text = animSubPageItems[i].tabLabel,
//                            onClick = {
//                                scope.launch {
//                                    pagerState.animateScrollToPage(i)
//                                }
//                            }
//                        )
//                    }
//                }

                HorizontalPager(
                    modifier = Modifier.background(MaterialTheme.colorScheme.background),
                    state = pagerState,
                    count = animSubPageItems.size
                ) {
                    animSubPageItems[it].content()
                }

            }




        }
    )

}