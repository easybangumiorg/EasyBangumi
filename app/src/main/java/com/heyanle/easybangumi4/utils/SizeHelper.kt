package com.heyanle.easybangumi4.utils

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.heyanle.easybangumi4.LocalWindowSizeController
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.inject.core.Inject

/**
 * Created by HeYanLe on 2023/6/4 16:42.
 * https://github.com/heyanLE
 */
@Composable
fun isCurPadeMode(): Boolean {
    val windowSize = LocalWindowSizeController.current
    val settingPreferences: SettingPreferences by Inject.injectLazy()
    val padMode by settingPreferences.padMode.flow()
        .collectAsState(settingPreferences.padMode.get())
    val isPad = remember(padMode, windowSize) {
        when (padMode) {
            SettingPreferences.PadMode.AUTO -> windowSize.widthSizeClass == WindowWidthSizeClass.Expanded
            SettingPreferences.PadMode.ENABLE -> true
            else -> false
        }
    }
    padMode.loge("SizeHelper")
    return isPad
}