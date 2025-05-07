package org.easybangumi.next.shared

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import org.easybangumi.next.lib.utils.global

@Composable
actual fun AnimatedContentScope.NavHook(
    entity: NavBackStackEntry,
    content: @Composable (AnimatedContentScope.(NavBackStackEntry) -> Unit)
) {
    global
    BackHandler {  }
    // TODO 状态栏颜色处理
    content(entity)
}