package org.easybangumi.next.shared.compose

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.easybangumi.next.shared.LocalActivity

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
actual fun isTabletModeWhenAuto(): Boolean {
    val act = LocalActivity.current ?: return false
    val windowClass = calculateWindowSizeClass(act)
    return remember(windowClass) {
        windowClass.widthSizeClass == WindowWidthSizeClass.Expanded
    }
}