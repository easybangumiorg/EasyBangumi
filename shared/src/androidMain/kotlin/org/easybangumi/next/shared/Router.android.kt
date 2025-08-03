package org.easybangumi.next.shared

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.navigation.NavBackStackEntry
import org.easybangumi.next.lib.utils.global
import org.easybangumi.next.shared.foundation.systembar.EasySystemBar
import org.easybangumi.next.shared.foundation.systembar.EasySystemBarContext
import org.easybangumi.next.shared.foundation.systembar.LocalEasySystemBar
import org.easybangumi.next.shared.theme.ThemeController
import org.koin.compose.koinInject
import kotlin.reflect.KClass

@Composable
actual fun AnimatedContentScope.NavHook(
    routerPage: RouterPage,
    entity: NavBackStackEntry,
    content: @Composable (AnimatedContentScope.(NavBackStackEntry) -> Unit)
) {
    val themeController = koinInject<ThemeController>()
    val theme = themeController.themeFlow.collectAsState()
    val isDark = theme.value.isDark()
    when (routerPage) {
        is RouterPage.Main -> {
            val config = remember(isDark) {
                EasySystemBarContext.SystemBarConfig(
                    isStatusBarAppearanceLight = !isDark,
                    isNavBarAppearanceLight = !isDark,
                )
            }
            EasySystemBar(config)
        }
        is RouterPage.Debug -> {
            val config = remember(isDark) {
                EasySystemBarContext.SystemBarConfig(
                    isStatusBarAppearanceLight = !isDark,
                    isNavBarAppearanceLight = !isDark,
                )
            }
            EasySystemBar(config)
        }
        is RouterPage.Detail -> {
            val config = remember(isDark) {
                EasySystemBarContext.SystemBarConfig(
                    isStatusBarAppearanceLight = !isDark,
                    isNavBarAppearanceLight = !isDark,
                )
            }
            EasySystemBar(config)
        }
        is RouterPage.Media -> {
            val config = remember() {
                EasySystemBarContext.SystemBarConfig(
                    isStatusBarAppearanceLight = true,
                    isNavBarAppearanceLight = true,
                )
            }
            EasySystemBar(config)
        }
        else -> {}
    }

    content(entity)
}