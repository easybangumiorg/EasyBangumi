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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.heyanle.easybangumi4.source.utils.WebViewHelperImpl
import com.heyanle.easybangumi4.source_api.entity.CartoonCover
import com.heyanle.easybangumi4.source_api.entity.CartoonSummary
import com.heyanle.easybangumi4.theme.NormalSystemBarColor
import com.heyanle.easybangumi4.ui.WebViewUser
import com.heyanle.easybangumi4.ui.about.About
import com.heyanle.easybangumi4.ui.cartoon_play.CartoonPlay
import com.heyanle.easybangumi4.ui.cartoon_play.CartoonPlayViewModel
import com.heyanle.easybangumi4.ui.dlna.Dlna
import com.heyanle.easybangumi4.ui.dlna.DlnaViewModel
import com.heyanle.easybangumi4.ui.download.Download
import com.heyanle.easybangumi4.ui.extension_store.ExtensionStore
import com.heyanle.easybangumi4.ui.local_play.LocalPlay
import com.heyanle.easybangumi4.ui.main.Main
import com.heyanle.easybangumi4.ui.main.history.History
import com.heyanle.easybangumi4.ui.search_migrate.migrate.CartoonMigrate
import com.heyanle.easybangumi4.ui.search_migrate.search.Search
import com.heyanle.easybangumi4.ui.setting.Setting
import com.heyanle.easybangumi4.ui.setting.SettingPage
import com.heyanle.easybangumi4.ui.source_config.SourceConfig
import com.heyanle.easybangumi4.ui.source_manage.SourceManager
import com.heyanle.easybangumi4.ui.tags.CartoonTag
import com.heyanle.easybangumi4.utils.jsonTo
import com.heyanle.easybangumi4.utils.loge
import com.heyanle.easybangumi4.utils.toJson
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
const val LOCAL_PLAY = "local_play"

const val WEB_VIEW_USER = "web_view_user"

const val SOURCE_HOME = "source_home"

const val HISTORY = "history"

const val SOURCE_MANAGER = "source_manager"

const val SEARCH = "search"

const val CARTOON_MIGRATE = "cartoon_migrate"

const val ABOUT = "about"

const val SOURCE_CONFIG = "source_config"

const val DLNA = "dlna"

const val SETTING = "setting"

const val DOWNLOAD = "download"

const val TAG_MANAGE = "tag_manage"

const val EXTENSION_STORE = "extension_store"

fun NavHostController.navigationSearch(defSourceKey: String) {
    val ed = URLEncoder.encode(defSourceKey, "utf-8")
    navigate("${SEARCH}?&defSourceKey=${ed}"){
        launchSingleTop = true
    }
}

fun NavHostController.navigationSearch(defSearchKey: String, defSourceKey: String) {
    val ed = URLEncoder.encode(defSearchKey, "utf-8")
    val es = URLEncoder.encode(defSourceKey, "utf-8")
    navigate("${SEARCH}?defSearchKey=${ed}&defSourceKey=${es}")
}

fun NavHostController.navigationCartoonMigrate(defSearchKey: String, defSourceKey: List<String> = emptyList()) {
    val ed = URLEncoder.encode(defSearchKey, "utf-8")
    val es = URLEncoder.encode(defSourceKey.toJson(), "utf-8")
    navigate("${CARTOON_MIGRATE}?defSearchKey=${ed}&defSourceKeys=${es}")
}

fun NavHostController.navigationSourceHome(key: String) {
    navigate("${SOURCE_HOME}?key=${key}")
}

fun NavHostController.navigationDetailed(id: String, url: String, source: String) {
    val el = URLEncoder.encode(url, "utf-8")
    val ed = URLEncoder.encode(id, "utf-8")
    val es = URLEncoder.encode(source, "utf-8")
    // easyTODO("详情页")
    navigate("${DETAILED}?url=${el}&source=${es}&id=${ed}")
}

