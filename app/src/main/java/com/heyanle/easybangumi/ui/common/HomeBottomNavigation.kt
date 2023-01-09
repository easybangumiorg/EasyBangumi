package com.heyanle.easybangumi.ui.common

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.heyanle.easybangumi.ui.home.pageItems
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/1/9 22:04.
 * https://github.com/heyanLE
 */
@Composable
fun HomeNavigationBar(
    content: @Composable RowScope.()->Unit,
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        content = content,
    )
}

@Composable
fun RowScope.HomeNavigationItem(
    selected: Boolean,
    icon: @Composable ()->Unit,
    label: @Composable ()->Unit,
    onClick: ()->Unit,
){
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
        onClick = onClick,
        icon = icon,
        label = label
    )
}
