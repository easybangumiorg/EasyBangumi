package com.heyanle.easybangumi4.v2.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.ScreenShowEvent
import com.heyanle.easybangumi4.navControllerRef
import com.heyanle.easybangumi4.navigation.PLAYBACK_DETAIL_LEGACY_ROUTE
import com.heyanle.easybangumi4.navigation.PLAYBACK_DETAIL_V2_ROUTE
import com.heyanle.easybangumi4.navigation.decodePlaybackDetailRouteArguments
import com.heyanle.easybangumi4.plugin.api.entity.CartoonSummary
import com.heyanle.easybangumi4.plugin.source.utils.network.WebViewHelperV2Impl
import com.heyanle.easybangumi4.theme.NormalSystemBarColor
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.CartoonPlayViewModel
import com.heyanle.easybangumi4.ui.setting.SettingPage
import com.heyanle.easybangumi4.utils.jsonTo
import com.heyanle.easybangumi4.v2.ui.legacy.LegacyAboutScreen
import com.heyanle.easybangumi4.v2.ui.legacy.LegacyCartoonMigrateScreen
import com.heyanle.easybangumi4.v2.ui.legacy.LegacyDlnaScreen
import com.heyanle.easybangumi4.v2.ui.legacy.LegacyHistoryScreen
import com.heyanle.easybangumi4.v2.ui.legacy.LegacyLocalPlayPlaceholderScreen
import com.heyanle.easybangumi4.v2.ui.legacy.LegacyMainScreen
import com.heyanle.easybangumi4.v2.ui.legacy.LegacyPlaybackDetailRollbackScreen
import com.heyanle.easybangumi4.v2.ui.legacy.LegacySearchScreen
import com.heyanle.easybangumi4.v2.ui.legacy.LegacySettingScreen
import com.heyanle.easybangumi4.v2.ui.legacy.LegacySourceConfigScreen
import com.heyanle.easybangumi4.v2.ui.legacy.LegacySourceManagerScreen
import com.heyanle.easybangumi4.v2.ui.legacy.LegacySourcePushScreen
import com.heyanle.easybangumi4.v2.ui.legacy.LegacyStorageScreen
import com.heyanle.easybangumi4.v2.ui.legacy.LegacyStoryScreen
import com.heyanle.easybangumi4.v2.ui.legacy.LegacyTagManageScreen
import com.heyanle.easybangumi4.v2.ui.legacy.LegacyWebVerificationScreen
import java.lang.ref.WeakReference
import java.net.URLDecoder
import com.heyanle.easybangumi4.v2.ui.playback.PlaybackDetailV2

/**
 * Route contract owned by the V2 graph. Values deliberately mirror the legacy graph so existing
 * navigation calls inside delegated screens keep working during the incremental migration.
 */
internal object V2Routes {
    const val MAIN = "home"
    const val PLAYBACK_DETAIL = PLAYBACK_DETAIL_V2_ROUTE
    const val PLAYBACK_DETAIL_ROLLBACK = PLAYBACK_DETAIL_LEGACY_ROUTE
    const val LOCAL_PLAY = "local_play"
    const val WEB_VERIFICATION = "web_view_user"
    const val HISTORY = "history"
    const val SOURCE_MANAGER = "source_manager"
    const val SEARCH = "search"
    const val CARTOON_MIGRATE = "cartoon_migrate"
    const val ABOUT = "about"
    const val SOURCE_CONFIG = "source_config"
    const val DLNA = "dlna"
    const val SETTING = "setting"
    const val TAG_MANAGE = "tag_manage"
    const val STORAGE = "storage"
    const val STORY = "story"
    const val SOURCE_PUSH = "source_push"

    val allDestinationBases = setOf(
        MAIN,
        PLAYBACK_DETAIL,
        PLAYBACK_DETAIL_ROLLBACK,
        DLNA,
        LOCAL_PLAY,
        SETTING,
        WEB_VERIFICATION,
        HISTORY,
        STORY,
        SOURCE_PUSH,
        SOURCE_MANAGER,
        SEARCH,
        CARTOON_MIGRATE,
        ABOUT,
        SOURCE_CONFIG,
        TAG_MANAGE,
        STORAGE,
    )
}

/**
 * Complete V2 navigation shell.
 *
 * The optional controller makes the graph testable and lets a future activity retain ownership of
 * controller creation. V2 still provides the legacy CompositionLocal until all screens have moved.
 */
