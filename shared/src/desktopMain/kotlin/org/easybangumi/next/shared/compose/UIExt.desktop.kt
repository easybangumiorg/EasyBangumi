package org.easybangumi.next.shared.compose

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
actual fun isTabletModeWhenAuto(): Boolean {
    val windowClass = calculateWindowSizeClass()
    return remember(windowClass.widthSizeClass) {
        windowClass.widthSizeClass >= WindowWidthSizeClass.Medium
    }
}