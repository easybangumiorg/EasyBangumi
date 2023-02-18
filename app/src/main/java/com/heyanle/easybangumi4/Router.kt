package com.heyanle.easybangumi4

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController
import java.lang.ref.WeakReference

/**
 * Created by HeYanLe on 2023/2/19 0:10.
 * https://github.com/heyanLE
 */
val LocalNavController = staticCompositionLocalOf<NavHostController> {
    error("AppNavController Not Provide")
}

var navControllerRef: WeakReference<NavHostController>? = null