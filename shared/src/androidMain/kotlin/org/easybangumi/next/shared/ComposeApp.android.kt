package org.easybangumi.next.shared

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import org.easybangumi.next.shared.compose.UI
import org.easybangumi.next.shared.foundation.LocalUIMode
import org.easybangumi.next.shared.foundation.image.LocalImageLoader
import org.easybangumi.next.shared.foundation.image.createImageLoader
import org.easybangumi.next.shared.foundation.snackbar.MoeSnackBar
import org.easybangumi.next.shared.scheme.LocalSizeScheme
import org.easybangumi.next.shared.scheme.SizeScheme
import org.easybangumi.next.shared.theme.EasyTheme
import org.koin.compose.KoinContext

@Composable
actual fun ComposeApp() {
    KoinContext() {
        val top = with(LocalDensity.current) {
            WindowInsets.statusBars.getTop(LocalDensity.current).toDp()
        }
        CompositionLocalProvider(
            LocalImageLoader provides createImageLoader(),
            LocalUIMode provides UI.getUiMode(),
            LocalSizeScheme provides SizeScheme(statusBarHeight = top)
        ) {
            EasyTheme {
                Router()
                MoeSnackBar()
                NeedKnowDialog()
            }
        }


    }
}
