package com.heyanle.easy_bangumi_cm.shared.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.heyanle.easy_bangumi_cm.base.service.provider.IPathProvider
import com.heyanle.easy_bangumi_cm.base.service.system.ILogger
import com.heyanle.easy_bangumi_cm.base.service.system.IPlatformInformation
import com.heyanle.easy_bangumi_cm.common.resources.Res
import com.heyanle.easy_bangumi_cm.shared.DEBUG
import com.heyanle.easy_bangumi_cm.shared.LocalNavController
import com.heyanle.easy_bangumi_cm.shared.ui.main.home.Home
import com.heyanle.lib.inject.api.get
import com.heyanle.lib.inject.core.Inject
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview


/**
 * Created by HeYanLe on 2025/1/5 23:26.
 * https://github.com/heyanLE
 */

sealed class MainPage(
    val route: String,
    val tabLabel: @Composable (() -> Unit),
    val icon: @Composable ((Boolean) -> Unit),
    val content: @Composable (() -> Unit),
) {
    data object Home : MainPage(
        route = "home",
        tabLabel = {
            Text(text = stringResource(Res.strings.home))
        },
        icon = {
            Icon(
                if (it) Icons.Filled.Home else Icons.Outlined.Home,
                contentDescription = stringResource(Res.strings.home)
            )
        },
        content = {
            Home()
        }
    )

    object SearchPage : MainPage(
        route = "search",
        tabLabel = { Text(text = "搜索") },
        icon = {
            Icon(
                if (it) Icons.Filled.Search else Icons.Outlined.Search,
                contentDescription = "搜索"
            )
        },
        content = {

        },
    )

    object StorePage : MainPage(
        route = "storage",
        tabLabel = { Text(text = "媒体库") },
        icon = {
            Icon(
                if (it) Icons.Filled.Folder else Icons.Outlined.Folder,
                contentDescription = "媒体库"
            )
        },
        content = {

        }
    )

    object MorePage : MainPage(
        route = "more",
        tabLabel = { Text(text = "更多") },
        icon = {
            Icon(
                if (it) Icons.Filled.Settings else Icons.Outlined.Settings,
                contentDescription = "更多"
            )
        },
        content = {

        },
    )

}

val MainPageItems = listOf(
    MainPage.Home,
    MainPage.SearchPage,
    MainPage.StorePage,
    MainPage.MorePage,
)

@Composable
expect fun MainHook()

@Preview
@Composable
fun Main() {
    MainHook()
    val navController = LocalNavController.current


    val needDebug = remember {
        ! Inject.get<IPlatformInformation>().isRelease
    }
    val pagerState = rememberPagerState(0) { MainPageItems.size + if (needDebug) 1 else 0 }
    val scope = rememberCoroutineScope()

    val fileProvider = Inject.get<IPathProvider>()
    val platform = Inject.get<IPlatformInformation>()
    val logger = Inject.get<ILogger>()
    logger.i("Main", "platform: ${platform}")
    logger.i("Main", "fileProvider: ${fileProvider.getFilePath("test")}")
    logger.i("Main", "fileProvider: ${fileProvider.getCachePath("test")}")

    // val windowSizeClass = calculateWindowSizeClass()

    Row {
        NavigationRail {
            MainPageItems.forEachIndexed { index, page ->
                val selected = pagerState.currentPage == index
                NavigationRailItem(
                    selected = selected,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                            if (needDebug && index == MainPageItems.size) {
                                navController.navigate(DEBUG)
                            } else {

                            }
                        }
                    },
                    icon = {
                        page.icon(selected)
                    },
                    label = {
                        page.tabLabel()
                    }
                )
            }

            if (needDebug) {
                NavigationRailItem(
                    selected = false,
                    onClick = {
                        scope.launch {
                            navController.navigate(DEBUG)
                        }
                    },
                    icon = {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = "debug"
                        )
                    },
                    label = {
                        Text("Debug")
                    }
                )
            }


        }
        Column {
            VerticalPager(state = pagerState) {
                MainPageItems[it].content()
            }
        }
    }
}