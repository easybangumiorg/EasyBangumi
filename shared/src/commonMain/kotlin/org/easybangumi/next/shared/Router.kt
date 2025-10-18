package org.easybangumi.next.shared

import androidx.compose.animation.AnimatedContentScope
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
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import org.easybangumi.next.shared.debug.DebugHost
import org.easybangumi.next.shared.debug.DebugPage
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.easybangumi.next.lib.utils.WeakRef
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.compose.detail.Detail
import org.easybangumi.next.shared.compose.home.Home
import org.easybangumi.next.shared.compose.media.Media
import org.easybangumi.next.shared.compose.media.MediaParam
import org.easybangumi.next.shared.compose.media_radar.MediaRadarParam
import org.easybangumi.next.shared.compose.search.Search
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.source.bangumi.source.BangumiInnerSource

/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 */

val LocalNavController = staticCompositionLocalOf<NavHostController> {
    error("AppNavController Not Provide")
}

internal var innerNavControllerRef: WeakRef<NavHostController>? = null
val navController: NavHostController? get() = innerNavControllerRef?.targetOrNull


sealed class RouterPage {
    @Serializable
    object Main: RouterPage()

    @Serializable
    data class Debug(
        val debugPageName: String,
    ): RouterPage() {
        companion object  {
            val HOME = Debug(DebugPage.HOME.name)
        }

        fun toRoute(): DebugPage =  DebugPage.valueOf(debugPageName)

    }

    @Serializable
    data class Detail(
        val cartoonIndex: CartoonIndex,
    ): RouterPage() {
        companion object {
            fun fromCartoonIndex(cartoonIndex: CartoonIndex): Detail {
                return Detail(
                    cartoonIndex
                )
            }
        }
    }

    @Serializable
    data class Media(
        val cartoonIndex: CartoonIndex,
        val cartoonCover: CartoonCover? = null,
        val suggestEpisode: Int? = null,
        val suggestMediaRadarParam: MediaRadarParam? = null,
    ): RouterPage() {

        companion object {
            fun from(
                cartoonIndex: CartoonIndex,
                cartoonCover: CartoonCover? = null,
                suggestEpisode: Int? = null,
                suggestMediaRadarParam: MediaRadarParam? = null,
            ): Media {
                return Media(
                    cartoonIndex,
                    cartoonCover,
                    suggestEpisode,
                    suggestMediaRadarParam
                )
            }
        }

    }


    @Serializable
    data class Search(
        val defSearchKeyword: String = "",
        val defSourceKey: String? = null,
    ): RouterPage() {
        companion object {
            fun from(
                defSearchKeyword: String = "",
                defSourceKey: String? = null,
            ): Search {
                return Search(defSearchKeyword, defSourceKey)
            }
        }
    }

    companion object {
        val DEFAULT = Main
    }



}

fun NavController.navigateToDetailOrMedia(
    cartoonIndex: CartoonIndex,
    cartoonCover: CartoonCover? = null,
) {
    if (cartoonIndex.source == BangumiInnerSource.SOURCE_KEY) {
        val detail = RouterPage.Detail.fromCartoonIndex(cartoonIndex)
        this.navigate(detail)
    } else {
        val media = RouterPage.Media.from(
            cartoonIndex = cartoonIndex,
            cartoonCover = cartoonCover
        )
        this.navigate(media)
    }

}


@Composable
expect fun AnimatedContentScope.NavHook(
    routerPage: RouterPage,
    entity: NavBackStackEntry,
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
)

@Composable
fun Router() {
    val navController = rememberNavController()
    LaunchedEffect(Unit) {
        innerNavControllerRef = WeakRef(navController)
    }
    CompositionLocalProvider(LocalNavController provides navController) {
        // 所有页面都尽量走 NavHook
        NavHost(
            navController, RouterPage.DEFAULT,
            modifier = Modifier.fillMaxSize(),
            enterTransition = { slideInHorizontally(tween()) { it } },
            exitTransition = { slideOutHorizontally(tween()) { -it } + fadeOut(tween()) },
            popEnterTransition = { slideInHorizontally(tween()) { -it } },
            popExitTransition = { slideOutHorizontally(tween()) { it } },
            typeMap = NavTypeMap
        ) {

            composable<RouterPage.Main>(
                typeMap = NavTypeMap
            ) {
                val main = it.toRoute<RouterPage.Main>()
                NavHook(main, it) {
                    Home()
                }
            }

            composable<RouterPage.Debug>(
                typeMap = NavTypeMap
            ) {
                val debugPage = it.toRoute<RouterPage.Debug>()
                NavHook(debugPage, it) {
                    DebugHost(
                        debugPage.toRoute(),
                        onNav = {
                            // Handle navigation from debug page
                            val debug = RouterPage.Debug(it.name)
                            navController.navigate(debug)
                        },
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }

            composable<RouterPage.Detail>(
                typeMap = NavTypeMap
            ) {
                val detail = it.toRoute<RouterPage.Detail>()
                val cartoonIndex = detail.cartoonIndex
                NavHook(detail, it) {
                    Detail(cartoonIndex)
                }
            }

            composable<RouterPage.Media>(
                typeMap = NavTypeMap
            ){
                val media = it.toRoute<RouterPage.Media>()
                NavHook(media, it) {
                    val param = media.let { param ->
                        MediaParam(
                            cartoonIndex = param.cartoonIndex,
                            cartoonCover = param.cartoonCover,
                            suggestEpisode = param.suggestEpisode,
                            suggestMediaRadarParam = param.suggestMediaRadarParam
                        )
                    }
                    Media(param)
                }

            }

            // Search
            composable<RouterPage.Search>(
                typeMap = NavTypeMap
            ) {
                val search = it.toRoute<RouterPage.Search>()
                NavHook(search, it) {
                    Search(
                        defSearchKeyword = search.defSearchKeyword,
                        defSourceKey = search.defSourceKey,
                    )
                }
            }
        }
    }
}
