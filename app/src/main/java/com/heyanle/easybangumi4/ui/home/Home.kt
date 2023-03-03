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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.ui.home.explore.Explore
import com.heyanle.okkv2.core.okkv

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
            Text(text = stringResource(id = R.string.my_anim))
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
            Text(text = stringResource(id = R.string.history))
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

var homePageIndex by okkv("homePageInitPageIndex", 0)

@OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class,
    ExperimentalAnimationApi::class
)
@Composable
fun Home() {
    val homeNavController = rememberNavController()

    Surface(
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        Column() {
            NavHost(
                modifier = Modifier.weight(1f),
                startDestination = HomePageItems.getOrNull(homePageIndex)?.route
                    ?: HomePage.StarPage.route,
                navController = homeNavController
            ) {
                HomePageItems.forEach { page ->
                    composable(page.route){
                        page.content()
                    }
                }
            }
            NavigationBar(){
                val navBackStackEntry by homeNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                HomePageItems.forEachIndexed { i, page ->
                    val select  = currentDestination?.hierarchy?.any { it.route == page.route } == true
                    NavigationBarItem(
                        icon = {
                            page.icon(select)
                        },
                        label = page.tabLabel,
                        selected = select,
                        alwaysShowLabel = false,
                        onClick = {
                            homePageIndex = i
                            homeNavController.navigate(page.route) {
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
                        }
                    )
                }
            }

        }

    }

}