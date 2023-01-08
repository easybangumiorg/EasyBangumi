package com.heyanle.easybangumi.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import com.heyanle.easybangumi.ANIM
import com.heyanle.easybangumi.COMIC
import com.heyanle.easybangumi.LocalNavController
import com.heyanle.easybangumi.NOVEL
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.SEARCH
import com.heyanle.easybangumi.SETTING
import com.heyanle.easybangumi.ui.LoadingPage
import com.heyanle.easybangumi.ui.common.HomeTopAppBar
import com.heyanle.easybangumi.ui.setting.SettingPage
import com.heyanle.okkv2.core.okkv
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/1/7 16:44.
 * https://github.com/heyanLE
 */

sealed class HomePage @OptIn(ExperimentalMaterial3Api::class) constructor(
    val router: String,
    val icon: @Composable () -> Unit,
    val tabLabel: @Composable (() -> Unit),
    val topBarLabel: @Composable (()->Unit),
    val isShowSearch: Boolean = true,
    val content: @Composable (() -> Unit),

    ){
    @OptIn(ExperimentalMaterial3Api::class)
    object Anim: HomePage(
        router = ANIM,
        icon = {
            Icon(Icons.Filled.LiveTv, contentDescription = stringResource(id = R.string.watch_anim))
        },
        topBarLabel = {
            Text(text = stringResource(id = R.string.anim_title))
        },
        tabLabel = {
            Text(text = stringResource(id = R.string.watch_anim))
        },
        content = {
            LoadingPage(
                modifier = Modifier.fillMaxSize(),
                loadingMsg = "开发中"
            )
        }
    )

    @OptIn(ExperimentalMaterial3Api::class)
    object Comic: HomePage(
        router = COMIC,
        icon = {
            Icon(Icons.Filled.Book, contentDescription = stringResource(id = R.string.read_comic))
        },
        topBarLabel = {
            Text(text = stringResource(id = R.string.comic_title))
        },
        tabLabel = {
            Text(text = stringResource(id = R.string.read_comic))
        },
        content = {
            LoadingPage(
                modifier = Modifier.fillMaxSize(),
                loadingMsg = "开发中"
            )
        }
    )

    @OptIn(ExperimentalMaterial3Api::class)
    object Novel: HomePage(
        router = NOVEL,
        icon = {
            Icon(Icons.Filled.MenuBook, contentDescription = stringResource(id = R.string.read_novel))
        },
        tabLabel = {
            Text(text = stringResource(id = R.string.read_novel))
        },
        topBarLabel = {
            Text(text = stringResource(id = R.string.novel_title))
        },
        content = {
            LoadingPage(
                modifier = Modifier.fillMaxSize(),
                loadingMsg = "开发中"
            )
        }
    )

    @OptIn(ExperimentalMaterial3Api::class)
    object Setting: HomePage(
        router = SETTING,
        icon = {
            Icon(Icons.Filled.Settings, contentDescription = stringResource(id = R.string.setting))
        },
        tabLabel = {
            Text(text = stringResource(id = R.string.setting))
        },
        topBarLabel = {
            Text(text = stringResource(id = R.string.setting))
        },
        isShowSearch = false,
        content = {
            SettingPage()
        }
    )
}
val pageItems = listOf(
    HomePage.Anim,
    HomePage.Comic,
    HomePage.Novel,
    HomePage.Setting,
)

var initialPage by okkv("initialPage", 0)
@OptIn(ExperimentalMaterial3Api::class)
val LocalTopAppBarScrollBehavior = staticCompositionLocalOf<TopAppBarScrollBehavior> {
    error("AppNavController Not Provide")
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun Home(){

    val pageState = rememberPagerState(initialPage = initialPage)
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        canScroll = {
            true
        }
    )
    val nav = LocalNavController.current
    val scope = rememberCoroutineScope()

    val page = pageItems[pageState.currentPage]

    SideEffect {
        initialPage = pageState.currentPage
    }

    CompositionLocalProvider(LocalTopAppBarScrollBehavior provides scrollBehavior) {
        Scaffold(
            topBar = {

                HomeTopAppBar(
                    scrollBehavior = scrollBehavior,
                    label = page.topBarLabel,
                    onSearch = {
                        nav.navigate("${SEARCH}/${page.router}")
                    },
                    isShowSearch = page.isShowSearch
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.primary
                ){
                    for (i in pageItems.indices){
                        val selected = pageState.currentPage == i
                        NavigationBarItem(
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primary,
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                                unselectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                unselectedTextColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            alwaysShowLabel = false,
                            selected = selected,
                            onClick = {
                                scope.launch {
                                    pageState.animateScrollToPage(i)
                                }
                            },
                            icon = {
                                pageItems[i].icon()
                            },
                            label = {
                                pageItems[i].tabLabel()
                            }
                        )
                    }
                }
            },
            content = { padding ->
                HorizontalPager(
                    modifier = Modifier
                        .padding(padding)
                        .nestedScroll(scrollBehavior.nestedScrollConnection),
                    count = pageItems.size,
                    state = pageState,
                ) {
                    pageItems[it].content()
                }

            }
        )
    }
}