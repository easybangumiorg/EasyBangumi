package org.easybangumi.next.shared

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry

@Composable
actual fun AnimatedContentScope.NavHook(
    entity: NavBackStackEntry,
    content: @Composable (AnimatedContentScope.(NavBackStackEntry) -> Unit)
) {
    BackHandler {  }
    // TODO 状态栏颜色处理
    content(entity)
}