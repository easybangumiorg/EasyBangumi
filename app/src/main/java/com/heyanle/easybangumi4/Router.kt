package com.heyanle.easybangumi4

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.heyanle.bangumi_source_api.api.entity.CartoonCover
import com.heyanle.easybangumi4.ui.home.Home
import com.heyanle.easybangumi4.ui.sourcehome.SourceHome
import com.heyanle.easybangumi4.utils.easyTODO
import java.lang.ref.WeakReference

/**
 * Created by HeYanLe on 2023/2/19 0:10.
 * https://github.com/heyanLE
 */
val LocalNavController = staticCompositionLocalOf<NavHostController> {
    error("AppNavController Not Provide")
}

var navControllerRef: WeakReference<NavHostController>? = null

const val NAV = "nav"

const val HOME = "home"

const val DETAILED = "detailed"

const val WEB_VIEW_USER = "web_view_user"

const val SOURCE_HOME = "source_home"
fun NavHostController.navigationSourceHome(key: String) {
    navigate("${SOURCE_HOME}?key=${key}")
}

fun NavHostController.navigationDetailed(cartoonCover: CartoonCover) {
    easyTODO("详情页")
}



// 缺省路由
const val DEFAULT = HOME

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Nav() {

    val nav = rememberAnimatedNavController()
    LaunchedEffect(key1 = nav) {
        navControllerRef = WeakReference(nav)
    }
    CompositionLocalProvider(LocalNavController provides nav) {
//        NavHost(navController = nav, DEFAULT) {
//            composable(HOME) {
//                Home()
//            }
//
//            composable(
//                route = "${SOURCE_HOME}?key={key}",
//                arguments = listOf(
//                    navArgument("key") { defaultValue = "" },
//                )
//            ){
//                SourceHome(
//                    it.arguments?.getString("key") ?: "",
//                )
//            }
//        }
        AnimatedNavHost(nav, DEFAULT,
            modifier = Modifier.fillMaxSize(),
            enterTransition = { slideInHorizontally(tween()) { it } },
            exitTransition = { slideOutHorizontally(tween()) { -it } + fadeOut(tween()) },
            popEnterTransition = { slideInHorizontally(tween()) { -it } },
            popExitTransition = { slideOutHorizontally(tween()) { it } }
        ) {

            composable(
                HOME,
            ) {
                Home()
            }

            composable(
                route = "${SOURCE_HOME}?key={key}",
                arguments = listOf(
                    navArgument("key") { defaultValue = "" },
                )
            ){
                SourceHome(
                    it.arguments?.getString("key") ?: "",
                )
            }

        }
    }

}