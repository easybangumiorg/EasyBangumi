package org.easybangumi.next.shared.compose.media

import android.media.MediaRouter
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import org.easybangumi.next.shared.RouterPage


actual fun RouterPage.Media.navigation(
    navHostController: NavHostController,
    needNewWindowWhenDesktop: Boolean
) {
    navHostController.navigate(this)
}