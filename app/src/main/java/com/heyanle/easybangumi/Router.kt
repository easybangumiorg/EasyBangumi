package com.heyanle.easybangumi

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.heyanle.bangumi_source_api.api.entity.Bangumi
import com.heyanle.easybangumi.source.utils.WebViewUserHelperImpl
import com.heyanle.easybangumi.ui.WebViewUser
import com.heyanle.easybangumi.ui.home.Home
import com.heyanle.easybangumi.ui.player.BangumiPlayManager
import com.heyanle.easybangumi.ui.player.Play
import com.heyanle.easybangumi.ui.search.Search
import java.lang.ref.WeakReference
import java.net.URLDecoder
import java.net.URLEncoder

/**
 * Created by HeYanLe on 2023/1/7 13:38.
 * https://github.com/heyanLE
 */

val LocalNavController = staticCompositionLocalOf<NavHostController> {
    error("AppNavController Not Provide")
}

var navControllerRef: WeakReference<NavHostController>? = null

const val NAV = "nav"

const val HOME = "home"
const val SEARCH = "search"

const val PLAY = "play"

const val WEB_VIEW_USER = "web_view_user"

// 缺省路由
const val DEFAULT = HOME

fun NavHostController.navigationSearch(keyword: String) {
    navigate("${SEARCH}?keyword=${keyword}")
}

fun NavHostController.navigationSearch(keyword: String, source: String) {
    navigate("${SEARCH}?keyword=${keyword}&source=${source}")
}

fun NavHostController.navigationPlay(bangumi: Bangumi) {
    navigationPlay(bangumi.id, bangumi.source, bangumi.detailUrl)
}

fun NavHostController.navigationPlay(
    id: String,
    source: String,
    detailUrl: String,
    linesIndex: Int = -1,
    episode: Int = -1,
    startPosition: Long = -1L
) {
    val idl = URLEncoder.encode(id, "utf-8")
    val uel = URLEncoder.encode(detailUrl, "utf-8")
    navigate("${PLAY}/${source}/${uel}?id=${idl}&linesIndex=${linesIndex}&episode=${episode}&startPosition=${startPosition}")
}

fun NavHostController.navigationPlay(id: String, source: String, detailUrl: String) {
    val uel = URLEncoder.encode(detailUrl, "utf-8")
    val idl = URLEncoder.encode(id, "utf-8")
    navigate("${PLAY}/${source}/${uel}?id=${idl}")
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Nav() {
    val nav = rememberAnimatedNavController()
    LaunchedEffect(key1 = nav) {
        navControllerRef = WeakReference(nav)
    }
    CompositionLocalProvider(LocalNavController provides nav) {
        AnimatedNavHost(nav, DEFAULT,
            enterTransition = { slideInHorizontally(tween()) { it } },
            exitTransition = { slideOutHorizontally(tween()) { -it } + fadeOut(tween()) },
            popEnterTransition = { slideInHorizontally(tween()) { -it } },
            popExitTransition = { slideOutHorizontally(tween()) { it } })
        {

            composable(HOME) {
                Home()
            }

            composable(
                "${SEARCH}?keyword={keyword}&sourceIndex={sourceIndex}",
                arguments = listOf(
                    navArgument("keyword") { defaultValue = "" },
                    navArgument("source") {
                        defaultValue = ""
                    }
                )
            ) {
                Search(
                    it.arguments?.getString("keyword") ?: "",
                    it.arguments?.getString("source") ?: ""
                )
            }

            composable(
                "${PLAY}/{source}/{detailUrl}?id={id}&linesIndex={linesIndex}&episode={episode}&startPosition={startPosition}",
                arguments = listOf(
                    navArgument("id") {defaultValue = ""},
                    navArgument("source") { defaultValue = "" },
                    navArgument("detailUrl") { defaultValue = "" },
                    navArgument("linesIndex") {
                        defaultValue = -1
                        type = NavType.IntType
                    },
                    navArgument("episode") {
                        defaultValue = -1
                        type = NavType.IntType
                    },
                    navArgument("startPosition") {
                        defaultValue = -1L
                        type = NavType.LongType
                    },
                ),
                deepLinks = listOf(navDeepLink {
                    uriPattern = "${NAV}://${PLAY}/{source}/{detailUrl}"
                }),
            ) {
                val id = it.arguments?.getString("id") ?: ""
                val source = it.arguments?.getString("source") ?: ""
                val detailUrl = it.arguments?.getString("detailUrl") ?: ""
                val linesIndex = it.arguments?.getInt("linesIndex") ?: -1
                val episode = it.arguments?.getInt("episode") ?: -1
                val startPosition = it.arguments?.getLong("startPosition") ?: -1L

                val enterData = BangumiPlayManager.EnterData(
                    lineIndex = linesIndex,
                    episode = episode,
                    startProcess = startPosition

                )

                Play(
                    id = URLDecoder.decode(id, "utf-8"),
                    source = source,
                    detail = URLDecoder.decode(detailUrl, "utf-8"),
                    enterData = enterData
                )
            }

            composable(WEB_VIEW_USER) {
                kotlin.runCatching {
                    val wb = WebViewUserHelperImpl.webViewRef?.get() ?: throw NullPointerException()
                    val onCheck =
                        WebViewUserHelperImpl.onCheck?.get() ?: throw NullPointerException()
                    val onStop = WebViewUserHelperImpl.onStop?.get() ?: throw NullPointerException()
                    WebViewUser(webView = wb, onCheck = onCheck, onStop = onStop)
                }.onFailure {
                    nav.popBackStack()
                }
            }


        }
    }
}
