package com.heyanle.easybangumi4

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.heyanle.bangumi_source_api.api.entity.CartoonCover
import com.heyanle.easybangumi4.source.utils.WebViewUserHelperImpl
import com.heyanle.easybangumi4.theme.NormalSystemBarColor
import com.heyanle.easybangumi4.ui.WebViewUser
import com.heyanle.easybangumi4.ui.about.About
import com.heyanle.easybangumi4.ui.cartoon_play.CartoonPlay
import com.heyanle.easybangumi4.ui.cartoon_play.CartoonPlayViewModel
import com.heyanle.easybangumi4.ui.history.History
import com.heyanle.easybangumi4.ui.main.Main
import com.heyanle.easybangumi4.ui.search.Search
import com.heyanle.easybangumi4.ui.setting.AppearanceSetting
import com.heyanle.easybangumi4.ui.sourcehome.SourceHome
import com.heyanle.easybangumi4.utils.TODO
import com.heyanle.easybangumi4.utils.loge
import java.lang.ref.WeakReference
import java.net.URLDecoder
import java.net.URLEncoder

/**
 * Created by HeYanLe on 2023/2/19 0:10.
 * https://github.com/heyanLE
 */
val LocalNavController = staticCompositionLocalOf<NavHostController> {
    error("AppNavController Not Provide")
}

var navControllerRef: WeakReference<NavHostController>? = null

const val NAV = "nav"

const val MAIN = "home"

const val DETAILED = "detailed"

const val WEB_VIEW_USER = "web_view_user"

const val SOURCE_HOME = "source_home"

const val HISTORY = "history"

const val SEARCH = "search"

const val ABOUT = "about"

const val APPEARANCE_SETTING = "appearance_setting"

fun NavHostController.navigationSearch(defSourceKey: String) {
    navigate("${SEARCH}?&defSourceKey=${defSourceKey}")
}

fun NavHostController.navigationSearch(defSearchKey: String, defSourceKey: String) {
    navigate("${SEARCH}?defSearchKey=${defSearchKey}&defSourceKey=${defSourceKey}")
}

fun NavHostController.navigationSourceHome(key: String) {
    navigate("${SOURCE_HOME}?key=${key}")
}

fun NavHostController.navigationDetailed(id: String, url: String, source: String) {
    val el = URLEncoder.encode(url, "utf-8")
    val ed = URLEncoder.encode(id, "utf-8")
    // easyTODO("详情页")
    navigate("${DETAILED}?url=${el}&source=${source}&id=${ed}")
}

fun NavHostController.navigationDetailed(cartoonCover: CartoonCover) {
    val url = URLEncoder.encode(cartoonCover.url, "utf-8")
    val id = URLEncoder.encode(cartoonCover.id, "utf-8")
    // easyTODO("详情页")
    navigate("${DETAILED}?url=${url}&source=${cartoonCover.source}&id=${id}")
}


fun NavHostController.navigationDetailed(
    cartoonCover: CartoonCover,
    lineIndex: Int,
    episode: Int,
    adviceProgress: Long,
) {
    // easyTODO("详情页")
    val url = URLEncoder.encode(cartoonCover.url, "utf-8")
    val id = URLEncoder.encode(cartoonCover.id, "utf-8")
    // easyTODO("详情页")
    navigate("${DETAILED}?url=${url}&source=${cartoonCover.source}&id=${id}&lineIndex=${lineIndex}&episode=${episode}&adviceProgress=${adviceProgress}")
}

fun NavHostController.navigationDetailed(
    id: String, url: String, source: String,
    lineIndex: Int,
    episode: Int,
    adviceProgress: Long,
) {
    // easyTODO("详情页")
    val el = URLEncoder.encode(url, "utf-8")
    val ed = URLEncoder.encode(id, "utf-8")
    // easyTODO("详情页")
    navigate("${DETAILED}?url=${el}&source=${source}&id=${ed}&lineIndex=${lineIndex}&episode=${episode}&adviceProgress=${adviceProgress}")
}

