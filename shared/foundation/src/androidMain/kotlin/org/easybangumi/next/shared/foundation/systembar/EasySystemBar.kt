package org.easybangumi.next.shared.foundation.systembar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

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
 *
 */
@Composable
fun EasySystemBar(
    config: EasySystemBarContext.SystemBarConfig,
) {
    val systemBar = LocalEasySystemBar.current
    DisposableEffect(systemBar, config) {
        systemBar.pushConfig(config)
        onDispose {
            systemBar.removeConfig(config)
        }
    }


}