package com.heyanle.easybangumi4.utils

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.heyanle.easybangumi4.LocalWindowSizeController
import com.heyanle.easybangumi4.preferences.PadModePreferences

/**
 * Created by HeYanLe on 2023/6/4 16:42.
 * https://github.com/heyanLE
 */
@Composable
fun isCurPadeMode(): Boolean {
    val windowSize = LocalWindowSizeController.current

    val padMode by PadModePreferences.stateFlow.collectAsState()
    val isPad = remember(padMode, windowSize) {
        when(padMode){
            0 -> windowSize.widthSizeClass == WindowWidthSizeClass.Expanded
            1 -> true
            else -> false
        }
    }
    return isPad
}