fun NavHostController.navigationDLNA(
    cartoonCover: CartoonCover
) {
    TODO("投屏")
}

fun NavHostController.navigationDLNA(
    cartoonCover: CartoonCover,
    lineIndex: Int,
    episode: Int,
) {
    TODO("投屏")
}

// 缺省路由
const val DEFAULT = MAIN


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Nav() {

    val nav = rememberAnimatedNavController()
    LaunchedEffect(key1 = nav) {
        navControllerRef = WeakReference(nav)
    }
    CompositionLocalProvider(LocalNavController provides nav) {

        AnimatedNavHost(nav, DEFAULT,
            modifier = Modifier.fillMaxSize(),
            enterTransition = { slideInHorizontally(tween()) { it } },
            exitTransition = { slideOutHorizontally(tween()) { -it } + fadeOut(tween()) },
            popEnterTransition = { slideInHorizontally(tween()) { -it } },
            popExitTransition = { slideOutHorizontally(tween()) { it } }
        ) {

            composable(
                MAIN,
            ) {
                NormalSystemBarColor()
                Main()

            }

            composable(
                route = "${SOURCE_HOME}?key={key}",
                arguments = listOf(
                    navArgument("key") { defaultValue = "" },
                )
            ) {
                NormalSystemBarColor()
                SourceHome(
                    it.arguments?.getString("key") ?: "",
                )

            }

            composable(
                route = "${DETAILED}?url={url}&source={source}&id={id}&lineIndex={lineIndex}&episode={episode}&adviceProgress={adviceProgress}",
                arguments = listOf(
                    navArgument("url") { defaultValue = "" },
                    navArgument("source") { defaultValue = "" },
                    navArgument("id") { defaultValue = "" },
                    navArgument("lineIndex") {
                        defaultValue = -1
                        type = NavType.IntType
                    },
                    navArgument("episode") {
                        defaultValue = -1
                        type = NavType.IntType
                    },
                    navArgument("adviceProgress") {
                        defaultValue = -1L
                        type = NavType.LongType
                    },

                    )
            ) {
                val id = it.arguments?.getString("id") ?: ""
                val source = it.arguments?.getString("source") ?: ""
                val url = it.arguments?.getString("url") ?: ""
                LaunchedEffect(Unit) {
                    "id:$id, source: $source, url: $url".loge()
                }
                val lineIndex = it.arguments?.getInt("lineIndex") ?: -1
                val episode = it.arguments?.getInt("episode") ?: -1
                val adviceProgress = it.arguments?.getLong("adviceProgress") ?: -1L
                NormalSystemBarColor(
                    getStatusBarDark = {
                        false
                    }
                )
                CartoonPlay(
                    id = URLDecoder.decode(id, "utf-8"),
                    source = source,
                    url = URLDecoder.decode(url, "utf-8"),
                    CartoonPlayViewModel.EnterData(lineIndex, episode, adviceProgress)
                )
            }

            composable(APPEARANCE_SETTING) {
                NormalSystemBarColor()
                AppearanceSetting()
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

            composable(HISTORY) {
                NormalSystemBarColor()
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onBackground
                ) {
                    History()
                }

            }

            composable(
                "${SEARCH}?defSearchKey={defSearchKey}&defSourceKey={defSourceKey}",
                arguments = listOf(
                    navArgument("defSearchKey") { defaultValue = "" },
                    navArgument("defSourceKey") { defaultValue = "" },
                )
            ) {
                val defSearchKey = it.arguments?.getString("defSearchKey") ?: ""
                val defSourceKey = it.arguments?.getString("defSourceKey") ?: ""
                NormalSystemBarColor()
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onBackground
                ) {
                    Search(defSearchKey = defSearchKey, defSourceKey = defSourceKey)
                }


            }

            composable(ABOUT){
                NormalSystemBarColor()
                About()
            }

        }
    }

}