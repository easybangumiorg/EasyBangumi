package org.easybangumi.next.shared.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.launch
import org.easybangumi.next.platformInformation
import org.easybangumi.next.shared.LocalNavController
import org.easybangumi.next.shared.RouterPage
import org.easybangumi.next.shared.foundation.stringRes
import org.easybangumi.next.shared.resources.Res
import org.easybangumi.next.shared.ui.UI
import org.easybangumi.next.shared.ui.home.history.History
import org.easybangumi.next.shared.ui.home.more.More
import org.easybangumi.next.shared.ui.home.star.Star
import org.easybangumi.next.shared.ui.home.discover.HomeDiscover

/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 */

sealed class HomePage(
    val route: String,
    val tabLabel: @Composable (() -> Unit),
    val icon: @Composable ((Boolean) -> Unit),
    val content: @Composable (() -> Unit),
) {
    data object DiscoverPage : HomePage(
        route = "discover",
        tabLabel = {
            Text(text = stringResource(Res.strings.discover))
        },
        icon = {
            Icon(
                if (it) Icons.Filled.Explore else Icons.Outlined.Explore,
                contentDescription = stringResource(Res.strings.home)
            )
        },
        content = {
            HomeDiscover()
        }
    )

    data object StarPage : HomePage(
        route = "star",
        tabLabel = {
            Text(text = stringResource(Res.strings.star))
        },
        icon = {
            Icon(
                if (it) Icons.Filled.Star else Icons.Filled.StarOutline,
                contentDescription = stringResource(Res.strings.star)
            )
        },
        content = {
            Star()
        }
    )

    object HistoryPage : HomePage(
        route = "history",
        tabLabel = { Text(text = stringResource(Res.strings.history)) },
        icon = {
            Icon(
                if (it) Icons.Filled.History else Icons.Outlined.History,
                contentDescription = stringResource(Res.strings.history)
            )
        },
        content = {
            History()
            // Text(text = stringResource(id = R.string.history))
        }
    )

    object MorePage : HomePage(
        route = "more",
        tabLabel = { Text(text = stringResource(Res.strings.more)) },
        icon = {
            Icon(
                if (it) Icons.Filled.MoreHoriz else Icons.Outlined.MoreHoriz,
                contentDescription = stringResource(Res.strings.more)
            )
        },
        content = {
            More()
        },
    )
}

val HomePageList = listOf(
    HomePage.DiscoverPage,
    HomePage.StarPage,
    HomePage.HistoryPage,
    HomePage.MorePage,
)

@Composable
fun Home() {

    val navController = LocalNavController.current
    val pagerState = rememberPagerState(0) { HomePageList.size }
    val scope = rememberCoroutineScope()
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (UI.isTabletMode()) {
            Row(
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                NavigationRail(
                    modifier = Modifier.fillMaxHeight(),
                    contentColor = Color.Transparent,
                    header = {
                        IconButton(onClick = {

                        }) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = stringRes(Res.strings.search)
                            )
                        }
                    }
                ) {
                    HomePageList.forEachIndexed { index, page ->
                        val selected = pagerState.currentPage == index
                        NavigationRailItem(
                            selected = selected,
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
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
                }
                VerticalPager(
                    modifier = Modifier.fillMaxHeight().weight(1f).clip(RoundedCornerShape(16.dp)),
                    state = pagerState,
                    userScrollEnabled = false
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        HomePageList[it].content()
                    }
                }
            }
        } else {
            Column {
                HorizontalPager(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    state = pagerState,
                    userScrollEnabled = false
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        HomePageList[it].content()
                    }
                }
                NavigationBar(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HomePageList.forEachIndexed { index, page ->
                        val selected = pagerState.currentPage == index
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
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
                }
            }
        }
        Text(
            modifier = Modifier.align(Alignment.TopStart).clickable {
                navController.navigate(RouterPage.Debug.HOME)
            },
            text = "Debug Mode"
        )
        if (platformInformation.isDebug) {

        }
    }



}