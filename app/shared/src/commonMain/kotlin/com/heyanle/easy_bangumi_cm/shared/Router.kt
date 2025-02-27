package com.heyanle.easy_bangumi_cm.shared

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.heyanle.easy_bangumi_cm.shared.ui.debug.Debug
import com.heyanle.easy_bangumi_cm.shared.ui.main.Main
import java.lang.ref.WeakReference

/**
 * Created by heyanlin on 2024/12/4.
 */

val LocalNavController = staticCompositionLocalOf<NavHostController> {
    error("AppNavController Not Provide")
}

var navControllerRef: WeakReference<NavHostController>? = null


const val MAIN = "main"

const val DEBUG = "debug"

const val DEFAULT = MAIN

@Composable
expect fun AnimatedContentScope.NavHook(entity: NavBackStackEntry, content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit)

@Composable
fun Nav() {

    val nav = rememberNavController()
    LaunchedEffect(key1 = nav) {
        navControllerRef = WeakReference(nav)
    }

    CompositionLocalProvider(LocalNavController provides nav) {
        NavHost(nav, DEFAULT,
            modifier = Modifier.fillMaxSize(),
            enterTransition = { slideInHorizontally(tween()) { it } },
            exitTransition = { slideOutHorizontally(tween()) { -it } + fadeOut(tween()) },
            popEnterTransition = { slideInHorizontally(tween()) { -it } },
            popExitTransition = { slideOutHorizontally(tween()) { it } }
        ) {

            composableWithHook(MAIN) {
                Main()
            }

            composable(DEBUG) {
                Debug()
            }



        }
    }
}

public fun NavGraphBuilder.composableWithHook(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    enterTransition:
    (@JvmSuppressWildcards
    AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? =
        null,
    exitTransition:
    (@JvmSuppressWildcards
    AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? =
        null,
    popEnterTransition:
    (@JvmSuppressWildcards
    AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? =
        enterTransition,
    popExitTransition:
    (@JvmSuppressWildcards
    AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? =
        exitTransition,
    sizeTransform:
    (@JvmSuppressWildcards
    AnimatedContentTransitionScope<NavBackStackEntry>.() -> SizeTransform?)? =
        null,
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
){
    composable(route, arguments, deepLinks, enterTransition, exitTransition, popEnterTransition, popExitTransition, sizeTransform) {
        NavHook(it, content)
    }

}