@Composable
fun V2NavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = V2Routes.MAIN,
) {
    LaunchedEffect(navController) {
        navControllerRef = WeakReference(navController)
    }

    CompositionLocalProvider(LocalNavController provides navController) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.fillMaxSize(),
            enterTransition = { slideInHorizontally(tween()) { it } },
            exitTransition = { slideOutHorizontally(tween()) { -it } + fadeOut(tween()) },
            popEnterTransition = { slideInHorizontally(tween()) { -it } },
            popExitTransition = { slideOutHorizontally(tween()) { it } },
        ) {
            composable(V2Routes.MAIN) {
                ScreenShowEvent()
                NormalSystemBarColor()
                LegacyMainScreen()
            }

            composable(
                route = "${V2Routes.PLAYBACK_DETAIL}?source={source}&id={id}&enter_data={enter_data}",
                arguments = playbackDetailArguments(),
            ) { entry ->
                val arguments = decodePlaybackDetailRouteArguments(
                    id = entry.arguments?.getString("id") ?: "",
                    source = entry.arguments?.getString("source") ?: "",
                    enterDataJson = entry.arguments?.getString("enter_data") ?: "{}",
                )
                ScreenShowEvent(
                    "id" to arguments.id,
                    "source" to arguments.source,
                    "enter_data" to arguments.enterDataJson,
                )
                NormalSystemBarColor(getStatusBarDark = { false })
                PlaybackDetailV2(
                    id = arguments.id,
                    source = arguments.source,
                    enterData = arguments.enterDataJson.toPlaybackEnterDataOrNull(),
                )
            }

            composable(
                route = "${V2Routes.PLAYBACK_DETAIL_ROLLBACK}?source={source}&id={id}&enter_data={enter_data}",
                arguments = playbackDetailArguments(),
            ) { entry ->
                val arguments = decodePlaybackDetailRouteArguments(
                    id = entry.arguments?.getString("id") ?: "",
                    source = entry.arguments?.getString("source") ?: "",
                    enterDataJson = entry.arguments?.getString("enter_data") ?: "{}",
                )
                LegacyPlaybackDetailRollbackScreen(
                    id = arguments.id,
                    source = arguments.source,
                    enterData = arguments.enterDataJson.toPlaybackEnterDataOrNull(),
                )
            }

            composable(
                route = "${V2Routes.DLNA}?source={source}&id={id}&enter_data={enter_data}",
                arguments = playbackDetailArguments(),
            ) { entry ->
                val encodedId = entry.arguments?.getString("id") ?: ""
                val encodedSource = entry.arguments?.getString("source") ?: ""
                val enterDataJson = URLDecoder.decode(
                    entry.arguments?.getString("enter_data") ?: "",
                    Charsets.UTF_8.name(),
                )
                ScreenShowEvent(
                    "id" to encodedId,
                    "source" to encodedSource,
                    "enter_data" to enterDataJson,
                )
                NormalSystemBarColor()
                LegacyDlnaScreen(
                    id = URLDecoder.decode(encodedId, Charsets.UTF_8.name()),
                    source = URLDecoder.decode(encodedSource, Charsets.UTF_8.name()),
                    enterData = enterDataJson.toPlaybackEnterDataOrNull(),
                )
            }

            composable(
                route = "${V2Routes.LOCAL_PLAY}?uuid={uuid}",
                arguments = listOf(navArgument("uuid") { defaultValue = "" }),
            ) { entry ->
                ScreenShowEvent()
                NormalSystemBarColor(getStatusBarDark = { false })
                LegacyLocalPlayPlaceholderScreen(entry.arguments?.getString("uuid") ?: "")
            }

            composable(
                route = "${V2Routes.SETTING}/{router}",
                arguments = listOf(
                    navArgument("router") { defaultValue = SettingPage.Appearance.router },
                ),
            ) { entry ->
                val router = entry.arguments?.getString("router") ?: SettingPage.Appearance.router
                ScreenShowEvent("sub_router" to router)
                NormalSystemBarColor()
                LegacySettingScreen(router)
            }

            composable(
                route = "${V2Routes.WEB_VERIFICATION}?tips={tips}",
                arguments = listOf(navArgument("tips") { defaultValue = "" }),
            ) { entry ->
                ScreenShowEvent()
                DisposableEffect(Unit) {
                    onDispose { WebViewHelperV2Impl.webPageShowing = false }
                }
                runCatching {
                    val webView = WebViewHelperV2Impl.webViewRef?.get() ?: error("WebView missing")
                    val onCheck = WebViewHelperV2Impl.check?.get() ?: error("Check callback missing")
                    val onStop = WebViewHelperV2Impl.stop?.get() ?: error("Stop callback missing")
                    LegacyWebVerificationScreen(
                        webView = webView,
                        tips = entry.arguments?.getString("tips") ?: "",
                        onCheck = onCheck,
                        onStop = onStop,
                    )
                }.onFailure {
                    navController.popBackStack()
                }
            }

            composable(V2Routes.HISTORY) {
                ScreenShowEvent()
                NormalSystemBarColor()
                LegacySurface { LegacyHistoryScreen() }
            }

            composable(
                route = "${V2Routes.STORY}?defIndex={defIndex}",
                arguments = listOf(
                    navArgument("defIndex") {
                        type = NavType.IntType
                        defaultValue = 0
                    },
                ),
            ) { entry ->
                ScreenShowEvent()
                NormalSystemBarColor()
                LegacyStoryScreen(entry.arguments?.getInt("defIndex") ?: 0)
            }

            composable(V2Routes.SOURCE_PUSH) {
                ScreenShowEvent()
                NormalSystemBarColor()
                LegacySourcePushScreen()
            }

            composable(
                route = "${V2Routes.SOURCE_MANAGER}?defIndex={defIndex}",
                arguments = listOf(
                    navArgument("defIndex") {
                        type = NavType.IntType
                        defaultValue = -1
                    },
                ),
            ) { entry ->
                ScreenShowEvent()
                NormalSystemBarColor()
                LegacySurface {
                    LegacySourceManagerScreen(entry.arguments?.getInt("defIndex") ?: -1)
                }
            }

            composable(
                route = "${V2Routes.SEARCH}?defSearchKey={defSearchKey}&defSourceKey={defSourceKey}",
                arguments = listOf(
                    navArgument("defSearchKey") { defaultValue = "" },
                    navArgument("defSourceKey") { defaultValue = "" },
                ),
            ) { entry ->
                ScreenShowEvent()
                NormalSystemBarColor()
                LegacySurface {
                    LegacySearchScreen(
                        initialKeyword = entry.arguments?.getString("defSearchKey").decodeUrl(),
                        initialSourceKey = entry.arguments?.getString("defSourceKey").decodeUrl(),
                    )
                }
            }

            composable(
                route = "${V2Routes.CARTOON_MIGRATE}?summaries={summaries}&sourceKeys={sourceKeys}",
                arguments = listOf(
                    navArgument("summaries") { defaultValue = "" },
                    navArgument("sourceKeys") { defaultValue = "" },
                ),
            ) { entry ->
                ScreenShowEvent()
                val summaries = entry.arguments?.getString("summaries")
                    .decodeUrl()
                    .jsonToOrEmptyList<CartoonSummary>()
                val sourceKeys = entry.arguments?.getString("sourceKeys")
                    .decodeUrl()
                    .jsonToOrEmptyList<String>()
                if (summaries.isEmpty() || sourceKeys.isEmpty()) {
                    LaunchedEffect(Unit) { navController.popBackStack() }
                }
                NormalSystemBarColor()
                LegacySurface {
                    LegacyCartoonMigrateScreen(summaries = summaries, sourceKeys = sourceKeys)
                }
            }

            composable(V2Routes.ABOUT) {
                ScreenShowEvent()
                NormalSystemBarColor()
                LegacyAboutScreen()
            }

            composable(
                route = "${V2Routes.SOURCE_CONFIG}?source_key={key}",
                arguments = listOf(navArgument("key") { defaultValue = "" }),
            ) { entry ->
                ScreenShowEvent()
                NormalSystemBarColor()
                LegacySurface {
                    LegacySourceConfigScreen(
                        sourceKey = entry.arguments?.getString("key").decodeUrl(),
                    )
                }
            }

            composable(V2Routes.TAG_MANAGE) {
                ScreenShowEvent()
                NormalSystemBarColor()
                LegacyTagManageScreen()
            }

            composable(V2Routes.STORAGE) {
                ScreenShowEvent()
                NormalSystemBarColor()
                LegacyStorageScreen()
            }
        }
    }
}

private fun playbackDetailArguments() = listOf(
    navArgument("source") { defaultValue = "" },
    navArgument("id") { defaultValue = "" },
    navArgument("enter_data") { defaultValue = "{}" },
)

private fun String?.decodeUrl(): String = URLDecoder.decode(
    this.orEmpty(),
    Charsets.UTF_8.name(),
)

private fun String.toPlaybackEnterDataOrNull(): CartoonPlayViewModel.EnterData? = runCatching {
    jsonTo<CartoonPlayViewModel.EnterData>()
}.getOrNull()

private inline fun <reified T> String.jsonToOrEmptyList(): List<T> = runCatching {
    jsonTo<List<T>>()
}.getOrNull().orEmpty()

@Composable
private fun LegacySurface(content: @Composable () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        content = content,
    )
}
