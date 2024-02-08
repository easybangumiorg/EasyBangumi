package com.heyanle.easybangumi4.ui.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.ui.common.SourceContainer
import com.heyanle.easybangumi4.ui.main.history.History
import com.heyanle.easybangumi4.ui.main.home.Home
import com.heyanle.easybangumi4.ui.main.more.More
import com.heyanle.easybangumi4.ui.main.star.Star
import com.heyanle.easybangumi4.ui.source_manage.SourceManager
import com.heyanle.easybangumi4.utils.isCurPadeMode
import com.heyanle.okkv2.core.okkv
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/2/19 0:11.
 * https://github.com/heyanLE
 */
// 番剧页面相关的子页面
sealed class MainPage constructor(
    val route: String,
    val tabLabel: @Composable (() -> Unit),
    val icon: @Composable ((Boolean) -> Unit),
    val content: @Composable (() -> Unit),
) {

    object HomePage : MainPage(
        route = "home",
        tabLabel = { Text(text = stringResource(id = R.string.home)) },
        icon = {
            Icon(
                if (it) Icons.Filled.Home else Icons.Outlined.Home,
                contentDescription = stringResource(id = R.string.home)
            )
        },
        content = {
            SourceContainer {
                Home()
            }
        },
    )

    object StarPage : MainPage(
        route = "star",
        tabLabel = { Text(text = stringResource(id = R.string.my_anim)) },
        icon = {
            Icon(
                if (it) Icons.Filled.Star else Icons.Filled.StarOutline,
                contentDescription = stringResource(id = R.string.my_anim)
            )
        },
        content = {
            SourceContainer {
                Star()
            }
        },
    )

//    object UpdatePage : MainPage(
//        route = "update",
//        tabLabel = { Text(text = stringResource(id = R.string.update)) },
//        icon = {
//            Icon(
//                if (it) Icons.Filled.Report else Icons.Outlined.Report,
//                contentDescription = stringResource(id = R.string.update)
//            )
//        },
//        content = {
//            Update()
//            //Text(text = stringResource(id = R.string.update))
//        }
//    )

    object HistoryPage : MainPage(
        route = "history",
        tabLabel = { Text(text = stringResource(id = R.string.mine_history)) },
        icon = {
            Icon(
                if (it) Icons.Filled.History else Icons.Outlined.History,
                contentDescription = stringResource(id = R.string.mine_history)
            )
        },
        content = {
            History()
            // Text(text = stringResource(id = R.string.history))
        }
    )

    object SourceManagePage : MainPage(
        route = "source_manage",
        tabLabel = { Text(text = stringResource(id = R.string.manage)) },
        icon = {
            Icon(
                if (it) Icons.Filled.Explore else Icons.Outlined.Explore,
                contentDescription = stringResource(id = R.string.manage)
            )
        },
        content = {
            SourceManager()
        }
    )

    object MorePage : MainPage(
        route = "more",
        tabLabel = { Text(text = stringResource(id = R.string.more)) },
        icon = {
            Icon(
                if (it) Icons.Filled.MoreHoriz else Icons.Outlined.MoreHoriz,
                contentDescription = stringResource(id = R.string.more)
            )
        },
        content = {
            More()
            //Text(text = stringResource(id = R.string.more))
        },
    )

}

val MainPageItems = listOf(
    MainPage.HomePage,
    MainPage.StarPage,
    MainPage.HistoryPage,
    // MainPage.SourceManagePage,
    MainPage.MorePage,
)

var homePageIndexOkkv by okkv("home_page_index", 0)


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Main() {

    val pagerState =
        rememberPagerState(initialPage = if (homePageIndexOkkv >= 0 && homePageIndexOkkv < MainPageItems.size) homePageIndexOkkv else 0,0F) { MainPageItems.size }

    


    val scope = rememberCoroutineScope()

    val vm = viewModel<MainViewModel>()

    Surface(
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        val isPad = isCurPadeMode()

        if(!isPad){
            Column() {
                HorizontalPager(
                    userScrollEnabled = false,
                    state = pagerState,
                    modifier = Modifier.weight(1f),
                ) {
                    MainPageItems[it].content()
                }

                if (vm.customBottomBar == null) {
                    NavigationBar() {
                        MainPageItems.forEachIndexed { i, page ->
                            val select = pagerState.currentPage == i
                            NavigationBarItem(
                                icon = {
                                    page.icon(select)
                                },
                                label = page.tabLabel,
                                selected = select,
                                alwaysShowLabel = true,
                                onClick = {
                                    scope.launch {
                                        pagerState.scrollToPage(i)
                                    }
                                    homePageIndexOkkv = i
                                }
                            )
                        }
                    }
                } else {
                    vm.customBottomBar?.let { it() }
                }
            }
        }else{
            Row {
                NavigationRail (
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),

                    ){
                    MainPageItems.forEachIndexed { i, page ->
                        val select = pagerState.currentPage == i
                        NavigationRailItem(
                            selected = select,
                            onClick = {
                                scope.launch {
                                    pagerState.scrollToPage(i)
                                }
                                homePageIndexOkkv = i
                            },
                            icon = {page.icon(select) },
                            label = page.tabLabel,
                            alwaysShowLabel = false,
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                ) {
                    VerticalPager(state = pagerState, userScrollEnabled = false,modifier = Modifier.weight(1f), ) {
                        MainPageItems[it].content()
                    }
                    if (vm.customBottomBar != null) {
                        vm.customBottomBar?.let { it() }
                    }
                }

            }
        }
    }


}