fun NavHostController.navigationDetailed(cartoonCover: CartoonCover) {
    val url = URLEncoder.encode(cartoonCover.url, "utf-8")
    val id = URLEncoder.encode(cartoonCover.id, "utf-8")
    val es = URLEncoder.encode(cartoonCover.source, "utf-8")
    // easyTODO("详情页")
    navigate("${DETAILED}?url=${url}&source=${es}&id=${id}")
}

fun NavHostController.navigationSourceManager(defIndex: Int = -1) {
    navigate("${SOURCE_MANAGER}?defIndex=${defIndex}")
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
    val es = URLEncoder.encode(cartoonCover.source, "utf-8")
    // easyTODO("详情页")
    navigate("${DETAILED}?url=${url}&source=${es}&id=${id}&lineIndex=${lineIndex}&episode=${episode}&adviceProgress=${adviceProgress}")
}

fun NavHostController.navigationDlna(
    cartoonCover: CartoonSummary,
    lineIndex: Int,
    episode: Int,
) {
    // easyTODO("详情页")
    val url = URLEncoder.encode(cartoonCover.url, "utf-8")
    val id = URLEncoder.encode(cartoonCover.id, "utf-8")
    val ed = URLEncoder.encode(cartoonCover.source, "utf-8")
    // easyTODO("详情页")
    navigate("${DLNA}?url=${url}&source=${ed}&id=${id}&lineIndex=${lineIndex}&episode=${episode}")
}

fun NavHostController.navigationDetailed(
    i: String, u: String, s: String,
    e: CartoonPlayViewModel.EnterData,
) {
    // easyTODO("详情页")
    val url = URLEncoder.encode(u, "utf-8")
    val id = URLEncoder.encode(i, "utf-8")
    val ed = URLEncoder.encode(s, "utf-8")
    val enterData = URLEncoder.encode(e.toJson(), "utf-8")
    // easyTODO("详情页")
    navigate("${DETAILED}?url=${url}&source=${ed}&id=${id}&enter_date=${enterData}")
}

fun NavHostController.navigationLocalPlay(
    uuid: String
){
    val uuid = URLEncoder.encode(uuid, "utf-8")
    navigate("${LOCAL_PLAY}?uuid=${uuid}")
}
fun NavHostController.navigationSourceConfig(
    source: String
) {
    val es = URLEncoder.encode(source, "utf-8")
    navigate("${SOURCE_CONFIG}?source_key=${es}")
}

fun NavHostController.navigationSetting(
    settingPage: SettingPage
) {
    navigate("${SETTING}/${settingPage.router}")
}

fun NavHostController.navigationCartoonTag() {
    navigate(TAG_MANAGE)
}

// 缺省路由
const val DEFAULT = MAIN

