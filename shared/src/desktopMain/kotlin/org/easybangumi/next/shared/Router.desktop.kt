package org.easybangumi.next.shared

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.WindowState
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.utils.coroutineProvider
import org.easybangumi.next.libplayer.vlcj.VlcBridgeManagerProvider
import org.easybangumi.next.shared.foundation.elements.LoadingElements
import org.easybangumi.next.shared.window.EasyWindowController
import org.easybangumi.next.shared.window.EasyWindowState
import org.koin.compose.koinInject

@Composable
actual fun AnimatedContentScope.NavHook(
    routerPage: RouterPage,
    entity: NavBackStackEntry,
    content: @Composable (AnimatedContentScope.(NavBackStackEntry) -> Unit)
) {
    when(routerPage) {
        RouterPage.Media -> {
            val vlcBridgeManagerProvider: VlcBridgeManagerProvider = koinInject()
            val scope = rememberCoroutineScope()
            val vlcjState = vlcBridgeManagerProvider.stateFlow.collectAsState()
            LaunchedEffect(Unit) {
                scope.launch {
                    vlcBridgeManagerProvider.tryInit()
                }
            }
            when (vlcjState.value) {
                is VlcBridgeManagerProvider.State.None, is VlcBridgeManagerProvider.State.Initializing -> {
                    LoadingElements(
                        modifier = Modifier.fillMaxSize(),
                        loadingMsg = "播放引擎加载中..."
                    )
                }

                else -> {
                    content(entity)
                }
            }

        }
        else -> {
            content(entity)
        }
    }

}

actual fun NavHostController.navigate(
    routerPage: RouterPage,
    needNewWindowWhenDesktop: Boolean
) {
    if (needNewWindowWhenDesktop) {
        val easyWindowState = EasyWindowState(
            state = WindowState(),
            initPage = routerPage,
        )
        EasyWindowController.addWindowState(easyWindowState)
    } else {
        navigate(this)
    }
}