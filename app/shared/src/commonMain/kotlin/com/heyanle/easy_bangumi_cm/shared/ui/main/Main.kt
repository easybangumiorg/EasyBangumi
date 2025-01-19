package com.heyanle.easy_bangumi_cm.shared.ui.main

import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.heyanle.easy_bangumi_cm.compose_base.back_handler.InnerBackHandler
import com.heyanle.easy_bangumi_cm.shared.ui.main.home.Home
import easybangumi.app.shared.generated.resources.Res
import easybangumi.app.shared.generated.resources.home
import org.jetbrains.compose.resources.stringResource


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
    data object Home: MainPage(
        route = "home",
        tabLabel = {
            Text(text = stringResource(Res.string.home))
        },
        icon = {
            Icon(
                if (it) Icons.Filled.Home else Icons.Outlined.Home,
                contentDescription = stringResource(Res.string.home)
            )
        },
        content = {
            Home()
        }
    )

}

val MainPageItems = listOf(
    MainPage.Home
)

@Composable
expect fun MainHook()

@Composable
fun Main(){
    MainHook()
    val pagerState =
        rememberPagerState(0) { MainPageItems.size }

    // val windowSizeClass = calculateWindowSizeClass()

    Surface(
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {

    }
}