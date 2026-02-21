package org.easybangumi.next.shared.window

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.utils.coroutineProvider
import org.easybangumi.next.shared.ComposeApp
import org.easybangumi.next.shared.NeedKnowDialog
import org.easybangumi.next.shared.Router
import org.easybangumi.next.shared.RouterPage
import org.easybangumi.next.shared.compose.UI
import org.easybangumi.next.shared.foundation.LocalUIMode
import org.easybangumi.next.shared.foundation.image.LocalImageLoader
import org.easybangumi.next.shared.foundation.image.createImageLoader
import org.easybangumi.next.shared.foundation.snackbar.MoeSnackBar
import org.easybangumi.next.shared.scheme.LocalSizeScheme
import org.easybangumi.next.shared.scheme.SizeScheme
import org.easybangumi.next.shared.theme.EasyTheme
import org.koin.compose.KoinContext

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
object EasyWindowController {

    val mainWindowState = EasyWindowState(
        WindowState(),
        RouterPage.DEFAULT
    )
    private val windowStateList = mutableStateListOf(
        mainWindowState
    )
    private var exitApplication: (() -> Unit)? = null

    fun bindExitApplication(
        exitApplication: () -> Unit
    ) {
        this.exitApplication = exitApplication
    }

    fun addWindowState(windowState: EasyWindowState) {
        coroutineProvider.globalScope().launch(
            coroutineProvider.io()
        ) {
            windowStateList.add(windowState)
        }

    }

    fun removeWindowState(windowState: EasyWindowState) {
        windowStateList.remove(windowState)
        if (windowStateList.isEmpty()) {
            exitApplication?.invoke()
        }
    }

//    fun findWindowStateByNavController(
//        navController: NavHostController
//    ): EasyWindowState? {
//        return windowStateList.find {
//            it.navController == navController
//        }
//    }

    @Composable
    fun EasyWindowHost() {
        KoinContext() {
            CompositionLocalProvider(
                LocalImageLoader provides createImageLoader(),

            ) {
                EasyTheme {
                    windowStateList.forEach { state ->
                        Window(
                            onCloseRequest = {
                                removeWindowState(state)
                            },
                            state = state.state,
                            title = "纯纯看番 Next",
                        ) {
                            val top = with(LocalDensity.current) {
                                WindowInsets.statusBars.getTop(LocalDensity.current).toDp()
                            }
                            CompositionLocalProvider(
                                LocalEasyWindowState provides state,
                                LocalUIMode provides UI.getUiMode(),
                                LocalSizeScheme provides SizeScheme(statusBarHeight = top)
                            ) {
                                Router(
                                    initRoute = state.initPage ?: RouterPage.DEFAULT,
                                )
                                MoeSnackBar()
                                if (state == mainWindowState) {
                                    NeedKnowDialog()
                                }
                            }

                        }
                    }
                }
            }
        }

    }

}