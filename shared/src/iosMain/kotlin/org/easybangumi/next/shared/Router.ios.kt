package org.easybangumi.next.shared

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController

@Composable
actual fun AnimatedContentScope.NavHook(
    routerPage: RouterPage,
    entity: NavBackStackEntry,
    content: @Composable (AnimatedContentScope.(NavBackStackEntry) -> Unit)
) {
    content(entity)
}

actual fun NavHostController.navigate(
    routerPage: RouterPage,
    windowModeWhenDesktop: NavigationWindowMode,
) {
    (this as NavController).navigate(routerPage)
}

actual fun NavHostController.navigate(
    webPage: RouterPage.WebPage,
    windowModeWhenDesktop: NavigationWindowMode,
) {
    (this as NavController).navigate(webPage)
}

actual fun NavHostController.popBackStackWithWindowMode(): Boolean {
    return popBackStack()
}
