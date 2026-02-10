package org.easybangumi.next.shared.compose.media

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.rememberWindowState
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import org.easybangumi.next.shared.RouterPage
import org.easybangumi.next.shared.window.EasyWindowController
import org.easybangumi.next.shared.window.EasyWindowState
import org.easybangumi.next.shared.window.LocalEasyWindowState

actual fun RouterPage.Media.navigation(
    navHostController: NavHostController,
    needNewWindowWhenDesktop: Boolean
) {
    if (needNewWindowWhenDesktop) {
        val easyWindowState = EasyWindowState(
            state = WindowState(),
            initPage = this
        )
        EasyWindowController.addWindowState(easyWindowState)
    } else {
        navHostController.navigate(this)
    }
}