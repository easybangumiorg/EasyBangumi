package org.easybangumi.next.shared

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import org.easybangumi.next.shared.foundation.systembar.EasySystemBarContext
import org.easybangumi.next.shared.foundation.systembar.LocalEasySystemBar

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

val LocalActivity = staticCompositionLocalOf<Activity?> { null }

@Composable
fun ActivityHost(
    activity: Activity,
    content: @Composable () -> Unit
) {
    val systemBar = remember(activity) {
        EasySystemBarContext(activity)
    }
    CompositionLocalProvider(
        LocalActivity provides activity,
        LocalContext provides activity,
        LocalEasySystemBar provides systemBar
    ) {
        systemBar.Effect()
        content()
    }

}
