package com.heyanle.easybangumi4.v2.ui.main

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.ui.main.MainViewModel
import com.heyanle.easybangumi4.ui.main.homePageIndexOkkv
import com.heyanle.easybangumi4.utils.isCurPadeMode
import com.heyanle.easybangumi4.v2.ui.legacy.LegacyHistoryScreen
import com.heyanle.easybangumi4.v2.ui.legacy.LegacyHomeScreen
import com.heyanle.easybangumi4.v2.ui.legacy.LegacyMoreScreen
import com.heyanle.easybangumi4.v2.ui.legacy.LegacyStarScreen
import com.heyanle.easybangumi4.v2.theme.V2Tokens
import com.heyanle.easybangumi4.v2.theme.V2Theme
import com.heyanle.easybangumi4.v2.ui.component.V2Icon
import kotlinx.coroutines.launch

/**
 * V2 ownership boundary for the four main destinations.
 *
 * Each tab owns a V2 presentation while continuing to share the existing [MainViewModel].
 * Secondary destinations remain behind named adapters and can be replaced independently without
 * changing the activity or route contract.
 */
private sealed class MainV2Page(
    val label: @Composable () -> Unit,
    val icon: @Composable (selected: Boolean) -> Unit,
    val content: @Composable () -> Unit,
) {
    data object Home : MainV2Page(
        label = { Text(stringResource(R.string.home)) },
        icon = { selected ->
            V2Icon(
                imageVector = if (selected) Icons.Filled.Home else Icons.Outlined.Home,
                contentDescription = stringResource(R.string.home),
                tint = LocalContentColor.current,
            )
        },
        content = { LegacyHomeScreen() },
    )

    data object Star : MainV2Page(
        label = { Text(stringResource(R.string.my_anim)) },
        icon = { selected ->
            V2Icon(
                imageVector = if (selected) Icons.Filled.Star else Icons.Filled.StarOutline,
                contentDescription = stringResource(R.string.my_anim),
                tint = LocalContentColor.current,
            )
        },
        content = { LegacyStarScreen() },
    )

    data object History : MainV2Page(
        label = { Text(stringResource(R.string.mine_history)) },
        icon = { selected ->
            V2Icon(
                imageVector = if (selected) Icons.Filled.History else Icons.Outlined.History,
                contentDescription = stringResource(R.string.mine_history),
                tint = LocalContentColor.current,
            )
        },
        content = { LegacyHistoryScreen() },
    )

    data object More : MainV2Page(
        label = { Text(stringResource(R.string.more)) },
        icon = { selected ->
            V2Icon(
                imageVector = if (selected) Icons.Filled.MoreHoriz else Icons.Outlined.MoreHoriz,
                contentDescription = stringResource(R.string.more),
                tint = LocalContentColor.current,
            )
        },
        content = { LegacyMoreScreen() },
    )
}

private val mainV2Pages = listOf(
    MainV2Page.Home,
    MainV2Page.Star,
    MainV2Page.History,
    MainV2Page.More,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun MainV2Shell() {
    val initialPage = homePageIndexOkkv.takeIf { it in mainV2Pages.indices } ?: 0
    val pagerState = rememberPagerState(initialPage = initialPage) { mainV2Pages.size }
    val activity = LocalContext.current as Activity
    val scope = rememberCoroutineScope()
    val viewModel = viewModel<MainViewModel>()

    BackHandler {
        if (!activity.moveTaskToBack(true)) {
            activity.finish()
        }
    }

    Surface(
        color = V2Tokens.WarmBackground,
        contentColor = V2Tokens.TextPrimary,
    ) {
        if (!isCurPadeMode()) {
            Column {
                HorizontalPager(
                    state = pagerState,
                    userScrollEnabled = false,
                    modifier = Modifier.weight(1f),
                ) { pageIndex ->
                    mainV2Pages[pageIndex].content()
                }

                val customBottomBar = viewModel.customBottomBar
                if (customBottomBar == null) {
                    V2BottomNavigation(
                        selectedIndex = pagerState.currentPage,
                        onSelected = { index ->
                            scope.launch { pagerState.scrollToPage(index) }
                            homePageIndexOkkv = index
                        },
                    )
                } else {
                    customBottomBar()
                }
            }
        } else {
            Row {
                NavigationRail(
                    containerColor = V2Tokens.Surface,
                ) {
                    mainV2Pages.forEachIndexed { index, page ->
                        NavigationRailItem(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                scope.launch { pagerState.scrollToPage(index) }
                                homePageIndexOkkv = index
                            },
                            icon = { page.icon(pagerState.currentPage == index) },
                            label = page.label,
                            alwaysShowLabel = false,
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = V2Theme.colors.accent,
                                selectedTextColor = V2Tokens.TextPrimary,
                                indicatorColor = V2Theme.colors.accentContainer,
                                unselectedIconColor = V2Tokens.TextSecondary,
                                unselectedTextColor = V2Tokens.TextSecondary,
                            ),
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f),
                ) {
                    VerticalPager(
                        state = pagerState,
                        userScrollEnabled = false,
                        modifier = Modifier.weight(1f),
                    ) { pageIndex ->
                        mainV2Pages[pageIndex].content()
                    }
                    viewModel.customBottomBar?.invoke()
                }
            }
        }
    }
}

@Composable
private fun V2BottomNavigation(
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
) {
    NavigationBar(
        containerColor = V2Tokens.Surface,
        contentColor = V2Tokens.TextPrimary,
    ) {
        mainV2Pages.forEachIndexed { index, page ->
            val selected = selectedIndex == index
            NavigationBarItem(
                icon = { page.icon(selected) },
                label = page.label,
                selected = selected,
                alwaysShowLabel = true,
                onClick = { onSelected(index) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = V2Theme.colors.accent,
                    selectedTextColor = V2Theme.colors.accent,
                    indicatorColor = V2Theme.colors.accentContainer,
                    unselectedIconColor = V2Tokens.TextSecondary,
                    unselectedTextColor = V2Tokens.TextSecondary,
                ),
            )
        }
    }
}
