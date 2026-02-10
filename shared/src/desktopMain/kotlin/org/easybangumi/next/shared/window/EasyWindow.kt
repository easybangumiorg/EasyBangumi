package org.easybangumi.next.shared.window

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.window.WindowState
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import org.easybangumi.next.shared.RouterPage

/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 */

val LocalEasyWindowState = staticCompositionLocalOf<EasyWindowState> {
    error("AppNavController Not Provide")
}
class EasyWindowState(
    val state: WindowState,
    val initPage: RouterPage? = RouterPage.DEFAULT,
) {}