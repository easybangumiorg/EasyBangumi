package com.heyanle.easybangumi4

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Created by HeYanLe on 2023/10/29 21:20.
 * https://github.com/heyanLE
 */

val LocalWindowSizeController = staticCompositionLocalOf<WindowSizeClass> {
    error("AppNavController Not Provide")
}

class MainActivity {
}