package com.heyanle.easybangumi.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
import com.heyanle.easybangumi.ui.anim.AnimPage
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
    ){
    @OptIn(ExperimentalMaterial3Api::class)
    object Anim: HomePage(
        router = ANIM,
        icon = {
            Icon(Icons.Filled.LiveTv, contentDescription = stringResource(id = R.string.watch_anim))
        },
        tabLabel = {
            Text(text = stringResource(id = R.string.watch_anim))
        },
    )

    @OptIn(ExperimentalMaterial3Api::class)
    object Comic: HomePage(
        router = COMIC,
        icon = {
            Icon(Icons.Filled.Book, contentDescription = stringResource(id = R.string.read_comic))
        },
        tabLabel = {
            Text(text = stringResource(id = R.string.read_comic))
        },
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

    val scope = rememberCoroutineScope()

    val homeNavController = rememberNavController()

    var currentPageIndex by remember {
        mutableStateOf(initialPage)
    }

    SideEffect {
        initialPage = currentPageIndex
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ){
                for (i in pageItems.indices){
                    val selected = currentPageIndex == i
                    NavigationBarItem(
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onSecondary,
                            selectedTextColor = MaterialTheme.colorScheme.secondary,
                            unselectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            unselectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            indicatorColor = MaterialTheme.colorScheme.secondary
                        ),
                        alwaysShowLabel = false,
                        selected = selected,
                        onClick = {
                            scope.launch {
                                homeNavController.navigate(pageItems[i].router){
                                    // Pop up to the start destination of the graph to
                                    // avoid building up a large stack of destinations
                                    // on the back stack as users select items
                                    popUpTo(homeNavController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    // Avoid multiple copies of the same destination when
                                    // reselecting the same item
                                    launchSingleTop = true
                                    // Restore state when reselecting a previously selected item
                                    restoreState = true
                                }
                                currentPageIndex = i
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
        content = {padding ->
            NavHost(homeNavController, startDestination = pageItems[currentPageIndex].router, Modifier.padding(padding)) {
                composable(HomePage.Anim.router) {
                    AnimPage()
                }
                composable(HomePage.Comic.router) {

                }
                composable(HomePage.Novel.router) {

                }
                composable(HomePage.Setting.router) {
                    SettingPage()
                }
            }
        }
    )
}