package com.heyanle.easybangumi4.ui.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Report
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.ui.common.SourceContainer
import com.heyanle.easybangumi4.ui.home.explore.Explore
import com.heyanle.easybangumi4.ui.home.history.History
import com.heyanle.easybangumi4.ui.home.star.Star
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/2/19 0:11.
 * https://github.com/heyanLE
 */
// 番剧页面相关的子页面
sealed class HomePage(
    val route: String,
    val tabLabel: @Composable (() -> Unit),
    val icon: @Composable ((Boolean) -> Unit),
    val content: @Composable (() -> Unit),
) {
    object StarPage : HomePage(
        route = "star",
        tabLabel = { Text(text = stringResource(id = R.string.my_anim)) },
        icon = {
            Icon(
                if(it) Icons.Filled.Star else Icons.Filled.StarOutline,
                contentDescription = stringResource(id = R.string.my_anim)
            )
        },
        content = {
            SourceContainer {
                Star()
            }
        },
    )

    object UpdatePage : HomePage(
        route = "update",
        tabLabel = { Text(text = stringResource(id = R.string.update)) },
        icon = {
            Icon(
                if(it) Icons.Filled.Report else Icons.Outlined.Report,
                contentDescription = stringResource(id = R.string.update)
            )
        },
        content = {
            Text(text = stringResource(id = R.string.update))
        }
    )

    object HistoryPage : HomePage(
        route = "history",
        tabLabel = { Text(text = stringResource(id = R.string.history)) },
        icon = {
            Icon(
                if(it) Icons.Filled.History else Icons.Outlined.History,
                contentDescription = stringResource(id = R.string.history)
            )
        },
        content = {
            History()
            // Text(text = stringResource(id = R.string.history))
        }
    )

    object ExplorePage : HomePage(
        route = "explore",
        tabLabel = { Text(text = stringResource(id = R.string.explore)) },
        icon = {
            Icon(
                if(it) Icons.Filled.Explore else Icons.Outlined.Explore,
                contentDescription = stringResource(id = R.string.explore)
            )
        },
        content = {
            Explore()
        }
    )

    object MorePage : HomePage(
        route = "more",
        tabLabel = { Text(text = stringResource(id = R.string.more)) },
        icon = {
            Icon(
                if(it) Icons.Filled.MoreHoriz else Icons.Outlined.MoreHoriz,
                contentDescription = stringResource(id = R.string.more)
            )
        },
        content = {
            Text(text = stringResource(id = R.string.more))
        }
    )

}

val HomePageItems = listOf(
    HomePage.StarPage,
    HomePage.UpdatePage,
    HomePage.HistoryPage,
    HomePage.ExplorePage,
    HomePage.MorePage,
)

val LocalHomeViewModel = staticCompositionLocalOf<HomeViewModel> {
    error("HomeViewModel Not Provide")
}


@OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class,
    ExperimentalAnimationApi::class
)
@Composable
fun Home() {

    val pagerState = rememberPagerState(initialPage = 0)

    val scope = rememberCoroutineScope()

    val vm = viewModel<HomeViewModel>()

    CompositionLocalProvider(
        LocalHomeViewModel provides vm
    ) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ) {
            Column() {

                HorizontalPager(
                    userScrollEnabled = false,
                    state = pagerState,
                    modifier = Modifier.weight(1f),
                    count = HomePageItems.size,
                ) {
                    HomePageItems[it].content()

                }

                if(vm.customBottomBar == null){
                    NavigationBar(){
                        HomePageItems.forEachIndexed { i, page ->
                            val select  = pagerState.currentPage == i
                            NavigationBarItem(
                                icon = {
                                    page.icon(select)
                                },
                                label = page.tabLabel,
                                selected = select,
                                alwaysShowLabel = false,
                                onClick = {
                                    scope.launch {
                                        pagerState.animateScrollToPage(i)
                                    }
                                }
                            )
                        }
                    }
                }else{
                    vm.customBottomBar?.let { it() }
                }
            }

        }
    }



}