@OptIn(ExperimentalAnimationApi::class)
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

            composable(
                MAIN,
            ) {
                NormalSystemBarColor()
                Main()

            }

            composable(
                route = "${DETAILED}?url={url}&source={source}&id={id}&enter_data={enter_data}",
                arguments = listOf(
                    navArgument("url") { defaultValue = "" },
                    navArgument("source") { defaultValue = "" },
                    navArgument("id") { defaultValue = "" },
                    navArgument("enter_data") { defaultValue = "{}" },
                    )
            ) {
                val id = it.arguments?.getString("id") ?: ""
                val source = it.arguments?.getString("source") ?: ""
                val url = it.arguments?.getString("url") ?: ""
                LaunchedEffect(Unit) {
                    "id:$id, source: $source, url: $url".loge()
                }

                var enterDataString = it.arguments?.getString("enter_data") ?: ""
                enterDataString = URLDecoder.decode(enterDataString, "utf-8")
                NormalSystemBarColor(
                    getStatusBarDark = {
                        false
                    }
                )

                val enterData = kotlin.runCatching {
                    enterDataString.jsonTo<CartoonPlayViewModel.EnterData>()
                }.getOrNull()
                CartoonPlay(
                    id = URLDecoder.decode(id, "utf-8"),
                    source = URLDecoder.decode(source, "utf-8"),
                    url = URLDecoder.decode(url, "utf-8"),
                    enterData
                )
            }

            composable(
                "${LOCAL_PLAY}?uuid={uuid}",
                arguments = listOf(
                    navArgument("uuid") { defaultValue = "" },
                )
            ) {
                NormalSystemBarColor(
                    getStatusBarDark = {
                        false
                    }
                )
                val uuid = it.arguments?.getString("uuid") ?: ""
                LocalPlay(uuid = URLDecoder.decode(uuid, "utf-8"))
            }

            composable(
                "${SETTING}/{router}",
                arguments = listOf(
                    navArgument("router") { defaultValue = SettingPage.Appearance.router },
                )
            ) {
                val router = it.arguments?.getString("router") ?: SettingPage.Appearance.router
                NormalSystemBarColor()
                Setting(router = router)
            }

            composable(WEB_VIEW_USER) {
                runCatching {
                    val wb = WebViewHelperImpl.webViewRef?.get() ?: throw NullPointerException()
                    val onCheck = WebViewHelperImpl.check?.get() ?: throw NullPointerException()
                    val onStop = WebViewHelperImpl.stop?.get() ?: throw NullPointerException()
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
                "${SOURCE_MANAGER}?defIndex={defIndex}",
                arguments = listOf(
                    navArgument("defIndex") {
                        type = NavType.IntType
                        defaultValue = -1
                    }
                )
            ) {
                val defSearchKey = it.arguments?.getInt("defIndex", -1) ?: -1
                NormalSystemBarColor()
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onBackground
                ) {
                    SourceManager(defSearchKey)
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
                    Search(
                        defSearchKey = URLDecoder.decode(defSearchKey, "utf-8"),
                        defSourceKey = URLDecoder.decode(defSourceKey, "utf-8")
                    )
                }


            }

            composable(
                "${CARTOON_MIGRATE}?defSearchKey={defSearchKey}&defSourceKeys={defSourceKey}",
                arguments = listOf(
                    navArgument("defSearchKey") { defaultValue = "" },
                    navArgument("defSourceKey") { defaultValue = "" },
                )
            ) {
                val defSearchKey = it.arguments?.getString("defSearchKey") ?: ""
                val defSourceKey = URLDecoder.decode(it.arguments?.getString("defSourceKey") ?: "", "utf-8")
                val defSourcesKey = defSourceKey.jsonTo<List<String>>() ?: emptyList()
                NormalSystemBarColor()
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onBackground
                ) {
                    CartoonMigrate(def = URLDecoder.decode(defSearchKey, "utf-8"), defSourceKey = defSourcesKey)
                }


            }

            composable(ABOUT) {
                NormalSystemBarColor()
                About()
            }

            composable(
                "${DLNA}?url={url}&source={source}&id={id}&lineIndex={lineIndex}&episode={episode}",
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

                NormalSystemBarColor()
                Dlna(
                    id = URLDecoder.decode(id, "utf-8"),
                    source = URLDecoder.decode(source, "utf-8"),
                    url = URLDecoder.decode(url, "utf-8"),
                    DlnaViewModel.EnterData(lineIndex, episode)
                )
            }

            composable(
                "${SOURCE_CONFIG}?source_key={key}",
                arguments = listOf(
                    navArgument("key") { defaultValue = "" },
                )
            ) {
                val source = it.arguments?.getString("key") ?: ""
                NormalSystemBarColor()
                SourceConfig(sourceKey = URLDecoder.decode(source, "utf-8"))
            }

            composable(TAG_MANAGE) {
                NormalSystemBarColor()
                CartoonTag()
            }

            composable(DOWNLOAD) {
                NormalSystemBarColor()
                Download()
            }

            composable(EXTENSION_STORE) {
                NormalSystemBarColor()
                ExtensionStore()
            }
        }
    }

}
